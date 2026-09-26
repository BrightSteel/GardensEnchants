package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensfurniture.util.ProtectionUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shared block breaking for Timber, Vein Miner and Super Breaker.
 */
public final class AreaBreaker {

    public static final int DEFAULT_MAX_BLOCKS = 64;
    private static final String MAX_BLOCKS_PATH = "areaBreak.maxBlocks";

    // players whose extra blocks are being broken right now. player.breakBlock fires a nested
    // BlockBreakEvent, and area-break handlers must ignore those so enchants don't chain.
    private static final Set<UUID> breakingPlayers = new HashSet<>();

    private AreaBreaker() {
    }

    public static boolean isBreaking(Player player) {
        return breakingPlayers.contains(player.getUniqueId());
    }

    public static int getMaxBlocks() {
        return Math.max(0, GardensEnchants.plugin.getConfig().getInt(MAX_BLOCKS_PATH, DEFAULT_MAX_BLOCKS));
    }

    /**
     * Breaks the candidates as the player, skipping the origin and duplicates, up to the configured cap.
     */
    public static void breakBlocks(Player player, Block origin, Iterable<Block> candidates) {
        var targets = AreaBreakTargets.select(origin, candidates, getMaxBlocks());
        if (targets.isEmpty() || !breakingPlayers.add(player.getUniqueId())) {
            return;
        }
        try {
            for (Block target : targets) {
                if (target.getType().isAir() || !ProtectionUtil.canBuild(target.getLocation(), player)) {
                    continue;
                }
                player.breakBlock(target);
            }
        } finally {
            breakingPlayers.remove(player.getUniqueId());
        }
    }
}
