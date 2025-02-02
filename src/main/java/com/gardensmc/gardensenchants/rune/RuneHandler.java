package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public abstract class RuneHandler {

    private final ItemStack rune;
    private final EnchantRestrict itemRestrictOld;

    public RuneHandler(ItemStack rune) {
        this.rune = rune;
        this.itemRestrictOld = EnchantRestricts.ALL;
    }

    public boolean canApply(ItemStack target) {
        return rune.getAmount() == 1 && itemRestrictOld.isEnchantable(target);
    }

    public abstract int getApplyCost(ItemStack target);

    public abstract ItemStack apply(ItemStack target);
}
