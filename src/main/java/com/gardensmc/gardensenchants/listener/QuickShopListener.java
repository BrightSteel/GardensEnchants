package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensfurniture.GardensFurniture;
import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class QuickShopListener implements Listener {

    @EventHandler
    public void onQuickShop(ShopItemMatchEvent event) {
        if (event.matches()) {
            return;
        }
        var original = event.original();
        var comparison = event.comparison();
        if (!isValidMaterial(original) || !isValidMaterial(comparison)) {
            return;
        }
        var book1 = BooksHandler.INSTANCE.getBookInstance(original);
        if (book1 == null) {
            return;
        }
        var book2 = BooksHandler.INSTANCE.getBookInstance(comparison);
        if (book2 == null) {
            return;
        }
        event.matches(book1.isSame(book2));
    }

    private boolean isValidMaterial(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() == Material.ENCHANTED_BOOK;
    }
}
