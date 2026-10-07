package com.gardensmc.gardensenchants.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The table's re-roll gets dearer with each re-roll in a row: a quarter more each time, so browsing stays cheap but
 * hunting one enchant costs real effort. The streak ends after a while without re-rolling, or on buying an offer that
 * cost at least what the next re-roll would (so buying a cheap book just to end it doesn't pay).
 */
public class RerollPricing {

    public static final RerollPricing INSTANCE = new RerollPricing();

    RerollPricing() {
    }

    /** How much dearer each re-roll in a row is. */
    public static final double STEP = 1.25;
    /** How long without re-rolling before the price is back to normal. */
    public static final long RESET_AFTER_MS = 30 * 60 * 1000;

    private record Streak(int rerolls, long lastAt) {
    }

    private final Map<UUID, Streak> streaks = new ConcurrentHashMap<>();

    /** The re-roll price now, from what it costs without a streak. */
    public int price(UUID player, int base, long now) {
        return (int) Math.round(base * Math.pow(STEP, rerolls(player, now)));
    }

    /** How many re-rolls in a row the player is on. */
    public int rerolls(UUID player, long now) {
        var streak = streaks.get(player);
        if (streak == null) return 0;
        if (now - streak.lastAt() >= RESET_AFTER_MS) {
            streaks.remove(player, streak);
            return 0;
        }
        return streak.rerolls();
    }

    public void rerolled(UUID player, long now) {
        streaks.put(player, new Streak(rerolls(player, now) + 1, now));
    }

    /** An offer was bought for {@code paid}, when the next re-roll would have cost {@code rerollPrice}. */
    public void bought(UUID player, int paid, int rerollPrice) {
        if (paid >= rerollPrice) streaks.remove(player);
    }
}
