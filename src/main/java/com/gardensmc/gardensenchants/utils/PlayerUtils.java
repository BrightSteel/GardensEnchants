package com.gardensmc.gardensenchants.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.EnchantRarity;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerUtils {


    public static void updateGuiTitlePacket(HumanEntity player) {
//        Bukkit.getpl
//        int id = player.getOpenInventory().getTopInventory()
//        PacketContainer pc = GardensEnchants.protocolManager.createPacket(PacketType.Play.Server.OPEN_WINDOW);

    }

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

}
