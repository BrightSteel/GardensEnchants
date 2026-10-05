package com.gardensmc.gardensenchants.enchantment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;

@Getter
@AllArgsConstructor
public enum EnchantRarity {

    COMMON(0x939393, '\u0119', 3, 25, 1, DestructionLevel.NONE),
    UNCOMMON(0x57C42D, '\u011A', 4, 50, 2, DestructionLevel.NONE),
    RARE(0x386FFF, '\u011B', 5, 75, 3, DestructionLevel.NONE),
    EPIC(0xB26BFF, '\u011C', 6, 150, 5, DestructionLevel.NONE),
    LEGENDARY(0xFFAE00, '\u011D', 7, 300, 8, DestructionLevel.NONE);

    private final int color;
    private final char tag;
    private final int baseCost;
    private final int tableCost;
    private final int tableXpCost;
    private final DestructionLevel destructionLevel;

    /**
     * Whether the table only sells this rarity when surrounded by a full bookshelf setup.
     */
    public boolean needsFullBookSetup() {
        return compareTo(EPIC) >= 0;
    }

    // helper method for component usage
    public TextColor getTextColor() {
        return TextColor.color(color);
    }

}
