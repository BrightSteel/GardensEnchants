package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityUtil {

    public static boolean roll(double percent) {
        return ThreadLocalRandom.current().nextDouble() < percent / 100;
    }

    public static EnchantRarity rollRarity() {
        // number between 0 and 1 exclusive
        double num = ThreadLocalRandom.current().nextDouble();
        // 78% chance
        if (num <= 0.78) {
            return EnchantRarity.ORDINARY;
        }
        // 15% chance
        else if (num <= 0.93) {
            return EnchantRarity.UNIQUE;
        }
        // 5% chance
        else if (num <= 0.98) {
            return EnchantRarity.MYSTICAL;
        }
        // 1% chance
        else {
            return EnchantRarity.DIVINE;
        }
    }

    public static int getRandomIndex(int length) {
        if (length == 0) return 0;
        return ThreadLocalRandom.current().nextInt(0, length);
    }
}
