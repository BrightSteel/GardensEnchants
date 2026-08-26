package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GrindstoneListener implements Listener {

    @EventHandler
    public void onGrindstoneRemoveEnchantsFromTool(PrepareGrindstoneEvent e) {
        var result = e.getResult();
        var inv = e.getInventory();
        if (result == null) {
            // might only have custom enchants
            if (!isEmpty(inv.getUpperItem()) && isEmpty(inv.getLowerItem())) {
                result = inv.getUpperItem().clone();
            } else if (isEmpty(inv.getUpperItem()) && !isEmpty(inv.getLowerItem())) {
                result = inv.getLowerItem().clone();
            }
        }
        if (result != null) {
            var enchantHandler = new GardensEnchantHandler(result);
            if (enchantHandler.hasEnchantNBT()) {
                enchantHandler.clearEnchants(true);
                if (!isEmpty(inv.getUpperItem()) && !isEmpty(inv.getLowerItem())) {
                    var slots1 = new GardensEnchantHandler(inv.getUpperItem()).getSlots();
                    var slots2 = new GardensEnchantHandler(inv.getLowerItem()).getSlots();
                    // take larger of two slots
                    enchantHandler.setSlots(Math.max(slots1, slots2));
                }
                e.setResult(result);
            }
        }
    }

    @EventHandler
    public void onGrindstonePrepare(PrepareGrindstoneEvent e) {
        var inv = e.getInventory();
        ItemStack item = null;
        if (isEmpty(inv.getUpperItem()) && !isEmpty(inv.getLowerItem())) {
            item = inv.getLowerItem();
        } else if (isEmpty(inv.getLowerItem()) && !isEmpty(inv.getUpperItem())) {
            item = inv.getUpperItem();
        }
        if (item != null) {
            if (BooksHandler.INSTANCE.getBookInstance(item) != null) {
                e.setResult(new ItemStack(Material.BOOK));
            }
        }
    }

    @EventHandler
    public void onGrindstoneClick(InventoryClickEvent e) {
        if (e.getClick().isShiftClick()) return;
        if (e.getClickedInventory() instanceof GrindstoneInventory grindstone) {
            var cursor = e.getView().getCursor();
            if (!isEmpty(cursor)) {
                var book = BooksHandler.INSTANCE.getBookInstance(cursor);
                if (book != null) {
                    var clonedCursor = cursor.clone();
                    var slotItem = grindstone.getItem(e.getSlot());
                    var clonedSlotItem = slotItem == null ? null : slotItem.clone();
                    Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
                        // swap cursor with slot
                        grindstone.setItem(e.getSlot(), clonedCursor);
                        e.getView().setCursor(clonedSlotItem);
                    }, 1);
                }
            }
        }
    }

    @EventHandler
    public void onShiftClickIntoGrindstone(InventoryClickEvent e) {
        if (e.getClick() != ClickType.SHIFT_LEFT && e.getClick() != ClickType.SHIFT_RIGHT) {
            return;
        }
        if (e.getClickedInventory() instanceof PlayerInventory playerInventory && e.getView().getTopInventory() instanceof GrindstoneInventory grindstone) {
            int slot;
            if (isEmpty(grindstone.getUpperItem())) {
                slot = 0;
            } else if (isEmpty(grindstone.getLowerItem())) {
                slot = 1;
            } else {
                slot = -1;
            }
            if (slot > -1) {
                var item = playerInventory.getItem(e.getSlot());
                if (!isEmpty(item) && BooksHandler.INSTANCE.getBookInstance(item) != null) {
                    grindstone.setItem(slot, item);
                    playerInventory.setItem(e.getSlot(), null);
                }
            }
        }
    }

    private boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }
}
