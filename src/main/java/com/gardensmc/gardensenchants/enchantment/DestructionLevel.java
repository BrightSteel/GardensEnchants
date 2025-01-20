package com.gardensmc.gardensenchants.enchantment;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor
public enum DestructionLevel {
    NONE,
    EASY(5, 15),
    MEDIUM(15, 30),
    HARD(30, 60);

    private int minPercent;
    private int maxPercent;

    DestructionLevel(int minPercent, int maxPercent) {
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
    }

    @Nullable
    public Integer generateDestructionChance() {
        if (maxPercent == 0) {
            return null;
        }
        return ThreadLocalRandom.current().nextInt(minPercent, maxPercent);
    }
}
