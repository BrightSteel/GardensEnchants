package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.utils.MessageUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class VanillaEnchantment implements GardensEnchantment {

    private final Enchantment enchantment;
    private EnchantRarity rarity = EnchantRarity.ORDINARY;
    private String displayName;

    public VanillaEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public VanillaEnchantment(Enchantment enchantment, EnchantRarity rarity, String displayName) {
        this(enchantment);
        this.rarity = rarity;
        this.displayName = displayName;
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
    public String getDisplayName() {
        return displayName != null ? displayName : MessageUtil.readableId(getId());
    }

    @Override
    public EnchantRarity getRarity() {
        return rarity;
    }

    @Override
    public boolean isVisibleInTable() {
        return true;
    }

    @Override
    public boolean conflictsWith(GardensEnchantment ench) {
        if (ench instanceof VanillaEnchantment vanilla) {
            return enchantment.conflictsWith(vanilla.enchantment);
        }
        return false; // todo impl later
    }

}
