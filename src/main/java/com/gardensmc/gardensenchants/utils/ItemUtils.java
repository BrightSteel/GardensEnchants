package com.gardensmc.gardensenchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
}
