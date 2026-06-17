package com.Qwikkspell.WorkshopPractice.commands;

import com.Qwikkspell.WorkshopPractice.player.PlayerSettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /pregamecountdown <seconds>} — sets the player's per-game pre-game countdown.
 * With no argument it reports the current value.
 */
public class PregameCountdownCommand implements CommandExecutor {

    private final PlayerSettingsManager settings;

    public PregameCountdownCommand(PlayerSettingsManager settings) {
        this.settings = settings;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can set a pre-game countdown.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Your pre-game countdown is " + ChatColor.GOLD
                    + settings.getPregameCountdown(player.getUniqueId()) + "s"
                    + ChatColor.YELLOW + ". Use " + ChatColor.GOLD + "/pregamecountdown <"
                    + PlayerSettingsManager.MIN_COUNTDOWN + "-" + PlayerSettingsManager.MAX_COUNTDOWN + ">"
                    + ChatColor.YELLOW + " to change it.");
            return true;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please enter a whole number of seconds.");
            return true;
        }

        if (seconds < PlayerSettingsManager.MIN_COUNTDOWN || seconds > PlayerSettingsManager.MAX_COUNTDOWN) {
            player.sendMessage(ChatColor.RED + "Countdown must be between " + PlayerSettingsManager.MIN_COUNTDOWN
                    + " and " + PlayerSettingsManager.MAX_COUNTDOWN + " seconds.");
            return true;
        }

        settings.setPregameCountdown(player.getUniqueId(), seconds);
        player.sendMessage(ChatColor.GREEN + "Pre-game countdown set to " + ChatColor.GOLD + seconds + "s"
                + ChatColor.GREEN + ".");
        return true;
    }
}
