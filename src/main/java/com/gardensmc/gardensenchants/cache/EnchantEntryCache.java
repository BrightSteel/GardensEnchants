package com.gardensmc.gardensenchants.cache;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.database.Tables;
import com.gardensmc.gardensenchants.database.entry.EnchantEntry;
import com.gardensmc.gardensenchants.database.entry.nested.EnchantJsonEntry;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
            return Objects.requireNonNullElseGet(
                    enchantEntry,
                    () -> Tables.enchant.createEnchantEntry(uuid, type, createEnchantEntry(type))
            );
        });
    }

    private EnchantJsonEntry createEnchantEntry(String type) {
        var enchants = BooksHandler.INSTANCE.getRandomEnchantments(type).toArray(EnchantmentInstance[]::new);
        return new EnchantJsonEntry(enchants);
    }
}
