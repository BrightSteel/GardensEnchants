package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.cache.EnchantEntryCache;
import com.gardensmc.gardensenchants.cache.EnchantEntryKey;
import com.gardensmc.gardensenchants.database.entry.EnchantEntry;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A player's enchanting-table offers for one item type. Offers are loaded from MySQL off the main
 * thread; callers must hop back to the main thread before touching the world or inventories.
 */
@AllArgsConstructor
public class EnchantEntryHandler {

    private final UUID player;
    private final String type;

    /**
     * Loads (or rolls) the offers without blocking. Completes on a database thread.
     */
    public CompletableFuture<List<EnchantmentInstance>> getOffers() {
        return EnchantEntryCache.INSTANCE.get(getKey()).thenApply(EnchantEntryHandler::toOffers);
    }

    /**
     * The offers if they're already loaded, otherwise null. Used to re-check an offer at purchase.
     */
    @Nullable
    public List<EnchantmentInstance> getLoadedOffers() {
        var entry = EnchantEntryCache.INSTANCE.getIfLoaded(getKey());
        return entry == null ? null : toOffers(entry);
    }

    /**
     * Whether the offer at this index is still the one the player saw. Stops a stale menu row or
     * form (e.g. two forms, or a click after a purchase) from buying the same roll twice.
     */
    public boolean isCurrentOffer(int index, EnchantmentInstance offer) {
        var offers = getLoadedOffers();
        return offers != null && index >= 0 && index < offers.size() && offers.get(index).equals(offer);
    }

    /**
     * Replaces the offers with a fresh roll after a purchase.
     */
    public void reroll() {
        EnchantEntryCache.INSTANCE.reroll(getKey());
    }

    private EnchantEntryKey getKey() {
        return new EnchantEntryKey(player, type);
    }

    private static List<EnchantmentInstance> toOffers(EnchantEntry entry) {
        var json = entry.getEnchantJsonEntry();
        if (json == null || json.getEnchantments() == null) {
            return List.of();
        }
        return Arrays.stream(json.getEnchantments())
                .filter(Objects::nonNull)
                .filter(offer -> offer.getEnchantment() != null)
                .toList();
    }
}
