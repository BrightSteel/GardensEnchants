package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent e) {
        var player = e.getPlayer();
        var tool = PlayerUtil.getHeldItem(player);
        if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
            e.setCancelled(true);
            PlayerUtil.giveItems(player, e.getItems().stream().map(Item::getItemStack).toList());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var player = e.getPlayer();
        var tool = PlayerUtil.getHeldItem(player);
        if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
            player.giveExp(e.getExpToDrop());
            e.setExpToDrop(0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.SPAWNER) {
            ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
            if (CustomEnchantments.SILK_TOUCH_SPAWNER.hasEnchantment(tool)) {
                NBTTileEntity tileEntity = new NBTTileEntity(e.getBlock().getState());
                NBTCompound nbtCompound = tileEntity.getCompound("SpawnData");
                String entity_id = nbtCompound.getCompound("entity").getString("id");
                String entity_name = entity_id.split(":")[1];

                ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
                ItemMeta meta = spawnerItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.BLUE + entity_name.substring(0, 1).toUpperCase() + entity_name.substring(1) + " Spawner");
                    spawnerItem.setItemMeta(meta);
                }

                NBTItem nbtItem = new NBTItem(spawnerItem);
                nbtItem.setString("entity_id", entity_id);
                e.getPlayer().getWorld().dropItemNaturally(e.getBlock().getLocation(), nbtItem.getItem());
                e.setExpToDrop(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.SPAWNER) {
            NBTItem spawnerItem = new NBTItem(e.getItemInHand());

            if (!spawnerItem.hasKey("entity_id")) return;
            String entity_id = spawnerItem.getString("entity_id");

            NBT.modify(e.getBlockPlaced().getState(), nbt -> {
                nbt.getOrCreateCompound("SpawnData").getOrCreateCompound("entity").setString("id", entity_id);
            });
        }
    }
}
