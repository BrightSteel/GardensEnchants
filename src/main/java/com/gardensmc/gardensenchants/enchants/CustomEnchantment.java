package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomEnchantment extends Enchantment {

    private String name;
    private final String bookNamespace;
    private EnchantRarity rarity;

    public CustomEnchantment(String name, String key, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, key));
        this.name = name;
        this.bookNamespace = "gardens:" + key;
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

    @NotNull
    @Override
    public String getName() {
        return name;
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
