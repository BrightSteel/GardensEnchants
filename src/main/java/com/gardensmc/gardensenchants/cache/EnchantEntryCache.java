package com.gardensmc.gardensenchants.cache;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.database.DatabaseConnector;
import com.gardensmc.gardensenchants.database.Tables;
import com.gardensmc.gardensenchants.database.entry.EnchantEntry;
import com.gardensmc.gardensenchants.database.entry.nested.EnchantJsonEntry;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EnchantEntryCache extends AsyncCache<EnchantEntryKey, EnchantEntry> {

    public static final EnchantEntryCache INSTANCE = new EnchantEntryCache();

    private EnchantEntryCache() {
        super(2);
    }

    @Override
    protected CompletableFuture<EnchantEntry> createCacheEntry(EnchantEntryKey key) {
        return CompletableFuture.supplyAsync(() -> {
            var uuid = key.uuid().toString();
            var type = key.typeId();
            var enchantEntry = Tables.enchant.getEnchantEntry(uuid, type);
            if (isValid(type, enchantEntry)) {
                return enchantEntry;
            }
            // missing, or stored before a fix/config change (null or no longer applicable offers)
            var newEntry = createEnchantEntry(type);
            if (newEntry.getEnchantments().length > 0) {
                Tables.enchant.saveEnchantEntry(uuid, type, newEntry);
            }
            return new EnchantEntry(newEntry);
        }, DatabaseConnector.INSTANCE.getExecutor());
    }

    /**
     * Replaces the player's offers for this type with a fresh roll, e.g. after a purchase. The cache
     * is updated immediately so the bought offer can't be seen again; the row is saved in the background.
     */
    public EnchantEntry reroll(EnchantEntryKey key) {
        var newEntry = createEnchantEntry(key.typeId());
        var entry = new EnchantEntry(newEntry);
        put(key, entry);
        if (newEntry.getEnchantments().length > 0) {
            CompletableFuture.runAsync(
                    () -> Tables.enchant.saveEnchantEntry(key.uuid().toString(), key.typeId(), newEntry),
                    DatabaseConnector.INSTANCE.getExecutor()
            ).exceptionally(throwable -> {
                GardensEnchants.plugin.getLogger().log(Level.SEVERE, "Failed to save enchant offers", throwable);
                return null;
            });
        }
        return entry;
    }

    private boolean isValid(String type, EnchantEntry enchantEntry) {
        if (enchantEntry == null || enchantEntry.getEnchantJsonEntry() == null) {
            return false;
        }
        var enchantments = enchantEntry.getEnchantJsonEntry().getEnchantments();
        return enchantments != null
                && enchantments.length == BooksHandler.ENCHANT_GENERATE_COUNT
                && Arrays.stream(enchantments).allMatch(offer -> BooksHandler.INSTANCE.isValidOffer(type, offer));
    }

    private EnchantJsonEntry createEnchantEntry(String type) {
        var enchants = BooksHandler.INSTANCE.getRandomEnchantments(type).toArray(EnchantmentInstance[]::new);
        return new EnchantJsonEntry(enchants);
    }
}
