package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public class VanillaEnchantment extends GardensEnchantment {

    private final Enchantment enchantment;
    private final boolean isVisibleInTable;

    public VanillaEnchantment(
            Enchantment enchantment,
            EnchantRarity rarity,
            String displayName,
            List<Component> lore,
            String bookIdentifier,
            EnchantRestrict enchantRestrict,
            boolean isVisibleInTable
    ) {
        super(rarity, displayName, lore, bookIdentifier, enchantRestrict);
        this.enchantment = enchantment;
        this.isVisibleInTable = isVisibleInTable;
    }

    @Override
    public boolean hasEnchantment(ItemStack itemStack) {
        return itemStack.containsEnchantment(enchantment);
    }

    @Override
    public void applyEnchantment(ItemStack itemStack, int level) {
        itemStack.addEnchantment(enchantment, level);
    }

    @Override
    public void removeEnchantment(ItemStack itemStack) {
        itemStack.removeEnchantment(enchantment);
    }

    @Override
    public int getEnchantmentLevel(ItemStack itemStack) {
        return itemStack.getEnchantmentLevel(enchantment);
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return enchantment.canEnchantItem(itemStack);
    }

    @Override
    public int getMaxLevel() {
        return enchantment.getMaxLevel();
    }

    @Override
    public String getId() {
        return enchantment.getKey().getKey();
    }

    @Override
    public boolean isVisibleInTable() {
        return isVisibleInTable;
    }

    @Override
    public boolean conflictsWith(GardensEnchantment ench) {
        if (ench instanceof VanillaEnchantment vanilla) {
            return enchantment.conflictsWith(vanilla.enchantment);
        }
        // call conflictsWith on customEnchantment
        return ench.conflictsWith(this);
    }

}
