package com.gardensmc.gardensenchants.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodMathTest {

    private static final double NEVER = 0.999;
    private static final double ALWAYS = 0.0;

    @Test
    void sprinterNeverDrainsMoreThanVanilla() {
        for (int level = 0; level <= 3; level++) {
            for (int current = 1; current <= 20; current++) {
                int result = FoodMath.sprinterFoodLevel(current, current - 1, level, () -> ALWAYS);
                assertTrue(result >= current - 1 && result <= current, "level " + level + " at " + current);
            }
        }
    }

    @Test
    void sprinterScalesTheLossNotTheLevel() {
        // used to be round(19 * 0.4) = 8 with Sprinter III
        assertEquals(19, FoodMath.sprinterFoodLevel(20, 19, 3, () -> ALWAYS));
        // 1 * 0.4 = 0.4: the point is lost only when the roll is under 0.4
        assertEquals(19, FoodMath.sprinterFoodLevel(20, 19, 3, () -> 0.39));
        assertEquals(20, FoodMath.sprinterFoodLevel(20, 19, 3, () -> 0.41));
        // 5 * 0.8 = 4 exactly: no roll needed
        assertEquals(16, FoodMath.sprinterFoodLevel(20, 15, 1, () -> NEVER));
    }

    @Test
    void sprinterIgnoresGainsAndNoEnchant() {
        assertEquals(15, FoodMath.sprinterFoodLevel(10, 15, 3, () -> ALWAYS));
        assertEquals(9, FoodMath.sprinterFoodLevel(10, 9, 0, () -> NEVER));
    }

    @Test
    void muncherScalesTheGainAndCapsAtMax() {
        // used to be (int) (20 + 20 * 0.3) = 26
        assertEquals(20, FoodMath.muncherFoodLevel(18, 20, 3));
        // 2 * 1.3 = 2.6 -> 3
        assertEquals(13, FoodMath.muncherFoodLevel(10, 12, 3));
        // 8 * 1.1 = 8.8 -> 9
        assertEquals(9, FoodMath.muncherFoodLevel(0, 8, 1));
    }

    @Test
    void muncherIgnoresDrainsAndNoEnchant() {
        assertEquals(5, FoodMath.muncherFoodLevel(10, 5, 3));
        assertEquals(12, FoodMath.muncherFoodLevel(10, 12, 0));
    }
}
