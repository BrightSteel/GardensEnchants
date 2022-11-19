package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BubbleHeadEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new BubbleHeadEnchant("gardens:bubble_head");

    public BubbleHeadEnchant(String bookNameSpace) {
        super(new NamespacedKey(GardensEnchants.plugin, "bubble_head"), bookNameSpace);
    }

    @NotNull
    @Override
    public String getName() {
        return "Bubble Head";
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().name().endsWith("HELMET");
    }
}
