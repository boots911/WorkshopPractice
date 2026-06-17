package com.Qwikkspell.WorkshopPractice.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;

public class SidebarManager {

    private final ScoreboardManager manager;
    private final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();
    private final GameManager gameManager;

    public SidebarManager(GameManager gameManager) {
        this.manager = Bukkit.getScoreboardManager();
        this.gameManager = gameManager;
    }

    public void updateSidebar(Player player) {
        Game game = gameManager.getActiveGame(player);
        if (game != null && game.getStatus() == GameStatus.IN_PROGRESS) {
            createSidebar(player, game.getCurrentCraftIndex() + 1);
        } else {
            showGeneralInfo(player);
        }
    }

    public void createSidebar(Player player, int craftNumber) {
        Scoreboard board = getOrCreateScoreboard(player);
        Objective objective = getOrCreateObjective(board);

        clearSidebar(board);
        // Set up the scoreboard for a game
        objective.getScore(ChatColor.GRAY + getDate()).setScore(10);
        objective.getScore(" ").setScore(9);
        objective.getScore("Game:").setScore(8);
        objective.getScore(ChatColor.GREEN + "Workshop").setScore(7);
        objective.getScore("  ").setScore(6);
        objective.getScore(ChatColor.AQUA + player.getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + craftNumber).setScore(5);

        // Per-mode personal best (or a practice label for seeded runs)
        objective.getScore(pbLine(player)).setScore(4);

        objective.getScore("   ").setScore(3);
        objective.getScore(ChatColor.YELLOW + "Workshop Practice").setScore(2);

        // Apply the scoreboard to the player
        player.setScoreboard(board);
    }

    public void showGeneralInfo(Player player) {
        Scoreboard board = getOrCreateScoreboard(player);
        Objective objective = getOrCreateObjective(board);

        clearSidebar(board);
        // Set up the scoreboard for general information
        objective.getScore(ChatColor.GRAY + getDate()).setScore(10);
        objective.getScore(" ").setScore(9);
        objective.getScore("Game:").setScore(8);
        objective.getScore(ChatColor.GREEN + "Workshop").setScore(7);
        objective.getScore("  ").setScore(6);
        objective.getScore(ChatColor.AQUA + player.getName()).setScore(5);

        // Personal best (Left shown as the representative board when idle)
        objective.getScore(pbLine(player)).setScore(4);

        objective.getScore("   ").setScore(3);
        objective.getScore(ChatColor.YELLOW + "Workshop Practice").setScore(2);

        // Apply the scoreboard to the player
        player.setScoreboard(board);
    }

    /** Personal-best sidebar line for the player's current mode, or a seeded-run label. */
    private String pbLine(Player player) {
        Game game = gameManager.getActiveGame(player);
        StatsManager stats = gameManager.getStatsManager();

        if (game != null && game.isSeeded()) {
            return ChatColor.LIGHT_PURPLE + "Practice / Seeded";
        }

        GameMode mode = (game != null) ? game.getMode() : GameMode.LEFT;
        double pb = stats.getModePB(player, mode);
        String pbStr;
        if (pb < 0) {
            pbStr = "None";
        } else if (mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS) {
            pbStr = ((int) pb) + " crafts";
        } else {
            pbStr = stats.formatTime(pb);
        }

        String prefix = (game == null) ? "PB (Left): " : "PB: ";
        return ChatColor.GOLD + prefix + ChatColor.YELLOW + pbStr;
    }

    private Scoreboard getOrCreateScoreboard(Player player) {
        return playerScoreboards.computeIfAbsent(player, p -> manager.getNewScoreboard());
    }

    private Objective getOrCreateObjective(Scoreboard board) {
        Objective objective = board.getObjective("sidebar");
        if (objective == null) {
            objective = board.registerNewObjective("sidebar", "dummy", ChatColor.YELLOW + "" + ChatColor.BOLD + "Party Games");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        return objective;
    }

    private void clearSidebar(Scoreboard board) {
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
    }

    private String getDate() {
        // Simple date formatting
        return java.time.LocalDate.now().toString();
    }
}
