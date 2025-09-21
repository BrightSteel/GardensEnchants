package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import java.util.*;
import java.util.stream.Collectors;

import static com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler.DEFAULT_SLOTS;

public class LootListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInteractLootChest(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var block = e.getClickedBlock();
            if (block != null && block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                if (chest instanceof Lootable lootable && lootable.getLootTable() != null) {
                    var inventory = chest.getBlockInventory();
                    inventory.setStorageContents(getLootContents(
                            lootable.getLootTable(),
                            inventory.getSize(),
                            new LootContext
                                    .Builder(chest.getLocation())
                                    .build())
                    );
                    lootable.clearLootTable();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLootChestBreak(BlockBreakEvent e) {
        var block = e.getBlock();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            if (chest instanceof Lootable lootable && lootable.getLootTable() != null) {
                e.setDropItems(false);
                var loot = getLootContents(lootable.getLootTable(), 0, new LootContext
                        .Builder(chest.getLocation())
                        .build()
                );
                var tool = PlayerUtil.getHeldItem(e.getPlayer());
                if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
                    PlayerUtil.giveItems(e.getPlayer(), List.of(loot));
                } else {
                    Arrays.stream(loot).forEach(itemStack -> block.getWorld().dropItemNaturally(
                            block.getLocation(),
                            itemStack
                    ));
                }
            }
        }
    }

    private ItemStack[] getLootContents(LootTable lootTable, int invSize, LootContext lootContext) {
        var air = new ItemStack(Material.AIR);
        var loot = lootTable.populateLoot(null, lootContext)
                .stream()
                .map(itemStack -> {
                    if (itemStack.getType() == Material.ENCHANTED_BOOK) {
                        return VillagerListener.getCustomEnchantBook(itemStack);
                    } else if (!itemStack.getEnchantments().isEmpty()) {
                        var clone = itemStack.clone();
                        var enchantHandler = new GardensEnchantHandler(clone);
                        enchantHandler.applyPostEnchantChanges();
                        int enchantCount = enchantHandler.getEnchantments().size();
                        if (enchantCount > DEFAULT_SLOTS) {
                            enchantHandler.setSlots(enchantCount);
                        }
                        return clone;
                    }
                    return itemStack;
                })
                .collect(Collectors.toList());
        var remainingSlots = invSize - loot.size();
        for (int i = 0; i < remainingSlots; i++) {
            loot.add(air.clone());
        }
        Collections.shuffle(loot);
        return loot.toArray(ItemStack[]::new);
    }
}
