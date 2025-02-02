package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.EnchantEntryCache;
import com.gardensmc.gardensenchants.cache.EnchantEntryKey;
import com.gardensmc.gardensenchants.database.Tables;
import com.gardensmc.gardensenchants.database.entry.EnchantEntry;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@AllArgsConstructor
public class EnchantEntryHandler {

    private final UUID player;
    private final String type;

    public Queue<EnchantmentInstance> getEnchantmentInstances() {
        var enchantEntry = getOrCreateEnchantEntry();
        return new LinkedList<>(Arrays.asList(enchantEntry.getEnchantJsonEntry().getEnchantments()));
    }

    public EnchantEntry getOrCreateEnchantEntry() {
        try {
            return EnchantEntryCache.INSTANCE.get(new EnchantEntryKey(
                    player,
                    type
            )).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void resetEnchantEntry() {
        try {
            CompletableFuture.runAsync(() ->
                    Tables.enchant.removeEnchantEntry(
                            player.toString(),
                            type)
            ).get();
        } catch (Exception e) {
            GardensEnchants.plugin.getLogger().log(Level.SEVERE, "Failed to remove enchant entry from table", e);
        }
        EnchantEntryCache.INSTANCE.remove(new EnchantEntryKey(
                player,
                type
        ));
    }
}
