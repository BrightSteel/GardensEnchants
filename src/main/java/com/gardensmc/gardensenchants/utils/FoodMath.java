package com.gardensmc.gardensenchants.utils;

import java.util.function.DoubleSupplier;

/**
 * Food level maths for Sprinter and Muncher. Both scale the change in food, never the food level itself.
 */
public final class FoodMath {

    public static final int MAX_FOOD_LEVEL = 20;
    private static final double SPRINTER_REDUCTION_PER_LEVEL = 0.2;
    private static final double MUNCHER_BONUS_PER_LEVEL = 0.1;

    private FoodMath() {
    }

    /**
     * Food level after a drain with Sprinter: each level removes 20% of the loss. Drains are usually a
     * single point, so the fraction is applied as a chance rather than rounded away.
     *
     * @param random returns a value in [0, 1)
     */
    public static int sprinterFoodLevel(int currentLevel, int resultLevel, int sprinterLevel, DoubleSupplier random) {
        int loss = currentLevel - resultLevel;
        if (loss <= 0 || sprinterLevel <= 0) {
            return resultLevel;
        }
        double scaledLoss = loss * Math.max(0, 1 - SPRINTER_REDUCTION_PER_LEVEL * sprinterLevel);
        int actualLoss = (int) scaledLoss;
        if (random.getAsDouble() < scaledLoss - actualLoss) {
            actualLoss++;
        }
        return clamp(currentLevel - actualLoss);
    }

    /**
     * Food level after eating with Muncher: each level adds 10% to the food gained, capped at the max.
     */
    public static int muncherFoodLevel(int currentLevel, int resultLevel, int muncherLevel) {
        int gain = resultLevel - currentLevel;
        if (gain <= 0 || muncherLevel <= 0) {
            return resultLevel;
        }
        int boostedGain = (int) Math.round(gain * (1 + MUNCHER_BONUS_PER_LEVEL * muncherLevel));
        return clamp(currentLevel + boostedGain);
    }

    private static int clamp(int foodLevel) {
        return Math.max(0, Math.min(MAX_FOOD_LEVEL, foodLevel));
    }
}
