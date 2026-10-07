package com.gardensmc.gardensenchants.enchantment.custom;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Material.*;
import static org.bukkit.Material.DIAMOND_CHESTPLATE;

public class EnchantRestricts {

    public static final Map<String, EnchantRestrict> registeredEnchantRestricts = new HashMap<>();

    /**
     * Resolves the concrete (leaf) restrict an item belongs to - e.g. a diamond sword resolves
     * to {@link #SWORD}. This is the same restrict its matching typed book uses, so inserting a
     * tool into the enchanting table yields the same enchant pool as inserting that typed book.
     * Returns null if no leaf restrict directly permits the item.
     */
    @Nullable
    public static EnchantRestrict getMatchingType(ItemStack itemStack) {
        return registeredEnchantRestricts.values().stream()
                .filter(EnchantRestrict::isLeaf)
                .filter(restrict -> restrict.isDirectlyPermitted(itemStack))
                .findFirst()
                .orElse(null);
    }

    public static final EnchantRestrict PICKAXE = new EnchantRestrict.Builder("pickaxe")
            .withMaterials(
                    WOODEN_PICKAXE,
                    STONE_PICKAXE,
                    IRON_PICKAXE,
                    GOLDEN_PICKAXE,
                    DIAMOND_PICKAXE,
                    NETHERITE_PICKAXE,
                    COPPER_PICKAXE
            )
            .withIcon('\uE248')
            .build();

    public static final EnchantRestrict AXE = new EnchantRestrict.Builder("axe")
            .withMaterials(
                    WOODEN_AXE,
                    STONE_AXE,
                    IRON_AXE,
                    GOLDEN_AXE,
                    DIAMOND_AXE,
                    NETHERITE_AXE,
                    COPPER_AXE
            )
            .withIcon('\uE245')
            .build();

    public static final EnchantRestrict SHOVEL = new EnchantRestrict.Builder("shovel")
            .withMaterials(
                    WOODEN_SHOVEL,
                    STONE_SHOVEL,
                    IRON_SHOVEL,
                    GOLDEN_SHOVEL,
                    DIAMOND_SHOVEL,
                    NETHERITE_SHOVEL,
                    COPPER_SHOVEL
            ).withIcon('\uE249').build();

    public static final EnchantRestrict SWORD = new EnchantRestrict.Builder("sword")
            .withMaterials(
                    WOODEN_SWORD,
                    STONE_SWORD,
                    IRON_SWORD,
                    GOLDEN_SWORD,
                    DIAMOND_SWORD,
                    NETHERITE_SWORD,
                    COPPER_SWORD
            ).withIcon('\uE24A').build();

    public static final EnchantRestrict HOE = new EnchantRestrict.Builder("hoe")
            .withMaterials(
                    WOODEN_HOE,
                    STONE_HOE,
                    IRON_HOE,
                    GOLDEN_HOE,
                    DIAMOND_HOE,
                    NETHERITE_HOE,
                    COPPER_HOE
            ).withIcon('\uE247').build();

    public static final EnchantRestrict MACE = new EnchantRestrict.Builder("mace")
            .withMaterial(Material.MACE).withIcon('\uE255').build();

    public static final EnchantRestrict HELMET = new EnchantRestrict.Builder("helmet")
            .withMaterials(
                    CHAINMAIL_HELMET,
                    LEATHER_HELMET,
                    IRON_HELMET,
                    GOLDEN_HELMET,
                    DIAMOND_HELMET,
                    NETHERITE_HELMET,
                    TURTLE_HELMET,
                    COPPER_HELMET
                    ).withIcon('\uE251').build();

    public static final EnchantRestrict CHESTPLATE = new EnchantRestrict.Builder("chestplate")
            .withMaterials(
                    CHAINMAIL_CHESTPLATE,
                    LEATHER_CHESTPLATE,
                    IRON_CHESTPLATE,
                    GOLDEN_CHESTPLATE,
                    DIAMOND_CHESTPLATE,
                    NETHERITE_CHESTPLATE,
                    COPPER_CHESTPLATE
            ).withIcon('\uE252').build();

    public static final EnchantRestrict LEGGINGS = new EnchantRestrict.Builder("leggings")
            .withMaterials(
                    CHAINMAIL_LEGGINGS,
                    LEATHER_LEGGINGS,
                    IRON_LEGGINGS,
                    GOLDEN_LEGGINGS,
                    DIAMOND_LEGGINGS,
                    NETHERITE_LEGGINGS,
                    COPPER_LEGGINGS
            ).withIcon('\uE253').build();

    public static final EnchantRestrict BOOTS = new EnchantRestrict.Builder("boots")
            .withMaterials(
                    CHAINMAIL_BOOTS,
                    LEATHER_BOOTS,
                    IRON_BOOTS,
                    GOLDEN_BOOTS,
                    DIAMOND_BOOTS,
                    NETHERITE_BOOTS,
                    COPPER_BOOTS
            ).withIcon('\uE254').build();

    public static final EnchantRestrict SPEAR = new EnchantRestrict.Builder("spear")
            .withMaterials(
                    WOODEN_SPEAR,
                    STONE_SPEAR,
                    IRON_SPEAR,
                    COPPER_SPEAR,
                    GOLDEN_SPEAR,
                    DIAMOND_SPEAR,
                    NETHERITE_SPEAR
            ).build();

    public static final EnchantRestrict SHIELD = new EnchantRestrict.Builder("shield")
            .withMaterial(Material.SHIELD).build();

    public static final EnchantRestrict BOW = new EnchantRestrict.Builder("bow")
            .withMaterial(Material.BOW).withIcon('\uE24C').build();

    public static final EnchantRestrict CROSS_BOW = new EnchantRestrict.Builder("cross_bow")
            .withMaterial(CROSSBOW).withIcon('\uE24D').build();

    public static final EnchantRestrict TRIDENT = new EnchantRestrict.Builder("trident")
            .withMaterial(Material.TRIDENT).withIcon('\uE24E').build();

    public static final EnchantRestrict SHEARS = new EnchantRestrict.Builder("shears")
            .withMaterial(Material.SHEARS).withIcon('\uE24F').build();

    public static final EnchantRestrict ENCHANTED_BOOK = new EnchantRestrict.Builder("enchanted_book")
            .withMaterial(Material.ENCHANTED_BOOK).withIcon('\uE250').build();

    public static final EnchantRestrict FISHING_ROD = new EnchantRestrict.Builder("fishing_rod")
            .withMaterial(Material.FISHING_ROD).withIcon('\uE256').build();

    public static final EnchantRestrict ELYTRA = new EnchantRestrict.Builder("elytra")
            .withMaterial(Material.ELYTRA).withIcon('\uE257').build();

    // GardensCrops' watering can, one item per fill level
    public static final EnchantRestrict WATERING_CAN = new EnchantRestrict.Builder("watering_can")
            .withCustomIdentifiers("watering_can_0", "watering_can_1", "watering_can_2", "watering_can_3", "watering_can_4", "watering_can_5")
            .build();

    // EnderWardrobe's balloon (a GardensFurniture item, skinned or not) and staffs (its own offhand items, a tier each)
    public static final EnchantRestrict BALLOON = new EnchantRestrict.Builder("balloon")
            .withCustomIdentifiers("balloon")
            .build();

    public static final EnchantRestrict STAFF = new EnchantRestrict.Builder("staff")
            .withWardrobeItems("wooden_staff", "stone_staff", "copper_staff", "iron_staff",
                    "golden_staff", "diamond_staff", "netherite_staff")
            .build();

    // Speedy: boots, and a staff held (they add up to Speedy's max level; see events/Events)
    public static final EnchantRestrict SPEEDY = new EnchantRestrict.Builder("speedy")
            .withChildren(BOOTS, STAFF)
            .build();

    // backpacks with storage; their enchantments work on the one worn in EnderWardrobe's backpack slot
    public static final EnchantRestrict BACKPACK = new EnchantRestrict.Builder("backpack")
            .withBackpacks()
            .build();

    // items that wear out but that vanilla doesn't count as damageable, so its Unbreaking and Mending are let on
    public static final EnchantRestrict WARDROBE = new EnchantRestrict.Builder("wardrobe")
            .withChildren(BALLOON, STAFF)
            .build();

    public static final EnchantRestrict BASIC_TOOLS = new EnchantRestrict.Builder("basic_tools")
            .withChildren(
                    PICKAXE,
                    AXE,
                    SHOVEL,
                    HOE
            ).build();

    public static final EnchantRestrict TOOLS = new EnchantRestrict.Builder("tools")
            .withChildren(
                    BASIC_TOOLS,
                    SWORD
            ).build();

    public static final EnchantRestrict WEAPON = new EnchantRestrict.Builder("weapon")
            .withChildren(SWORD, AXE).build();

    public static final EnchantRestrict EXCAVATION = new EnchantRestrict.Builder("excavation")
            .withChildren(PICKAXE, SHOVEL).build();

    public static final EnchantRestrict ARMOR = new EnchantRestrict.Builder("armor")
            .withChildren(
                    HELMET,
                    CHESTPLATE,
                    LEGGINGS,
                    BOOTS
            ).build();

    public static final EnchantRestrict ALL = new EnchantRestrict.Builder("all")
            .withChildren(
                    ARMOR,
                    TOOLS,
                    WEAPON,
                    MACE,
                    BOW,
                    CROSS_BOW,
                    TRIDENT,
                    SHEARS,
                    FISHING_ROD,
                    ELYTRA,
                    SPEAR,
                    SHIELD,
                    WARDROBE
            )
            .setIcons(Collections.emptyList())
            .build();
}
