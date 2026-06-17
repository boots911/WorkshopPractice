package com.Qwikkspell.WorkshopPractice.listeners;

import com.Qwikkspell.WorkshopPractice.game.Game;
import com.Qwikkspell.WorkshopPractice.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Ends a player's game when they disconnect, so the station is freed and the game task is
 * cancelled. The run is abandoned (no stats saved) — identical to leaving the station or using
 * {@code /l}. Without this, a player who logs out while standing inside the station would leak it
 * (it would stay occupied until a restart).
 */
public class PlayerQuitListener implements Listener {

    private final GameManager gameManager;

    public PlayerQuitListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Game game = gameManager.getActiveGame(event.getPlayer());
        if (game != null) {
            game.end();
        }
    }
}
