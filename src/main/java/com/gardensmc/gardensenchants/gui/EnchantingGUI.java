package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.EnchantCache;
import com.gardensmc.gardensenchants.database.PlayerTable;
import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.EnchantRarity;
import com.gardensmc.gardensenchants.enchants.FontMappings;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import com.gardensmc.gardensenchants.utils.ItemUtils;
import com.gardensmc.gardensenchants.utils.OraxenShift;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import de.themoep.inventorygui.InventoryGui;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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

    public EnchantingGUI(ItemStack book) {
        this.book = book;
        String sample = FontMappings.INSTANCE.replaceAll("Bubble Head I", FontMappings.INSTANCE.fontMap1);
        String ninty = OraxenShift.INSTANCE.getShift(90, true);
        title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE05F&f" + ninty + sample);

    }

    private void initializeItems(Inventory inv) {
        ItemStack blankItem = ItemUtils.getBlankItem();
//        int[] blanks = new int[]{0, 2, 9, 10, 11, 18, 20};
        int[] blanks = new int[]{0, 2};
        for (int blank : blanks) {
            inv.setItem(blank, blankItem);
        }

        if (book != null) {
            // btw this does nothing, only run on initial opening...
            ItemStack[] books = EnchantUtils.getRandomBooks(player.getUniqueId());

            inv.setItem(8, books[0]);
            inv.setItem(17, books[1]);
            inv.setItem(26, books[2]);

            int j = 0;
            for (ItemStack book : books) {
                ItemStack display = blankItem.clone();
                ItemMeta displayMeta = display.getItemMeta();
                ItemMeta bookMeta = (book != null) ? book.getItemMeta() : null;
                if (displayMeta != null && bookMeta != null) {
                    displayMeta.setDisplayName(bookMeta.getDisplayName());
                    displayMeta.setLore(bookMeta.getLore());
                    display.setItemMeta(displayMeta);
                }
                for (int i = 3 + (j * 9); i < 8 + (j * 9); i++) {
                    inv.setItem(i, display);
                }
                j++;
            }
        }

        ItemStack stack = ItemUtils.getBlankItem();
        ItemMeta meta = stack.getItemMeta();
        EnchantCache cache = new EnchantCache(player.getUniqueId());
        LocalDateTime expiration = cache.getExpiration();
        long duration = Duration.between(LocalDateTime.now(Clock.systemUTC()), expiration).toMillis();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "View Enchants");
            List<String> lore = new ArrayList<>();
//            if (lore != null) {
                lore.add("");
                lore.add(ChatColor.WHITE + "Current table changes in ");
                lore.add(ChatColor.LIGHT_PURPLE + "" + TimeUnit.MILLISECONDS.toHours(duration) + " hr");
                meta.setLore(lore);
//            }
            stack.setItemMeta(meta);
        }
        inv.setItem(19, stack);
    }

    private ItemStack createItem(ItemStack stack, String name, String... lore) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        stack.setItemMeta(meta);
        return stack;
    }


    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 27, title);
        initializeItems(inv);
        return inv;
    }
}
