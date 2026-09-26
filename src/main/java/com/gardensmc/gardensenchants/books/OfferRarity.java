package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Picks the rarity a table offer is generated from.
 */
public final class OfferRarity {

    private OfferRarity() {
    }

    /**
     * Returns the rolled rarity if it has enchants to offer, otherwise the nearest rarity that does
     * (the lower one on a tie, so a gap never upgrades an offer). Null if no rarity has any.
     */
    @Nullable
    public static EnchantRarity nearestAvailable(EnchantRarity rolled, Predicate<EnchantRarity> hasPool) {
        var rarities = EnchantRarity.values();
        for (int distance = 0; distance < rarities.length; distance++) {
            int lower = rolled.ordinal() - distance;
            if (lower >= 0 && hasPool.test(rarities[lower])) {
                return rarities[lower];
            }
            int higher = rolled.ordinal() + distance;
            if (higher < rarities.length && hasPool.test(rarities[higher])) {
                return rarities[higher];
            }
        }
        return null;
    }
}
