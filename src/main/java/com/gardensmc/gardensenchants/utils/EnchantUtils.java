package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.EnchantCache;
import com.gardensmc.gardensenchants.database.EnchantEntry;
import com.gardensmc.gardensenchants.database.EnchantTable;
import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.CustomEnchantments;
import com.gardensmc.gardensenchants.enchants.EnchantRarity;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EnchantUtils {

    public static void enchantItem(ItemStack item, Enchantment enchantment, int level) {
        item.addEnchantment(enchantment, level);
        updateLore(item);
    }




    public static void updateLore(ItemStack item) {

        Comparator<Map.Entry<CustomEnchantment, Integer>> enchantmentComparator = (o1, o2) -> {
            int compare = o1.getKey().getRarity().compareTo(o2.getKey().getRarity());
            return Integer.compare(compare, 0);
        };

        List<Map.Entry<CustomEnchantment, Integer>> enchantQueue = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            CustomEnchantment c = CustomEnchantments.getCustomEnchant(enchantment.getKey().getKey());

            enchantQueue.add(new AbstractMap.SimpleEntry<>(c, level));
        }

        enchantQueue.sort(enchantmentComparator);
//        enchantQueue.addAll(new LinkedList<>(item.getEnchantments().entrySet()));

//        ItemStack item = nbtItem.getItem();
        List<String> lore = new ArrayList<>();
        int num = 0;
        NBTItem nbtItem = new NBTItem(item);
        for (Map.Entry<CustomEnchantment, Integer> entry : enchantQueue) {
            CustomEnchantment c = entry.getKey();
            int level = entry.getValue();

            if (c == CustomEnchantments.WINGED_BOOTS && nbtItem.hasKey("charge")) {
//                NBTItem nbtItem = new NBTItem(item);
//                int charge = 3600;
                int charge = nbtItem.getInteger("charge");

                int min = charge/60;
//                System.out.println("Updating lore: " + min);

                lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level) + " " + ChatColor.GRAY  + min + " min");
            }
            else {
                lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level));
            }
            num++;
        }
//        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            int level = entry.getValue();
//            CustomEnchantment c = CustomEnchantments.getCustomEnchant("gardens:" + enchantment.getKey().getKey());
////            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
//                lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level));
//                num++;
////            }
//        }

//        NBTItem nbtItem = new NBTItem(item);
        int slots = nbtItem.getInteger("slots");

        lore.add("");
        lore.add(ChatColor.WHITE + "Enchant Slots: " + ChatColor.GOLD + "(" + num + "/" + slots + ")");
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
    }

    public static boolean hasEnchantment(ItemStack item, Enchantment custom_enchant) {
        boolean hasEnchant = false;
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
//            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
                if (enchantment.equals(custom_enchant)) {
//                    System.out.println("want: " + enchantment.getKey());
//                    System.out.println("has: " + custom_enchant.getKey());

                    hasEnchant = true;
                }

//            }
        }
        return hasEnchant;
    }

    /**
     * Called on join to refresh user enchants and apply where needed
     * @param player
     */
    public static void refreshEnchants(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() != Material.AIR) {
            refreshHelmetEnchants(player, helmet);
        }

        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.getType() != Material.AIR) {
            refreshBootsEnchants(player, boots);
            refreshWingedBoots(player, boots);
        }
    }

    private static void refreshHelmetEnchants(Player player, ItemStack helmet) {

        boolean appliedNightVision = false;
        for (Map.Entry<Enchantment, Integer> entry : helmet.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

//            String key = enchantment.getKey().getKey().toLowerCase();
//            if (key.equals("night_vision")) {
            if (enchantment.equals(CustomEnchantments.NIGHT_VISION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                appliedNightVision = true;
            }
        }

        PotionEffect nightVision = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (!appliedNightVision && nightVision != null && nightVision.getDuration() > 9500) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    private static void refreshBootsEnchants(Player player, ItemStack boots) {

        boolean appliedSpeedy = false;
        for (Map.Entry<Enchantment, Integer> entry : boots.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            if (enchantment.equals(CustomEnchantments.SPEEDY)) {
                float newSpeed = 0.2f + (level * 0.025f);
                player.setWalkSpeed(newSpeed);
                appliedSpeedy = true;
            }
        }

        if (!appliedSpeedy && player.getWalkSpeed() > 0.20f) {
            player.setWalkSpeed(0.20f);
        }
    }

    public static void refreshWingedBoots(Player player, ItemStack boots) {
        if (!player.hasPermission("gardens.fly.anywhere")) {
            String[] anywhere_req = new String[]{
                    "resource_world",
                    "nether",
                    "end"
            };
            if (Arrays.asList(anywhere_req).contains(player.getWorld().getName().toLowerCase()) || !player.hasPermission("gardens.fly")) {
                refreshWinged(player, boots);
            }

        }
    }

    private static void refreshWinged(Player player, ItemStack boots) {
        boolean applied = false;
        for (Map.Entry<Enchantment, Integer> entry : boots.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (enchantment.equals(CustomEnchantments.WINGED_BOOTS)) {
                NBTItem nbtItem = new NBTItem(boots);
                if (nbtItem.hasKey("charge")) {
                    int charge = nbtItem.getInteger("charge");
                    if (charge > 0) {
                        player.setAllowFlight(true);
                        applied = true;
                        if (player.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.AIR) {
                            player.setFlying(true);
                        }
                    }
                }
            }
        }

        if (!applied && player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

//    public static boolean getEnchantmentLevel(ItemStack item, Enchantment custom_enchant) {
//        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
//                if (enchantment.getName().equals(custom_enchant.getName())) {
//                    return enchantment.getMaxLevel()
//                }
//
//            }
//        }
//        return hasEnchant;
//    }

    public static ItemStack removeEnchant(ItemStack item, Enchantment enchant) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompoundList lst = nbtItem.getCompoundList("Enchantments");
        if (lst.isEmpty()) return item;

        lst.removeIf(comp -> comp.getString("id").equals(enchant.getKey().toString()));

        // remove charge if winged boots is being removed
        if (enchant instanceof CustomEnchantment c && c == CustomEnchantments.WINGED_BOOTS) {
            nbtItem.removeKey("charge");
        }
        return nbtItem.getItem();
    }

    public static int getEnchantLevel(ItemStack item, Enchantment custom_enchant) {
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
                if (enchantment.getName().equals(custom_enchant.getName())) {
                    return entry.getValue();
                }

            }
        }
        return 0;
    }

    public static int getLevelFromName(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 1;

        String roman = meta.getDisplayName().substring(meta.getDisplayName().length()-2);
        return RomanNumber.toNumber(roman);

    }

    public static String getColorFromRarity(EnchantRarity rarity) {
        String hex;
        switch (rarity) {
            default -> hex = "#CFD5DD";
            case UNIQUE -> hex = "#F7C941";
            case MYSTICAL -> hex = "#B756FE";
            case DIVINE -> hex = "#91E6DC";
        }
        return ChatUtils.translateHexColorCodes("#", "", hex);
    }

    public static String getRarityTag(EnchantRarity rarity) {
        String tag;
        switch (rarity) {
            default -> tag = "\uE05E";
            case UNIQUE -> tag = "\uE110";
            case MYSTICAL -> tag = "\uE111";
            case DIVINE -> tag = "\uE112";
        }
        return tag;
    }

    public static EnchantEntry getOrCreateEnchantEntry(UUID uuid) {
        EnchantEntry enchantEntry = null;
        try {
            enchantEntry = EnchantTable.getEnchantEntry(uuid.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDateTime current = LocalDateTime.now(Clock.systemUTC());

        if (enchantEntry == null || current.isAfter(enchantEntry.getExpiration())) {
            boolean create = (enchantEntry == null);
            enchantEntry = generateMagicEnchants();
            // update database to ensure data is kept
            try {
                if (create) {
                    EnchantTable.createEnchant(uuid.toString(), enchantEntry.getEnchant(), enchantEntry.getExpiration());
                } else {
                    EnchantTable.updateEnchant(uuid.toString(), enchantEntry.getEnchant(), enchantEntry.getExpiration());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return enchantEntry;
    }

    public static void resetEnchants(UUID uuid) {
        EnchantEntry entry = generateMagicEnchants();
        try {
            EnchantTable.updateEnchant(uuid.toString(), entry.getEnchant(), entry.getExpiration());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CustomEnchantment[] getEnchantmentsFromKey(String[] bookNamespaces) {

        CustomEnchantment[] customEnchantments = new CustomEnchantment[3];
        for (CustomEnchantment c : CustomEnchantments.ordinaryEnchants) {
            for (int i = 0; i < 3; i++) {
                if (c.getBookNamespace().equals(bookNamespaces[i])) {
                    customEnchantments[i] = c;
                }
            }
        }

        if (!Arrays.asList(customEnchantments).contains(null)) return customEnchantments;
        for (CustomEnchantment c : CustomEnchantments.uniqueEnchants) {
            for (int i = 0; i < 3; i++) {
                if (c.getBookNamespace().equals(bookNamespaces[i])) {
                    customEnchantments[i] = c;
                }
            }
        }

        if (!Arrays.asList(customEnchantments).contains(null)) return customEnchantments;
        for (CustomEnchantment c : CustomEnchantments.mysticalEnchants) {
            for (int i = 0; i < 3; i++) {
                if (c.getBookNamespace().equals(bookNamespaces[i])) {
                    customEnchantments[i] = c;
                }
            }
        }

        if (!Arrays.asList(customEnchantments).contains(null)) return customEnchantments;
        for (CustomEnchantment c : CustomEnchantments.divineEnchants) {
            for (int i = 0; i < 3; i++) {
                if (c.getBookNamespace().equals(bookNamespaces[i])) {
                    customEnchantments[i] = c;
                }
            }
        }

        return customEnchantments;
    }

    // best to call this asynchronously? although i think this will have fast performance
    public static EnchantEntry generateMagicEnchants() {

        CustomEnchantment[] enchantments = new CustomEnchantment[3];
        String[] levels = new String[3];
        Integer[] destructions = new Integer[3];
        for (int i = 0; i < enchantments.length; i++) {
            EnchantRarity rarity = ProbabilityUtil.rollRarity();
            enchantments[i] = CustomEnchantments.getRandomEnchant(rarity);
            levels[i] = EnchantUtils.getRandomLevel(enchantments[i]);
            destructions[i] = CustomEnchantments.getRandomDestruction(rarity);
        }

        LocalDateTime current = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime expiration = current.plusDays(1);

        return new EnchantEntry(enchantments, levels, destructions, expiration);
    }

    public static int genRandomLevel(Enchantment enchantment) {
        int min = enchantment.getStartLevel();
        int max = enchantment.getMaxLevel();

        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    public static String getRandomLevel(Enchantment enchantment) {
        int level = genRandomLevel(enchantment);
        return RomanNumber.toRoman(level);
    }

    public static ItemStack getEnchantBook(String bookNamespace) {
        CustomEnchantment enchant = CustomEnchantments.getCustomEnchant(bookNamespace);
        if (enchant == null) return null;

        String level = EnchantUtils.getRandomLevel(enchant);

        ItemStack book = enchant.getBook();
        int destruction = CustomEnchantments.getRandomDestruction(enchant.getRarity());
        if (book != null) {
            ItemMeta meta = book.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(meta.getDisplayName() + " " + level);
                if (meta.getLore() != null) {
                    List<String> lore = meta.getLore();
                    lore.add(ChatUtils.translateHexColorCodes("#", "", "#FF5555") + "Destruction Chance: " + ChatUtils.translateHexColorCodes("#", "", "#AAAAAA") + destruction + "%");
                    meta.setLore(lore);
                }
                book.setItemMeta(meta);
            }
            NBTItem nbtItem = new NBTItem(book);
            nbtItem.setInteger("destruction", destruction);
            nbtItem.setString("destroy_enchant", enchant.getBookNamespace());
            nbtItem.setInteger("level", RomanNumber.toNumber(level));
            book = nbtItem.getItem();
        }

        return book;

    }

    public static ItemStack getEnchantBook(String bookNamespace, int destruction) {
        CustomEnchantment enchant = CustomEnchantments.getCustomEnchant(bookNamespace);
        if (enchant == null) return null;

        String level = EnchantUtils.getRandomLevel(enchant);

        ItemStack book = enchant.getBook();
        if (book != null) {
            ItemMeta meta = book.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(meta.getDisplayName() + " " + level);
                if (meta.getLore() != null) {
                    List<String> lore = meta.getLore();
                    lore.add(ChatUtils.translateHexColorCodes("#", "", "#FF5555") + "Destruction Chance: " + ChatUtils.translateHexColorCodes("#", "", "#AAAAAA") + destruction + "%");
                    meta.setLore(lore);
                }
                book.setItemMeta(meta);
            }
            NBTItem nbtItem = new NBTItem(book);
            nbtItem.setInteger("destruction", destruction);
            nbtItem.setString("destroy_enchant", enchant.getBookNamespace());
            nbtItem.setInteger("level", RomanNumber.toNumber(level));
            book = nbtItem.getItem();
        }

        return book;

    }

    // used for loot, crates, etc
    public static ItemStack getRandomBook() {
        EnchantRarity rarity = ProbabilityUtil.rollRarity();
        CustomEnchantment enchantment = CustomEnchantments.getRandomEnchant(rarity);
        String level = EnchantUtils.getRandomLevel(enchantment);

        ItemStack book = enchantment.getBook();
        int destruction = CustomEnchantments.getRandomDestruction(rarity);
        if (book != null) {
            ItemMeta meta = book.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(meta.getDisplayName() + " " + level);
                if (meta.getLore() != null) {
                    List<String> lore = meta.getLore();
                    lore.add(ChatUtils.translateHexColorCodes("#", "", "#FF5555") + "Destruction Chance: " + ChatUtils.translateHexColorCodes("#", "", "#AAAAAA") + destruction + "%");
                    meta.setLore(lore);
                }
                book.setItemMeta(meta);
            }
            NBTItem nbtItem = new NBTItem(book);
            nbtItem.setInteger("destruction", destruction);
            nbtItem.setString("destroy_enchant", enchantment.getBookNamespace());
            nbtItem.setInteger("level", RomanNumber.toNumber(level));
            book = nbtItem.getItem();
        }

        return book;

    }

    // use to get books from cache table
    public static ItemStack[] getRandomBooks(UUID uuid) {
        ItemStack[] books = new ItemStack[3];
        EnchantCache enchantCache = new EnchantCache(uuid);
        CustomEnchantment[] enchants = enchantCache.getEnchants();
        String[] levels = enchantCache.getLevels();
        Integer[] destructions = enchantCache.getDestructions();
        for (int i = 0; i < 3; i++) {
            ItemStack book = enchants[i].getBook();
            int destruction = destructions[i];
            if (book != null) {
                ItemMeta meta = book.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(meta.getDisplayName() + " " + levels[i]);
                    if (meta.getLore() != null) {
                        List<String> lore = meta.getLore();
                        lore.add(ChatUtils.translateHexColorCodes("#", "", "#FF5555") + "Destruction Chance: " + ChatUtils.translateHexColorCodes("#", "", "#AAAAAA") + destruction + "%");
                        meta.setLore(lore);
                    }
                    book.setItemMeta(meta);
                }
                NBTItem nbtItem = new NBTItem(book);
                nbtItem.setInteger("destruction", destruction);
                nbtItem.setString("destroy_enchant", enchants[i].getBookNamespace());
                nbtItem.setInteger("level", RomanNumber.toNumber(levels[i]));
                books[i] = nbtItem.getItem();
            }

        }
        return books;
    }

    public static int getAmberCost(EnchantRarity rarity) {
        int cost = 0;
        switch (rarity) {
            case ORDINARY -> cost = 10;
            case UNIQUE -> cost = 25;
            case MYSTICAL -> cost = 45;
            case DIVINE -> cost = 75;
        }
        return cost;
    }

    public static int getXpCost(EnchantRarity rarity) {
        int cost = 0;
        switch (rarity) {
            case ORDINARY -> cost = 55; // 5 levels
            case UNIQUE -> cost = 160; // 10
            case MYSTICAL -> cost = 315; // 15
            case DIVINE -> cost = 1395; // 30
        }
        return cost;
    }

    public static int translateXpToLevel(int totalXp) {
        int level = 0;
        switch (totalXp) {
            case 55 -> level = 5;
            case 160 -> level = 10;
            case 315 -> level = 15;
            case 1395 -> level = 30;
        }
        return level;
    }
}
