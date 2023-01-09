package com.gardensmc.gardensenchants.enchants;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class UniversalEnchant extends CustomEnchantment {

    private String tool;

    public UniversalEnchant(String key, String display_name, EnchantRarity rarity) {
        super(key, display_name, rarity);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        if (tool != null) {
            return itemStack.getType().name().endsWith(tool.toUpperCase());
        }
        else {
            String[] tools = new String[]{"SWORD", "AXE", "HOE", "SHOVEL", "TRIDENT", "HELMET", "CHESTPLATE","LEGGINGS", "BOOTS"};
            String name = itemStack.getType().name();
            return Arrays.stream(tools).anyMatch(name::endsWith);
        }

    }
}
