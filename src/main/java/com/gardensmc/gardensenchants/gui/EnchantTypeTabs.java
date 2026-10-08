package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensfurniture.GardensFurniture;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * The tabs along the top of the enchantment list, one per item type, filtering the books to what goes on it. Each is
 * shown as the item its book icons draw (a diamond tool for tools), or the type's own item for Gardens' types.
 */
final class EnchantTypeTabs {

    /** A tab: its name, its icon, and the type it filters to (null: every enchantment). */
    record Tab(String name, Supplier<ItemStack> icon, @Nullable EnchantRestrict type) {

        boolean shows(GardensEnchantment enchant) {
            return type == null || enchant.getPermittedTypes().contains(type);
        }
    }

    /** Every tab in order; a type no enchantment goes on is left out (see {@link #available}). */
    private static final List<Tab> ALL = List.of(
            new Tab("All", () -> new ItemStack(Material.BOOK), null),
            tab("Pickaxes", Material.DIAMOND_PICKAXE, EnchantRestricts.PICKAXE),
            tab("Axes", Material.DIAMOND_AXE, EnchantRestricts.AXE),
            tab("Shovels", Material.DIAMOND_SHOVEL, EnchantRestricts.SHOVEL),
            tab("Hoes", Material.DIAMOND_HOE, EnchantRestricts.HOE),
            tab("Swords", Material.DIAMOND_SWORD, EnchantRestricts.SWORD),
            tab("Maces", Material.MACE, EnchantRestricts.MACE),
            tab("Spears", Material.DIAMOND_SPEAR, EnchantRestricts.SPEAR),
            tab("Tridents", Material.TRIDENT, EnchantRestricts.TRIDENT),
            tab("Bows", Material.BOW, EnchantRestricts.BOW),
            tab("Crossbows", Material.CROSSBOW, EnchantRestricts.CROSS_BOW),
            tab("Helmets", Material.DIAMOND_HELMET, EnchantRestricts.HELMET),
            tab("Chestplates", Material.DIAMOND_CHESTPLATE, EnchantRestricts.CHESTPLATE),
            tab("Leggings", Material.DIAMOND_LEGGINGS, EnchantRestricts.LEGGINGS),
            tab("Boots", Material.DIAMOND_BOOTS, EnchantRestricts.BOOTS),
            tab("Elytras", Material.ELYTRA, EnchantRestricts.ELYTRA),
            tab("Shields", Material.SHIELD, EnchantRestricts.SHIELD),
            tab("Fishing Rods", Material.FISHING_ROD, EnchantRestricts.FISHING_ROD),
            tab("Shears", Material.SHEARS, EnchantRestricts.SHEARS),
            new Tab("Staffs", () -> modeled(Material.STICK, "gardens:staff/diamond"), EnchantRestricts.STAFF),
            new Tab("Balloons", () -> gardensItem("balloon"), EnchantRestricts.BALLOON),
            new Tab("Backpacks", () -> gardensItem("backpack"), EnchantRestricts.BACKPACK),
            new Tab("Watering Cans", () -> gardensItem("watering_can_5"), EnchantRestricts.WATERING_CAN),
            tab("Books", Material.ENCHANTED_BOOK, EnchantRestricts.ENCHANTED_BOOK));

    private EnchantTypeTabs() {
    }

    /** The tabs with at least one of these enchantments on them ("All" always). */
    static List<Tab> available(Collection<GardensEnchantment> enchants) {
        var tabs = new ArrayList<Tab>();
        for (var tab : ALL) {
            if (tab.type() == null || enchants.stream().anyMatch(tab::shows)) tabs.add(tab);
        }
        return tabs;
    }

    /** A tab's icon: its item, named, without the item's own tooltip lines; glinting while selected. */
    static ItemStack icon(Tab tab, boolean selected) {
        var item = tab.icon().get();
        if (item == null || item.isEmpty()) item = new ItemStack(Material.BOOK);
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS, DataComponentTypes.ENCHANTMENTS,
                        DataComponentTypes.STORED_ENCHANTMENTS, DataComponentTypes.DYED_COLOR,
                        DataComponentTypes.UNBREAKABLE, DataComponentTypes.DAMAGE)
                .build());
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, selected);
        item.unsetData(DataComponentTypes.LORE);
        return item;
    }

    private static Tab tab(String name, Material material, EnchantRestrict type) {
        return new Tab(name, () -> new ItemStack(material), type);
    }

    private static ItemStack modeled(Material material, String model) {
        var item = new ItemStack(material);
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(model));
        return item;
    }

    @Nullable
    private static ItemStack gardensItem(String id) {
        return GardensFurniture.itemRegistry.buildItem(id);
    }
}
