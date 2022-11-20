package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import com.gardensmc.gardensenchants.utils.ItemUtils;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;

public class Events implements Listener {

    @EventHandler
    public static void onRepair(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        if (contents.length > 1) {

            if (contents[0] == null || contents[1] == null) return;

            ItemStack item = contents[0].clone();
            ItemStack book = contents[1];

            // our items adder category
            if (book.getType() != Material.GOLDEN_HORSE_ARMOR) return;

            CustomStack customStack = CustomStack.byItemStack(book);
            if (customStack == null) return;

            Enchantment enchantment = CustomEnchantments.getGardensEnchant(customStack.getNamespacedID());
            if (enchantment == null) return;

            if (enchantment.canEnchantItem(item)) {
                EnchantUtils.enchantItem(item, enchantment, EnchantUtils.getLevelFromName(book));
                inv.setRepairCost(1);
                e.setResult(item);
            }
        }
    }

    @EventHandler
    public void onVeinMine(PlayerVeinMineEvent e) {
        ItemStack itemStack = e.getItem();

        String block_name = e.getBlock().getType().name();
        boolean isWood =  (block_name.endsWith("WOOD") || block_name.endsWith("LOG") || block_name.endsWith("ROOTS"));
        if (itemStack != null) {
            String item_name = itemStack.getType().name();
            if (isWood) {
                if (item_name.endsWith("AXE")) {
                    if (EnchantUtils.hasEnchantment(itemStack, CustomEnchantments.TIMBER)) {
                        return;
                    }
                }
            }
            else {
                if (item_name.endsWith("PICKAXE")) {
                    if (EnchantUtils.hasEnchantment(itemStack, CustomEnchantments.VEIN_MINER)) {
                        return;
                    }
                }
            }
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        if (EnchantUtils.hasEnchantment(e.getItem(), CustomEnchantments.ETERNAL)) {
            int ench_level = EnchantUtils.getEnchantLevel(e.getItem(), CustomEnchantments.ETERNAL);
            if (ProbabilityUtil.roll((ench_level*10)*2)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.SKULL_STEALER)) {
                int ench_level = EnchantUtils.getEnchantLevel(weapon, CustomEnchantments.SKULL_STEALER);
                int percent = ench_level == 1 ? 15 : 30;
                if (ProbabilityUtil.roll(percent)) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), ItemUtils.getHead(e.getEntity().getUniqueId()));
                }
            }
        }
    }
}
