package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.enchantment.custom.ItemRestrict;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@NoArgsConstructor
public abstract class RuneHandler {

    private ItemRestrict itemRestrict;

    public boolean canApply(ItemStack target) {
        return itemRestrict.isEnchantable(target);
    }

    public abstract int getApplyCost(ItemStack target);

    public abstract ItemStack apply(ItemStack target);
}
