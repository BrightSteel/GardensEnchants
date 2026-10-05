package com.gardensmc.gardensenchants.utils;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Mimics what using a vanilla anvil does to the block, for anvil actions we finish outside the
 * anvil's own inventory.
 */
public final class AnvilUtil {

    // vanilla's chance of an anvil getting damaged per use
    private static final double DAMAGE_PERCENT = 12;
    // vanilla closes an anvil once the player is more than 8 blocks away
    private static final double MAX_DISTANCE_SQUARED = 8 * 8;

    private AnvilUtil() {
    }

    /**
     * The anvil block at the location, or null if there isn't one (e.g. a virtual anvil from a command).
     */
    @Nullable
    public static Block getAnvil(@Nullable Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        var block = location.getBlock();
        return isAnvil(block.getType()) ? block : null;
    }

    /**
     * Whether the player can still use the anvil they started at. Always true without a real anvil.
     */
    public static boolean canStillUse(Player player, @Nullable Location anvilLocation) {
        if (anvilLocation == null) {
            return true;
        }
        return getAnvil(anvilLocation) != null
                && player.getWorld().equals(anvilLocation.getWorld())
                && player.getLocation().distanceSquared(anvilLocation.toCenterLocation()) <= MAX_DISTANCE_SQUARED;
    }

    /**
     * Uses the anvil like vanilla does: plays the sound and, outside creative, may damage or break it.
     */
    public static void use(Player player, @Nullable Location anvilLocation) {
        var anvil = getAnvil(anvilLocation);
        if (anvil == null) {
            return;
        }
        if (player.getGameMode() != GameMode.CREATIVE && ProbabilityUtil.roll(DAMAGE_PERCENT)) {
            var damaged = getDamaged(anvil.getType());
            if (damaged == null) {
                anvil.setType(Material.AIR);
                anvil.getWorld().playEffect(anvil.getLocation(), Effect.ANVIL_BREAK, 0);
                return;
            }
            var data = damaged.createBlockData();
            if (data instanceof Directional directional && anvil.getBlockData() instanceof Directional old) {
                directional.setFacing(old.getFacing());
            }
            anvil.setBlockData(data);
        }
        anvil.getWorld().playEffect(anvil.getLocation(), Effect.ANVIL_USE, 0);
    }

    private static boolean isAnvil(Material material) {
        return material == Material.ANVIL || material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL;
    }

    @Nullable
    private static Material getDamaged(Material anvil) {
        return switch (anvil) {
            case ANVIL -> Material.CHIPPED_ANVIL;
            case CHIPPED_ANVIL -> Material.DAMAGED_ANVIL;
            default -> null;
        };
    }
}
