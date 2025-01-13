package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public class GardensEnchantHandler {

    private final GardensEnchantment enchantment;

    public void enchant(ItemStack item, int level) {
        enchantment.applyEnchantment(item, level);
        var meta = item.getItemMeta();
        // todo - add enchantment glint, this is a new tag in 1.21
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.lore(getLore(item));
        item.setItemMeta(meta);
    }

    public void removeEnchant(ItemStack item) {
        enchantment.removeEnchantment(item);
        var meta = item.getItemMeta();
        meta.lore(getLore(item));
        item.setItemMeta(meta);
    }

    public void clearEnchants(ItemStack item) {
        // remove vanilla enchants
        item.removeEnchantments();
        // remove custom enchants
        NBT.modify(item, nbt -> {
            nbt.removeKey(CustomEnchantment.GARDENS_ENCHANTS_KEY);
        });
        // refresh lore
        var meta = item.getItemMeta();
        meta.lore(getLore(item));
        item.setItemMeta(meta);
    }

    @Nullable
    public EnchantmentInstance getEnchantmentInstance(GardensEnchantment enchantment, ItemStack itemStack) {
        return getEnchantments(itemStack)
                .stream()
                .filter(enchantmentInstance -> enchantmentInstance.getEnchantment().getId().equals(enchantment.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<Component> getLore(ItemStack itemStack) {
        return getEnchantments(itemStack).stream().map(EnchantmentInstance::toLore).toList();
    }

    /**
     * Returns sorted list of enchant instances on given item, sorted by enchantment id.
     */
    public static List<EnchantmentInstance> getEnchantments(ItemStack itemStack) {
        var vanillaEnchants = itemStack.getEnchantments().entrySet()
                .stream()
                .map(entry -> new EnchantmentInstance(new VanillaEnchantment(entry.getKey()), entry.getValue()))
                .toList();
        var customEnchants = getCustomEnchantments(itemStack);
        return Stream.concat(vanillaEnchants.stream(), customEnchants.stream())
                .sorted()
                .toList();
    }

    private static List<EnchantmentInstance> getCustomEnchantments(ItemStack itemStack) {
        var enchantNBT = NBT.get(
                itemStack,
                nbt -> (NBTCompound) nbt.getCompound(CustomEnchantment.GARDENS_ENCHANTS_KEY));
        return enchantNBT.getKeys()
                .stream()
                .map(key -> new EnchantmentInstance(CustomEnchantments.customEnchantments.get(key), enchantNBT.getInteger(key)))
                .toList();
    }

}
