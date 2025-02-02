package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import org.bukkit.inventory.ItemStack;

public record BookInstance(
        ItemStack book,
        GardensEnchantment enchantment,
        int level,
        int destructionBase,
        int restorations
) {

    private static final int RESTORATION_MULTIPLIER = 3;

    public int getCostInXP() {
        return enchantment().getCostInXP(level);
    }

    public int calculateDestructionChance() {
        return destructionBase - (restorations * RESTORATION_MULTIPLIER);
    }
}
