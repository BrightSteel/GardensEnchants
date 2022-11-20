package com.gardensmc.gardensenchants.enchants;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomEnchantment extends Enchantment {

    private final String bookNamespace;
    private EnchantRarity rarity;

    public CustomEnchantment(@NotNull NamespacedKey key, String bookNamespace, EnchantRarity rarity) {
        super(key);
        this.bookNamespace = bookNamespace;
        this.rarity = rarity;
    }

    public String getBookNamespace() {
        return bookNamespace;
    }

    @Nullable
    public ItemStack getBook() {
        CustomStack customStack = CustomStack.getInstance(bookNamespace);
        if (customStack != null) {
            return customStack.getItemStack();
        }

        return null;
    }

    public EnchantRarity getRarity() {
        return rarity;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }
}
