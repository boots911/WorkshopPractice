package com.Qwikkspell.WorkshopPractice.game;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;
import com.Qwikkspell.WorkshopPractice.player.PlayerSettingsManager;
import com.Qwikkspell.WorkshopPractice.utils.ConfigUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    public final WorkshopPractice plugin;
    private final Map<Player, Game> activeGames = new ConcurrentHashMap<>();
    private final List<CraftStation> craftStations = new ArrayList<>();
    private SidebarManager sidebarManager;
    private final ScoreManager scoreManager;
    private final StatsManager statsManager;
    private final PlayerSettingsManager settingsManager;
    private List<Material> allCraftMaterials;
    private BukkitTask furnaceCheckTask;
    private final List<String> crafts = Arrays.asList(
            "PISTON", "ENCHANTING_TABLE", "COAL_BLOCK", "DIAMOND_BLOCK", "IRON_BLOCK",
            "GOLD_BLOCK", "EMERALD_BLOCK", "WOODEN_SWORD", "WOODEN_AXE", "WOODEN_HOE",
            "IRON_SWORD", "IRON_AXE", "IRON_HOE", "GOLDEN_SWORD", "GOLDEN_AXE", "GOLDEN_HOE",
            "DIAMOND_SWORD", "DIAMOND_AXE", "DIAMOND_HOE", "STONE_SWORD", "STONE_AXE",
            "STONE_HOE", "TORCH", "CRAFTING_TABLE", "OAK_BUTTON", "OAK_PRESSURE_PLATE", "OAK_BOAT",
            "NOTE_BLOCK", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS",
            "DIAMOND_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS", "IRON_HELMET",
            "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS", "GOLDEN_BOOTS", "GOLDEN_HELMET",
            "ARMOR_STAND", "MINECART", "FISHING_ROD", "BOW", "SHEARS", "RAIL",
            "FURNACE", "COMPASS", "OAK_SIGN", "OAK_FENCE", "OAK_FENCE_GATE", "CAULDRON", "REDSTONE_TORCH",
            "CHEST", "LAPIS_BLOCK", "REDSTONE_BLOCK", "WOODEN_PICKAXE", "GOLDEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "DIAMOND_PICKAXE"

    );
    private final List<Material> randomBlocks = Arrays.asList(
            Material.SPRUCE_LOG, Material.REDSTONE_ORE, Material.GOLD_ORE, Material.COBWEB,
            Material.STONE, Material.IRON_ORE, Material.MELON, Material.OBSIDIAN, Material.DIAMOND_ORE
    );

    public GameManager(WorkshopPractice plugin) {
        this.plugin = plugin;
        this.scoreManager = new ScoreManager(plugin);
        this.statsManager = new StatsManager(plugin);
        // Legacy scores.yml times are all Left Easy runs — fold them into the lefteasy board.
        this.statsManager.importLegacyScores(this.scoreManager.getLegacyScores());
        this.settingsManager = new PlayerSettingsManager(plugin);
        buildCraftMaterials();
        loadCraftStations();
    }

    private void buildCraftMaterials() {
        List<Material> materials = new ArrayList<>();
        for (String name : crafts) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                materials.add(material);
            } else {
                plugin.getLogger().warning("Unknown craft material in list: " + name);
            }
        }
        this.allCraftMaterials = Collections.unmodifiableList(materials);
    }

    public void startGame(Player player, GameMode mode, Long seed) {
        Game existing = activeGames.get(player);
        if (existing != null) {
            existing.end();
        }

        long actualSeed = (seed != null) ? seed : CraftSequence.randomSeed(allCraftMaterials, mode);
        boolean isSeeded = seed != null;

        CraftStation station = mode.isAnyDirection()
                ? findAnyCraftStation()
                : findCraftStation(mode.getDirection().station());

        if (station == null) {
            player.sendMessage("We couldn't find you a game, try again later.");
            plugin.getLogger().warning("No available stations for player: " + player.getName());
            return;
        }
        station.setOccupied(true);
        plugin.getLogger().info("Player " + player.getName() + " assigned to station " + station.getId()
                + " mode=" + mode.getAlias() + " seed=" + actualSeed + (isSeeded ? " (seeded)" : ""));

        List<Material> runCrafts = CraftSequence.generate(allCraftMaterials, mode, actualSeed);

        String foreman = InfoMaps.getRandomForemanName();
        int waitTime = settingsManager.getPregameCountdown(player.getUniqueId());

        Game game = new Game(player, runCrafts, mode, actualSeed, isSeeded, station, this, this.plugin,
                sidebarManager, foreman, waitTime);
        activeGames.put(player, game);
        game.start();

        station.getSpawn().setYaw(getYawDirection(station.getDirection()));
        player.teleport(station.getSpawn());
    }

    public void drawBlocks(Game game) {

        CraftStation station = game.getStation();
        Material currentCraft = game.getCurrentCraft();
        List<Material> requiredMaterials = InfoMaps.getBaseMaterials(currentCraft);

        List<Location> sideBlocks;
        List<Location> otherBlocks = new ArrayList<>(station.getCombinedBlocks());

        if (game.getDifficulty().equalsIgnoreCase("easy")) {
            sideBlocks = station.getDirection().equalsIgnoreCase("north") ?
                    new ArrayList<>(station.getLeftSideEasy()) : new ArrayList<>(station.getRightSideEasy());
        }

        else {
            sideBlocks = station.getDirection().equalsIgnoreCase("north") ?
                    new ArrayList<>(station.getCombinedLeftSide()) : new ArrayList<>(station.getCombinedRightSide());
        }

        Random rng = game.getLayoutRandom();
        Collections.shuffle(sideBlocks, rng);

        for (int i = 0; i < requiredMaterials.size(); i++) {
            Location loc = sideBlocks.get(i);
            loc.getBlock().setType(requiredMaterials.get(i));
            otherBlocks.remove(loc);
        }

        for (Location location : otherBlocks) {
            location.getBlock().setType(randomBlocks.get(rng.nextInt(randomBlocks.size())));
        }

        List<Location> craftGrid = station.getCraftGrid();

        clearExistingItemFrames(craftGrid);
       placeItemFrame(this.plugin, craftGrid.get(0), currentCraft);


        List<Material> recipe = InfoMaps.getRecipe(currentCraft);

        for (int i = 1; i <=9; i++) {
            if (recipe.get(i - 1) != null) {
                placeItemFrame(this.plugin, craftGrid.get(i), recipe.get(i - 1));
            } else {
                placeItemFrame(this.plugin, craftGrid.get(i), null);
            }
        }




    }

    private void clearExistingItemFrames(List<Location> locations) {
        for (Location location : locations) {
            for (ItemFrame frame : location.getWorld().getEntitiesByClass(ItemFrame.class)) {
                if (frame.getLocation().getBlockX() == location.getBlockX() &&
                        frame.getLocation().getBlockY() == location.getBlockY() &&
                        frame.getLocation().getBlockZ() == location.getBlockZ()) {
                    frame.remove();
                }
            }
        }
    }


    public void placeItemFrame(JavaPlugin plugin, Location location, Material material) {
        World world = location.getWorld();
        Block block = location.getBlock();

        // Remove any oak signs at the location
        if (block.getType() == Material.OAK_SIGN || block.getType() == Material.OAK_WALL_SIGN) {
            block.setType(Material.AIR);
        }

        // Schedule the item frame placement after one tick
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Place the item frame
            Block delayedBlock = location.getBlock();
            delayedBlock.setType(Material.AIR);
            ItemFrame itemFrame = (ItemFrame) world.spawn(location, ItemFrame.class);
            if (material != null) {
                itemFrame.setItem(new ItemStack(material));
            }
        }, 1L);
    }

//    public void placeItemFrame(Location location, Material material) {
//        World world = location.getWorld();
//        Block block = location.getBlock();
//        block.setType(Material.AIR);
//        ItemFrame itemFrame = (ItemFrame) world.spawn(location, ItemFrame.class);
//        if (material != null) {
//            itemFrame.setItem(new ItemStack(material));
//        }
//    }

    public void placeSideBlocks(List<Location> locations, List<Material> materials) {
        for (int i = 0; i < materials.size(); i++) {
            locations.get(i).getBlock().setType(materials.get(i));
        }
        locations.subList(0, materials.size()).clear();

    }

    public void placeRandomBlocks(List<Location> locations) {
        Random random = new Random();
        for (Location location : locations) {
            location.getBlock().setType(randomBlocks.get(random.nextInt(randomBlocks.size())));
        }

    }

    public void startFurnaceCheckTask(Game game) {
        furnaceCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getStatus() == GameStatus.IN_PROGRESS) {
                    checkFurnaces(game);
                }
                if (game.getStatus() != GameStatus.PREGAME && game.getStatus() != GameStatus.IN_PROGRESS) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Runs every tick
    }

    private void checkFurnaces(Game game) {
        CraftStation station = game.getStation();
        if (station != null && station.isOccupied()) {
            for (Location furnaceLocation : station.getFurnaces()) {
                Block block = furnaceLocation.getBlock();
                if (block.getState() instanceof Furnace) {
                    Furnace furnace = (Furnace) block.getState();
                    ItemStack smeltingItem = furnace.getInventory().getSmelting();
                    ItemStack resultItem = furnace.getInventory().getResult();
                    ItemStack fuel = furnace.getInventory().getFuel();

                    // Ensure furnace has coal as fuel before auto-smelting
                    if (fuel != null && fuel.getType() == Material.COAL) {
                        if (smeltingItem != null) {
                            Material smeltResult = InfoMaps.getSmeltResult(smeltingItem.getType());
                            if (smeltResult != null) {
                                if (resultItem == null || resultItem.getType() == smeltResult) {
                                    int newAmount = (resultItem == null ? 0 : resultItem.getAmount()) + smeltingItem.getAmount();
                                    furnace.getInventory().setResult(new ItemStack(smeltResult, newAmount));
                                    furnace.getInventory().setSmelting(null);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public synchronized CraftStation findCraftStation(String direction) {
        for (CraftStation c : craftStations) {
            if (!c.isOccupied() && c.getDirection().equalsIgnoreCase(direction)) {
                return c;
            }
        }
        return null;
    }

    /** Any free station regardless of direction — used by direction-agnostic modes (All Crafts, Time Trial). */
    public synchronized CraftStation findAnyCraftStation() {
        for (CraftStation c : craftStations) {
            if (!c.isOccupied()) {
                return c;
            }
        }
        return null;
    }

    public List<CraftStation> getCraftStations() {
        return craftStations;
    }

    private void loadCraftStations() {
        ConfigUtils configUtils = new ConfigUtils(plugin);
        for (CraftStation station : configUtils.getCraftStations()) {
            this.craftStations.add(station);
        }
    }

    void endGame(Game game) {
        CraftStation station = game.getStation();
        if (station != null) {
            synchronized (station) {
                station.setOccupied(false);
            }
        }
        activeGames.remove(game.getPlayer());
    }


    public void setSidebarManager(SidebarManager sideBarManager) {
        this.sidebarManager = sideBarManager;
    }

    private float getYawDirection(String direction) {
        return switch (direction.toUpperCase()) {
            case "NORTH" -> -180.0f;
            case "EAST" -> -90.0f;
            case "SOUTH" -> 0.0f;
            case "WEST" -> 90.0f;
            default -> 0.0f;
        };
    }

    public Game getActiveGame(Player player) {
        return activeGames.get(player);
    }

//    public void checkAndUpdateGameScore(Player player, double time) {
//        if (scoreManager.updateGameScore(player, time)) {
//            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "NEW RECORD! " + ChatColor.GOLD + "Fastest Time: " + ChatColor.YELLOW + scoreManager.getFormattedTopGameScore(player));
//        }
//    }


    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    /** The ordered craft universe (used by the Discord bot's /findseed seed encoder). */
    public List<Material> getCraftUniverse() {
        return allCraftMaterials;
    }

    public PlayerSettingsManager getSettingsManager() {
        return settingsManager;
    }


}
