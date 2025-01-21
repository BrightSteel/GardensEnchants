package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import org.bukkit.inventory.ItemStack;

public record BookInstance(
        ItemStack book,
        GardensEnchantment enchantment,
        int level,
        int destruction,
        int restorations
) {

    public int getCostInXP() {
        return enchantment().getCostInXP(level);
    }
}
