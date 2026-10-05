package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.enchantment.VanillaEnchantment;
import com.gardensmc.gardensfurniture.GardensFurniture;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * The extraction rune moves one enchant off an item onto a book. Unlike the other runes it has two
 * results, so the anvil hands it off to {@link com.gardensmc.gardensenchants.gui.ExtractionRuneMenu}.
 */
public final class ExtractionRune {

    public static final String ID = "extraction_rune";

    private ExtractionRune() {
    }

    public static boolean isExtractionRune(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        var customItem = GardensFurniture.itemRegistry.getCustomItem(item);
        return customItem != null && ID.equals(customItem.getIdentifier());
    }

    public static boolean canExtractFrom(@Nullable ItemStack item) {
        return item != null && item.getAmount() == 1 && !getExtractableEnchantments(item).isEmpty();
    }

    /**
     * Enchants that can be taken off the item, in alphabetical order of their names.
     */
    public static List<EnchantmentInstance> getExtractableEnchantments(ItemStack item) {
        return new GardensEnchantHandler(item).getEnchantments()
                .stream()
                // unregistered vanilla enchants have no book to go on
                .filter(instance -> GardensEnchantments.INSTANCE.getEnchantment(instance.getEnchantment().getId()) != null)
                // curses stay, as at the grindstone
                .filter(instance -> !(instance.getEnchantment() instanceof VanillaEnchantment vanilla && vanilla.isCursed()))
                .sorted(Comparator.comparing(instance -> instance.getEnchantment().getDisplayName()))
                .toList();
    }

    /**
     * The extractable enchant on the item with this id, or null if it's no longer there.
     */
    @Nullable
    public static EnchantmentInstance findExtractable(ItemStack item, String enchantmentId) {
        return getExtractableEnchantments(item)
                .stream()
                .filter(instance -> instance.getEnchantment().getId().equals(enchantmentId))
                .findFirst()
                .orElse(null);
    }

    public static int getCost(EnchantmentInstance instance) {
        return instance.getEnchantment().getCostInXP(instance.getLevel());
    }

    /**
     * Builds what extracting the enchant gives back, without touching the inputs.
     */
    public static Result extract(ItemStack item, EnchantmentInstance instance) {
        var stripped = item.clone();
        new GardensEnchantHandler(stripped).removeEnchant(instance.getEnchantment());
        var book = BooksHandler.INSTANCE.buildBook(new EnchantmentInstance(instance.getEnchantment(), instance.getLevel())).book();
        return new Result(stripped, book);
    }

    public record Result(ItemStack item, ItemStack book) {
    }
}
