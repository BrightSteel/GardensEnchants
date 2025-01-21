package com.gardensmc.gardensenchants.enchantment;

import lombok.NoArgsConstructor;

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

    public Integer generateDestructionChance() {
        if (maxPercent == 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(minPercent, maxPercent);
    }
}
