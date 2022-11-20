package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LifeStealEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new LifeStealEnchant("gardens:life_steal", EnchantRarity.UNIQUE);

    public LifeStealEnchant(String bookNameSpace, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, "life_steal"), bookNameSpace, rarity);
    }

    @NotNull
    @Override
    public String getName() {
        return "Life Steal";
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
