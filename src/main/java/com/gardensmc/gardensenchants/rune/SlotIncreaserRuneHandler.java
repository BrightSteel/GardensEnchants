package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import org.bukkit.inventory.ItemStack;

public final class SlotIncreaserRuneHandler extends RuneHandler {

    private static final int MAX_SLOTS = 12;

    public SlotIncreaserRuneHandler(ItemStack rune) {
        super(rune);
    }

    @Override
    public int getApplyCost(ItemStack target) {
        return new GardensEnchantHandler(target).getSlots() / 2;
    }

    @Override
    public ItemStack apply(ItemStack itemStack) {
        var clone = itemStack.clone();
        new GardensEnchantHandler(clone).incrementSlots();
        return clone;
    }

    @Override
    public boolean canApply(ItemStack target) {
        if (!super.canApply(target)) {
            return false;
        }
        return new GardensEnchantHandler(target).getSlots() < MAX_SLOTS;
    }
}
