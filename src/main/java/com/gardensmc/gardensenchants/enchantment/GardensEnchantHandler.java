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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
public class GardensEnchantHandler {

    private final ItemStack item;
    public static final String ITEM_NBT_KEY = "GardensEnchants";
    private static final String SLOTS_KEY = "slots";
    private static final String CUSTOM_LORE_KEY = "customLore";
    public static final int DEFAULT_SLOTS = 4;
    private static final String SLOTS_LORE_PREFIX = "Enchant Slots: ";

    public boolean isEnchantable(GardensEnchantment enchantment) {
        if (!enchantment.canEnchant(item)) {
            return false;
        }
        var enchants = getEnchantments();
        // an enchant already on the item is an upgrade: it can't conflict with itself (Bukkit says
        // vanilla enchants do) and it doesn't take another slot
        var others = enchants
                .stream()
                .filter(enchantmentInstance -> !enchantmentInstance.getEnchantment().getId().equals(enchantment.getId()))
                .toList();
        var hasConflicts = others
                .stream()
                .anyMatch(enchantmentInstance -> enchantmentInstance.getEnchantment().conflictsWith(enchantment));
        if (hasConflicts) {
            return false;
        }
        if (others.size() != enchants.size()) {
            return true;
        }
        // enchants that this enchantment replaces (e.g. eternal removes unbreaking/mending)
        // free up their slot, so don't count them against the limit
        var replacedIds = enchantment.getReplacedEnchantmentIds();
        var freedSlots = enchants
                .stream()
                .filter(enchantmentInstance -> replacedIds.contains(enchantmentInstance.getEnchantment().getId()))
                .count();
        return (enchants.size() - freedSlots) < getSlots();
    }

    public List<EnchantmentInstance> getUpgradeableEnchantments() {
        return getEnchantments()
                .stream()
                .filter(enchant -> enchant.getLevel() < enchant.getEnchantment().getMaxLevel())
                .toList();
    }

    public void upgradeEnchantments() {
        adoptExistingLore();
        getUpgradeableEnchantments().forEach(this::upgradeEnchant);
        applyPostEnchantChanges();
    }

    private void upgradeEnchant(EnchantmentInstance enchantment) {
        enchantment.getEnchantment().applyEnchantment(item, enchantment.getLevel() + 1, false);
    }

    public void enchant(GardensEnchantment enchantment, int level, boolean isForce) {
        adoptExistingLore();
        enchantment.applyEnchantment(item, level, isForce);
        applyPostEnchantChanges();
    }

    public void enchantAll(List<EnchantmentInstance> enchantments) {
        adoptExistingLore();
        enchantments.forEach(enchantmentInstance -> {
            var enchantment = enchantmentInstance.getEnchantment();
            enchantment.applyEnchantment(item, enchantmentInstance.getLevel(), false);
        });
        applyPostEnchantChanges();
    }

    public void applyPostEnchantChanges() {
        adoptExistingLore();
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

    /**
     * The first time this handler manages an item's lore, keep what's already there (other plugins'
     * lore, /lore lines) as custom lore instead of overwriting it. Items enchanted before this fix
     * may carry our generated lore without the NBT compound, so anything from the "Enchant Slots"
     * line on is dropped rather than kept twice.
     */
    private void adoptExistingLore() {
        if (hasEnchantNBT()) {
            return; // lore is already generated here, custom lines live in NBT
        }
        var lore = item.getItemMeta().lore();
        List<String> existing = new ArrayList<>();
        if (lore != null) {
            for (var line : lore) {
                if (MessageUtil.toPlainText(line).startsWith(SLOTS_LORE_PREFIX)) {
                    // drop the blank line we put above the slots line too
                    if (!existing.isEmpty() && existing.getLast().isEmpty()) {
                        existing.removeLast();
                    }
                    break;
                }
                existing.add(MessageUtil.toMiniMessage(line));
            }
        }
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            if (!existing.isEmpty()) {
                itemNBT.getStringList(CUSTOM_LORE_KEY).addAll(existing);
            }
        });
    }

    public void removeEnchant(GardensEnchantment enchantment) {
        adoptExistingLore();
        enchantment.removeEnchantment(item);
        if (getEnchantments().isEmpty()) {
            // drop the glint we added in applyPostEnchantChanges
            var meta = item.getItemMeta();
            meta.setEnchantmentGlintOverride(null);
            item.setItemMeta(meta);
        }
        refreshLore();
    }

    public void clearEnchants(boolean isRefreshLore) {
        adoptExistingLore();
        // remove vanilla enchants
        item.removeEnchantments();
        // remove custom enchants
        NBT.modify(item, nbt -> {
            var compound = nbt.getCompound(ITEM_NBT_KEY);
            if (compound != null) {
                compound.removeKey(CustomEnchantment.ENCHANTS_KEY);
            }
        });
        // remove glint override
        var meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(null);
        item.setItemMeta(meta);
        // refresh lore
        if (isRefreshLore) {
            refreshLore();
        }
    }

    public void refreshLore() {
        adoptExistingLore();
        var meta = item.getItemMeta();
        meta.lore(getLore());
        item.setItemMeta(meta);
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
                .content(SLOTS_LORE_PREFIX)
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component
                        .text("(%s/%s)".formatted(enchantCount, slots))
                        .color(NamedTextColor.GOLD))
                .build()
        );
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
                    // skip ids that no longer exist (e.g. winged_boots) instead of failing on the whole item
                    return enchants.getKeys()
                            .stream()
                            .map(key -> {
                                var enchantment = CustomEnchantments.customEnchantments.get(key);
                                if (enchantment == null) {
                                    GardensEnchantments.INSTANCE.warnUnknown(key);
                                    return null;
                                }
                                return new EnchantmentInstance(enchantment, enchants.getInteger(key));
                            })
                            .filter(Objects::nonNull)
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
        adoptExistingLore();
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            var slots = itemNBT.getOrDefault(SLOTS_KEY, DEFAULT_SLOTS);
            itemNBT.setInteger(SLOTS_KEY, ++slots);
        });
        refreshLore();
    }

    public void setSlots(int slots) {
        adoptExistingLore();
        NBT.modify(item, nbt -> {
            var itemNBT = nbt.getOrCreateCompound(ITEM_NBT_KEY);
            itemNBT.setInteger(SLOTS_KEY, slots);
        });
        refreshLore();
    }

}
