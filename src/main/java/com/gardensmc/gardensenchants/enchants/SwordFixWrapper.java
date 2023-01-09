package com.gardensmc.gardensenchants.enchants;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SwordFixWrapper extends CustomWrapper {


    public SwordFixWrapper(Enchantment enchantment, String key, String display_name, EnchantRarity rarity) {
        super(enchantment, key, display_name, rarity);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().name().endsWith("SWORD");
    }
}
