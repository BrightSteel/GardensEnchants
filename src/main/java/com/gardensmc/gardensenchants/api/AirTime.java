package com.gardensmc.gardensenchants.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enchants that hold a player up in the air (Lift, Drift, Updraft, Grapple, Tidecaller, Rocket Boots, Weathervane,
 * Tethered) mark them here for a few seconds, so a flying check (GardensPlayers') doesn't take it for flying.
 */
public final class AirTime {

    /** How long after an enchant last helped them the player may still be up, in ticks. */
    public static final int GRACE_TICKS = 60;

    private static final Map<UUID, Integer> allowedUntil = new ConcurrentHashMap<>();

    private AirTime() {
    }

    /** The player may be in the air, by an enchant, for the next {@link #GRACE_TICKS}. */
    public static void allow(Player player) {
        allowedUntil.put(player.getUniqueId(), Bukkit.getCurrentTick() + GRACE_TICKS);
    }

    public static boolean isAllowed(Player player) {
        var until = allowedUntil.get(player.getUniqueId());
        if (until == null) return false;
        if (Bukkit.getCurrentTick() <= until) return true;
        allowedUntil.remove(player.getUniqueId(), until);
        return false;
    }
}
