package com.gardensmc.gardensenchants.enchants;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BootsEnchant extends CustomEnchantment {

    public BootsEnchant(String key, String display_name, EnchantRarity rarity) {
        super(key, display_name, rarity);
    }

    public BootsEnchant(String key, String display_name, EnchantRarity rarity, int max_level) {
        super(key, display_name, rarity, max_level);
    }


    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().name().endsWith("BOOTS");
    }
}
