package com.gardensmc.gardensenchants.events;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Picks which blocks an area-break enchant may break. Kept free of Bukkit types so it can be unit tested.
 */
public final class AreaBreakTargets {

    private AreaBreakTargets() {
    }

    /**
     * Returns the candidates in order with duplicates and the origin removed, capped at {@code max}.
     * The origin is excluded because it is already being broken by the event in progress.
     */
    public static <T> List<T> select(T origin, Iterable<T> candidates, int max) {
        Set<T> seen = new LinkedHashSet<>();
        for (T candidate : candidates) {
            if (seen.size() >= max) {
                break;
            }
            if (!candidate.equals(origin)) {
                seen.add(candidate);
            }
        }
        return new ArrayList<>(seen);
    }
}
