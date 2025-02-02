package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class RestorationRune extends RuneHandler {

    public RestorationRune(ItemStack rune) {
        super(rune, EnchantRestricts.ENCHANTED_BOOK);
    }

    @Override
    public int getApplyCost(ItemStack target) {
        var bookInstance = BooksHandler.INSTANCE.getBookInstance(target);
        if (bookInstance == null) {
            throw new RuntimeException("Could not get book instance to calculate rune cost!");
        }
        return bookInstance.enchantment().getRarity().getBaseCost() / 2;
    }

    @Override
    public ItemStack apply(ItemStack target) {
        var bookInstance = Objects.requireNonNull(BooksHandler.INSTANCE.getBookInstance(target.clone()));
        BooksHandler.INSTANCE.applyRestoration(bookInstance, 1);
        return bookInstance.book();
    }

    @Override
    public boolean canApply(ItemStack target) {
        if (!super.canApply(target)) {
            return false;
        }
        var bookInstance = BooksHandler.INSTANCE.getBookInstance(target);
        if (bookInstance == null || bookInstance.restorations() == BooksHandler.MAX_RESTORATIONS) {
            return false;
        }
        return bookInstance.calculateDestructionChance() > 0;
    }
}
