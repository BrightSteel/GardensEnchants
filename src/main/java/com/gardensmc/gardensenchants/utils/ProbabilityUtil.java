package com.gardensmc.gardensenchants.utils;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityUtil {

    public static boolean roll(double percent) {
        return ThreadLocalRandom.current().nextDouble() < percent / 100;
    }
}
