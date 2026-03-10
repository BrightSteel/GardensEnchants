package com.gardensmc.gardensenchants.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.gardensmc.gardensenchants.utils.MessageUtil.readableId;

public class ItemUtil {

    public static ItemStack buildSpawnerItem(String entityId) {
        String entity_name = entityId.contains(":") ? entityId.split(":")[1] : entityId;

        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawnerItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + readableId(entity_name) + " Spawner");
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP); // would do this with component api but no block_entity_data component to hide yet ?
            spawnerItem.setItemMeta(meta);
        }


        NBTItem nbtItem = new NBTItem(spawnerItem);
        nbtItem.setString("entity_id", entityId);
        return nbtItem.getItem();
    }
}
