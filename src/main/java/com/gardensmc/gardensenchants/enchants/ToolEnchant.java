package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToolEnchant extends CustomEnchantment {

    private String tool;

    public ToolEnchant(String key, EnchantRarity rarity) {
        super(key, rarity);
    }

    public ToolEnchant(String key, EnchantRarity rarity, String tool) {
        super(key, rarity);
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
