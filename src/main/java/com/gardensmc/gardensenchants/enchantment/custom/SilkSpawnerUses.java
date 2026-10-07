package com.gardensmc.gardensenchants.enchantment.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Silk Spawner lasts {@value #USES} spawners, then wears off the tool and has to be enchanted again, so each spawner
 * costs a share of the enchant. The uses left are kept on the tool; one without a count (newly enchanted, or enchanted
 * before uses counted) has them all.
 */
public final class SilkSpawnerUses {

    public static final int USES = 4;
    private static final NamespacedKey KEY = new NamespacedKey("gardensenchants", "silk_spawner_uses");

    private SilkSpawnerUses() {
    }

    public static int left(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta()) return USES;
        Integer left = tool.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);
        return left == null ? USES : left;
    }

    public static void set(ItemStack tool, int left) {
        tool.editMeta(meta -> meta.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, left));
    }

    /** Back to a full count, for when the enchant comes off (a new one starts fresh). */
    public static void clear(ItemStack tool) {
        tool.editMeta(meta -> meta.getPersistentDataContainer().remove(KEY));
    }
}
