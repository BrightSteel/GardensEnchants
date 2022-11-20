package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.enchants.EnchantRarity;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityUtil {

    public static boolean roll(double percent) {
        return ThreadLocalRandom.current().nextDouble() < percent / 100;
    }

    public static EnchantRarity rollRarity() {
        // number between 0 and 1 exclusive
        double num = ThreadLocalRandom.current().nextDouble();
        // 69% chance
        if (num <= 0.67) {
            return EnchantRarity.ORDINARY;
        }
        // 18% chance
        else if (num <= 0.87) {
            return EnchantRarity.UNIQUE;
        }
        // 9% chance
        else if (num <= 0.96) {
            return EnchantRarity.MYSTICAL;
        }
        // 3% chance
        else {
            return EnchantRarity.DIVINE;
        }
    }

    public static int getRandomIndex(int length) {
        return ThreadLocalRandom.current().nextInt(0, length);
    }
}
