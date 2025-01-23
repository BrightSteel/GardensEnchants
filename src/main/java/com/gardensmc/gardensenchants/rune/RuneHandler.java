package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.enchantment.custom.ItemRestrict;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public abstract class RuneHandler {

    private final ItemStack rune;
    private final ItemRestrict itemRestrict;

    public RuneHandler(ItemStack rune) {
        this.rune = rune;
        this.itemRestrict = ItemRestrict.ALL;
    }

    public boolean canApply(ItemStack target) {
        return rune.getAmount() == 1 && itemRestrict.isEnchantable(target);
    }

    public abstract int getApplyCost(ItemStack target);

    public abstract ItemStack apply(ItemStack target);
}
