package com.Qwikkspell.WorkshopPractice.game;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfoMaps {

        public static List<Material> getRecipe(Material material) {
               return recipeMap.get(material);
        }

        public static List<Material> getBaseMaterials(Material material) {
                return baseMaterialsMap.get(material);
        }

        public static Material getBlockDrop(Material material) {
                Material blockDrop = blockDropMap.get(material);
                if (blockDrop != null) {
                        return blockDrop;
                }
                return null;
        }
        public static Material getSmeltResult(Material material) {
                return smeltingMap.get(material);
        }

        public static String getItemName(Material material) {
                return itemNameMap.getOrDefault(material, formatItemName(material));
        }

        private static String formatItemName(Material material) {
                String name = material.name().replace('_', ' ').toLowerCase();
                String[] words = name.split(" ");
                StringBuilder formattedName = new StringBuilder("a ");
                for (String word : words) {
                        formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                }
                return formattedName.toString().trim();
        }

        private static final Map<Material, String> itemNameMap = Map.ofEntries(
                Map.entry(Material.WOODEN_SWORD, "a Wood Sword"),
                Map.entry(Material.WOODEN_AXE, "a Wood Axe"),
                Map.entry(Material.WOODEN_HOE, "a Wood Hoe"),
                Map.entry(Material.WOODEN_PICKAXE, "a Wood Pickaxe"),
                Map.entry(Material.WOODEN_SHOVEL, "a Wood Shovel"),
                Map.entry(Material.GOLDEN_SWORD, "a Gold Sword"),
                Map.entry(Material.GOLDEN_AXE, "a Gold Axe"),
                Map.entry(Material.GOLDEN_HOE, "a Gold Hoe"),
                Map.entry(Material.GOLDEN_PICKAXE, "a Gold Pickaxe"),
                Map.entry(Material.GOLDEN_SHOVEL, "a Gold Shovel"),
                Map.entry(Material.GOLDEN_HELMET, "a Gold Helmet"),
                Map.entry(Material.GOLDEN_CHESTPLATE, "a Gold Chestplate"),
                Map.entry(Material.GOLDEN_LEGGINGS, "Gold Leggings"),
                Map.entry(Material.GOLDEN_BOOTS, "Gold Boots"),
                Map.entry(Material.IRON_LEGGINGS, "Iron Leggings"),
                Map.entry(Material.IRON_BOOTS, "Iron Boots"),
                Map.entry(Material.DIAMOND_LEGGINGS, "Diamond Leggings"),
                Map.entry(Material.DIAMOND_BOOTS, "Diamond Boots"),
                Map.entry(Material.PISTON, "a Piston Base"),
                Map.entry(Material.REDSTONE_TORCH, "a Redstone Torch On"),
                Map.entry(Material.CRAFTING_TABLE, "a Workbench"),
                Map.entry(Material.RAIL, "Rails"),
                Map.entry(Material.OAK_BOAT, "a Boat"),
                Map.entry(Material.OAK_FENCE, "a Fence"),
                Map.entry(Material.OAK_FENCE_GATE, "a Fence Gate"),
                Map.entry(Material.SHEARS, "Shears")
        );

        private static final List<String> foremanNames = Arrays.asList(
                "José", "Scott", "Andy", "Foreman", "John", "George Foreman", "Joe",
                "Roberto", "Victor", "Fred", "Jack", "Marc", "Eddy", "Edd",
                "Juan", "Felipe", "Pablo", "Clarence", "Russel", "Chris", "Ron", "Patrick", "Morris", "Bill",
                "Harry", "Ed", "Mark"
        );

        // Function to get a random foreman name
        public static String getRandomForemanName() {
                Random random = new Random();
                int index = random.nextInt(foremanNames.size());
                return foremanNames.get(index);
        }


        private static final Map<Material, List<Material>> recipeMap = Map.ofEntries(
                // Example for piston
                Map.entry(Material.PISTON, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        Material.COBBLESTONE, Material.IRON_INGOT, Material.COBBLESTONE,
                        Material.COBBLESTONE, Material.REDSTONE, Material.COBBLESTONE
                )),
                // Example for enchanting table
                Map.entry(Material.ENCHANTING_TABLE, Arrays.asList(
                        null, Material.BOOK, null,
                        Material.DIAMOND, Material.OBSIDIAN, Material.DIAMOND,
                        Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN
                )),
                Map.entry(Material.COAL_BLOCK, Arrays.asList(
                        Material.COAL, Material.COAL, Material.COAL,
                        Material.COAL, Material.COAL, Material.COAL,
                        Material.COAL, Material.COAL, Material.COAL
                )),
                Map.entry(Material.DIAMOND_BLOCK, Arrays.asList(
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND
                )),
                Map.entry(Material.IRON_BLOCK, Arrays.asList(
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT
                )),
                Map.entry(Material.GOLD_BLOCK, Arrays.asList(
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT
                )),
                Map.entry(Material.EMERALD_BLOCK, Arrays.asList(
                        Material.EMERALD, Material.EMERALD, Material.EMERALD,
                        Material.EMERALD, Material.EMERALD, Material.EMERALD,
                        Material.EMERALD, Material.EMERALD, Material.EMERALD
                )),
                Map.entry(Material.WOODEN_SWORD, Arrays.asList(
                        null, Material.OAK_PLANKS, null,
                        null, Material.OAK_PLANKS, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.WOODEN_AXE, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, null,
                        Material.OAK_PLANKS, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.WOODEN_HOE, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, null,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.IRON_SWORD, Arrays.asList(
                        null, Material.IRON_INGOT, null,
                        null, Material.IRON_INGOT, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.IRON_AXE, Arrays.asList(
                        Material.IRON_INGOT, Material.IRON_INGOT, null,
                        Material.IRON_INGOT, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.IRON_HOE, Arrays.asList(
                        Material.IRON_INGOT, Material.IRON_INGOT, null,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.GOLDEN_SWORD, Arrays.asList(
                        null, Material.GOLD_INGOT, null,
                        null, Material.GOLD_INGOT, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.GOLDEN_AXE, Arrays.asList(
                        Material.GOLD_INGOT, Material.GOLD_INGOT, null,
                        Material.GOLD_INGOT, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.GOLDEN_HOE, Arrays.asList(
                        Material.GOLD_INGOT, Material.GOLD_INGOT, null,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.DIAMOND_SWORD, Arrays.asList(
                        null, Material.DIAMOND, null,
                        null, Material.DIAMOND, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.DIAMOND_AXE, Arrays.asList(
                        Material.DIAMOND, Material.DIAMOND, null,
                        Material.DIAMOND, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.DIAMOND_HOE, Arrays.asList(
                        Material.DIAMOND, Material.DIAMOND, null,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.STONE_SWORD, Arrays.asList(
                        null, Material.COBBLESTONE, null,
                        null, Material.COBBLESTONE, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.STONE_AXE, Arrays.asList(
                        Material.COBBLESTONE, Material.COBBLESTONE, null,
                        Material.COBBLESTONE, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.STONE_HOE, Arrays.asList(
                        Material.COBBLESTONE, Material.COBBLESTONE, null,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.TORCH, Arrays.asList(
                        null, null, null,
                        null, Material.COAL, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.CRAFTING_TABLE, Arrays.asList(
                        null, null, null,
                        null, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        null, Material.OAK_PLANKS, Material.OAK_PLANKS
                )),
                Map.entry(Material.OAK_BOAT, Arrays.asList(
                        Material.OAK_PLANKS, null, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        null, null, null
                )),
                Map.entry(Material.NOTE_BLOCK, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, Material.REDSTONE, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS
                )),
                Map.entry(Material.DIAMOND_CHESTPLATE, Arrays.asList(
                        Material.DIAMOND, null, Material.DIAMOND,
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND
                )),
                Map.entry(Material.DIAMOND_LEGGINGS, Arrays.asList(
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
                        Material.DIAMOND, null, Material.DIAMOND,
                        Material.DIAMOND, null, Material.DIAMOND
                )),
                Map.entry(Material.DIAMOND_BOOTS, Arrays.asList(
                        null, null, null,
                        Material.DIAMOND, null, Material.DIAMOND,
                        Material.DIAMOND, null, Material.DIAMOND
                )),
                Map.entry(Material.DIAMOND_HELMET, Arrays.asList(
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
                        Material.DIAMOND, null, Material.DIAMOND,
                        null, null, null
                )),
                Map.entry(Material.IRON_CHESTPLATE, Arrays.asList(
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT
                )),
                Map.entry(Material.IRON_LEGGINGS, Arrays.asList(
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        Material.IRON_INGOT, null, Material.IRON_INGOT
                )),
                Map.entry(Material.IRON_BOOTS, Arrays.asList(
                        null, null, null,
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        Material.IRON_INGOT, null, Material.IRON_INGOT
                )),
                Map.entry(Material.IRON_HELMET, Arrays.asList(
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        null, null, null
                )),
                Map.entry(Material.GOLDEN_CHESTPLATE, Arrays.asList(
                        Material.GOLD_INGOT, null, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT
                )),
                Map.entry(Material.GOLDEN_LEGGINGS, Arrays.asList(
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, null, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, null, Material.GOLD_INGOT
                )),
                Map.entry(Material.GOLDEN_BOOTS, Arrays.asList(
                        null, null, null,
                        Material.GOLD_INGOT, null, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, null, Material.GOLD_INGOT
                )),
                Map.entry(Material.GOLDEN_HELMET, Arrays.asList(
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT,
                        Material.GOLD_INGOT, null, Material.GOLD_INGOT,
                        null, null, null
                )),
                Map.entry(Material.ARMOR_STAND, Arrays.asList(
                        Material.STICK, Material.STICK, Material.STICK,
                        null, Material.STICK, null,
                        Material.STICK, Material.SMOOTH_STONE_SLAB, Material.STICK
                )),
                Map.entry(Material.MINECART, Arrays.asList(
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        null, null, null
                )),
                Map.entry(Material.FISHING_ROD, Arrays.asList(
                        null, null, Material.STICK,
                        null, Material.STICK, Material.STRING,
                        Material.STICK, null, Material.STRING
                )),
                Map.entry(Material.BOW, Arrays.asList(
                        null, Material.STICK, Material.STRING,
                        Material.STICK, null, Material.STRING,
                        null, Material.STICK, Material.STRING
                )),
                Map.entry(Material.SHEARS, Arrays.asList(
                        null, null, null,
                        null, null, Material.IRON_INGOT,
                        null, Material.IRON_INGOT, null
                )),
                Map.entry(Material.RAIL, Arrays.asList(
                        Material.IRON_INGOT, Material.STICK, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.STICK, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.STICK, Material.IRON_INGOT
                )),
                Map.entry(Material.FURNACE, Arrays.asList(
                        Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
                        Material.COBBLESTONE, null, Material.COBBLESTONE,
                        Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE
                )),
                Map.entry(Material.COMPASS, Arrays.asList(
                        null, Material.IRON_INGOT, null,
                        Material.IRON_INGOT, Material.REDSTONE, Material.IRON_INGOT,
                        null, Material.IRON_INGOT, null
                )),
                Map.entry(Material.OAK_SIGN, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        null, Material.STICK, null
                )),
                Map.entry(Material.OAK_FENCE, Arrays.asList(
                        null, null, null,
                        Material.OAK_PLANKS, Material.STICK, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, Material.STICK, Material.OAK_PLANKS
                )),
                Map.entry(Material.OAK_FENCE_GATE, Arrays.asList(
                        null, null, null,
                        Material.STICK, Material.OAK_PLANKS, Material.STICK,
                        Material.STICK, Material.OAK_PLANKS, Material.STICK
                )),
                Map.entry(Material.CAULDRON, Arrays.asList(
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        Material.IRON_INGOT, null, Material.IRON_INGOT,
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT
                )),
                Map.entry(Material.OAK_BUTTON, Arrays.asList(
                        null, null, null,
                        null, Material.OAK_PLANKS, null,
                        null, null, null
                )),
                Map.entry(Material.OAK_PRESSURE_PLATE, Arrays.asList(
                        null, null, null,
                        null, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        null, null, null
                )),
                Map.entry(Material.REDSTONE_TORCH, Arrays.asList(
                        null, null, null,
                        null, Material.REDSTONE, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.CHEST, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, null, Material.OAK_PLANKS,
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS
                )),
                Map.entry(Material.LAPIS_BLOCK, Arrays.asList(
                        Material.LAPIS_LAZULI, Material.LAPIS_LAZULI, Material.LAPIS_LAZULI,
                        Material.LAPIS_LAZULI, Material.LAPIS_LAZULI, Material.LAPIS_LAZULI,
                        Material.LAPIS_LAZULI, Material.LAPIS_LAZULI, Material.LAPIS_LAZULI
                )),
                Map.entry(Material.REDSTONE_BLOCK, Arrays.asList(
                        Material.REDSTONE, Material.REDSTONE, Material.REDSTONE,
                        Material.REDSTONE, Material.REDSTONE, Material.REDSTONE,
                        Material.REDSTONE, Material.REDSTONE, Material.REDSTONE
                )),
                Map.entry(Material.WOODEN_PICKAXE, Arrays.asList(
                        Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.GOLDEN_PICKAXE, Arrays.asList(
                        Material.GOLD_INGOT, Material.GOLD_INGOT, Material.GOLD_INGOT,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.STONE_PICKAXE, Arrays.asList(
                        Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.IRON_PICKAXE, Arrays.asList(
                        Material.IRON_INGOT, Material.IRON_INGOT, Material.IRON_INGOT,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                )),
                Map.entry(Material.DIAMOND_PICKAXE, Arrays.asList(
                        Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
                        null, Material.STICK, null,
                        null, Material.STICK, null
                ))
        );



        private static final Map<Material, List<Material>> baseMaterialsMap = Map.ofEntries(
                // Example for piston
                Map.entry(Material.PISTON, List.of(
                        Material.SPRUCE_LOG, Material.STONE, Material.STONE, Material.STONE, Material.STONE,
                        Material.IRON_ORE, Material.REDSTONE_ORE
                )),
                // Example for enchanting table
                Map.entry(Material.ENCHANTING_TABLE, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, // 2 diamond ores
                        Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, // 4 obsidian
                        Material.BOOKSHELF // 1 book
                )),
                Map.entry(Material.COAL_BLOCK, List.of(
                        Material.COAL_ORE, Material.COAL_ORE, Material.COAL_ORE,
                        Material.COAL_ORE, Material.COAL_ORE, Material.COAL_ORE,
                        Material.COAL_ORE, Material.COAL_ORE, Material.COAL_ORE
                )),
                Map.entry(Material.DIAMOND_BLOCK, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE
                )),
                Map.entry(Material.IRON_BLOCK, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE
                )),
                Map.entry(Material.GOLD_BLOCK, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE
                )),
                Map.entry(Material.EMERALD_BLOCK, List.of(
                        Material.EMERALD_ORE, Material.EMERALD_ORE, Material.EMERALD_ORE,
                        Material.EMERALD_ORE, Material.EMERALD_ORE, Material.EMERALD_ORE,
                        Material.EMERALD_ORE, Material.EMERALD_ORE, Material.EMERALD_ORE
                )),
                Map.entry(Material.WOODEN_SWORD, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks and sticks
                )),
                Map.entry(Material.WOODEN_AXE, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks and sticks
                )),
                Map.entry(Material.WOODEN_HOE, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks and sticks
                )),
                Map.entry(Material.IRON_SWORD, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, // 2 iron ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.IRON_AXE, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE, // 3 iron ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.IRON_HOE, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, // 2 iron ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.GOLDEN_SWORD, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, // 2 gold ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.GOLDEN_AXE, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE, // 3 gold ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.GOLDEN_HOE, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, // 2 gold ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.DIAMOND_SWORD, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, // 2 diamond ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.DIAMOND_AXE, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE, // 3 diamond ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.DIAMOND_HOE, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, // 2 diamond ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.STONE_SWORD, List.of(
                        Material.STONE, Material.STONE, // 2 stones
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.STONE_AXE, List.of(
                        Material.STONE, Material.STONE, Material.STONE, // 3 stones
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.STONE_HOE, List.of(
                        Material.STONE, Material.STONE, // 2 stones
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.TORCH, List.of(
                        Material.COAL_ORE, // 1 coal ore for coal
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.CRAFTING_TABLE, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks
                )),
                Map.entry(Material.OAK_BOAT, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks
                )),
                Map.entry(Material.NOTE_BLOCK, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG, // 2 oak logs for planks
                        Material.REDSTONE_ORE // 1 redstone ore for redstone
                )),
                Map.entry(Material.DIAMOND_CHESTPLATE, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE // 8 diamond ores for diamonds
                )),
                Map.entry(Material.DIAMOND_LEGGINGS, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE // 7 diamond ores for diamonds
                )),
                Map.entry(Material.DIAMOND_BOOTS, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, // 2 diamond ores for diamonds
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE // 2 more diamond ores for diamonds
                )),
                Map.entry(Material.DIAMOND_HELMET, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE,
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE // 5 diamond ores for diamonds
                )),
                Map.entry(Material.IRON_CHESTPLATE, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE // 8 iron ores for iron ingots
                )),
                Map.entry(Material.IRON_LEGGINGS, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE // 7 iron ores for iron ingots
                )),
                Map.entry(Material.IRON_BOOTS, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, // 2 iron ores for iron ingots
                        Material.IRON_ORE, Material.IRON_ORE // 2 more iron ores for iron ingots
                )),
                Map.entry(Material.IRON_HELMET, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE // 5 iron ores for iron ingots
                )),
                Map.entry(Material.GOLDEN_CHESTPLATE, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE, Material.GOLD_ORE // 8 gold ores for gold ingots
                )),
                Map.entry(Material.GOLDEN_LEGGINGS, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE // 7 gold ores for gold ingots
                )),
                Map.entry(Material.GOLDEN_BOOTS, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, // 2 gold ores for gold ingots
                        Material.GOLD_ORE, Material.GOLD_ORE // 2 more gold ores for gold ingots
                )),
                Map.entry(Material.GOLDEN_HELMET, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE,
                        Material.GOLD_ORE, Material.GOLD_ORE // 5 gold ores for gold ingots
                )),
                Map.entry(Material.ARMOR_STAND, List.of(
                        Material.SPRUCE_LOG, Material.SMOOTH_STONE_SLAB
                )),
                Map.entry(Material.MINECART, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE // 5 iron ores for iron ingots
                )),
                Map.entry(Material.FISHING_ROD, List.of(
                        Material.SPRUCE_LOG, Material.COBWEB, Material.COBWEB // 2 oak logs for sticks
                )),
                Map.entry(Material.BOW, List.of(
                        Material.SPRUCE_LOG, Material.COBWEB, Material.COBWEB, Material.COBWEB // 2 oak logs for sticks
                )),
                Map.entry(Material.SHEARS, List.of(
                        Material.IRON_ORE, Material.IRON_ORE // 2 iron ores for iron ingots
                )),
                Map.entry(Material.RAIL, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE, // 6 iron ores for iron ingots
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.FURNACE, List.of(
                        Material.STONE, Material.STONE, Material.STONE,
                        Material.STONE, Material.STONE, Material.STONE,
                        Material.STONE, Material.STONE // 8 stones
                )),
                Map.entry(Material.COMPASS, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.REDSTONE_ORE // 4 iron ores and 1 redstone ore
                )),
                Map.entry(Material.OAK_SIGN, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks
                )),
                Map.entry(Material.OAK_FENCE, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks and sticks
                )),
                Map.entry(Material.OAK_FENCE_GATE, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks and sticks
                )),
                Map.entry(Material.CAULDRON, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE,
                        Material.IRON_ORE // 7 iron ores for iron ingots
                )),
                Map.entry(Material.OAK_BUTTON, List.of(
                        Material.SPRUCE_LOG
                )),
                Map.entry(Material.OAK_PRESSURE_PLATE, List.of(
                        Material.SPRUCE_LOG
                )),
                Map.entry(Material.REDSTONE_TORCH, List.of(
                        Material.REDSTONE_ORE, // 1 redstone ore for redstone
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.CHEST, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks
                )),
                Map.entry(Material.LAPIS_BLOCK, List.of(
                        Material.LAPIS_ORE, Material.LAPIS_ORE, Material.LAPIS_ORE,
                        Material.LAPIS_ORE, Material.LAPIS_ORE, Material.LAPIS_ORE,
                        Material.LAPIS_ORE, Material.LAPIS_ORE, Material.LAPIS_ORE // 9 lapis ores for lapis lazuli
                )),
                Map.entry(Material.REDSTONE_BLOCK, List.of(
                        Material.REDSTONE_ORE, Material.REDSTONE_ORE, Material.REDSTONE_ORE,
                        Material.REDSTONE_ORE, Material.REDSTONE_ORE, Material.REDSTONE_ORE,
                        Material.REDSTONE_ORE, Material.REDSTONE_ORE, Material.REDSTONE_ORE // 9 redstone ores for redstone
                )),
                Map.entry(Material.WOODEN_PICKAXE, List.of(
                        Material.SPRUCE_LOG, Material.SPRUCE_LOG // 2 oak logs for planks and sticks
                )),
                Map.entry(Material.GOLDEN_PICKAXE, List.of(
                        Material.GOLD_ORE, Material.GOLD_ORE, Material.GOLD_ORE, // 3 gold ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.STONE_PICKAXE, List.of(
                        Material.STONE, Material.STONE, Material.STONE, // 3 stones
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.IRON_PICKAXE, List.of(
                        Material.IRON_ORE, Material.IRON_ORE, Material.IRON_ORE, // 3 iron ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                )),
                Map.entry(Material.DIAMOND_PICKAXE, List.of(
                        Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE, // 3 diamond ores
                        Material.SPRUCE_LOG // 1 oak log for sticks
                ))
        );


        private static final Map<Material, Material> blockDropMap = Map.ofEntries(
                Map.entry(Material.SPRUCE_LOG, Material.OAK_LOG),
                Map.entry(Material.REDSTONE_ORE, Material.REDSTONE),
                Map.entry(Material.GOLD_ORE, Material.GOLD_ORE),
                Map.entry(Material.COBWEB, Material.STRING),
                Map.entry(Material.STONE, Material.COBBLESTONE),
                Map.entry(Material.IRON_ORE, Material.IRON_ORE),
                Map.entry(Material.MELON, Material.MELON),
                Map.entry(Material.OBSIDIAN, Material.OBSIDIAN),
                Map.entry(Material.COAL_ORE, Material.COAL),
                Map.entry(Material.DIAMOND_ORE, Material.DIAMOND),
                Map.entry(Material.EMERALD_ORE, Material.EMERALD),
                Map.entry(Material.LAPIS_ORE, Material.LAPIS_LAZULI),
                Map.entry(Material.BOOKSHELF, Material.BOOK),
                Map.entry(Material.SMOOTH_STONE_SLAB, Material.SMOOTH_STONE_SLAB)
        );

        private static final Map<Material, Material> smeltingMap = Map.ofEntries(
                Map.entry(Material.OAK_LOG, Material.CHARCOAL),  // Smelting logs to charcoal
                Map.entry(Material.REDSTONE_ORE, Material.REDSTONE),  // Smelting redstone ore to redstone
                Map.entry(Material.GOLD_ORE, Material.GOLD_INGOT),  // Smelting gold ore to gold ingot
                Map.entry(Material.STONE, Material.SMOOTH_STONE),  // Smelting stone to smooth stone
                Map.entry(Material.IRON_ORE, Material.IRON_INGOT),  // Smelting iron ore to iron ingot
                Map.entry(Material.COBBLESTONE, Material.STONE),  // Smelting cobblestone to stone
                Map.entry(Material.SAND, Material.GLASS),  // Smelting sand to glass
                Map.entry(Material.CLAY, Material.TERRACOTTA),  // Smelting clay to terracotta
                Map.entry(Material.NETHERRACK, Material.NETHER_BRICK),  // Smelting netherrack to nether brick
                Map.entry(Material.POTATO, Material.BAKED_POTATO),  // Smelting potato to baked potato
                Map.entry(Material.BEEF, Material.COOKED_BEEF),  // Smelting raw beef to cooked beef
                Map.entry(Material.CHICKEN, Material.COOKED_CHICKEN),  // Smelting raw chicken to cooked chicken
                Map.entry(Material.PORKCHOP, Material.COOKED_PORKCHOP),  // Smelting raw porkchop to cooked porkchop
                Map.entry(Material.MUTTON, Material.COOKED_MUTTON),  // Smelting raw mutton to cooked mutton
                Map.entry(Material.RABBIT, Material.COOKED_RABBIT),  // Smelting raw rabbit to cooked rabbit
                Map.entry(Material.CACTUS, Material.GREEN_DYE)  // Smelting cactus to green dye
        );



}
