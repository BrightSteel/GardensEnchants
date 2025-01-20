package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import de.tr7zw.nbtapi.NBT;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import static com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler.ITEM_NBT_KEY;

@Getter
public class CustomEnchantment implements GardensEnchantment {

    public static final String ENCHANTS_KEY = "Enchants";

    private final String id;
    private EnchantRarity rarity = EnchantRarity.ORDINARY;
    private int maxLevel = 1;
    private ItemRestrict restrict = ItemRestrict.ALL;
    @Getter(AccessLevel.NONE)
    private String displayName;
    private boolean isVisibleInTable;

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
    public String getDisplayName() {
        return displayName != null ? displayName : MessageUtil.readableId(id);
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
        private ItemRestrict restrict;
        private Boolean isVisibleInTable;

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

        public Builder enchantRestrict(ItemRestrict restrict) {
            this.restrict = restrict;
            return this;
        }

        public Builder isVisibleInTable(boolean isVisibleInTable) {
            this.isVisibleInTable = isVisibleInTable;
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
            return customEnchant;
        }
    }
}
