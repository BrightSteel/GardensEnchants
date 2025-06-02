package com.gardensmc.gardensenchants.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlayerUtil {

    /**
     * Gets the BlockFace of the block the player is currently targeting.
     *
     * @param player the player's whos targeted blocks BlockFace is to be checked.
     * @return the BlockFace of the targeted block, or null if the targeted block is non-occluding.
     */
    public static BlockFace getBlockFaceLooking(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) return null;
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

    public static void giveItems(Player player, Collection<ItemStack> items) {
        var emptySlots = getEmptyStorageSlots(player);
        if (items.size() <= emptySlots) {
            // presumably faster than individually adding each item?
            player.getInventory().addItem(items.toArray(ItemStack[]::new));
        } else {
            for (ItemStack item : items) {
                if (emptySlots > 0) {
                    emptySlots--;
                    player.getInventory().addItem(item);
                } else {
                    // drop by player
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
    }

    @Nullable
    public static ItemStack getHeldItem(Player player) {
        var item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            return null;
        }
        return item;
    }

    private static int getEmptyStorageSlots(Player player) {
        return (int) Arrays.stream(player.getInventory().getStorageContents())
                .filter(Objects::isNull)
                .count();
    }

}
