package com.gardensmc.gardensenchants.enchantment.custom;

import de.tr7zw.nbtapi.NBT;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Used to place an enchant effect on an entity
 */
@AllArgsConstructor
public class LingeringEnchantHandler {

    private static final String ENCHANTS_EFFECTS_KEY = "GardensEnchantEffects";
    private final Entity entity;

    public void putLingeringEnchant(String id, int level, UUID source) {
        NBT.modifyPersistentData(entity, nbt -> {
            var effects = nbt.getOrCreateCompound(ENCHANTS_EFFECTS_KEY);
            effects.setInteger(id, level);
            effects.setUUID("source", source);
        });
    }

    @Nullable
    public LingeringEnchant getLingeringEnchant(String id) {
        return NBT.getPersistentData(entity, nbt -> {
            var effects = nbt.getCompound(ENCHANTS_EFFECTS_KEY);
            if (effects != null) {
                return new LingeringEnchant(effects.getInteger(id), effects.getUUID("source"));
            }
            return null;
        });
    }

    public record LingeringEnchant(int level, UUID source) {
    }

}
