package com.Qwikkspell.WorkshopPractice.commands;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import com.Qwikkspell.WorkshopPractice.game.StatsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code /wpnames} — one-time backfill of missing usernames (op/console only).
 *
 * <p>Legacy {@code scores.yml} only stored UUIDs, so imported entries have no username and show as
 * "Unknown" on the Discord leaderboards. This resolves each missing name from Mojang's profile API
 * asynchronously (throttled) and writes them into {@code stats.yml}. After this runs once, names
 * stay current via login refresh ({@code PlayerJoinListener}) and whenever a new time is recorded.
 * Safe to re-run — it only touches entries that are still missing a name.</p>
 */
public class BackfillNamesCommand implements CommandExecutor {

    private static final Pattern NAME = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    private final WorkshopPractice plugin;
    private final StatsManager stats;
    private volatile boolean running = false;

    public BackfillNamesCommand(WorkshopPractice plugin, StatsManager stats) {
        this.plugin = plugin;
        this.stats = stats;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.isOp()) {
            sender.sendMessage(ChatColor.RED + "You must be an operator to run this.");
            return true;
        }
        if (running) {
            sender.sendMessage(ChatColor.YELLOW + "A name backfill is already running.");
            return true;
        }

        List<UUID> missing = stats.uuidsMissingNames();
        if (missing.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "All stats entries already have usernames.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Backfilling " + missing.size() + " usernames from Mojang (running async)...");
        running = true;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> runBackfill(sender, missing));
        return true;
    }

    private void runBackfill(CommandSender sender, List<UUID> missing) {
        HttpClient client = HttpClient.newHttpClient();
        int resolved = 0;
        int updated = 0;
        for (UUID uuid : missing) {
            String name = resolveName(client, uuid);
            if (name != null) {
                resolved++;
                if (stats.setNameIfPresent(uuid, name)) {
                    updated++;
                }
            }
            try {
                Thread.sleep(60); // be gentle with Mojang's rate limit
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        stats.save();
        running = false;

        final int fResolved = resolved;
        final int fUpdated = updated;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            String msg = "Name backfill complete: resolved " + fResolved + "/" + missing.size()
                    + " (" + fUpdated + " updated). Re-run /wpnames to retry any that failed.";
            sender.sendMessage(ChatColor.GREEN + msg);
            plugin.getLogger().info("[Names] " + msg);
        });
    }

    private String resolveName(HttpClient client, UUID uuid) {
        try {
            String id = uuid.toString().replace("-", "");
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + id)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null; // 204 = no such profile, 429 = rate limited (re-run to retry)
            }
            Matcher m = NAME.matcher(response.body());
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception ignored) {
            // network hiccup — leave it for a re-run
        }
        return null;
    }
}
