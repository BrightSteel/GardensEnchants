package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.EnchantCache;
import com.gardensmc.gardensenchants.database.EnchantEntry;
import com.gardensmc.gardensenchants.database.EnchantTable;
import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.CustomEnchantments;
import com.gardensmc.gardensenchants.enchants.EnchantRarity;
import de.dustplanet.util.SilkUtil;
import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            CustomEnchantment c = CustomEnchantments.getCustomEnchant("gardens:" + enchantment.getKey().getKey());

            enchantQueue.add(new AbstractMap.SimpleEntry<>(c, level));
        }

        enchantQueue.sort(enchantmentComparator);
//        enchantQueue.addAll(new LinkedList<>(item.getEnchantments().entrySet()));

//        ItemStack item = nbtItem.getItem();
        List<String> lore = new ArrayList<>();
        int num = 0;
        for (Map.Entry<CustomEnchantment, Integer> entry : enchantQueue) {
            CustomEnchantment c = entry.getKey();
            int level = entry.getValue();

            lore.add(getColorFromRarity(c.getRarity()) + c.getDisplayName() + " " + RomanNumber.toRoman(level));
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

        NBTItem nbtItem = new NBTItem(item);
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
            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
                if (enchantment.getName().equals(custom_enchant.getName())) {
                    hasEnchant = true;
                }

            }
        }
        return hasEnchant;
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

    public static void removeEnchant(ItemStack item, Enchantment custom_enchant) {
        ItemStack tempStack = new ItemStack(item.getType());
        ItemMeta newMeta = tempStack.getItemMeta();
        ItemMeta meta = item.getItemMeta();

        if (newMeta != null && meta != null) {
            if (meta.hasLore())
                newMeta.setLore(meta.getLore());

            if (meta.hasDisplayName())
                newMeta.setDisplayName(meta.getDisplayName());

            if (meta.hasLocalizedName())
                newMeta.setLocalizedName(meta.getLocalizedName());

            if (meta.hasCustomModelData()) {
                newMeta.setCustomModelData(meta.getCustomModelData());
            }
        }
       item.setItemMeta(newMeta);
        // re-add all enchants except for the one we are removing
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.getName().equals(custom_enchant.getName())) {
                item.addEnchantment(enchantment, entry.getValue());
            }
        }
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
        for (int i = 0; i < enchantments.length; i++) {
            EnchantRarity rarity = ProbabilityUtil.rollRarity();
            enchantments[i] = CustomEnchantments.getRandomEnchant(rarity);
            levels[i] = EnchantUtils.getRandomLevel(enchantments[i]);
        }

        LocalDateTime current = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime expiration = current.plusDays(1);

        return new EnchantEntry(enchantments, levels, expiration);
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
        for (int i = 0; i < 3; i++) {
            ItemStack book = enchants[i].getBook();
            EnchantRarity rarity = enchants[i].getRarity();
            int destruction = CustomEnchantments.getRandomDestruction(rarity);
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
