package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensenchants.enchantment.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused") // all custom enchants should be made available here when registered
public class CustomEnchantments {

    public static final Map<String, CustomEnchantment> customEnchantments = new HashMap<>();

    public static final CustomEnchantment SKULL_STEALER = register("skull_stealer", builder -> builder
            .rarity(EnchantRarity.UNCOMMON)
            .lore("<gray>Chance of dropping player's skull on death")
            .enchantRestrict(EnchantRestricts.WEAPON));
    public static final CustomEnchantment TIMBER = register( "timber", builder -> builder
            .lore("<gray>Chop down logs or leaves in one swing")
            .rarity(EnchantRarity.RARE)
            .enchantRestrict(EnchantRestricts.AXE));
    public static final CustomEnchantment VEIN_MINER = register("vein_miner", builder -> builder
            .lore("<gray>Break entire ore veins")
            .rarity(EnchantRarity.RARE)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment BUBBLE_HEAD = register("bubble_head", builder -> builder
            .lore("<gray>Breathe underwater")
            .rarity(EnchantRarity.EPIC)
            .enchantRestrict(EnchantRestricts.HELMET));
    public static final CustomEnchantment LIFE_STEAL = register("life_steal", builder -> builder
            .enchantRestrict(EnchantRestricts.WEAPON)
            .rarity(EnchantRarity.RARE)
            .lore("<gray>Steals some of target's health on hit")
            .maxLevel(3));
    public static final CustomEnchantment DOUBLE_STRIKE = register("double_strike", builder -> builder
            .lore("<gray>Chance of striking target twice")
            .enchantRestrict(EnchantRestricts.WEAPON)
            .rarity(EnchantRarity.EPIC)
            .maxLevel(3));
    public static final CustomEnchantment NIGHT_VISION = register("night_vision", builder -> builder
            .lore("<gray>See in the dark")
            .enchantRestrict(EnchantRestricts.HELMET)
            .rarity(EnchantRarity.RARE));
    public static final CustomEnchantment JELLY_LEGS = register("jelly_legs", builder -> builder
            .lore("<gray>Chance of taking no fall damage")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.RARE)
            .maxLevel(5));
    public static final CustomEnchantment SUPER_BREAKER = register("super_breaker", builder -> builder
            .lore("<gray>Mines in a 3x3 pattern")
            .rarity(EnchantRarity.LEGENDARY)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment SMELTER = register("smelter", builder -> builder
            .lore("<gray>Auto smelts ores")
            .rarity(EnchantRarity.RARE)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment ETERNAL = register( "eternal", builder -> builder
            .lore("<gray>Prevents your item from taking durability damage")
            .rarity(EnchantRarity.LEGENDARY));
    public static final CustomEnchantment NETHERLING = register("netherling", builder -> builder
            .lore("<gray>Deal extra damage to nether mobs")
            .rarity(EnchantRarity.EPIC)
            .enchantRestrict(EnchantRestricts.SWORD));
    public static final CustomEnchantment SILK_TOUCH_SPAWNER = register("silk_touch_spawner", builder -> builder
            .lore("<gray>Spawners will drop themselves when mined")
            .displayName("Silk Spawner")
            .enchantRestrict(EnchantRestricts.TOOLS)
            .rarity(EnchantRarity.LEGENDARY));
    public static final CustomEnchantment SPEEDY = register("speedy", builder -> builder
            .lore("<gray>Grants you a permanent speed boost")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.LEGENDARY)
            .maxLevel(3));
//    public static final CustomEnchantment WINGED_BOOTS = register("winged_boots", builder -> builder
//            .enchantRestrict(EnchantRestricts.BOOTS)
//            .rarity(EnchantRarity.LEGENDARY)
//            .isVisibleInTable(false));
    public static final CustomEnchantment EXPLOSIVE = register("explosive", builder -> builder
            .lore("<gray>Do explosion damage on hit")
            .enchantRestrict(EnchantRestricts.BOW)
            .rarity(EnchantRarity.RARE)
            .maxLevel(4));
    public static final CustomEnchantment BARRAGE = register("barrage", builder -> builder
            .lore("<gray>Shoot multiple arrows at the same time")
            .enchantRestrict(EnchantRestricts.BOW)
            .conflictsWith(EXPLOSIVE)
            .rarity(EnchantRarity.RARE)
            .maxLevel(3)
    );
    public static final CustomEnchantment SPRINTER = register("sprinter", builder -> builder
            .lore("<gray>Consume less food when running")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(3)
    );
    public static final CustomEnchantment CLOAK = register("cloak", builder -> builder
            .lore("<gray>Become invisible to sight-based mobs", "<gray>when not in combat")
            .enchantRestrict(EnchantRestricts.ARMOR)
            .rarity(EnchantRarity.EPIC)
    );
    public static final CustomEnchantment FLUTTER = register("flutter", builder -> builder
            .lore("<gray>Hold shift to activate slow falling")
            .enchantRestrict(EnchantRestricts.ELYTRA)
            .rarity(EnchantRarity.EPIC)
    );
    public static final CustomEnchantment FLIPPERS = register("flippers", builder -> builder
            .lore("<gray>Gain Dolphin's Grace while swimming")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.EPIC)
    );
    public static final CustomEnchantment SIPHON = register("siphon", builder -> builder
            .lore("<gray>Suck drops and xp straight into your inventory")
            .enchantRestrict(EnchantRestricts.TOOLS)
            .rarity(EnchantRarity.UNCOMMON)
    );
    public static final CustomEnchantment ROCKET_BOOTS = register("rocket_boots", builder -> builder
            .lore("<gray>Hold shift to gain jump boost")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.LEGENDARY)
            .maxLevel(4)
    );
    public static final CustomEnchantment ESCAPE_ARTIST = register("escape_artist", builder -> builder
            .lore("<gray>Gain speed when health is 1 heart or lower")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(2)
    );
    public static final CustomEnchantment ORB_BOOST = register("orb_boost", builder -> builder
            .lore("<gray>Gain more exp when collecting orbs")
            .enchantRestrict(EnchantRestricts.CHESTPLATE)
            .rarity(EnchantRarity.EPIC)
            .maxLevel(3)
    );
    public static final CustomEnchantment MUNCHER = register("muncher", builder -> builder
            .lore("<gray>Replenish more hunger when eating")
            .enchantRestrict(EnchantRestricts.HELMET)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(3)
    );

    private static CustomEnchantment register(String id) {
        return register(id, builder -> {});
    }

    private static CustomEnchantment register(String id, Consumer<CustomEnchantment.Builder> builder) {
        var enchantmentBuilder = new CustomEnchantment.Builder(id);
        builder.accept(enchantmentBuilder);

        var customEnchantment = enchantmentBuilder.build();
        customEnchantments.put(id, customEnchantment);
        return customEnchantment;
    }

}
