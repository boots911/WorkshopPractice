package com.Qwikkspell.WorkshopPractice;

import com.Qwikkspell.WorkshopPractice.commands.LeaderboardCommand;
import com.Qwikkspell.WorkshopPractice.commands.LeaveCommand;
import com.Qwikkspell.WorkshopPractice.commands.PlayCommand;
import com.Qwikkspell.WorkshopPractice.commands.PlayHelpCommand;
import com.Qwikkspell.WorkshopPractice.commands.PregameCountdownCommand;
import com.Qwikkspell.WorkshopPractice.discord.DiscordManager;
import com.Qwikkspell.WorkshopPractice.game.GameManager;
import com.Qwikkspell.WorkshopPractice.game.SidebarManager;
import com.Qwikkspell.WorkshopPractice.listeners.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorkshopPractice extends JavaPlugin {
    private SidebarManager sidebarManager;
    private GameManager gameManager;
    private DiscordManager discordManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        // Merge any keys added in newer versions (e.g. the discord section) into an existing
        // config.yml so upgrades don't silently miss new settings.
        getConfig().options().copyDefaults(true);
        saveConfig();
        gameManager = new GameManager(this);
        sidebarManager = new SidebarManager(gameManager);


        gameManager.setSidebarManager(sidebarManager);

        for (Player player : getServer().getOnlinePlayers()) {
            sidebarManager.showGeneralInfo(player);
        }

        getCommand("play").setExecutor(new PlayCommand(gameManager));
        getCommand("playhelp").setExecutor(new PlayHelpCommand());
        getCommand("pregamecountdown").setExecutor(new PregameCountdownCommand(gameManager.getSettingsManager()));
        getCommand("l").setExecutor(new LeaveCommand(gameManager));
        this.getCommand("leaderboard").setExecutor(new LeaderboardCommand(gameManager.getStatsManager()));
        getServer().getPluginManager().registerEvents(new PlayerRestrictionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, gameManager, sidebarManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new BlockClickListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new FurnaceListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(gameManager), this);
//        getServer().getPluginManager().registerEvents(new InventoryCraftingListener(gameManager), this);

        startDiscordBot();
    }

    private void startDiscordBot() {
        boolean enabled = getConfig().getBoolean("discord.enabled", false);
        if (!enabled) {
            getLogger().info("[Discord] Disabled in config (discord.enabled is not true) - skipping bot startup.");
            return;
        }
        String token = getConfig().getString("discord.token", "");
        String guildId = getConfig().getString("discord.guildId", "");
        String pbChannelId = getConfig().getString("discord.pbChannelId", "");
        getLogger().info("[Discord] enabled=true, tokenPresent=" + (token != null && !token.isBlank())
                + ", guildId='" + guildId + "', pbChannelId='" + pbChannelId + "'");
        if (token == null || token.isBlank()) {
            getLogger().warning("[Discord] No token set in config.yml; bot not started.");
            return;
        }
        discordManager = new DiscordManager(this, gameManager, token, guildId, pbChannelId);
        // Connecting blocks while it handshakes with Discord, so do it off the main thread.
        getLogger().info("[Discord] Connecting to Discord (async)...");
        getServer().getScheduler().runTaskAsynchronously(this, () -> discordManager.start());
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.getStatsManager().save();
            gameManager.getSettingsManager().save();
        }
        if (discordManager != null) {
            discordManager.shutdown();
        }
    }

    public SidebarManager getSidebarManager() {
        return sidebarManager;
    }

    /** The embedded Discord bot, or {@code null} if disabled / not configured. */
    public DiscordManager getDiscordManager() {
        return discordManager;
    }
}
