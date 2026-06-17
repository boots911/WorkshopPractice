package com.Qwikkspell.WorkshopPractice.commands;

import com.Qwikkspell.WorkshopPractice.game.GameManager;
import com.Qwikkspell.WorkshopPractice.game.GameMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * {@code /play <mode> [seed <number>]}.
 *
 * <p>Modes: left, lefteasy, right, righteasy, allcrafts, timetrial60. Omitting the seed starts a
 * ranked run with an auto-generated seed; supplying one replays that exact run as practice.</p>
 */
public class PlayCommand implements CommandExecutor {

    private final GameManager gameManager;

    public PlayCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can play.");
            return true;
        }

        if (args.length < 1) {
            sendUsage(player);
            return true;
        }

        Optional<GameMode> modeOpt = GameMode.fromAlias(args[0]);
        if (modeOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Unknown mode '" + args[0] + "'.");
            sendUsage(player);
            return true;
        }
        GameMode mode = modeOpt.get();

        Long seed = null;
        if (args.length >= 2) {
            if (!args[1].equalsIgnoreCase("seed")) {
                player.sendMessage(ChatColor.RED + "Unexpected argument '" + args[1] + "'.");
                sendUsage(player);
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: /play " + mode.getAlias() + " seed <number>");
                return true;
            }
            try {
                seed = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Seed must be a whole number.");
                return true;
            }
            if (seed < 0) {
                player.sendMessage(ChatColor.RED + "Seed must be a non-negative number.");
                return true;
            }
        }

        gameManager.startGame(player, mode, seed);
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.GOLD + "/play <mode> [seed <number>]");
        player.sendMessage(ChatColor.YELLOW + "Modes: " + ChatColor.GOLD
                + "left, lefteasy, right, righteasy, allcrafts, timetrial60");
        player.sendMessage(ChatColor.GRAY + "Type /playhelp for details.");
    }
}
