package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public abstract class GardensEnchantment {

    @Getter
    protected EnchantRarity rarity = EnchantRarity.COMMON;
    protected String displayName;
    @Getter
    protected List<Component> lore = Collections.emptyList();
    protected String bookIdentifier;
    @Getter
    protected EnchantRestrict restrict = EnchantRestricts.ALL;

    public abstract boolean hasEnchantment(ItemStack itemStack);

    public abstract void applyEnchantment(ItemStack itemStack, int level, boolean isForce);

    public abstract void removeEnchantment(ItemStack itemStack);

    public abstract int getEnchantmentLevel(ItemStack itemStack);

    public abstract boolean canEnchant(ItemStack itemStack);

    public abstract int getMaxLevel();

    public abstract String getId();

    public String getDisplayName() {
        return displayName != null ? displayName : MessageUtil.readableId(getId());
    }

    public abstract boolean isVisibleInTable();

    public abstract boolean conflictsWith(GardensEnchantment ench);

    public int getCostInXP(int level) {
        var base = getRarity().getBaseCost();
        return base + (base * (level / 2));
    }

    public int getTableCost() {
        return getRarity().getBaseCost() * 10;
    }

    public int getTableCostInXp() {
        return getCostInXP(0);
    }

    public String bookIdentifier() {
        if (bookIdentifier != null) {
            return bookIdentifier;
        }
        return getId() + "_book";
    }

    public List<Character> getPermittedItemIcons() {
        return restrict.iconRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GardensEnchantment that)) return false;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
