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
 * <p>Modes: left, lefteasy, right, righteasy, timetrial60, and All Crafts. All Crafts has four
 * side/difficulty variants: {@code /play allcrafts [left|lefteasy|right|righteasy]}, defaulting to
 * left easy (the most popular). Omitting the seed starts a ranked run with an auto seed; supplying
 * one replays that exact run as practice.</p>
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

        String first = args[0].toLowerCase();
        GameMode mode;
        int seedIndex; // where an optional "seed <n>" may begin

        if (first.equals("allcrafts")) {
            // /play allcrafts [left|lefteasy|right|righteasy] [seed <n>]  (defaults to left easy)
            String variant = "lefteasy";
            seedIndex = 1;
            if (args.length >= 2 && !args[1].equalsIgnoreCase("seed")) {
                variant = args[1].toLowerCase();
                seedIndex = 2;
            }
            mode = GameMode.allCrafts(variant);
            if (mode == null) {
                player.sendMessage(ChatColor.RED + "All Crafts variant must be left, lefteasy, right, or righteasy.");
                player.sendMessage(ChatColor.GRAY + "e.g. /play allcrafts right  (or just /play allcrafts for left easy)");
                return true;
            }
        } else {
            Optional<GameMode> modeOpt = GameMode.fromAlias(first);
            if (modeOpt.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Unknown mode '" + args[0] + "'.");
                sendUsage(player);
                return true;
            }
            mode = modeOpt.get();
            seedIndex = 1;
        }

        // Optional "seed <number>" beginning at seedIndex.
        Long seed = null;
        if (args.length > seedIndex) {
            if (!args[seedIndex].equalsIgnoreCase("seed")) {
                player.sendMessage(ChatColor.RED + "Unexpected argument '" + args[seedIndex] + "'.");
                sendUsage(player);
                return true;
            }
            if (args.length <= seedIndex + 1) {
                player.sendMessage(ChatColor.RED + "Usage: /play " + first + " ... seed <number>");
                return true;
            }
            try {
                seed = Long.parseLong(args[seedIndex + 1]);
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
        player.sendMessage(ChatColor.YELLOW + "Modes: " + ChatColor.GOLD + "left, lefteasy, right, righteasy, timetrial60");
        player.sendMessage(ChatColor.YELLOW + "All Crafts: " + ChatColor.GOLD + "/play allcrafts [left|lefteasy|right|righteasy]"
                + ChatColor.GRAY + " (defaults to left easy)");
        player.sendMessage(ChatColor.GRAY + "Type /playhelp for details.");
    }
}
