package com.gardensmc.gardensenchants.database;

import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.utils.EnchantUtils;

import java.time.LocalDateTime;

public class EnchantEntry {
    private LocalDateTime expiration;

    private CustomEnchantment[] enchantments;
    private String[] levels;

    // used by database
    public EnchantEntry(String enchant, LocalDateTime expiration) {
        this.expiration = expiration;
        decodeEnchant(enchant);
    }

    public EnchantEntry(CustomEnchantment[] enchantments, String[] levels, LocalDateTime expiration) {
        this.enchantments = enchantments;
        this.levels = levels;
        this.expiration = expiration;
    }

    public CustomEnchantment[] getEnchantments() {
        return enchantments;
    }

    public String[] getLevels() {
        return levels;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public String getEnchant() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            stringBuilder.append(enchantments[i].getBookNamespace()).append(",").append(levels[i]).append(";");
        }
        // encode enchantments as one string
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    private void decodeEnchant(String enchant) {
        String[] pairs = enchant.split(";");

        String[] levels_temp = new String[3];
        String[] bookNamespaces = new String[3];
        for (int i = 0; i < 3; i++) {
            String[] data = pairs[i].split(",");
            bookNamespaces[i] = data[0];
            levels_temp[i] = data[1];
        }

        enchantments = EnchantUtils.getEnchantmentsFromKey(bookNamespaces);
        levels = levels_temp;
    }
}
