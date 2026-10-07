package com.gardensmc.gardensenchants.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

/**
 * The table's re-roll gets dearer with each re-roll in a row: a quarter more each time, so browsing stays cheap but
 * hunting one enchant costs real effort. The streak comes down one step for every quarter hour without re-rolling, so
 * waiting it out takes as long as it took to build. It's kept on the player, so it outlasts logging out and restarts.
 */
public final class RerollPricing {

    /** How much dearer each re-roll in a row is. */
    public static final double STEP = 1.25;
    /** How long without re-rolling takes the streak down one step. */
    public static final long DECAY_MS = 15 * 60 * 1000;
    /** The least a re-roll costs in acorns, however cheap the offers showing. */
    public static final int MIN_ACORNS = 40;

    /** A streak: how many re-rolls in a row, and when the last one was. */
    public record Streak(int rerolls, long lastAt) {
    }

    private RerollPricing() {
    }

    /** How many re-rolls in a row the streak counts for now, after coming down for the time since. */
    public static int rerolls(@Nullable Streak streak, long now) {
        if (streak == null) return 0;
        long steps = Math.max(0, now - streak.lastAt()) / DECAY_MS;
        return (int) Math.max(0, streak.rerolls() - steps);
    }

    /** The price now, from what it costs without a streak (and at least {@code min}). */
    public static int price(int base, int min, @Nullable Streak streak, long now) {
        return (int) Math.round(Math.max(base, min) * Math.pow(STEP, rerolls(streak, now)));
    }

    /** The streak after re-rolling now. */
    public static Streak rerolled(@Nullable Streak streak, long now) {
        return new Streak(rerolls(streak, now) + 1, now);
    }

    // ---- kept on the player ----

    private static final NamespacedKey REROLLS = new NamespacedKey("gardensenchants", "reroll_streak");
    private static final NamespacedKey LAST_AT = new NamespacedKey("gardensenchants", "reroll_last_at");

    @Nullable
    public static Streak of(Player player) {
        var data = player.getPersistentDataContainer();
        Integer rerolls = data.get(REROLLS, PersistentDataType.INTEGER);
        Long lastAt = data.get(LAST_AT, PersistentDataType.LONG);
        return rerolls == null || lastAt == null ? null : new Streak(rerolls, lastAt);
    }

    public static void save(Player player, Streak streak) {
        var data = player.getPersistentDataContainer();
        data.set(REROLLS, PersistentDataType.INTEGER, streak.rerolls());
        data.set(LAST_AT, PersistentDataType.LONG, streak.lastAt());
    }
}
