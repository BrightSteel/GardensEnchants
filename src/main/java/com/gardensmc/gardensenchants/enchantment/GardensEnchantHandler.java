package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import de.tr7zw.nbtapi.NBT;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public class GardensEnchantHandler {

    private final ItemStack item;
    public static final String ITEM_NBT_KEY = "GardensEnchants";
    private static final String SLOTS_KEY = "slots";
    private static final String CUSTOM_LORE_KEY = "customLore";
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

    public void applyPostEnchantChanges() {
//        storeCustomLore();
        var meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ATTRIBUTES
        );
        meta.lore(getLore());
        item.setItemMeta(meta);
    }

    public boolean hasEnchantNBT() {
        return NBT.get(item, nbt -> (boolean) nbt.hasTag(ITEM_NBT_KEY));
    }

    private void storeCustomLore() {
        var customLore = item.getItemMeta().lore();
        if (customLore == null || customLore.isEmpty()) {
            return;
        }
        if (!hasEnchantNBT()) {
            setCustomLore(customLore.stream().map(MessageUtil::toMiniMessage).toList());
        }
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
        var slots = getSlots();
        var customLore = getCustomLore();
        var enchantCount = enchantments.size();
        var lore = new ArrayList<Component>();
        if (!customLore.isEmpty()) {
            lore.addAll(customLore
                    .stream()
                    .map(s -> Component.text()
                            .decoration(TextDecoration.ITALIC, false)
                            .append(MessageUtil.fromMiniMessage(s))
                            .build()
                    )
                    .toList()
            );
        }
        lore.add(Component.empty());
        lore.add(Component.text()
                .content("Enchant Slots: ")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component
                        .text("(%s/%s)".formatted(enchantCount, slots))
                        .color(NamedTextColor.GOLD))
                .build()
        );
//        lore.add(Component.empty());
        lore.addAll(enchantments.stream().map(EnchantmentInstance::toLore).toList());
        var remainingSlots = slots - enchantCount;
        if (remainingSlots > 0) {
            var component = Component
                    .text("+ Available Slot", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false);
            for (int i = 0; i < remainingSlots; i++) {
                lore.add(component);
            }
        }
        return lore;
    }

    /**
     * Returns sorted list of enchant instances on given item, sorted by enchantment id.
     */
    public List<EnchantmentInstance> getEnchantments() {
        var vanillaEnchants = item.getEnchantments().entrySet()
                .stream()
                .map(entry -> new EnchantmentInstance(fromVanillaEnchant(entry.getKey()), entry.getValue()))
                .toList();
        var customEnchants = getCustomEnchantments();
        return Stream.concat(vanillaEnchants.stream(), customEnchants.stream())
                .sorted()
                .toList();
    }

    private GardensEnchantment fromVanillaEnchant(Enchantment enchantment) {
        // try to get registered enchantment
        var registeredEnchant = GardensEnchantments.INSTANCE.getEnchantment(enchantment.getKey().getKey());
        if (registeredEnchant != null) {
            return registeredEnchant;
        }
        // possible for an item to have an enchant we don't allow? So lets just wrap it
        return new VanillaEnchantment(enchantment, enchantment.isDiscoverable() && !enchantment.isCursed());
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

    public void addCustomLore(String line) {
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            // get will create list if needed
            var stringList = itemNBT.getStringList(CUSTOM_LORE_KEY);
            stringList.add(line);
        });
        refreshLore();
    }

    // input -1 to remove last line
    // returns text removed
    public String removeCustomLore(int line) {
        var text = NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            // get will create list if needed
            var stringList = itemNBT.getStringList(CUSTOM_LORE_KEY);
            if (line == -1) {
                var i = stringList.size() - 1;
                var lore = stringList.get(i);
                stringList.remove(i);
                return lore;
            } else {
                var lore = stringList.get(line);
                stringList.remove(line);
                return lore;
            }
        });
        refreshLore();
        return text;
    }

    public void removeCustomLore() {
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            itemNBT.removeKey(CUSTOM_LORE_KEY);
        });
        refreshLore();
    }

    public void setCustomLore(List<String> customLore) {
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            // get will create list if needed
            var stringList = itemNBT.getStringList(CUSTOM_LORE_KEY);
            stringList.clear();
            stringList.addAll(customLore);
        });
        refreshLore();
    }

    public List<String> getCustomLore() {
        return NBT.get(item, nbt -> {
            var itemNBT = nbt.getCompound(ITEM_NBT_KEY);
            if (itemNBT != null && itemNBT.hasTag(CUSTOM_LORE_KEY)) {
                return itemNBT.getStringList(CUSTOM_LORE_KEY).toListCopy();
            }
            return Collections.emptyList();
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
