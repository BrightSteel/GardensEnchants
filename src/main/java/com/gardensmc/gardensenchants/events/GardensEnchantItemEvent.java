package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.books.BookInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player enchants an item with a Gardens enchant book in an anvil.
 */
public class GardensEnchantItemEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ItemStack item;
    private final BookInstance book;

    public GardensEnchantItemEvent(Player player, ItemStack item, BookInstance book) {
        this.player = player;
        this.item = item;
        this.book = book;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public BookInstance getBook() {
        return book;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
