package com.gardensmc.gardensenchants.cache;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.database.EnchantEntry;
import com.gardensmc.gardensenchants.database.EnchantTable;
import com.gardensmc.gardensenchants.database.PlayerTable;
import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class EnchantCache {

    private static HashMap<UUID, EnchantEntry> enchantEntryMap = new HashMap<>();

    private UUID uuid;


    /**
     * Accessible via player's uuid
     * @param uuid
     */
    public EnchantCache(UUID uuid) {
        this.uuid = uuid;
    }

    public CustomEnchantment[] getEnchants() {
        return getCache().getEnchantments();
    }

    public String[] getLevels() {
        return getCache().getLevels();
    }

    public Integer[] getDestructions() {
        return getCache().getDestructions();
    }

    private EnchantEntry getCache() {
        if (!enchantEntryMap.containsKey(uuid)) {
            EnchantEntry enchantEntry = EnchantUtils.getOrCreateEnchantEntry(uuid);
            enchantEntryMap.put(uuid, enchantEntry);
        }

        EnchantEntry entry = enchantEntryMap.get(uuid);
        // set tick to 0
        return entry;
    }

    public LocalDateTime getExpiration() {
        return getCache().getExpiration();
    }

    public void loadCache() {
        getCache();
    }

    public void resetEnchant() {
        EnchantUtils.resetEnchants(uuid);
        enchantEntryMap.remove(uuid);
        new EnchantCache(uuid).getCache();
        refresh(uuid);
    }

    public static void refresh(UUID uuid) {
        resetGlobalCache(uuid);
    }

    private static void resetGlobalCache(UUID uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GardensResetEnchantCache");
        out.writeUTF(uuid.toString());
        Bukkit.getServer().sendPluginMessage(GardensEnchants.plugin, "BungeeCord", out.toByteArray());
    }

    public static void refreshPacket(UUID uuid) {
        enchantEntryMap.remove(uuid);
        new EnchantCache(uuid).getCache();
    }

    public static void remove(UUID uuid) {
        enchantEntryMap.remove(uuid);
    }

}
