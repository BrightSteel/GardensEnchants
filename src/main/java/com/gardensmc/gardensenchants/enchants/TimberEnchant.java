package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TimberEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new TimberEnchant("gardens:timber", EnchantRarity.ORDINARY);

    public TimberEnchant(String bookNameSpace, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, "timber"), bookNameSpace, rarity);
    }

    @NotNull
    @Override
    public String getName() {
        return "Timber";
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }


    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().name().endsWith("AXE");
    }
}
