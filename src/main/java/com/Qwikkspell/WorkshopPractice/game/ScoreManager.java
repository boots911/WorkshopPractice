package com.Qwikkspell.WorkshopPractice.game;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreManager {
    private final WorkshopPractice plugin;
    private final File scoresFile;
    private FileConfiguration scoresConfig;

    // Map to hold top scores for each player
    private final Map<UUID, Double> topGameScores = new HashMap<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.000");

    public ScoreManager(WorkshopPractice plugin) {
        this.plugin = plugin;
        this.scoresFile = new File(plugin.getDataFolder(), "scores.yml");
        loadScores();
    }

    // Load scores from the file
    private void loadScores() {
        if (!scoresFile.exists()) {
            try {
                scoresFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scoresConfig = YamlConfiguration.loadConfiguration(scoresFile);

        for (String uuidStr : scoresConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            topGameScores.put(uuid, scoresConfig.getDouble(uuidStr + ".game"));
        }
    }

    // Save scores to the file
    public void saveScores() {
        for (Map.Entry<UUID, Double> entry : topGameScores.entrySet()) {
            UUID uuid = entry.getKey();
            scoresConfig.set(uuid.toString() + ".game", entry.getValue());
        }
        try {
            scoresConfig.save(scoresFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update game score if it's a new record
    public boolean updateTopScore(Player player, double time) {
        UUID uuid = player.getUniqueId();
        if (!topGameScores.containsKey(uuid) || time < topGameScores.get(uuid)) {
            topGameScores.put(uuid, time);
            saveScores();
            String message = ChatColor.GOLD + "" + ChatColor.BOLD + "NEW RECORD! " + ChatColor.GOLD + "Fastest Time: " + ChatColor.YELLOW + getFormattedScore(time) + " by " + ChatColor.AQUA + player.getName();
            Bukkit.broadcastMessage(message);

            // Play firework sound for all players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
            }
            return true; // New record
        }
        return false;
    }

    // Get the top game score for a player
    public double getTopGameScore(Player player) {
        return topGameScores.getOrDefault(player.getUniqueId(), -1.0);
    }

    // Get the formatted top game score for a player
    public String getFormattedTopGameScore(Player player) {
        double score = getTopGameScore(player);
        if (score == -1.0) {
            return "None";
        }
        return decimalFormat.format(score) + "s";
    }

    // Get the formatted top game score based on the double value
    public String getFormattedScore(double score) {
        if (score == -1.0) {
            return "None";
        }
        return decimalFormat.format(score) + "s";
    }

    // Get the top 100 scores
    public List<Map.Entry<UUID, Double>> getTopScores(int limit) {
        return topGameScores.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Read-only view of the legacy {@code scores.yml} data (UUID -> best total time). Preserved
     * so the owner can later merge these ~150 historical records into the {@code lefteasy} board.
     * The new game path writes to {@link StatsManager}, never here, so this data is never mutated.
     */
    public Map<UUID, Double> getLegacyScores() {
        return Collections.unmodifiableMap(topGameScores);
    }
}
