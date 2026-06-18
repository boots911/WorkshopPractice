package com.Qwikkspell.WorkshopPractice.commands;

import com.Qwikkspell.WorkshopPractice.game.GameMode;
import com.Qwikkspell.WorkshopPractice.game.InfoMaps;
import com.Qwikkspell.WorkshopPractice.game.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * {@code /leaderboard <mode|overall>}.
 *
 * <p>Per-mode boards for the six modes plus an {@code overall} board (sum of the four base-mode
 * PBs). Time modes sort fastest-first; {@code timetrial60} sorts by most crafts.</p>
 */
public class LeaderboardCommand implements CommandExecutor {

    private static final int LIMIT = 100;

    private final StatsManager statsManager;

    public LeaderboardCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String key = (args.length >= 1) ? args[0].toLowerCase() : "overall";

        if (key.equals("overall")) {
            showOverall(sender);
            return true;
        }

        if (key.equals("item")) {
            showItem(sender, args);
            return true;
        }

        if (key.equals("setseed")) {
            renderSimple(sender, "Best Seeded Times", statsManager.getSeededLeaderboard(LIMIT));
            return true;
        }
        if (key.equals("totalgames")) {
            renderGames(sender, "Most Games Completed", statsManager.getTotalGamesLeaderboard(LIMIT));
            return true;
        }
        if (key.equals("avgcraft")) {
            renderSimple(sender, "Fastest Average Craft", statsManager.getAvgCraftLeaderboard(LIMIT));
            return true;
        }

        Optional<GameMode> modeOpt = GameMode.fromAlias(key);
        if (modeOpt.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown leaderboard '" + key + "'.");
            sender.sendMessage(ChatColor.YELLOW + "Try: left, lefteasy, right, righteasy, allcrafts, timetrial60, overall,"
                    + " setseed, totalgames, avgcraft, item <item>");
            return true;
        }

        GameMode mode = modeOpt.get();
        boolean timeBased = mode.getScoringType() == GameMode.ScoringType.FASTEST_TIME;
        List<StatsManager.LeaderboardEntry> top = statsManager.getModeLeaderboard(mode, LIMIT);

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Top " + mode.getDisplayName() + " Players:");
        if (top.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No records yet.");
            return true;
        }
        int rank = 1;
        for (StatsManager.LeaderboardEntry entry : top) {
            String value = timeBased ? statsManager.formatTime(entry.value) : ((int) entry.value) + " crafts";
            sender.sendMessage(ChatColor.AQUA + "" + rank + ". " + nameOf(entry) + ": " + ChatColor.YELLOW + value);
            rank++;
        }
        return true;
    }

    private void showOverall(CommandSender sender) {
        List<StatsManager.LeaderboardEntry> top = statsManager.getOverallLeaderboard(LIMIT);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Overall Leaderboard "
                + ChatColor.GRAY + "(your fastest core-mode time):");
        renderRows(sender, top, true);
    }

    /** Render a time-based board (seeded / avg-craft / overall) with a title. */
    private void renderSimple(CommandSender sender, String title, List<StatsManager.LeaderboardEntry> top) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + title + ":");
        renderRows(sender, top, true);
    }

    /** Render a games-count board (integer values labelled "games"). */
    private void renderGames(CommandSender sender, String title, List<StatsManager.LeaderboardEntry> top) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + title + ":");
        if (top.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No records yet.");
            return;
        }
        int rank = 1;
        for (StatsManager.LeaderboardEntry entry : top) {
            sender.sendMessage(ChatColor.AQUA + "" + rank + ". " + nameOf(entry) + ": "
                    + ChatColor.YELLOW + (long) entry.value + " games");
            rank++;
        }
    }

    private void renderRows(CommandSender sender, List<StatsManager.LeaderboardEntry> top, boolean timeBased) {
        if (top.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No records yet.");
            return;
        }
        int rank = 1;
        for (StatsManager.LeaderboardEntry entry : top) {
            String value = timeBased ? statsManager.formatTime(entry.value) : ((int) entry.value) + " crafts";
            sender.sendMessage(ChatColor.AQUA + "" + rank + ". " + nameOf(entry) + ": " + ChatColor.YELLOW + value);
            rank++;
        }
    }

    private void showItem(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.GOLD + "/leaderboard item <item>");
            sender.sendMessage(ChatColor.GRAY + "  e.g. /leaderboard item DIAMOND_SWORD");
            return;
        }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item '" + args[1] + "'. Use the Minecraft item id, e.g. DIAMOND_SWORD.");
            return;
        }
        List<StatsManager.LeaderboardEntry> top = statsManager.getItemLeaderboard(material, LIMIT);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Fastest " + InfoMaps.getItemName(material) + " crafters:");
        if (top.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No records yet.");
            return;
        }
        int rank = 1;
        for (StatsManager.LeaderboardEntry entry : top) {
            sender.sendMessage(ChatColor.AQUA + "" + rank + ". " + nameOf(entry) + ": "
                    + ChatColor.YELLOW + statsManager.formatTime(entry.value));
            rank++;
        }
    }

    private String nameOf(StatsManager.LeaderboardEntry entry) {
        if (entry.name != null) {
            return entry.name;
        }
        String resolved = Bukkit.getOfflinePlayer(entry.uuid).getName();
        return resolved != null ? resolved : entry.uuid.toString();
    }
}
