package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import org.bukkit.inventory.ItemStack;

public class PowerUpRune extends RuneHandler {

    @Override
    public int getApplyCost(ItemStack target) {
        return new GardensEnchantHandler(target).getUpgradeableEnchantments().size();
    }

    @Override
    public ItemStack apply(ItemStack target) {
        var clone = target.clone();
        new GardensEnchantHandler(clone).upgradeEnchantments();
        return clone;
    }

    @Override
    public boolean canApply(ItemStack target) {
        if (!super.canApply(target)) {
            return false;
        }
        // todo - max number of runes allowed per item?
        return !new GardensEnchantHandler(target).getUpgradeableEnchantments().isEmpty();
    }
}
