package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ToolEnchant extends CustomEnchantment {

    private String tool;

    public ToolEnchant(String key, String display_name, EnchantRarity rarity) {
        super(key, display_name, rarity);
    }

    public ToolEnchant(String key, String display_name, EnchantRarity rarity, String tool) {
        super(key, display_name, rarity);
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
        else {
            String[] tools = new String[]{"SWORD", "AXE", "HOE", "SHOVEL", "TRIDENT"};
            String name = itemStack.getType().name();
            return Arrays.stream(tools).anyMatch(name::endsWith);
        }

    }
}
