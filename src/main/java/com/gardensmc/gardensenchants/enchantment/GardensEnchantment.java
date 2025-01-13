package com.gardensmc.gardensenchants.enchantment;

import org.bukkit.inventory.ItemStack;

public interface GardensEnchantment {

    boolean hasEnchantment(ItemStack itemStack);

    void applyEnchantment(ItemStack itemStack, int level);

    void removeEnchantment(ItemStack itemStack);

    int getEnchantmentLevel(ItemStack itemStack);

    boolean canEnchant(ItemStack itemStack);

    int getMaxLevel();

    String getId();

    String getDisplayName();

    EnchantRarity getRarity();

    boolean isVisibleInTable();

    boolean conflictsWith(GardensEnchantment ench);

}
