package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.utils.MessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public abstract class GardensEnchantment {

    @Getter
    protected EnchantRarity rarity = EnchantRarity.ORDINARY;
    protected String displayName;
    @Getter
    protected List<Component> lore = Collections.emptyList();
    protected String bookIdentifier;

    public abstract boolean hasEnchantment(ItemStack itemStack);

    public abstract void applyEnchantment(ItemStack itemStack, int level);

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

    public String bookIdentifier() {
        if (bookIdentifier != null) {
            return bookIdentifier;
        }
        return getId() + "_book";
    }

}
