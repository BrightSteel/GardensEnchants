package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EternalEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new EternalEnchant("gardens:eternal");

    public EternalEnchant(String bookNameSpace) {
        super(new NamespacedKey(GardensEnchants.plugin, "eternal"), bookNameSpace);
    }

    @NotNull
    @Override
    public String getName() {
        return "Eternal";
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}
