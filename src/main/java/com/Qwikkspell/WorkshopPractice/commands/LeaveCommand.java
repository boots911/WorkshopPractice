package com.Qwikkspell.WorkshopPractice.commands;

import com.Qwikkspell.WorkshopPractice.game.Game;
import com.Qwikkspell.WorkshopPractice.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * {@code /l} — leave the current game and return to spawn.
 *
 * <p>This abandons the run: {@link Game#end()} tears the game down without calling any of the
 * scoring paths, so no stats, PBs, or seed results are saved (exactly like walking out of the
 * station or timing out).</p>
 */
public class LeaveCommand implements CommandExecutor {

    private final GameManager gameManager;

    public LeaveCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Game game = gameManager.getActiveGame(player);
        if (game != null) {
            game.end(); // abandons the run (no stats saved) and teleports the player to spawn
            player.sendMessage(ChatColor.YELLOW + "You left the game. Nothing from that run was saved.");
        } else {
            player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.sendMessage(ChatColor.YELLOW + "Returned to spawn.");
        }
        return true;
    }
}
