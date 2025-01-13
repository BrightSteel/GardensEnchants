package com.gardensmc.gardensenchants.enchantment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class EnchantmentInstance implements Comparable<EnchantmentInstance> {

    private GardensEnchantment enchantment;
    private int level;

    @Override
    public int compareTo(@NotNull EnchantmentInstance o) {
        int rarity = enchantment.getRarity().compareTo(o.getEnchantment().getRarity());
        if (rarity != 0) {
            return rarity;
        }
        return enchantment.getId().compareTo(o.enchantment.getId());
    }

    public Component toLore() {
        return Component
                .text()
                .content(enchantment.getDisplayName() + " " + level)
                .color(NamedTextColor.BLUE)
                .build();
    }
}
