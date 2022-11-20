package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.CustomEnchantments;
import com.gardensmc.gardensenchants.enchants.EnchantRarity;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantUtils {

    public static void enchantItem(ItemStack item, Enchantment enchantment, int level) {
        item.addEnchantment(enchantment, level);
        updateLore(item);
    }

    // todo store magic slots max in custom NBT
    private static void updateLore(ItemStack item) {
        List<String> lore = new ArrayList<>();
        int num = 0;
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            if (enchantment.getKey().getNamespace().equals("gardensenchants")) {
                lore.add(getColorFromRarity(EnchantRarity.ORDINARY) + enchantment.getName() + " " + RomanNumber.toRoman(level));
                num++;
            }
        }
        lore.add("");
        lore.add(ChatColor.WHITE + "Magic Slots: " + ChatColor.GOLD + "(" + num + "/4)");
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
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
            default -> hex = "&#CFD5DD";
        }
        return ChatUtils.translateHexColorCodes("&#", "", hex);
    }





    // best to call this asynchronously? although i think this will have fast performance
    public static CustomEnchantment[] generateMagicEnchants() {

        CustomEnchantment[] enchantments = new CustomEnchantment[3];
        for (int i = 0; i < enchantments.length; i++) {
            EnchantRarity rarity = ProbabilityUtil.rollRarity();
            enchantments[i] = CustomEnchantments.getRandomEnchant(rarity);
        }

        return enchantments;
    }
}
