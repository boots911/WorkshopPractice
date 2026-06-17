package com.Qwikkspell.WorkshopPractice.player;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores per-player preferences in {@code settings.yml}, keyed by UUID.
 *
 * <p>Currently holds the per-player pre-game countdown (replacing the old per-command
 * {@code waitTime} argument). In memory it uses a {@link ConcurrentHashMap}; file writes go
 * through a {@code synchronized} save so concurrent updates stay consistent.</p>
 */
public class PlayerSettingsManager {

    public static final int MIN_COUNTDOWN = 0;
    public static final int MAX_COUNTDOWN = 10;
    public static final int DEFAULT_COUNTDOWN = 6;

    private final WorkshopPractice plugin;
    private final File file;
    private final int defaultCountdown;
    private final ConcurrentHashMap<UUID, Integer> pregameCountdown = new ConcurrentHashMap<>();

    public PlayerSettingsManager(WorkshopPractice plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "settings.yml");
        this.defaultCountdown = clamp(plugin.getConfig().getInt("defaultPregameCountdown", DEFAULT_COUNTDOWN));
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                pregameCountdown.put(uuid, clamp(cfg.getInt(key + ".pregameCountdown", defaultCountdown)));
            } catch (IllegalArgumentException ignored) {
                // skip malformed keys
            }
        }
    }

    public int getDefaultCountdown() {
        return defaultCountdown;
    }

    public int getPregameCountdown(UUID uuid) {
        return pregameCountdown.getOrDefault(uuid, defaultCountdown);
    }

    public void setPregameCountdown(UUID uuid, int seconds) {
        pregameCountdown.put(uuid, clamp(seconds));
        save();
    }

    public static int clamp(int seconds) {
        return Math.max(MIN_COUNTDOWN, Math.min(MAX_COUNTDOWN, seconds));
    }

    public synchronized void save() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : pregameCountdown.entrySet()) {
            cfg.set(entry.getKey().toString() + ".pregameCountdown", entry.getValue());
        }
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save settings.yml: " + ex.getMessage());
        }
    }
}
