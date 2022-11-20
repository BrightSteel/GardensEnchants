package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToolEnchant extends CustomEnchantment {

    private String tool;

    public ToolEnchant(String name, String key, EnchantRarity rarity) {
        super(name, key, rarity);
    }

    public ToolEnchant(String name, String key, EnchantRarity rarity, String tool) {
        super(name, key, rarity);
        this.tool = tool;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        if (tool != null) {
            return itemStack.getType().name().endsWith(tool.toUpperCase());
        }
        return true;
    }
}
