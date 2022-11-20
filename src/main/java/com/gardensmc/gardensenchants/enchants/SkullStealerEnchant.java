package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SkullStealerEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new SkullStealerEnchant("gardens:skull_stealer", EnchantRarity.ORDINARY);

    public SkullStealerEnchant(String bookNameSpace, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, "skull_stealer"), bookNameSpace, rarity);
    }

    @NotNull
    @Override
    public String getName() {
        return "Skull Stealer";
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
