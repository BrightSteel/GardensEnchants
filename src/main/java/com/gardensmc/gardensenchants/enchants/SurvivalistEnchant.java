package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SurvivalistEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new SurvivalistEnchant("gardens:survivalist", EnchantRarity.ORDINARY);

    public SurvivalistEnchant(String bookNameSpace, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, "survivalist"), bookNameSpace, rarity);
    }

    @NotNull
    @Override
    public String getName() {
        return "Survivalist";
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
