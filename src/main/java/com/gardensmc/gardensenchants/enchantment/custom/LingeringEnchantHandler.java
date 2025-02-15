package com.gardensmc.gardensenchants.enchantment.custom;

import de.tr7zw.nbtapi.NBT;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;

/**
 * Used to place an enchant effect on an entity
 */
@AllArgsConstructor
public class LingeringEnchantHandler {

    private static final String ENCHANTS_EFFECTS_KEY = "GardensEnchantEffects";
    private final Entity entity;

    public void putLingeringEnchant(String id, int level) {
        NBT.modifyPersistentData(entity, nbt -> {
            var effects = nbt.getOrCreateCompound(ENCHANTS_EFFECTS_KEY);
            effects.setInteger(id, level);
        });
    }

    public int getLingeringEnchant(String id) {
        return NBT.getPersistentData(entity, nbt -> {
            var effects = nbt.getCompound(ENCHANTS_EFFECTS_KEY);
            if (effects != null) {
                return effects.getInteger(id);
            }
            return 0;
        });
    }

}
