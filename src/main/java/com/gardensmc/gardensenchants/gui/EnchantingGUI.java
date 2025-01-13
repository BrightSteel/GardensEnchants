package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EnchantingGUI implements InventoryHolder {

    private Player player;

    private String title;
    private ItemStack book;

    public EnchantingGUI(Player player) {
        title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE094&f");
        this.player = player;
    }

    private void initializeItems(Inventory inv) {
        ItemStack blankItem = ItemUtils.getBlankItem();
        int[] blanks = new int[]{0, 2};
        for (int blank : blanks) {
            inv.setItem(blank, blankItem);
        }

        ItemStack stack = ItemUtils.getBlankItem();
        ItemMeta meta = stack.getItemMeta();
//        EnchantCache cache = new EnchantCache(player.getUniqueId());
//        LocalDateTime expiration = cache.getExpiration();
//        long duration = Duration.between(LocalDateTime.now(Clock.systemUTC()), expiration).toMillis();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "View Enchants");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.WHITE + "Current table changes in ");
//            lore.add(ChatColor.LIGHT_PURPLE + "" + TimeUnit.MILLISECONDS.toHours(duration) + " hr");
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        inv.setItem(19, stack);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 27, title);
        initializeItems(inv);
        return inv;
    }
}
