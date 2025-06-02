package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;

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
}
