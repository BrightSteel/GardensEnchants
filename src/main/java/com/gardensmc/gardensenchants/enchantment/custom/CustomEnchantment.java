package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.enchantment.VanillaEnchantment;
import de.tr7zw.nbtapi.NBT;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler.ITEM_NBT_KEY;

@Getter
public class CustomEnchantment extends GardensEnchantment {

    public static final String ENCHANTS_KEY = "Enchants";

    private final String id;
    private int maxLevel = 1;
    private boolean isVisibleInTable = true;
    private List<GardensEnchantment> conflictingEnchantments = new ArrayList<>();

    private CustomEnchantment(String id) {
        this.id = id;
    }

    @Override
    public boolean hasEnchantment(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        return NBT.get(itemStack, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                var enchants = compound.getCompound(ENCHANTS_KEY);
                if (enchants != null) {
                    return enchants.hasTag(id);
                }
            }
            return false;
        });
    }

    @Override
    public void applyEnchantment(ItemStack itemStack, int level, boolean isForce) {
        NBT.modify(itemStack, nbt -> {
            var compound = nbt.getOrCreateCompound(ITEM_NBT_KEY).getOrCreateCompound(ENCHANTS_KEY);
            compound.setInteger(id, level);
        });
        if (id.equals(CustomEnchantments.ETERNAL.id)) {
            // remove durability damage
            if (itemStack.getItemMeta() instanceof Damageable damageable) {
                damageable.setDamage(0);
                itemStack.setItemMeta(damageable);
            }
            // remove un-breaking and mending
            Stream.of(Enchantment.UNBREAKING, Enchantment.MENDING).filter(itemStack::containsEnchantment).forEach(itemStack::removeEnchantment);
        } else if (id.equals(CustomEnchantments.BUBBLE_HEAD.id)) {
            if (itemStack.containsEnchantment(Enchantment.RESPIRATION)) {
                itemStack.removeEnchantment(Enchantment.RESPIRATION);
            }
        }
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
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return 0;
        }
        return NBT.get(itemStack, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                var enchants = compound.getCompound(ENCHANTS_KEY);
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
        if (conflictingEnchantments.contains(ench)) {
            return true;
        }
        if (ench instanceof CustomEnchantment customEnchantment) {
            // check their list to avoid having to list conflicts twice each time...
            return customEnchantment.conflictingEnchantments.contains(this);
        }
        return false;
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
        private List<GardensEnchantment> conflictingEnchantments;

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

        public Builder lore(String... lore) {
            this.lore = Arrays.stream(lore).map(s -> MiniMessage.miniMessage().deserialize(s)).toList();
            return this;
        }

        public Builder bookIdentifier(String bookIdentifier) {
            this.bookIdentifier = bookIdentifier;
            return this;
        }

        public Builder conflictsWith(GardensEnchantment... gardensEnchantments) {
            this.conflictingEnchantments = List.of(gardensEnchantments);
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
            if (conflictingEnchantments != null) {
                customEnchant.conflictingEnchantments = conflictingEnchantments;
            }
            return customEnchant;
        }
    }
}
