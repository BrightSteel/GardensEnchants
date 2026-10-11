package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Shift-clicking books from your inventory onto the books already in the enchanting menu's slot. Runs before
 * InventoryGui's own handling (HIGHEST), which would otherwise look for an empty slot.
 */
public class BookStackListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShiftClick(InventoryClickEvent e) {
        if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
        var top = e.getView().getTopInventory();
        if (e.getRawSlot() < top.getSize()) return; // from the player's own inventory only
        var clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.BOOK) return;
        var menu = EnchantingMenu.of(top);
        if (menu == null) return;
        var books = clicked.clone();
        if (!menu.addBooks(books)) return;
        e.setCancelled(true);
        e.setCurrentItem(books.getAmount() > 0 ? books : null);
    }
}
