package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensfurniture.register.CustomRegister;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.UUID;

public class ItemUtils {

    public static ItemStack getHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null)
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        head.setItemMeta(skullMeta);
        return head;
    }

    public static ItemStack getHeadBase64(String name, String value) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), name);
            profile.getProperties().put("textures", new Property("textures", value));
            Field profileField;
            try {
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    public static ItemStack getCustomItem(String id) {
        ItemStack stack = new ItemStack(Material.PAPER);
//        CustomStack customStack = CustomStack.getInstance(namespace);
        ItemStack customItem = CustomRegister.INSTANCE.itemRegistry.buildItem(id);
//        ItemBuilder itemBuilder = OraxenItems.getItemById(id);
        if (customItem != null) {
            stack = customItem;
        }
        return stack;
    }

    public static ItemStack getBlankItem() {
        return getCustomItem("blank_item");
    }

    @Nullable
    public static ItemStack getMobHead(String entity_type) {
        ConfigurationSection section = GardensEnchants.plugin.getConfig().getConfigurationSection("mob_heads");
        if (section != null) {
            ConfigurationSection section1 = section.getConfigurationSection(entity_type);
            if (section1 != null) {
                String name = section1.getString("name");
                String base64 = section1.getString("base64");
                return ItemUtils.getHeadBase64(name, base64);
            }
        }
        return null;
    }


}
