package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stops fishing for one enchant: each re-roll, or buy of the cheapest offer (which rolls new offers too, and with a
 * grindstone is a re-roll), heats the player's enchanting table. Too many in a short time and it overheats, and
 * no table enchants for them until it cools. Kept per player in memory only, so a restart cools every table.
 * <p>
 * Config (all optional): {@code table-heat.enabled}, {@code max-rolls}, {@code window-seconds},
 * {@code cooldown-seconds}.
 */
public final class TableHeat {

    private static final String PATH = "table-heat.";
    private static final int DEFAULT_MAX_ROLLS = 8;
    private static final int DEFAULT_WINDOW_SECONDS = 300;
    private static final int DEFAULT_COOLDOWN_SECONDS = 180;
    /** How many rolls short of overheating the re-roll button starts warning. */
    private static final int WARM_MARGIN = 2;

    private static final Map<UUID, Deque<Long>> rolls = new HashMap<>();
    private static final Map<UUID, Long> cooledAt = new HashMap<>();

    private TableHeat() {
    }

    private static boolean enabled() {
        return GardensEnchants.plugin.getConfig().getBoolean(PATH + "enabled", true);
    }

    private static int maxRolls() {
        return Math.max(1, GardensEnchants.plugin.getConfig().getInt(PATH + "max-rolls", DEFAULT_MAX_ROLLS));
    }

    private static long windowMs() {
        return GardensEnchants.plugin.getConfig().getInt(PATH + "window-seconds", DEFAULT_WINDOW_SECONDS) * 1000L;
    }

    private static long cooldownMs() {
        return GardensEnchants.plugin.getConfig().getInt(PATH + "cooldown-seconds", DEFAULT_COOLDOWN_SECONDS) * 1000L;
    }

    /** How long until the player's table has cooled, or 0 when it isn't overheated. */
    public static long remainingMs(UUID player) {
        if (!enabled()) {
            return 0;
        }
        var until = cooledAt.get(player);
        if (until == null) {
            return 0;
        }
        long left = until - System.currentTimeMillis();
        if (left <= 0) {
            cooledAt.remove(player);
            return 0;
        }
        return left;
    }

    /** Whether the player is a couple of rolls away from overheating. */
    public static boolean isWarm(UUID player) {
        return enabled() && recentRolls(player, System.currentTimeMillis()) >= maxRolls() - WARM_MARGIN;
    }

    /** Counts a roll; overheats the table when it's one too many. */
    public static void roll(UUID player) {
        if (!enabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        recentRolls(player, now);
        var times = rolls.computeIfAbsent(player, k -> new ArrayDeque<>());
        times.addLast(now);
        if (times.size() >= maxRolls()) {
            times.clear();
            cooledAt.put(player, now + cooldownMs());
        }
    }

    /** Drops rolls older than the window and counts the rest. */
    private static int recentRolls(UUID player, long now) {
        var times = rolls.get(player);
        if (times == null) {
            return 0;
        }
        while (!times.isEmpty() && now - times.peekFirst() > windowMs()) {
            times.removeFirst();
        }
        if (times.isEmpty()) {
            rolls.remove(player);
            return 0;
        }
        return times.size();
    }

    /** The overheated message, with the time left as m:ss. */
    public static String message(long remainingMs) {
        long seconds = (remainingMs + 999) / 1000;
        return "<red>Your enchanting table overheated! Give it <white>" + seconds / 60 + ":"
                + String.format("%02d", seconds % 60) + "<red> to cool down.";
    }
}
