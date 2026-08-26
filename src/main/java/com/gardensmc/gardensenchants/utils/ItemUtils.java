package com.gardensmc.gardensenchants.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensfurniture.GardensFurniture;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.UUID;

public class ItemUtils {

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

    public static ItemStack getHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            head.setItemMeta(skullMeta);
        }
        return head;
    }

    private static PlayerProfile getProfile(String base64) {
        PlayerProfile profile = Bukkit.getServer().createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64));
//        profile.complete();
        return profile;
    }

    public static ItemStack getCustomItem(String id) {
        ItemStack stack = new ItemStack(Material.PAPER);
//        CustomStack customStack = CustomStack.getInstance(namespace);
        ItemStack customItem = GardensFurniture.itemRegistry.buildItem(id);
//        ItemBuilder itemBuilder = OraxenItems.getItemById(id);
        if (customItem != null) {
            stack = customItem;
        }
        return stack;
    }

    public static ItemStack getBlankItem() {
        return getCustomItem("blank");
    }

    @Nullable
    public static ItemStack getMobHead(String entity_type) {
        ConfigurationSection section = GardensEnchants.plugin.getConfig().getConfigurationSection("mob_heads");
        if (section != null) {
            ConfigurationSection section1 = section.getConfigurationSection(entity_type);
            if (section1 != null) {
                String name = section1.getString("name");
                String base64 = section1.getString("base64");
                return ItemUtils.getHead(name, base64);
            }
        }
        return null;
    }


}
