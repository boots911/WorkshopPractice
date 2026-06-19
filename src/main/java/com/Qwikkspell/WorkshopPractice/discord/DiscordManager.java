package com.Qwikkspell.WorkshopPractice.discord;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import com.Qwikkspell.WorkshopPractice.game.CraftSequence;
import com.Qwikkspell.WorkshopPractice.game.GameManager;
import com.Qwikkspell.WorkshopPractice.game.GameMode;
import com.Qwikkspell.WorkshopPractice.game.StatsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Embedded Discord bot (JDA). Runs inside the Minecraft server process, reads stats directly from
 * {@link StatsManager}, and exposes {@code /leaderboard} and {@code /findseed} slash commands plus
 * personal-best announcements. Event handling lives in {@link BotCommandListener}; this class owns
 * the lifecycle, command registration, and the data/rendering helpers it calls.
 */
public class DiscordManager {

    public static final int PAGE_SIZE = 15;
    public static final int MAX_ENTRIES = 50;

    private final WorkshopPractice plugin;
    private final GameManager gameManager;
    private final String token;
    private final String guildId;
    private final String pbChannelId;

    private JDA jda;

    public DiscordManager(WorkshopPractice plugin, GameManager gameManager,
                          String token, String guildId, String pbChannelId) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.token = token;
        this.guildId = guildId;
        this.pbChannelId = pbChannelId;
    }

    // ------------------------------------------------------------------ lifecycle

    /** Connects and registers commands. Blocking — call from an async task. */
    public void start() {
        try {
            plugin.getLogger().info("[Discord] Logging in to Discord...");
            jda = JDABuilder.createLight(token)
                    .setActivity(Activity.playing("Workshop Practice"))
                    .addEventListeners(new BotCommandListener(this))
                    .build();
            jda.awaitReady();
            plugin.getLogger().info("[Discord] Logged in as " + jda.getSelfUser().getName()
                    + " (in " + jda.getGuilds().size() + " server(s)).");
            registerCommands();
            plugin.getLogger().info("[Discord] Bot ready.");
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.WARNING,
                    "[Discord] Failed to start bot: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }

    private void registerCommands() {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            plugin.getLogger().warning("[Discord] Guild '" + guildId + "' not found - check guildId and that the bot"
                    + " was invited to that server. Commands not registered. Bot is in: "
                    + jda.getGuilds().stream().map(g -> g.getName() + "(" + g.getId() + ")").toList());
            return;
        }
        plugin.getLogger().info("[Discord] Registering slash commands in guild " + guild.getName() + "...");

        OptionData category = new OptionData(OptionType.STRING, "category", "Which leaderboard to show", true)
                .addChoice("Left", "left")
                .addChoice("Left Easy", "lefteasy")
                .addChoice("Right", "right")
                .addChoice("Right Easy", "righteasy")
                .addChoice("All Crafts (Left)", "allcraftsleft")
                .addChoice("All Crafts (Left Easy)", "allcraftslefteasy")
                .addChoice("All Crafts (Right)", "allcraftsright")
                .addChoice("All Crafts (Right Easy)", "allcraftsrighteasy")
                .addChoice("Time Trial 60s", "timetrial60")
                .addChoice("Overall", "overall")
                .addChoice("Best Seeded", "setseed")
                .addChoice("Total Games", "totalgames")
                .addChoice("Avg Craft", "avgcraft")
                .addChoice("Item", "item");
        OptionData item = new OptionData(OptionType.STRING, "item", "Item id (required when category is Item)", false, true);
        SlashCommandData leaderboard = Commands.slash("leaderboard", "View Workshop Practice leaderboards")
                .addOptions(category, item);

        SlashCommandData findseed = Commands.slash("findseed", "Get the seed that plays 5 crafts in order")
                .addOptions(
                        new OptionData(OptionType.STRING, "craft1", "1st craft", true, true),
                        new OptionData(OptionType.STRING, "craft2", "2nd craft", true, true),
                        new OptionData(OptionType.STRING, "craft3", "3rd craft", true, true),
                        new OptionData(OptionType.STRING, "craft4", "4th craft", true, true),
                        new OptionData(OptionType.STRING, "craft5", "5th craft", true, true));

        guild.updateCommands().addCommands(leaderboard, findseed).queue(
                ok -> plugin.getLogger().info("Registered Discord slash commands."),
                err -> plugin.getLogger().warning("Failed to register Discord commands: " + err.getMessage()));
    }

    // ------------------------------------------------------------------ PB announcements

    public void announcePersonalBest(String playerName, String modeDisplay, String valueStr) {
        if (jda == null || pbChannelId == null || pbChannelId.isBlank()) {
            return;
        }
        TextChannel channel = jda.getTextChannelById(pbChannelId);
        if (channel == null) {
            plugin.getLogger().warning("[Discord] PB channel '" + pbChannelId + "' not found - check pbChannelId and"
                    + " that the bot can see that channel. Announcement skipped.");
            return;
        }
        channel.sendMessage("🏆 **" + playerName + "** set a new **" + modeDisplay + "** PB: `" + valueStr + "`")
                .queue();
    }

    // ------------------------------------------------------------------ leaderboard rendering

    /** Build one page of a leaderboard, or {@code null} if the category/item is invalid. */
    public Page renderPage(String category, String itemArg, int page) {
        List<StatsManager.LeaderboardEntry> all = entriesFor(category, itemArg);
        if (all == null) {
            return null;
        }
        int total = Math.min(all.size(), MAX_ENTRIES);
        int maxPage = total == 0 ? 0 : (total - 1) / PAGE_SIZE;
        int safePage = Math.max(0, Math.min(page, maxPage));

        StringBuilder sb = new StringBuilder();
        sb.append("**").append(titleFor(category, itemArg)).append("**\n");
        if (total == 0) {
            sb.append("_No records yet._");
        } else {
            sb.append("```\n");
            int start = safePage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, total);
            for (int i = start; i < end; i++) {
                StatsManager.LeaderboardEntry e = all.get(i);
                String name = (e.name != null && !e.name.isBlank()) ? e.name : "Unknown";
                sb.append(String.format("%2d. %-16s %s%n", i + 1, truncate(name, 16), formatValue(category, e.value)));
            }
            sb.append("```");
            sb.append("Page ").append(safePage + 1).append('/').append(maxPage + 1);
        }

        Page p = new Page();
        p.content = sb.toString();
        p.category = category;
        p.item = itemArg;
        p.page = safePage;
        p.hasPrev = safePage > 0;
        p.hasNext = safePage < maxPage;
        return p;
    }

    private List<StatsManager.LeaderboardEntry> entriesFor(String category, String itemArg) {
        StatsManager s = gameManager.getStatsManager();
        switch (category) {
            case "overall":     return s.getOverallLeaderboard(MAX_ENTRIES);
            case "setseed":     return s.getSeededLeaderboard(MAX_ENTRIES);
            case "totalgames":  return s.getTotalGamesLeaderboard(MAX_ENTRIES);
            case "avgcraft":    return s.getAvgCraftLeaderboard(MAX_ENTRIES);
            case "item":
                if (itemArg == null) return null;
                org.bukkit.Material m = org.bukkit.Material.matchMaterial(itemArg);
                return m == null ? null : s.getItemLeaderboard(m, MAX_ENTRIES);
            default:
                Optional<GameMode> mode = GameMode.fromAlias(category);
                return mode.map(value -> s.getModeLeaderboard(value, MAX_ENTRIES)).orElse(null);
        }
    }

    private String titleFor(String category, String itemArg) {
        switch (category) {
            case "overall":    return "Overall — Fastest Core Time";
            case "setseed":    return "Best Seeded (Practice) Times";
            case "totalgames": return "Most Games Completed";
            case "avgcraft":   return "Fastest Average Craft";
            case "item":
                org.bukkit.Material m = itemArg == null ? null : org.bukkit.Material.matchMaterial(itemArg);
                return "Fastest " + (m == null ? itemArg : prettyName(m)) + " Crafters";
            default:
                return GameMode.fromAlias(category).map(gm -> gm.getDisplayName() + " — Best Times").orElse("Leaderboard");
        }
    }

    private String formatValue(String category, double value) {
        switch (category) {
            case "timetrial60": return (long) value + " crafts";
            case "totalgames":  return (long) value + " games";
            default:            return gameManager.getStatsManager().formatTime(value);
        }
    }

    // ------------------------------------------------------------------ /findseed

    /** Encode 5 ordered craft names into their seed. */
    public SeedResult findSeed(List<String> craftNames) {
        List<org.bukkit.Material> universe = gameManager.getCraftUniverse();
        List<org.bukkit.Material> ordered = new ArrayList<>();
        for (String raw : craftNames) {
            org.bukkit.Material m = org.bukkit.Material.matchMaterial(raw);
            if (m == null || !universe.contains(m)) {
                return SeedResult.fail("❌ `" + raw + "` isn't a craftable item. Pick from the autocomplete suggestions.");
            }
            if (ordered.contains(m)) {
                return SeedResult.fail("❌ `" + raw + "` is listed twice — the 5 crafts must be different.");
            }
            ordered.add(m);
        }
        try {
            long seed = CraftSequence.encodeSelection(universe, ordered);
            StringBuilder sb = new StringBuilder();
            sb.append("🌱 Seed **").append(seed).append("** plays these crafts in order:\n");
            for (int i = 0; i < ordered.size(); i++) {
                sb.append(i + 1).append(". ").append(prettyName(ordered.get(i))).append('\n');
            }
            sb.append("Play it with `/play left seed ").append(seed).append("` (works for any core mode).");
            return SeedResult.ok(sb.toString());
        } catch (IllegalArgumentException e) {
            return SeedResult.fail("❌ " + e.getMessage());
        }
    }

    /** Material ids (for slash-command autocomplete), in the canonical craft order. */
    public List<String> craftNames() {
        List<String> names = new ArrayList<>();
        for (org.bukkit.Material m : gameManager.getCraftUniverse()) {
            names.add(m.name());
        }
        return names;
    }

    private static String prettyName(org.bukkit.Material m) {
        String[] words = m.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    // ------------------------------------------------------------------ data holders

    public static final class Page {
        public String content;
        public String category;
        public String item;
        public int page;
        public boolean hasPrev;
        public boolean hasNext;
    }

    public static final class SeedResult {
        public final boolean success;
        public final String message;

        private SeedResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static SeedResult ok(String message) {
            return new SeedResult(true, message);
        }

        static SeedResult fail(String message) {
            return new SeedResult(false, message);
        }
    }
}
