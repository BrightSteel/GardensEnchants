package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import de.tr7zw.nbtapi.NBT;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler.ITEM_NBT_KEY;

@Getter
public class CustomEnchantment extends GardensEnchantment {

    public static final String ENCHANTS_KEY = "Enchants";

    private final String id;
    private int maxLevel = 1;
    private boolean isVisibleInTable = true;

    private CustomEnchantment(String id) {
        this.id = id;
    }

    @Override
    public boolean hasEnchantment(ItemStack itemStack) {
        return NBT.get(itemStack, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                var enchants = compound.getCompound(ENCHANTS_KEY);
                if (enchants != null) {
                    return compound.hasTag(id);
                }
            }
            return false;
        });
    }

    @Override
    public void applyEnchantment(ItemStack itemStack, int level) {
        NBT.modify(itemStack, nbt -> {
            var compound = nbt.getOrCreateCompound(ITEM_NBT_KEY).getOrCreateCompound(ENCHANTS_KEY);
            compound.setInteger(id, level);
        });
    }

    @Override
    public void removeEnchantment(ItemStack itemStack) {
        NBT.modify(itemStack, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                var enchants = compound.getCompound(ENCHANTS_KEY);
                if (enchants != null) {
                    enchants.removeKey(id);
                }
            }
        });
    }

    @Override
    public int getEnchantmentLevel(ItemStack itemStack) {
        return NBT.get(itemStack, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                var enchants = nbt.getCompound(ENCHANTS_KEY);
                if (enchants != null) {
                    return enchants.getInteger(id);
                }
            }
            return 0;
        });
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return restrict.isEnchantable(itemStack);
    }

    @Override
    public boolean isVisibleInTable() {
        return isVisibleInTable;
    }

    @Override
    public boolean conflictsWith(GardensEnchantment ench) {
        return false; // todo impl later
    }

    public static class Builder {

        private final String id;
        private EnchantRarity rarity;
        private Integer maxLevel;
        private String displayName;
        private EnchantRestrict restrict;
        private Boolean isVisibleInTable;
        private List<Component> lore;
        private String bookIdentifier;

        public Builder(String id) {
            this.id = id;
        }

        public Builder rarity(EnchantRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder enchantRestrict(EnchantRestrict restrict) {
            this.restrict = restrict;
            return this;
        }

        public Builder isVisibleInTable(boolean isVisibleInTable) {
            this.isVisibleInTable = isVisibleInTable;
            return this;
        }

        public Builder lore(List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public Builder bookIdentifier(String bookIdentifier) {
            this.bookIdentifier = bookIdentifier;
            return this;
        }

        public CustomEnchantment build() {
            var customEnchant = new CustomEnchantment(id);
            if (rarity != null) {
                customEnchant.rarity = rarity;
            }
            if (maxLevel != null) {
                if (maxLevel == 0) {
                    throw new RuntimeException("Max level cannot be 0 for enchant: " + id);
                }
                customEnchant.maxLevel = maxLevel;
            }
            if (displayName != null) {
                customEnchant.displayName = displayName;
            }
            if (restrict != null) {
                customEnchant.restrict = restrict;
            }
            if (isVisibleInTable != null) {
                customEnchant.isVisibleInTable = isVisibleInTable;
            }
            if (lore != null) {
                customEnchant.lore = lore;
            }
            if (bookIdentifier != null) {
                customEnchant.bookIdentifier = bookIdentifier;
            }
            return customEnchant;
        }
    }
}
