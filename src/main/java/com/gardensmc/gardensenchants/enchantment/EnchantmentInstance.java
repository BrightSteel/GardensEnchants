package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.utils.RomanNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class EnchantmentInstance implements Comparable<EnchantmentInstance> {

    private GardensEnchantment enchantment;
    @Setter
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
                .content(enchantment.getDisplayName() + " " + RomanNumber.toRoman(level))
                .color(enchantment.getRarity().getTextColor())
                .decoration(TextDecoration.ITALIC, false)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnchantmentInstance that)) return false;
        return level == that.level && enchantment.getId().equals(that.enchantment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(enchantment.getId(), level);
    }

    @Override
    public EnchantmentInstance clone() {
        try {
            return (EnchantmentInstance) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCombineCost() {
        return enchantment.getCostInXP(level) / 2;
    }
}
