package com.gardensmc.gardensenchants.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class RuneUtil {

    private static int max_slots = 10;
    private static int max_restorations = 4;

    /**
     *
     * @param rune
     * @param item
     * @return null if cannot be applied
     */
    @Nullable
    public static ItemStack getRuneResult(Rune rune, ItemStack item) {
        String enchantables[] = new String[]{
                "HELMET", "LEGGINGS", "CHESTPLATE", "BOOTS",
                "SWORD", "AXE", "SHOVEL", "HOE", "BOW", "TRIDENT"
        };
        NBTItem nbtItem = new NBTItem(item.clone());
        switch (rune) {
            case SLOT_INCREASER -> {
                String itemType = item.getType().name();
                String enchantable = Arrays.stream(enchantables).filter(itemType::endsWith).findFirst().orElse(null);
                if (enchantable == null) return null;

                int slots = 4; // default
                if (nbtItem.hasKey("slots")) {
                    slots = nbtItem.getInteger("slots");
                    if (slots >= max_slots) return null;
                }
                slots++;

                nbtItem.setInteger("slots", slots);
                EnchantUtils.updateLore(nbtItem.getItem());

                return nbtItem.getItem();
            }
            case RESTORATION -> {
                if (!nbtItem.hasKey("destruction")) return null;
                int restorations = 0;
                int destruction = nbtItem.getInteger("destruction");
                if (nbtItem.hasKey("restorations")) {
                    restorations = nbtItem.getInteger("restorations");
                    if (restorations >= max_restorations) return null;
                }
                int newDestruction = destruction - 5;
                restorations++;
                nbtItem.setInteger("restorations", restorations);
                nbtItem.setInteger("destruction", newDestruction);
                updateDestructionLore(nbtItem.getItem(), restorations, newDestruction);

                return nbtItem.getItem();
            }
        }
        return null;
    }

    public static ItemStack amberUpgrade(ItemStack item, ItemStack amber) {
        int amount = 24;
        if (amber.getAmount() >= amount) {
            if (item.getType().name().toLowerCase().endsWith("boots")) {
                NBTItem nbtItem = new NBTItem(item);
                if (nbtItem.hasKey("charge")) {
                    nbtItem.setInteger("charge", 3600);
                    EnchantUtils.updateLore(nbtItem.getItem());
                    return nbtItem.getItem();
                }
            }
        }
        return null;
    }

    public static void updateDestructionLore(ItemStack book, int restorations, int destruction) {
        if (book.getType() != Material.ENCHANTED_BOOK) return;

        ItemMeta meta = book.getItemMeta();
        if (meta == null || meta.getLore() == null) return;
        List<String> lore = meta.getLore();
        // should at least have line for destruction, may have line for restorations
        int last = lore.size()-1;
        String lastLine = lore.get(last);
        if (lastLine.contains("Restorations")) {
            lore.remove(last);
        }

        // replace destruction lore
        lore.remove(lore.size()-1);
        lore.add(ChatUtils.translateHexColorCodes("#", "", "#FF5555") + "Destruction Chance: " + ChatUtils.translateHexColorCodes("#", "", "#AAAAAA") + destruction + "%");

        lore.add(ChatColor.DARK_RED + "Restorations Applied: " + ChatUtils.translateHexColorCodes("#", "", "#AAAAAA") + "(" + restorations + "/4)");
        meta.setLore(lore);
        book.setItemMeta(meta);
    }
}
