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

    /**
     * Ids of enchantments that are removed from an item when this enchantment is applied
     * (e.g. eternal replaces vanilla unbreaking/mending). These don't count against the
     * item's slot limit since applying this enchant frees them.
     */
    public java.util.Set<String> getReplacedEnchantmentIds() {
        return Collections.emptySet();
    }

    public int getCostInXP(int level) {
        var base = getRarity().getBaseCost();
        return base + (base * (level / 2));
    }

    /** How much more each level above I costs at the table, as a share of the rarity's price (V costs three times I). */
    public static final double TABLE_COST_PER_LEVEL = 0.5;

    /** The table's price in acorns at a level: the rarity's, and half of it more for each level above I. */
    public int getTableCost(int level) {
        return (int) Math.round(rarity.getTableCost() * (1 + TABLE_COST_PER_LEVEL * (Math.max(1, level) - 1)));
    }

    /** The table's price in experience levels at a level: the rarity's, and one more for each level above I. */
    public int getTableCostInXp(int level) {
        return rarity.getTableXpCost() + Math.max(1, level) - 1;
    }

    public String bookIdentifier() {
        if (bookIdentifier != null) {
            return bookIdentifier;
        }
        return getId() + "_book";
    }

    /** The item types (leaf restricts) this enchant really goes on: its restrict's, where a sample item takes it. */
    public List<EnchantRestrict> getPermittedTypes() {
        return restrict.getLeaves().stream()
                .filter(type -> type.getSampleItems().stream().anyMatch(this::canEnchant))
                .toList();
    }

    /**
     * Icons for the item types this enchant really goes on. A restrict can be broader than vanilla allows
     * (Looting's "weapon" covers axes, which vanilla keeps it off), so types it can't go on are left out.
     */
    public List<Character> getPermittedItemIcons() {
        var types = restrict.getLeaves();
        var permitted = getPermittedTypes();
        if (permitted.size() == types.size()) {
            return restrict.iconRepresentation(); // all of them, keeping any icons set for the group
        }
        return permitted.stream().flatMap(type -> type.iconRepresentation().stream()).distinct().toList();
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
