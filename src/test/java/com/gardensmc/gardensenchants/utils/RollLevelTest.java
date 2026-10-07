package com.gardensmc.gardensenchants.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RollLevelTest {

    @Test
    void lowerLevelsAreLikelier() {
        int[] counts = new int[4];
        for (int i = 0; i < 60_000; i++) counts[ProbabilityUtil.rollLevel(3)]++;
        // weights 3:2:1 -> about 30000, 20000, 10000
        assertTrue(Math.abs(counts[1] - 30_000) < 1_500, "I " + counts[1]);
        assertTrue(Math.abs(counts[2] - 20_000) < 1_500, "II " + counts[2]);
        assertTrue(Math.abs(counts[3] - 10_000) < 1_500, "III " + counts[3]);
    }

    @Test
    void singleLevelIsAlwaysOne() {
        assertEquals(1, ProbabilityUtil.rollLevel(1));
        assertEquals(1, ProbabilityUtil.rollLevel(0));
    }
}
