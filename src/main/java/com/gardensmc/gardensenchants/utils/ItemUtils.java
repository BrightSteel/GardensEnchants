package com.gardensmc.gardensenchants.utils;

import com.gardensmc.gardensfurniture.register.CustomRegister;
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


}
