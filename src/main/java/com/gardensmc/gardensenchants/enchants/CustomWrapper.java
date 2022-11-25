package com.gardensmc.gardensenchants.enchants;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomWrapper extends CustomEnchantment {

    private final Enchantment enchantment;

    public CustomWrapper(Enchantment enchantment, String key, EnchantRarity rarity) {
        super(key, rarity);
        this.enchantment = enchantment;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return enchantment.getItemTarget();
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return enchantment.canEnchantItem(itemStack);
    }

    @Override
    public int getMaxLevel() {
        return enchantment.getMaxLevel();
    }

    @Override
    public int getStartLevel() {
        return enchantment.getStartLevel();
    }
}
