package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityUtil {

    public static boolean roll(double percent) {
        return ThreadLocalRandom.current().nextDouble() < percent / 100;
    }

    public static EnchantRarity rollRarity() {
        double num = ThreadLocalRandom.current().nextDouble(); // 0 - 0.99
        // 70% chance
        if (num <= 0.70) {
            return EnchantRarity.COMMON;
        }
        // 15% chance
        else if (num <= 0.85) {
            return EnchantRarity.UNCOMMON;
        }
        // 8% chance
        else if (num <= 0.93) {
            return EnchantRarity.RARE;
        }
        // 4% chance
        else if (num <= 0.97) {
            return EnchantRarity.EPIC;
        }
        // 2% chance
        else {
            return EnchantRarity.LEGENDARY;
        }
    }

    public static int getRandomIndex(int length) {
        if (length == 0) return 0;
        return ThreadLocalRandom.current().nextInt(0, length);
    }
}
