package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
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

    // Vanilla refuses custom books in the grindstone inputs, so place them ourselves.
    // Everything happens synchronously on a cancelled click and only ever touches input slots 0/1:
    // a delayed swap here used to let players clone the result slot.
    @EventHandler(ignoreCancelled = true)
    public void onGrindstoneClick(InventoryClickEvent e) {
        if (e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT) return;
        if (!(e.getClickedInventory() instanceof GrindstoneInventory grindstone)) return;
        int slot = e.getRawSlot();
        if (slot != 0 && slot != 1) return;
        var cursor = e.getView().getCursor();
        if (isEmpty(cursor) || BooksHandler.INSTANCE.getBookInstance(cursor) == null) return;

        e.setCancelled(true);
        var slotItem = grindstone.getItem(slot);
        if (isEmpty(slotItem)) {
            grindstone.setItem(slot, cursor.asOne());
            e.getView().setCursor(cursor.getAmount() > 1 ? cursor.asQuantity(cursor.getAmount() - 1) : null);
        } else if (cursor.getAmount() == 1) {
            // plain swap, as vanilla would do for any other item
            grindstone.setItem(slot, cursor.clone());
            e.getView().setCursor(slotItem.clone());
        }
    }

    @EventHandler(ignoreCancelled = true)
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
                return;
            }
            var item = playerInventory.getItem(e.getSlot());
            if (!isEmpty(item) && BooksHandler.INSTANCE.getBookInstance(item) != null) {
                e.setCancelled(true);
                // one book only: taking the result clears the whole input slot
                grindstone.setItem(slot, item.asOne());
                playerInventory.setItem(e.getSlot(), item.getAmount() > 1 ? item.asQuantity(item.getAmount() - 1) : null);
            }
        }
    }

    private boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }
}
