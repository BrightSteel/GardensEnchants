package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeaponEnchant extends CustomEnchantment {

    public WeaponEnchant(String key, String display_name, EnchantRarity rarity) {
        super(key, display_name, rarity);
    }

    public WeaponEnchant(String key, String display_name, EnchantRarity rarity, int max_level) {
        super(key, display_name, rarity, max_level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().name().endsWith("SWORD") || itemStack.getType().name().endsWith("AXE");
    }
}
