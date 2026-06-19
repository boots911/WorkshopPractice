package com.Qwikkspell.WorkshopPractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** {@code /playhelp} — lists every play command, the seed syntax, and examples. */
public class PlayHelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Workshop Practice — Play Commands");
        sender.sendMessage(ChatColor.YELLOW + "/play left" + ChatColor.GRAY + " - 5 crafts, normal (left side)");
        sender.sendMessage(ChatColor.YELLOW + "/play lefteasy" + ChatColor.GRAY + " - 5 crafts, easy (left side)");
        sender.sendMessage(ChatColor.YELLOW + "/play right" + ChatColor.GRAY + " - 5 crafts, normal (right side)");
        sender.sendMessage(ChatColor.YELLOW + "/play righteasy" + ChatColor.GRAY + " - 5 crafts, easy (right side)");
        sender.sendMessage(ChatColor.YELLOW + "/play allcrafts [left|lefteasy|right|righteasy]" + ChatColor.GRAY
                + " - craft every item once, no time limit (defaults to left easy)");
        sender.sendMessage(ChatColor.YELLOW + "/play timetrial60" + ChatColor.GRAY + " - most crafts you can do in 60 seconds");
        sender.sendMessage(ChatColor.GOLD + "Seeded (practice) runs:");
        sender.sendMessage(ChatColor.YELLOW + "/play <mode> seed <number>" + ChatColor.GRAY + " - replay an exact run");
        sender.sendMessage(ChatColor.GRAY + "  e.g. /play left seed 123456   /play allcrafts seed 987654");
        sender.sendMessage(ChatColor.GRAY + "  Seeded runs don't affect leaderboards or personal bests.");
        sender.sendMessage(ChatColor.GOLD + "Other:");
        sender.sendMessage(ChatColor.YELLOW + "/l" + ChatColor.GRAY + " - leave your current game and return to spawn");
        sender.sendMessage(ChatColor.YELLOW + "/leaderboard <mode|overall>" + ChatColor.GRAY + " - view rankings");
        sender.sendMessage(ChatColor.YELLOW + "/leaderboard item <item>" + ChatColor.GRAY + " - fastest crafters of an item");
        sender.sendMessage(ChatColor.YELLOW + "/pregamecountdown <0-10>" + ChatColor.GRAY + " - set your pre-game countdown");
        return true;
    }
}
