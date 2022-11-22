package com.gardensmc.gardensenchants.utils;

import dev.lone.itemsadder.api.CustomStack;
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

    public static ItemStack getCustomItem(String namespace) {
        ItemStack stack = new ItemStack(Material.PAPER);
        CustomStack customStack = CustomStack.getInstance(namespace);
        if (customStack != null) {
            stack = customStack.getItemStack();
        }
        return stack;
    }
}
