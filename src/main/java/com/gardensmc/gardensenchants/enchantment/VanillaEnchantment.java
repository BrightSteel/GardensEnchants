package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

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

    /**
     * Vanilla enchantments that also go on EnderWardrobe items, and which ones: Unbreaking and Mending on all of them
     * (they wear out though vanilla doesn't know it), Soul Speed on staffs (see events/StaffEnchants).
     */
    private static final java.util.Map<Enchantment, com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict> WARDROBE_ENCHANTMENTS = java.util.Map.of(
            Enchantment.UNBREAKING, EnchantRestricts.WARDROBE,
            Enchantment.MENDING, EnchantRestricts.WARDROBE,
            Enchantment.SOUL_SPEED, EnchantRestricts.STAFF);

    private boolean isWardrobeOnly(ItemStack itemStack) {
        var restrict = WARDROBE_ENCHANTMENTS.get(enchantment);
        return restrict != null && restrict.isEnchantable(itemStack);
    }

    @Override
    public void applyEnchantment(ItemStack itemStack, int level, boolean isForce) {
        // vanilla refuses them on items it doesn't think wear out
        if (isForce || isWardrobeOnly(itemStack) && !enchantment.canEnchantItem(itemStack)) {
            itemStack.addUnsafeEnchantment(enchantment, level);
        } else {
            itemStack.addEnchantment(enchantment, level);
        }
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
        return enchantment.canEnchantItem(itemStack) || isWardrobeOnly(itemStack);
    }

    @Override
    public int getMaxLevel() {
        return enchantment.getMaxLevel();
    }

    @Override
    public String getId() {
        return enchantment.getKey().getKey();
    }

    public boolean isCursed() {
        return enchantment.isCursed();
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
