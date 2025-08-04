package com.gardensmc.gardensenchants.utils;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
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

    /**
     * Copied from {@linkplain org.bukkit.craftbukkit.inventory.CraftInventoryView#sendInventoryTitleChange(InventoryView, String)}
     * and updated to accept a Component instead of plain text.
     * Note this does not update the CraftInventoryView's title variable, so all following calls to {@linkplain InventoryView#title()} will be inaccurate!
     *
     * @param view InventoryView to update
     * @param title Component to set inventory title to.
     */
    public static void sendInventoryTitleChange(InventoryView view, Component title) {
        Preconditions.checkArgument(view != null, "InventoryView cannot be null");
        Preconditions.checkArgument(title != null, "Title cannot be null");
        Preconditions.checkArgument(view.getPlayer() instanceof Player, "NPCs are not currently supported for this function");
        Preconditions.checkArgument(view.getTopInventory().getType().isCreatable(), "Only creatable inventories can have their title changed");
        ServerPlayer player = (ServerPlayer)((CraftHumanEntity)view.getPlayer()).getHandle();
        int containerId = player.containerMenu.containerId;
        net.minecraft.world.inventory.MenuType<?> windowType = CraftContainer.getNotchInventoryType(view.getTopInventory());
        player.connection.send(new ClientboundOpenScreenPacket(containerId, windowType, CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(title))));
        player.containerMenu.sendAllDataToRemote();
    }

}
