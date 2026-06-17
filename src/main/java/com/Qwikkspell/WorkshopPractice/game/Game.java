package com.Qwikkspell.WorkshopPractice.game;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Game {
    WorkshopPractice plugin;
    private final Player player;
    private final List<Material> crafts;
    private final GameMode mode;
    private final long seed;
    private final boolean seeded;
    private final String foreman;
    private int currentCraft;
    private final int finalCraftIndex;
    private long remainingTime;
    private boolean isWaiting;
    private GameStatus status;
    private final CraftStation station;
    private final GameManager gameManager;
    private final BossBar bossBar;
    private final SidebarManager sidebarManager;
    private BukkitTask pregameTask;
    private BukkitRunnable gameTask;
    private int countdown;
    private long startTime;
    private long endTime;
    private long craftStart;
    private long craftEnd;
    private int timeTrialCrafts;
    private final List<StatsManager.CraftRecord> completedCrafts = new ArrayList<>();
    private final Set<Location> accessedBlocks = new HashSet<>();
    private final int waitTime;

    public Game(Player player, List<Material> crafts, GameMode mode, long seed, boolean seeded,
                CraftStation station, GameManager gameManager, WorkshopPractice plugin,
                SidebarManager sidebarManager, String foreman, int waitTime) {
        this.player = player;
        this.crafts = crafts;
        this.mode = mode;
        this.seed = seed;
        this.seeded = seeded;
        this.currentCraft = 0;
        this.remainingTime = mode.getTimeLimitSeconds(); // 0 for no-limit modes (All Crafts)
        this.status = GameStatus.WAITING;
        this.isWaiting = false;
        this.station = station;
        this.gameManager = gameManager;
        this.bossBar = Bukkit.createBossBar("", BarColor.PINK, BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        this.plugin = plugin;
        this.sidebarManager = sidebarManager;
        this.finalCraftIndex = crafts.size() - 1;
        this.foreman = foreman;
        this.waitTime = waitTime;
    }


    public void start() {
        getStation().setOccupied(true);
        this.status = GameStatus.PREGAME;
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        getStation().clearStation();
        player.getInventory().clear();
        pregame();
    }

    public void pregame() {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        gameManager.startFurnaceCheckTask(this);
        station.spawnVillager();

        countdown = waitTime + 1;
        pregameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                countdown--;
                bossBar.setTitle(ChatColor.YELLOW + "" + ChatColor.BOLD + "Workshop starts in " + ChatColor.RED + "" + ChatColor.BOLD
                        + countdown + ChatColor.YELLOW + "" + ChatColor.BOLD + " seconds!");
                bossBar.setProgress(clampProgress(countdown / 10.0));

            if (countdown <= 4) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
            }

            if (countdown <= 0) {
                bossBar.removeAll();
                bossBar.setVisible(false);
                pregameTask.cancel();
                startGame();


            }

        }, 0L, 20L);

    }

    private void startGame() {
        bossBar.removeAll();
        sendCraftRequestMessage(player, getCurrentCraft());
        this.status = GameStatus.IN_PROGRESS;
        sidebarManager.updateSidebar(player);
        startTimer();
        bossBar.addPlayer(player);
        gameManager.drawBlocks(this);
        clearAccessedBlocks();
        player.getInventory().clear();
        equipChainmailArmor(player);
        craftStart = System.nanoTime();
    }



    public void startTimer() {
        startTime = System.nanoTime();
       gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mode.hasTimeLimit()) {
                    if (remainingTime <= 0) {
                        if (mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS) {
                            finishTimeTrial();
                        } else {
                            end(); // ran out of time without finishing — no score recorded
                        }
                        cancel();
                        return;
                    }
                    remainingTime--;
                }
                updateBossBar();
                bossBar.setVisible(true);
                if (!getStation().isPlayerInside(player)) {
                    if (mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS) {
                        finishTimeTrial(); // leaving banks the crafts done so far
                    } else {
                        end();             // leaving abandons a timed run
                    }
                }
            }
        };
        gameTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("WorkshopPractice"), 20L, 20L);
    }

    public void validateCraft() {
        if (isPlayerWaiting() || getStatus() != GameStatus.IN_PROGRESS) {
            return;
        }
        Material currentCraftMaterial = getCurrentCraft();
        boolean hasCraft = player.getInventory().contains(currentCraftMaterial) || isPlayerWearing();
        if (!hasCraft) {
            sendFailureMessage(player, currentCraftMaterial);
            return;
        }

        craftEnd = System.nanoTime();
        recordCraftTime(currentCraftMaterial);
        setPlayerWaiting(true);
        sendSuccessMessage(player);
        showHeartParticles();

        if (mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS) {
            timeTrialCrafts++;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getStatus() != GameStatus.IN_PROGRESS) {
                        return;
                    }
                    if (currentCraft + 1 >= crafts.size()) {
                        finishTimeTrial(); // exhausted the (very long) seeded supply
                        return;
                    }
                    giveNextCraft();
                    setPlayerWaiting(false);
                }
            }.runTaskLater(plugin, 20L);
            return;
        }

        if (getCurrentCraftIndex() < finalCraftIndex) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    giveNextCraft();
                    setPlayerWaiting(false);
                }
            }.runTaskLater(plugin, 20L);
        } else {
            this.currentCraft++;
            finish(player);
        }
    }

    private void recordCraftTime(Material material) {
        double craftSeconds = (craftEnd - craftStart) / 1_000_000_000.0;
        completedCrafts.add(new StatsManager.CraftRecord(material, craftSeconds));
    }

    private void finish(Player player) {
        if (status != GameStatus.IN_PROGRESS) {
            return;
        }
        endTime = System.nanoTime();
        double gameTime = (endTime - startTime) / 1_000_000_000.0;
        StatsManager stats = gameManager.getStatsManager();
        String timeStr = stats.formatTime(gameTime);

        if (seeded) {
            stats.recordSeedResult(mode, seed, player, gameTime);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Practice / Seeded Run "
                    + ChatColor.GRAY + "(" + mode.getDisplayName() + ", seed " + seed + ")");
            player.sendMessage(ChatColor.YELLOW + "Completed in " + ChatColor.AQUA + timeStr);
        } else {
            boolean newPB = stats.recordCompletion(player, mode, gameTime, completedCrafts);
            player.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " completed "
                    + mode.getDisplayName() + "! " + ChatColor.AQUA + "(" + timeStr + ")");
            if (newPB) {
                broadcastPersonalBest(mode.getDisplayName() + " PB", timeStr);
            }
        }
        sendSeedReplayMessage();
        end();
    }

    private void finishTimeTrial() {
        if (status != GameStatus.IN_PROGRESS) {
            return;
        }
        endTime = System.nanoTime();
        int count = timeTrialCrafts;
        StatsManager stats = gameManager.getStatsManager();

        if (seeded) {
            stats.recordSeedResult(mode, seed, player, count);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Practice / Seeded Run "
                    + ChatColor.GRAY + "(" + mode.getDisplayName() + ", seed " + seed + ")");
            player.sendMessage(ChatColor.YELLOW + "Time's up! Crafts completed: " + ChatColor.AQUA + count);
        } else {
            boolean newBest = stats.recordTimeTrial(player, mode, count, completedCrafts);
            player.sendMessage(ChatColor.YELLOW + "Time's up! Crafts completed: " + ChatColor.AQUA + count);
            if (newBest) {
                broadcastPersonalBest(mode.getDisplayName() + " PB", count + " crafts");
            }
        }
        sendSeedReplayMessage();
        end();
    }

    private void broadcastPersonalBest(String label, String value) {
        String message = ChatColor.GOLD + "" + ChatColor.BOLD + "NEW " + label + "! " + ChatColor.YELLOW + value
                + ChatColor.GRAY + " by " + ChatColor.AQUA + player.getName();
        Bukkit.broadcastMessage(message);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        }
    }

    public void end() {
        this.status = GameStatus.COMPLETED;
        this.getStation().setOccupied(false);
        this.gameManager.endGame(this);
        bossBar.removeAll();
        if (pregameTask != null) {
            pregameTask.cancel();
        }
        if (gameTask != null) {
            gameTask.cancel();
        }
        sidebarManager.updateSidebar(player);
        player.getInventory().clear();
        getStation().resetFurnaces();
        player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }


    public void giveNextCraft() {
        craftStart = System.nanoTime();
        currentCraft++;
        sidebarManager.updateSidebar(player);
        gameManager.drawBlocks(this);
        if (getStation().getDirection().equalsIgnoreCase("south")) {
            getStation().resetFurnaces();
        }
        clearAccessedBlocks();
        player.getInventory().clear();
        sendCraftRequestMessage(player, getCurrentCraft());

    }

    private void updateBossBar() {
        if (mode.hasTimeLimit()) {
            double progress = clampProgress((double) remainingTime / mode.getTimeLimitSeconds());
            if (mode.getScoringType() == GameMode.ScoringType.MOST_CRAFTS) {
                bossBar.setTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "Time left: " + ChatColor.RED + remainingTime
                        + ChatColor.GOLD + "   Crafts: " + ChatColor.GREEN + timeTrialCrafts + seedSuffix());
            } else {
                bossBar.setTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "Crafting time: " + ChatColor.RED + "" + ChatColor.BOLD + remainingTime + seedSuffix());
            }
            bossBar.setProgress(progress);
        } else {
            // No time limit (All Crafts): show elapsed time and progress through the craft list.
            long elapsed = (System.nanoTime() - startTime) / 1_000_000_000L;
            int total = crafts.size();
            bossBar.setTitle(ChatColor.GOLD + "" + ChatColor.BOLD + mode.getDisplayName() + ": " + ChatColor.GREEN
                    + Math.min(currentCraft + 1, total) + "/" + total + ChatColor.GOLD + "   " + formatClock(elapsed) + seedSuffix());
            bossBar.setProgress(clampProgress((double) currentCraft / total));
        }
    }

    /** Seed label appended to the boss bar so players always see which run they're on. */
    private String seedSuffix() {
        return ChatColor.GRAY + "   Seed: " + ChatColor.WHITE + seed;
    }

    /** Client-side message telling the player how to replay the run they just played. */
    private void sendSeedReplayMessage() {
        player.sendMessage(ChatColor.GRAY + "Seed: " + ChatColor.WHITE + seed + ChatColor.GRAY + " — replay with "
                + ChatColor.YELLOW + "/play " + mode.getAlias() + " seed " + seed);
    }

    private static double clampProgress(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static String formatClock(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public Material getCurrentCraft() {
        return crafts.get(currentCraft);
    }

    public int getCurrentCraftIndex() {
        return currentCraft;
    }

    /** Deterministic per-craft RNG so a (seed, craftIndex) reproduces the same wall layout. */
    public Random getLayoutRandom() {
        return CraftSequence.layoutRandom(seed, currentCraft);
    }


    private void showHeartParticles() {
        Villager villager = station.villager;
        if (villager != null) {
            villager.getWorld().spawnParticle(Particle.HEART, villager.getLocation().add(0, 1, 0), 10);
        }
    }

    private boolean isPlayerWearing() {
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null && armorPiece.getType() == getCurrentCraft()) {
                return true;
            }
            }
        return false;
    }


    // clean up everything

    public Player getPlayer() {
        return player;
    }

    public GameStatus getStatus() {
        return status;
    }

    public CraftStation getStation() {
        return station;
    }

    public GameMode getMode() {
        return mode;
    }

    public boolean isSeeded() {
        return seeded;
    }

    public long getSeed() {
        return seed;
    }

    public String getDifficulty() {
        return mode.getDifficulty();
    }

    public String getForeman() {
        return this.foreman;
    }


    public String getCraftTimeString() {
        double elapsedTime = (craftEnd - craftStart) / 1_000_000_000.0; // Convert to seconds
        return ChatColor.AQUA + "(" + String.format("%.3f", elapsedTime) + "s)";
    }

    public String getGameCraftTimeString() {
        double elapsedTime = (endTime - startTime) / 1_000_000_000.0; // Convert to seconds
        return ChatColor.AQUA + "(" + String.format("%.3f", elapsedTime) + "s)";
    }
    private void setPlayerWaiting(boolean waiting) {
        isWaiting = waiting;
    }

    private boolean isPlayerWaiting() {
        return isWaiting;
    }


    private void sendSuccessMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "Foreman " + getForeman() + ": " + ChatColor.YELLOW + "Perfect! That's just what I needed! "
        + getCraftTimeString());
    }

    private void sendFailureMessage(Player player, Material currentCraft) {
        player.sendMessage(ChatColor.GOLD + "Foreman " + getForeman() + ": " + ChatColor.YELLOW + "That's not what I need! I need "
                + InfoMaps.getItemName(currentCraft) + "! Look at the wall next to me to see the Recipe!");
    }

    private void sendCraftRequestMessage(Player player, Material currentCraft) {
        player.sendMessage(ChatColor.GOLD + "Foreman " + getForeman() + ": " + ChatColor.YELLOW + "Ok, so I need you to craft me " + InfoMaps.getItemName(currentCraft) + "!");
    }

    public void equipChainmailArmor(Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
    }
    public boolean isBlockAccessed(Location location) {
        return accessedBlocks.contains(location);
    }

    // Method to add a block to the accessed set
    public void addAccessedBlock(Location location) {
        accessedBlocks.add(location);
    }

    // Method to clear the accessed blocks set
    public void clearAccessedBlocks() {
        accessedBlocks.clear();
    }

    // Helper method to center text




}
