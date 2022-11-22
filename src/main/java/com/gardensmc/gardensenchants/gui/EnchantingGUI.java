package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.FontMappings;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import com.gardensmc.gardensenchants.utils.ItemUtils;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import de.themoep.inventorygui.InventoryGui;
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

import java.util.Arrays;

public class EnchantingGUI implements InventoryHolder {

    private String title;
    private ItemStack book;

    public EnchantingGUI() {
        title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:\uE094&f");

    }

    public EnchantingGUI(ItemStack book) {
        this.book = book;
        String sample = FontMappings.INSTANCE.replaceAll("Bubble Head I", FontMappings.INSTANCE.fontMap1);
        title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:\uE05F:offset_-190:" + sample);

    }

    private void initializeItems(Inventory inv) {
        ItemStack blankItem = ItemUtils.getCustomItem("gardens:blank_item");
        int[] blanks = new int[]{0, 2, 9, 10, 11, 18, 20};
        for (int blank : blanks) {
            inv.setItem(blank, blankItem);
        }

        if (book != null) {
            ItemStack[] books = EnchantUtils.getRandomBooks();
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
