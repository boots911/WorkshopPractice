package com.Qwikkspell.WorkshopPractice.game;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * New stats store: per-mode aggregates, per-item stats, the overall leaderboard, and
 * per-seed best results. Replaces {@link ScoreManager} in the live game path
 * ({@code ScoreManager} is kept purely as a read-only loader of the legacy {@code scores.yml}).
 *
 * <p>Storage is file-based YAML ({@code stats.yml} + {@code seeds.yml}), keyed by UUID with the
 * username kept only as {@code name} metadata. The schema is nested and additive so new modes,
 * items, or fields need no migration.</p>
 *
 * <p>Thread-safety: all game callbacks run on the main server thread, so mutations are already
 * serialized; this class additionally uses {@link ConcurrentHashMap} and {@code synchronized}
 * record/save methods so writes stay consistent.</p>
 */
public class StatsManager {

    private final WorkshopPractice plugin;
    private final File statsFile;
    private final File seedsFile;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.000");

    private final ConcurrentHashMap<UUID, PlayerStats> players = new ConcurrentHashMap<>();
    // modeKey -> (seed -> record)
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, SeedRecord>> seeds = new ConcurrentHashMap<>();

    public StatsManager(WorkshopPractice plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        this.seedsFile = new File(plugin.getDataFolder(), "seeds.yml");
        load();
    }

    // ---------------------------------------------------------------- recording

    /**
     * Record a completed FASTEST_TIME run (base modes + all crafts). Updates aggregates, item
     * stats, and the per-mode best. Must NOT be called for seeded runs.
     *
     * @return true if this set a new personal best for the mode (caller may broadcast)
     */
    public synchronized boolean recordCompletion(OfflinePlayer player, GameMode mode, double timeSeconds,
                                                 List<CraftRecord> crafts) {
        PlayerStats ps = playerStats(player);
        ModeStats ms = ps.mode(mode.leaderboardKey());
        ms.gamesPlayed++;
        ms.totalTime += timeSeconds;
        ms.craftsCompleted += crafts.size();
        boolean newPB = ms.bestTime < 0 || timeSeconds < ms.bestTime;
        if (newPB) {
            ms.bestTime = timeSeconds;
        }
        applyItemCrafts(ps, crafts);
        save();
        return newPB;
    }

    /**
     * Record a completed MOST_CRAFTS run (time trial). Must NOT be called for seeded runs.
     *
     * @return true if this set a new best craft count for the mode
     */
    public synchronized boolean recordTimeTrial(OfflinePlayer player, GameMode mode, int craftCount,
                                                List<CraftRecord> crafts) {
        PlayerStats ps = playerStats(player);
        ModeStats ms = ps.mode(mode.leaderboardKey());
        ms.gamesPlayed++;
        ms.totalCount += craftCount;
        ms.craftsCompleted += crafts.size();
        boolean newBest = ms.bestCount < 0 || craftCount > ms.bestCount;
        if (newBest) {
            ms.bestCount = craftCount;
        }
        applyItemCrafts(ps, crafts);
        save();
        return newBest;
    }

    private void applyItemCrafts(PlayerStats ps, List<CraftRecord> crafts) {
        for (CraftRecord cr : crafts) {
            ItemStats is = ps.item(cr.material);
            is.timesCrafted++;
            is.totalTime += cr.seconds;
            if (is.bestTime < 0 || cr.seconds < is.bestTime) {
                is.bestTime = cr.seconds;
            }
        }
    }

    /** Record the best result for a specific seeded run (the only thing seeded runs persist). */
    public synchronized void recordSeedResult(GameMode mode, long seed, OfflinePlayer player, double value) {
        ConcurrentHashMap<Long, SeedRecord> byMode =
                seeds.computeIfAbsent(mode.leaderboardKey(), k -> new ConcurrentHashMap<>());
        SeedRecord existing = byMode.get(seed);
        boolean better;
        if (existing == null) {
            better = true;
        } else if (mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS) {
            better = value > existing.value; // higher craft count is better
        } else {
            better = value < existing.value; // lower time is better
        }
        if (better) {
            SeedRecord rec = new SeedRecord();
            rec.value = value;
            rec.holderUuid = player.getUniqueId();
            rec.holderName = player.getName();
            byMode.put(seed, rec);
            saveSeeds();
        }
    }

    // ---------------------------------------------------------------- queries

    /** Personal best for a mode: best time (FASTEST_TIME) or best count (MOST_CRAFTS); -1 if none. */
    public double getModePB(OfflinePlayer player, GameMode mode) {
        PlayerStats ps = players.get(player.getUniqueId());
        if (ps == null) return -1;
        ModeStats ms = ps.modes.get(mode.leaderboardKey());
        if (ms == null) return -1;
        return mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS ? ms.bestCount : ms.bestTime;
    }

    /** Top entries for a single mode, sorted best-first (time asc / count desc). */
    public List<LeaderboardEntry> getModeLeaderboard(GameMode mode, int limit) {
        boolean timeBased = mode.getScoringType() == GameMode.ScoringType.FASTEST_TIME;
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (PlayerStats ps : players.values()) {
            ModeStats ms = ps.modes.get(mode.leaderboardKey());
            if (ms == null) continue;
            double value = timeBased ? ms.bestTime : ms.bestCount;
            if (value < 0) continue;
            entries.add(new LeaderboardEntry(ps.uuid, ps.name, value));
        }
        entries.sort(timeBased
                ? Comparator.comparingDouble(e -> e.value)
                : Comparator.comparingDouble((LeaderboardEntry e) -> e.value).reversed());
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    /**
     * Overall leaderboard: each player's single fastest time across the four base modes (their best
     * core-mode run, whichever mode it was). Any player with at least one base-mode PB is ranked.
     */
    public List<LeaderboardEntry> getOverallLeaderboard(int limit) {
        GameMode[] base = GameMode.overallModes();
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (PlayerStats ps : players.values()) {
            double best = -1;
            for (GameMode mode : base) {
                ModeStats ms = ps.modes.get(mode.leaderboardKey());
                if (ms == null || ms.bestTime < 0) {
                    continue;
                }
                if (best < 0 || ms.bestTime < best) {
                    best = ms.bestTime;
                }
            }
            if (best >= 0) {
                entries.add(new LeaderboardEntry(ps.uuid, ps.name, best));
            }
        }
        entries.sort(Comparator.comparingDouble(e -> e.value));
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    /** Per-item leaderboard ranked by fastest single craft time of that item. */
    public List<LeaderboardEntry> getItemLeaderboard(Material material, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (PlayerStats ps : players.values()) {
            ItemStats is = ps.items.get(material);
            if (is == null || is.bestTime < 0) {
                continue;
            }
            entries.add(new LeaderboardEntry(ps.uuid, ps.name, is.bestTime));
        }
        entries.sort(Comparator.comparingDouble(e -> e.value));
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    /**
     * One-time/idempotent migration: treat every legacy {@code scores.yml} best time as a
     * {@code lefteasy} personal best (the owner confirmed all legacy scores are Left Easy times).
     * Uses {@code min}, so re-running on every startup is safe and never overwrites a faster
     * real run. The legacy file itself is never modified.
     */
    public synchronized void importLegacyScores(Map<UUID, Double> legacy) {
        if (legacy == null || legacy.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (Map.Entry<UUID, Double> entry : legacy.entrySet()) {
            double time = entry.getValue();
            if (time <= 0) {
                continue;
            }
            PlayerStats ps = players.computeIfAbsent(entry.getKey(), PlayerStats::new);
            ModeStats ms = ps.mode(GameMode.LEFT_EASY.leaderboardKey());
            if (ms.bestTime < 0 || time < ms.bestTime) {
                ms.bestTime = time;
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    public SeedRecord getSeedBest(GameMode mode, long seed) {
        ConcurrentHashMap<Long, SeedRecord> byMode = seeds.get(mode.leaderboardKey());
        return byMode == null ? null : byMode.get(seed);
    }

    public String formatTime(double seconds) {
        if (seconds < 0) return "None";
        return decimalFormat.format(seconds) + "s";
    }

    // ---------------------------------------------------------------- persistence

    private PlayerStats playerStats(OfflinePlayer player) {
        PlayerStats ps = players.computeIfAbsent(player.getUniqueId(), PlayerStats::new);
        if (player.getName() != null) {
            ps.name = player.getName(); // refresh metadata
        }
        return ps;
    }

    private void load() {
        loadStats();
        loadSeeds();
    }

    private void loadStats() {
        if (!statsFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(statsFile);
        for (String key : cfg.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            PlayerStats ps = new PlayerStats(uuid);
            ps.name = cfg.getString(key + ".name");

            ConfigurationSection modes = cfg.getConfigurationSection(key + ".modes");
            if (modes != null) {
                for (String modeKey : modes.getKeys(false)) {
                    ModeStats ms = ps.mode(modeKey);
                    String base = key + ".modes." + modeKey;
                    ms.gamesPlayed = cfg.getInt(base + ".gamesPlayed");
                    ms.bestTime = cfg.getDouble(base + ".bestTime", -1);
                    ms.totalTime = cfg.getDouble(base + ".totalTime");
                    ms.craftsCompleted = cfg.getLong(base + ".craftsCompleted");
                    ms.wins = cfg.getInt(base + ".wins");
                    ms.bestCount = cfg.getInt(base + ".bestCount", -1);
                    ms.totalCount = cfg.getLong(base + ".totalCount");
                }
            }

            ConfigurationSection items = cfg.getConfigurationSection(key + ".items");
            if (items != null) {
                for (String itemKey : items.getKeys(false)) {
                    Material material = Material.matchMaterial(itemKey);
                    if (material == null) continue;
                    ItemStats is = ps.item(material);
                    String base = key + ".items." + itemKey;
                    is.timesCrafted = cfg.getLong(base + ".timesCrafted");
                    is.bestTime = cfg.getDouble(base + ".bestTime", -1);
                    is.totalTime = cfg.getDouble(base + ".totalTime");
                }
            }
            players.put(uuid, ps);
        }
    }

    private void loadSeeds() {
        if (!seedsFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(seedsFile);
        for (String modeKey : cfg.getKeys(false)) {
            ConfigurationSection modeSec = cfg.getConfigurationSection(modeKey);
            if (modeSec == null) continue;
            ConcurrentHashMap<Long, SeedRecord> byMode = new ConcurrentHashMap<>();
            for (String seedKey : modeSec.getKeys(false)) {
                long seed;
                try {
                    seed = Long.parseLong(seedKey);
                } catch (NumberFormatException e) {
                    continue;
                }
                String base = modeKey + "." + seedKey;
                SeedRecord rec = new SeedRecord();
                // Backwards/forwards tolerant: accept either bestTime or bestCount.
                if (cfg.contains(base + ".bestCount")) {
                    rec.value = cfg.getDouble(base + ".bestCount");
                } else {
                    rec.value = cfg.getDouble(base + ".bestTime");
                }
                String holder = cfg.getString(base + ".holderUuid");
                if (holder != null) {
                    try {
                        rec.holderUuid = UUID.fromString(holder);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                rec.holderName = cfg.getString(base + ".holderName");
                byMode.put(seed, rec);
            }
            seeds.put(modeKey, byMode);
        }
    }

    public synchronized void save() {
        FileConfiguration cfg = new YamlConfiguration();
        for (PlayerStats ps : players.values()) {
            String key = ps.uuid.toString();
            if (ps.name != null) {
                cfg.set(key + ".name", ps.name);
            }
            for (Map.Entry<String, ModeStats> e : ps.modes.entrySet()) {
                ModeStats ms = e.getValue();
                String base = key + ".modes." + e.getKey();
                cfg.set(base + ".gamesPlayed", ms.gamesPlayed);
                if (ms.bestTime >= 0) cfg.set(base + ".bestTime", ms.bestTime);
                cfg.set(base + ".totalTime", ms.totalTime);
                cfg.set(base + ".craftsCompleted", ms.craftsCompleted);
                cfg.set(base + ".wins", ms.wins);
                if (ms.bestCount >= 0) cfg.set(base + ".bestCount", ms.bestCount);
                if (ms.totalCount > 0) cfg.set(base + ".totalCount", ms.totalCount);
            }
            for (Map.Entry<Material, ItemStats> e : ps.items.entrySet()) {
                ItemStats is = e.getValue();
                String base = key + ".items." + e.getKey().name();
                cfg.set(base + ".timesCrafted", is.timesCrafted);
                if (is.bestTime >= 0) cfg.set(base + ".bestTime", is.bestTime);
                cfg.set(base + ".totalTime", is.totalTime);
            }
        }
        writeConfig(cfg, statsFile);
    }

    private synchronized void saveSeeds() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, ConcurrentHashMap<Long, SeedRecord>> modeEntry : seeds.entrySet()) {
            for (Map.Entry<Long, SeedRecord> seedEntry : modeEntry.getValue().entrySet()) {
                String base = modeEntry.getKey() + "." + seedEntry.getKey();
                SeedRecord rec = seedEntry.getValue();
                cfg.set(base + ".bestTime", rec.value);
                if (rec.holderUuid != null) cfg.set(base + ".holderUuid", rec.holderUuid.toString());
                if (rec.holderName != null) cfg.set(base + ".holderName", rec.holderName);
            }
        }
        writeConfig(cfg, seedsFile);
    }

    private void writeConfig(FileConfiguration cfg, File file) {
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save " + file.getName() + ": " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- data types

    /** One completed craft within a run, used to update item stats. */
    public static final class CraftRecord {
        public final Material material;
        public final double seconds;

        public CraftRecord(Material material, double seconds) {
            this.material = material;
            this.seconds = seconds;
        }
    }

    public static final class LeaderboardEntry {
        public final UUID uuid;
        public final String name;
        public final double value; // time (seconds) or craft count, depending on mode

        public LeaderboardEntry(UUID uuid, String name, double value) {
            this.uuid = uuid;
            this.name = name;
            this.value = value;
        }
    }

    public static final class SeedRecord {
        public double value; // best time (seconds) or best count
        public UUID holderUuid;
        public String holderName;
    }

    private static final class PlayerStats {
        final UUID uuid;
        String name;
        final Map<String, ModeStats> modes = new HashMap<>();
        final Map<Material, ItemStats> items = new HashMap<>();

        PlayerStats(UUID uuid) {
            this.uuid = uuid;
        }

        ModeStats mode(String key) {
            return modes.computeIfAbsent(key, k -> new ModeStats());
        }

        ItemStats item(Material material) {
            return items.computeIfAbsent(material, m -> new ItemStats());
        }
    }

    private static final class ModeStats {
        int gamesPlayed;
        double bestTime = -1;   // FASTEST_TIME modes; -1 = unset
        double totalTime;
        long craftsCompleted;
        int wins;               // reserved for future competitive modes
        int bestCount = -1;     // MOST_CRAFTS modes; -1 = unset
        long totalCount;
    }

    private static final class ItemStats {
        long timesCrafted;
        double bestTime = -1;
        double totalTime;
    }
}
