//package com.gardensmc.gardensenchants.utils;
//
//import com.gardensmc.gardensenchants.cache.EnchantCache;
//import com.gardensmc.gardensenchants.database.EnchantEntry;
//import com.gardensmc.gardensenchants.database.EnchantTable;
//import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
//import de.tr7zw.nbtapi.NBTCompoundList;
//import de.tr7zw.nbtapi.NBTItem;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.enchantments.Enchantment;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemFlag;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//
//import java.time.Clock;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class EnchantUtils {
//
//    public static void enchantItem(ItemStack item, Enchantment enchantment, int level) {
//        item.addEnchantment(enchantment, level);
//        updateLore(item);
//    }
//
//    public static void updateLore(ItemStack item) {
//
//        Comparator<Map.Entry<CustomEnchantmentOld, Integer>> enchantmentComparator = (o1, o2) -> {
//            int compare = o1.getKey().getRarity().compareTo(o2.getKey().getRarity());
//            return Integer.compare(compare, 0);
//        };
//
//        List<Map.Entry<CustomEnchantmentOld, Integer>> enchantQueue = new ArrayList<>();
//        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            int level = entry.getValue();
//            CustomEnchantmentOld c = CustomEnchantmentsOld.getCustomEnchant(enchantment.getKey().getKey());
//
//            enchantQueue.add(new AbstractMap.SimpleEntry<>(c, level));
//        }
//
//        enchantQueue.sort(enchantmentComparator);
////        enchantQueue.addAll(new LinkedList<>(item.getEnchantments().entrySet()));
//
////        ItemStack item = nbtItem.getItem();
//        List<String> lore = new ArrayList<>();
//        int num = 0;
//        NBTItem nbtItem = new NBTItem(item);
//        for (Map.Entry<CustomEnchantmentOld, Integer> entry : enchantQueue) {
//            CustomEnchantmentOld c = entry.getKey();
//            int level = entry.getValue();
//
//            if (c == CustomEnchantmentsOld.WINGED_BOOTS && nbtItem.hasKey("charge")) {
////                NBTItem nbtItem = new NBTItem(item);
////                int charge = 3600;
//                int charge = nbtItem.getInteger("charge");
//
//                int min = charge/60;
////                System.out.println("Updating lore: " + min);
//
//                lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level) + " " + ChatColor.GRAY  + min + " min");
//            }
//            else {
//                lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level));
//            }
//            num++;
//        }
//
//        if (nbtItem.hasKey("extraLore")) {
//            String extraLore = nbtItem.getString("extraLore");
//            String[] extra = extraLore.split(";");
//            for (String l : extra) {
//                lore.add(MessageUtil.parseIntoLegacy(l));
//            }
//        }
////        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
////            Enchantment enchantment = entry.getKey();
////            int level = entry.getValue();
////            CustomEnchantment c = CustomEnchantments.getCustomEnchant("gardens:" + enchantment.getKey().getKey());
//////            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
////                lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level));
////                num++;
//////            }
////        }
//
////        NBTItem nbtItem = new NBTItem(item);
//        int slots = nbtItem.getInteger("slots");
//
//        lore.add("");
//        lore.add(ChatColor.WHITE + "Enchant Slots: " + ChatColor.GOLD + "(" + num + "/" + slots + ")");
//        ItemMeta meta = item.getItemMeta();
//        if (meta != null) {
//            meta.setLore(lore);
//            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
//        }
//        item.setItemMeta(meta);
//    }
//
//    /**
//     * Called on join to refresh user enchants and apply where needed
//     * @param player
//     */
//    public static void refreshEnchants(Player player) {
//        ItemStack helmet = player.getInventory().getHelmet();
//        if (helmet != null && helmet.getType() != Material.AIR) {
//            refreshHelmetEnchants(player, helmet);
//        }
//
//        ItemStack boots = player.getInventory().getBoots();
//        if (boots != null && boots.getType() != Material.AIR) {
//            refreshBootsEnchants(player, boots);
//            refreshWingedBoots(player, boots);
//        }
//    }
//
//    private static void refreshHelmetEnchants(Player player, ItemStack helmet) {
//
//        boolean appliedNightVision = false;
//        for (Map.Entry<Enchantment, Integer> entry : helmet.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            int level = entry.getValue();
//
////            String key = enchantment.getKey().getKey().toLowerCase();
////            if (key.equals("night_vision")) {
//            if (enchantment.equals(CustomEnchantmentsOld.NIGHT_VISION)) {
//                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
//                appliedNightVision = true;
//            }
//        }
//
//        PotionEffect nightVision = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
//        if (!appliedNightVision && nightVision != null && nightVision.getDuration() > 9500) {
//            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
//        }
//    }
//
//    private static void refreshBootsEnchants(Player player, ItemStack boots) {
//
//        boolean appliedSpeedy = false;
//        for (Map.Entry<Enchantment, Integer> entry : boots.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            int level = entry.getValue();
//
//            if (enchantment.equals(CustomEnchantmentsOld.SPEEDY)) {
//                float newSpeed = 0.2f + (level * 0.025f);
//                player.setWalkSpeed(newSpeed);
//                appliedSpeedy = true;
//            }
//        }
//
//        if (!appliedSpeedy && player.getWalkSpeed() > 0.20f) {
//            player.setWalkSpeed(0.20f);
//        }
//    }
//
//    public static void refreshWingedBoots(Player player, ItemStack boots) {
//        if (!player.hasPermission("gardens.fly.anywhere")) {
//            String[] anywhere_req = new String[]{
//                    "resource_world",
//                    "nether",
//                    "end"
//            };
//            if (Arrays.asList(anywhere_req).contains(player.getWorld().getName().toLowerCase()) || !player.hasPermission("gardens.fly")) {
//                refreshWinged(player, boots);
//            }
//
//        }
//    }
//
//    private static void refreshWinged(Player player, ItemStack boots) {
//        boolean applied = false;
//        for (Map.Entry<Enchantment, Integer> entry : boots.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//
//            if (enchantment.equals(CustomEnchantmentsOld.WINGED_BOOTS)) {
//                NBTItem nbtItem = new NBTItem(boots);
//                if (nbtItem.hasKey("charge")) {
//                    int charge = nbtItem.getInteger("charge");
//                    if (charge > 0) {
//                        player.setAllowFlight(true);
//                        applied = true;
//                        if (player.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.AIR) {
//                            player.setFlying(true);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!applied && player.getAllowFlight()) {
//            player.setAllowFlight(false);
//            player.setFlying(false);
//        }
//    }
//
////    public static boolean getEnchantmentLevel(ItemStack item, Enchantment custom_enchant) {
////        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
////            Enchantment enchantment = entry.getKey();
////            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
////                if (enchantment.getName().equals(custom_enchant.getName())) {
////                    return enchantment.getMaxLevel()
////                }
////
////            }
////        }
////        return hasEnchant;
////    }
//
//    public static ItemStack removeEnchant(ItemStack item, Enchantment enchant) {
//        NBTItem nbtItem = new NBTItem(item);
//        NBTCompoundList lst = nbtItem.getCompoundList("Enchantments");
//        if (lst.isEmpty()) return item;
//
//        lst.removeIf(comp -> comp.getString("id").equals(enchant.getKey().toString()));
//
//        // remove charge if winged boots is being removed
//        if (enchant instanceof CustomEnchantmentOld c && c == CustomEnchantmentsOld.WINGED_BOOTS) {
//            nbtItem.removeKey("charge");
//        }
//        return nbtItem.getItem();
//    }
//
//    public static int getEnchantLevel(ItemStack item, Enchantment custom_enchant) {
//        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
//                if (enchantment.getName().equals(custom_enchant.getName())) {
//                    return entry.getValue();
//                }
//
//            }
//        }
//        return 0;
//    }
//
//    public static int getLevelFromName(ItemStack stack) {
//        ItemMeta meta = stack.getItemMeta();
//        if (meta == null) return 1;
//
//        String roman = meta.getDisplayName().substring(meta.getDisplayName().length()-2);
//        return RomanNumber.toNumber(roman);
//
//    }
//
//
//
//    public static EnchantEntry getOrCreateEnchantEntry(UUID uuid) {
//        EnchantEntry enchantEntry = null;
//        try {
//            enchantEntry = EnchantTable.getEnchantEntry(uuid.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        LocalDateTime current = LocalDateTime.now(Clock.systemUTC());
//
//        if (enchantEntry == null || current.isAfter(enchantEntry.getExpiration())) {
//            boolean create = (enchantEntry == null);
//            enchantEntry = generateMagicEnchants();
//            // update database to ensure data is kept
//            try {
//                if (create) {
//                    EnchantTable.createEnchant(uuid.toString(), enchantEntry.getEnchant(), enchantEntry.getExpiration());
//                } else {
//                    EnchantTable.updateEnchant(uuid.toString(), enchantEntry.getEnchant(), enchantEntry.getExpiration());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return enchantEntry;
//    }
//
//    public static void resetEnchants(UUID uuid) {
//        EnchantEntry entry = generateMagicEnchants();
//        try {
//            EnchantTable.updateEnchant(uuid.toString(), entry.getEnchant(), entry.getExpiration());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static CustomEnchantmentOld[] getEnchantmentsFromKey(String[] bookNamespaces) {
//
//        CustomEnchantmentOld[] customEnchantmentOlds = new CustomEnchantmentOld[3];
//        for (CustomEnchantmentOld c : CustomEnchantmentsOld.ordinaryEnchants) {
//            for (int i = 0; i < 3; i++) {
//                if (c.getBookNamespace().equals(bookNamespaces[i])) {
//                    customEnchantmentOlds[i] = c;
//                }
//            }
//        }
//
//        if (!Arrays.asList(customEnchantmentOlds).contains(null)) return customEnchantmentOlds;
//        for (CustomEnchantmentOld c : CustomEnchantmentsOld.uniqueEnchants) {
//            for (int i = 0; i < 3; i++) {
//                if (c.getBookNamespace().equals(bookNamespaces[i])) {
//                    customEnchantmentOlds[i] = c;
//                }
//            }
//        }
//
//        if (!Arrays.asList(customEnchantmentOlds).contains(null)) return customEnchantmentOlds;
//        for (CustomEnchantmentOld c : CustomEnchantmentsOld.mysticalEnchants) {
//            for (int i = 0; i < 3; i++) {
//                if (c.getBookNamespace().equals(bookNamespaces[i])) {
//                    customEnchantmentOlds[i] = c;
//                }
//            }
//        }
//
//        if (!Arrays.asList(customEnchantmentOlds).contains(null)) return customEnchantmentOlds;
//        for (CustomEnchantmentOld c : CustomEnchantmentsOld.divineEnchants) {
//            for (int i = 0; i < 3; i++) {
//                if (c.getBookNamespace().equals(bookNamespaces[i])) {
//                    customEnchantmentOlds[i] = c;
//                }
//            }
//        }
//
//        return customEnchantmentOlds;
//    }
//
//    // best to call this asynchronously? although i think this will have fast performance
//    public static EnchantEntry generateMagicEnchants() {
//
//        CustomEnchantmentOld[] enchantments = new CustomEnchantmentOld[3];
//        String[] levels = new String[3];
//        Integer[] destructions = new Integer[3];
//        for (int i = 0; i < enchantments.length; i++) {
//            EnchantRarity rarity = ProbabilityUtil.rollRarity();
//            enchantments[i] = CustomEnchantmentsOld.getRandomEnchant(rarity);
//            levels[i] = EnchantUtils.getRandomLevel(enchantments[i]);
//            destructions[i] = CustomEnchantmentsOld.getRandomDestruction(rarity);
//        }
//
//        LocalDateTime current = LocalDateTime.now(Clock.systemUTC());
//        LocalDateTime expiration = current.plusDays(1);
//
//        return new EnchantEntry(enchantments, levels, destructions, expiration);
//    }
//
//    public static int genRandomLevel(Enchantment enchantment) {
//        int min = enchantment.getStartLevel();
//        int max = enchantment.getMaxLevel();
//
//        return ThreadLocalRandom.current().nextInt(min, max+1);
//    }
//
//    public static String getRandomLevel(Enchantment enchantment) {
//        int level = genRandomLevel(enchantment);
//        return RomanNumber.toRoman(level);
//    }
//
//    public static int getAmberCost(EnchantRarity rarity) {
//        int cost = 0;
//        switch (rarity) {
//            case ORDINARY -> cost = 10;
//            case UNIQUE -> cost = 25;
//            case MYSTICAL -> cost = 45;
//            case DIVINE -> cost = 75;
//        }
//        return cost;
//    }
//
//    public static int getXpCost(EnchantRarity rarity) {
//        int cost = 0;
//        switch (rarity) {
//            case ORDINARY -> cost = 55; // 5 levels
//            case UNIQUE -> cost = 160; // 10
//            case MYSTICAL -> cost = 315; // 15
//            case DIVINE -> cost = 1395; // 30
//        }
//        return cost;
//    }
//
//    public static int translateXpToLevel(int totalXp) {
//        int level = 0;
//        switch (totalXp) {
//            case 55 -> level = 5;
//            case 160 -> level = 10;
//            case 315 -> level = 15;
//            case 1395 -> level = 30;
//        }
//        return level;
//    }
//}
