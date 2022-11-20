package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeaponEnchant extends CustomEnchantment {

    public WeaponEnchant(String name, String key, EnchantRarity rarity) {
        super(name, new NamespacedKey(GardensEnchants.plugin, key), "gardens:" + key, rarity);
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
