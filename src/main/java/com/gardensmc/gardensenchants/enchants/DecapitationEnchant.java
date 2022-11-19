package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DecapitationEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new DecapitationEnchant("gardens:decapitation");

    public DecapitationEnchant(String bookNameSpace) {
        super(new NamespacedKey(GardensEnchants.plugin, "decapitation"), bookNameSpace);
    }

    @NotNull
    @Override
    public String getName() {
        return "Decapitation";
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        String name = itemStack.getType().name();
        return name.endsWith("SWORD") || name.endsWith("AXE");
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
