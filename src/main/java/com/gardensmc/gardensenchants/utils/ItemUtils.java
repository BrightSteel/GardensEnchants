package com.gardensmc.gardensenchants.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class ItemUtils {

    private static YamlConfiguration mobHeads;

    public static ItemStack getHead(String name, String base64Skin) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setPlayerProfile(getProfile(base64Skin));
            skullMeta.displayName(MessageUtil.parseIntoComponent(name));
            head.setItemMeta(skullMeta);
        }
        return head;
    }

    /**
     * The player's head as just their id and name. The client looks their skin up itself, and leaving out the
     * signed skin data (which carries a timestamp that changes as Mojang re-signs it) lets every head of the
     * same player stack.
     */
    public static ItemStack getHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setPlayerProfile(Bukkit.getServer().createProfile(uuid, Bukkit.getOfflinePlayer(uuid).getName()));
            head.setItemMeta(skullMeta);
        }
        return head;
    }

    // the same id for the same skin, so heads with it stack (a random id made every head unique)
    private static PlayerProfile getProfile(String base64) {
        PlayerProfile profile = Bukkit.getServer().createProfile(UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8)));
        profile.setProperty(new ProfileProperty("textures", base64));
        return profile;
    }

    /**
     * Loads mob_heads.yml (copying the bundled default on first run). Heads used to be read from
     * config.yml, which has no mob_heads section, so Skull Stealer and /mobhead never found one.
     */
    public static void loadMobHeads() {
        mobHeads = GardensEnchants.plugin.loadConfig("mob_heads.yml");
    }

    public static Set<String> getMobHeadTypes() {
        return mobHeads == null ? Set.of() : mobHeads.getKeys(false);
    }

    @Nullable
    public static ItemStack getMobHead(String entity_type) {
        if (mobHeads == null) {
            return null;
        }
        ConfigurationSection section = mobHeads.getConfigurationSection(entity_type);
        if (section != null) {
            String name = section.getString("name");
            String base64 = section.getString("base64");
            if (name != null && base64 != null) {
                return ItemUtils.getHead(name, base64);
            }
        }
        return null;
    }

}
