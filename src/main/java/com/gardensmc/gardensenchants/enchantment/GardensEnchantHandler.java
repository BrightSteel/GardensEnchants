package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import de.tr7zw.nbtapi.NBT;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class GardensEnchantHandler {

    private final ItemStack item;
    public static final String ITEM_NBT_KEY = "GardensEnchants";
    private static final String SLOTS_KEY = "slots";
    private static final int DEFAULT_SLOTS = 4;

    public boolean isEnchantable(GardensEnchantment enchantment) {
        if (!enchantment.canEnchant(item)) {
            return false;
        }
        var enchants = getEnchantments();
        var hasConflicts = enchants
                .stream()
                .anyMatch(enchantmentInstance -> enchantmentInstance.getEnchantment().conflictsWith(enchantment));
        if (hasConflicts) {
            return false;
        }
        return enchants.size() < getSlots();
    }

    public List<EnchantmentInstance> getUpgradeableEnchantments() {
        return getEnchantments()
                .stream()
                .filter(enchant -> enchant.getLevel() < enchant.getEnchantment().getMaxLevel())
                .toList();
    }

    public void upgradeEnchantments() {
        getUpgradeableEnchantments().forEach(this::upgradeEnchant);
        applyPostEnchantChanges();
    }

    private void upgradeEnchant(EnchantmentInstance enchantment) {
        enchantment.getEnchantment().applyEnchantment(item, enchantment.getLevel() + 1);
    }

    public void enchant(GardensEnchantment enchantment, int level) {
        enchantment.applyEnchantment(item, level);
        applyPostEnchantChanges();
    }

    public void enchantAll(List<EnchantmentInstance> enchantments) {
        enchantments.forEach(enchantmentInstance -> {
            var enchantment = enchantmentInstance.getEnchantment();
            enchantment.applyEnchantment(item, enchantmentInstance.getLevel());
        });
        applyPostEnchantChanges();
    }

    private void applyPostEnchantChanges() {
        var meta = item.getItemMeta();
        // todo - add enchantment glint, this is a new tag in 1.21
        meta.addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ATTRIBUTES
        );
        meta.lore(getLore());
        item.setItemMeta(meta);
    }

    public void removeEnchant(GardensEnchantment enchantment) {
        enchantment.removeEnchantment(item);
        refreshLore();
    }

    public void clearEnchants(boolean isRefreshLore) {
        // remove vanilla enchants
        item.removeEnchantments();
        // remove custom enchants
        NBT.modify(item, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                compound.removeKey(CustomEnchantment.ENCHANTS_KEY);
            }
        });
        if (isRefreshLore) {
            refreshLore();
        }
    }

    public void refreshLore() {
        var meta = item.getItemMeta();
        meta.lore(getLore());
        item.setItemMeta(meta);
    }

    @Nullable
    public EnchantmentInstance getEnchantmentInstance(GardensEnchantment enchantment, ItemStack itemStack) {
        return getEnchantments()
                .stream()
                .filter(enchantmentInstance -> enchantmentInstance.getEnchantment().getId().equals(enchantment.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<Component> getLore() {
        var enchantments = getEnchantments();
        var enchantLore = enchantments.stream().map(EnchantmentInstance::toLore).collect(Collectors.toList());
        enchantLore.add(Component.text("")); // space
        enchantLore.add(Component.text()
                .content("%s/%s Slots".formatted(enchantments.size(), getSlots()))
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false)
                .build()
        );
        return enchantLore;
    }

    /**
     * Returns sorted list of enchant instances on given item, sorted by enchantment id.
     */
    public List<EnchantmentInstance> getEnchantments() {
        var vanillaEnchants = item.getEnchantments().entrySet()
                .stream()
                .map(entry -> new EnchantmentInstance(new VanillaEnchantment(entry.getKey()), entry.getValue()))
                .toList();
        var customEnchants = getCustomEnchantments();
        return Stream.concat(vanillaEnchants.stream(), customEnchants.stream())
                .sorted()
                .toList();
    }

    private List<EnchantmentInstance> getCustomEnchantments() {
        return NBT.get(
                item,
                nbt -> {
                    var compound = nbt.getCompound(ITEM_NBT_KEY);
                    if (compound == null) {
                        return Collections.emptyList();
                    }
                    var enchants = compound.getCompound(CustomEnchantment.ENCHANTS_KEY);
                    if (enchants == null) {
                        return Collections.emptyList();
                    }
                    return enchants.getKeys()
                            .stream()
                            .map(key -> new EnchantmentInstance(
                                    CustomEnchantments.customEnchantments.get(key),
                                    enchants.getInteger(key)))
                            .toList();
                });
    }

    public int getSlots() {
        return NBT.get(item, nbt -> {
            var itemNBT = nbt.getCompound(ITEM_NBT_KEY);
            if (itemNBT != null && itemNBT.hasTag(SLOTS_KEY)) {
                return itemNBT.getInteger(SLOTS_KEY);
            }
            return DEFAULT_SLOTS;
        });
    }

    public void incrementSlots() {
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            var slots = itemNBT.getOrDefault(SLOTS_KEY, DEFAULT_SLOTS);
            itemNBT.setInteger(SLOTS_KEY, ++slots);
        });
        refreshLore();
    }

}
