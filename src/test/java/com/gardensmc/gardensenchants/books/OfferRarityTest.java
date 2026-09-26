package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static com.gardensmc.gardensenchants.enchantment.EnchantRarity.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OfferRarityTest {

    @Test
    void keepsRolledRarityWhenItHasEnchants() {
        for (var rarity : EnchantRarity.values()) {
            assertEquals(rarity, OfferRarity.nearestAvailable(rarity, r -> true));
        }
    }

    @Test
    void fallsBackToNearestLowerFirst() {
        // e.g. no UNCOMMON enchant applies to elytra
        var available = EnumSet.of(COMMON, RARE);
        assertEquals(COMMON, OfferRarity.nearestAvailable(UNCOMMON, available::contains));
        assertEquals(RARE, OfferRarity.nearestAvailable(LEGENDARY, available::contains));
        assertEquals(RARE, OfferRarity.nearestAvailable(EPIC, available::contains));
    }

    @Test
    void fallsBackUpwardsWhenNothingLower() {
        var available = EnumSet.of(EPIC);
        assertEquals(EPIC, OfferRarity.nearestAvailable(COMMON, available::contains));
    }

    @Test
    void nullWhenNoRarityHasEnchants() {
        assertNull(OfferRarity.nearestAvailable(RARE, r -> false));
    }
}
