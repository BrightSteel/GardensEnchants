package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchants.BubbleHeadEnchant;
import com.gardensmc.gardensenchants.enchants.DecapitationEnchant;
import com.gardensmc.gardensenchants.enchants.EternalEnchant;
import com.gardensmc.gardensenchants.enchants.VeinMinerEnchant;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import com.gardensmc.gardensenchants.utils.ItemUtils;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.ChatColor;
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
import org.bukkit.inventory.meta.ItemMeta;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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

            Enchantment enchantment = GardensEnchants.getGardensEnchant(customStack.getNamespacedID());
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
        if (itemStack != null && itemStack.getType().name().endsWith("PICKAXE")) {
            if (EnchantUtils.hasEnchantment(itemStack, VeinMinerEnchant.INSTANCE)) {
                return;
            }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        if (EnchantUtils.hasEnchantment(e.getItem(), EternalEnchant.INSTANCE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (EnchantUtils.hasEnchantment(weapon, DecapitationEnchant.INSTANCE)) {
                if (ProbabilityUtil.roll(30)) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), ItemUtils.getHead(e.getEntity().getUniqueId()));
                }
            }
        }
    }
}
