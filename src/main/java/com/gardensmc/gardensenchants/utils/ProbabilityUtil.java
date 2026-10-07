package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityUtil {

    public static boolean roll(double percent) {
        return ThreadLocalRandom.current().nextDouble() < percent / 100;
    }

    public static EnchantRarity rollRarity() {
        double num = ThreadLocalRandom.current().nextDouble(); // 0 - 0.99
        // 71% chance
        if (num <= 0.71) {
            return EnchantRarity.COMMON;
        }
        // 15% chance
        else if (num <= 0.86) {
            return EnchantRarity.UNCOMMON;
        }
        // 8% chance
        else if (num <= 0.94) {
            return EnchantRarity.RARE;
        }
        // 4% chance
        else if (num <= 0.98) {
            return EnchantRarity.EPIC;
        }
        // 2% chance
        else {
            return EnchantRarity.LEGENDARY;
        }
    }

    /**
     * A level from 1 to max, the lower ones likelier: each level's weight is how many levels are at or above it
     * (max 3: I 50%, II 33%, III 17%; max 5: I 33% down to V 7%).
     */
    public static int rollLevel(int max) {
        if (max <= 1) return 1;
        int total = max * (max + 1) / 2;
        int pick = ThreadLocalRandom.current().nextInt(total);
        for (int level = 1; level <= max; level++) {
            pick -= max - level + 1;
            if (pick < 0) return level;
        }
        return max;
    }

    public static int getRandomIndex(int length) {
        if (length == 0) return 0;
        return ThreadLocalRandom.current().nextInt(0, length);
    }
}
