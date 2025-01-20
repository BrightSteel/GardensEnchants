package com.gardensmc.gardensenchants.enchantment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;

@Getter
@AllArgsConstructor
public enum EnchantRarity {

    ORDINARY(0xCFD5DD, '\uE05E', 3, DestructionLevel.NONE),
    UNIQUE(0xF7C941, '\uE110', 4, DestructionLevel.EASY),
    MYSTICAL(0xB756FE, '\uE111', 5, DestructionLevel.MEDIUM),
    DIVINE(0x91E6DC, '\uE112', 6, DestructionLevel.HARD);

    private final int color;
    private final char tag;
    private final int baseCost;
    private final DestructionLevel destructionLevel;

    // helper method for component usage
    public TextColor getTextColor() {
        return TextColor.color(color);
    }

}
