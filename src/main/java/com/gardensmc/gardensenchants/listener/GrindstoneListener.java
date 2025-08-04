package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;


public class GrindstoneListener implements Listener {

    @EventHandler
    public void onGrindstonePrepare(PrepareGrindstoneEvent e) {
        var inv = e.getInventory();

//        if (!isEmpty(e.getView().getCursor()) && BooksHandler.INSTANCE.getBookInstance(e.getView().getCursor()) != null && isEmpty(inv.getLowerItem()) && isEmpty(inv.getUpperItem())) {
//            var clone = e.getView().getCursor().clone();
//            e.getView().setCursor(null);
//            inv.setUpperItem(clone);
//            e.setResult(new ItemStack(Material.BOOK));
//        }
    }

    private boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }
}
