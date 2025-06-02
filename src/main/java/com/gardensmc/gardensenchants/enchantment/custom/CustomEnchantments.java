package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensenchants.enchantment.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused") // all custom enchants should be made available here when registered
public class CustomEnchantments {

    public static final Map<String, CustomEnchantment> customEnchantments = new HashMap<>();

    public static final CustomEnchantment SKULL_STEALER = register("skull_stealer", builder -> builder
            .rarity(EnchantRarity.UNIQUE)
            .enchantRestrict(EnchantRestricts.WEAPON));
    public static final CustomEnchantment TIMBER = register( "timber", builder -> builder
            .enchantRestrict(EnchantRestricts.AXE));
    public static final CustomEnchantment VEIN_MINER = register("vein_miner", builder -> builder
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment BUBBLE_HEAD = register("bubble_head", builder -> builder
            .rarity(EnchantRarity.MYSTICAL)
            .enchantRestrict(EnchantRestricts.HELMET));
    public static final CustomEnchantment LIFE_STEAL = register("life_steal", builder -> builder
            .enchantRestrict(EnchantRestricts.WEAPON)
            .rarity(EnchantRarity.UNIQUE)
            .maxLevel(3));
    public static final CustomEnchantment DOUBLE_STRIKE = register("double_strike", builder -> builder
            .enchantRestrict(EnchantRestricts.WEAPON)
            .rarity(EnchantRarity.MYSTICAL)
            .maxLevel(3));
    public static final CustomEnchantment NIGHT_VISION = register("night_vision", builder -> builder
            .enchantRestrict(EnchantRestricts.HELMET)
            .rarity(EnchantRarity.MYSTICAL));
    public static final CustomEnchantment JELLY_LEGS = register("jelly_legs", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.MYSTICAL)
            .maxLevel(5));
    public static final CustomEnchantment SUPER_BREAKER = register("super_breaker", builder -> builder
            .rarity(EnchantRarity.DIVINE)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment SMELTER = register("smelter", builder -> builder
            .rarity(EnchantRarity.MYSTICAL)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment ETERNAL = register( "eternal", builder -> builder
            .rarity(EnchantRarity.DIVINE));
    public static final CustomEnchantment NETHERLING = register("netherling", builder -> builder
            .rarity(EnchantRarity.DIVINE)
            .enchantRestrict(EnchantRestricts.SWORD));
    public static final CustomEnchantment SILK_TOUCH_SPAWNER = register("silk_touch_spawner", builder -> builder
            .displayName("Silk Spawner")
            .enchantRestrict(EnchantRestricts.TOOLS)
            .rarity(EnchantRarity.DIVINE));
    public static final CustomEnchantment SPEEDY = register("speedy", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.DIVINE)
            .maxLevel(3));
    public static final CustomEnchantment WINGED_BOOTS = register("winged_boots", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.DIVINE)
            .isVisibleInTable(false));
    public static final CustomEnchantment EXPLOSIVE = register("explosive", builder -> builder
            .enchantRestrict(EnchantRestricts.BOW)
            .rarity(EnchantRarity.UNIQUE)
            .maxLevel(4));
    public static final CustomEnchantment BARRAGE = register("barrage", builder -> builder
            .enchantRestrict(EnchantRestricts.BOW)
            .conflictsWith(EXPLOSIVE)
            .rarity(EnchantRarity.UNIQUE)
            .maxLevel(3)
    );
    public static final CustomEnchantment SPRINTER = register("sprinter", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.UNIQUE)
            .maxLevel(3)
    );
    public static final CustomEnchantment CLOAK = register("cloak", builder -> builder
            .enchantRestrict(EnchantRestricts.ARMOR)
            .rarity(EnchantRarity.MYSTICAL)
    );
    public static final CustomEnchantment FLUTTER = register("flutter", builder -> builder
            .enchantRestrict(EnchantRestricts.ELYTRA)
            .rarity(EnchantRarity.MYSTICAL)
    );
    public static final CustomEnchantment FLIPPERS = register("flippers", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.MYSTICAL)
    );
    public static final CustomEnchantment SIPHON = register("siphon", builder -> builder
            .enchantRestrict(EnchantRestricts.TOOLS)
            .rarity(EnchantRarity.UNIQUE)
    );
    public static final CustomEnchantment ROCKET_BOOTS = register("rocket_boots", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.DIVINE)
            .maxLevel(4)
    );
    public static final CustomEnchantment ESCAPE_ARTIST = register("escape_artist", builder -> builder
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.UNIQUE)
            .maxLevel(2)
    );
    public static final CustomEnchantment ORB_BOOST = register("orb_boost", builder -> builder
            .enchantRestrict(EnchantRestricts.CHESTPLATE)
            .rarity(EnchantRarity.MYSTICAL)
            .maxLevel(3)
    );
    public static final CustomEnchantment MUNCHER = register("muncher", builder -> builder
            .enchantRestrict(EnchantRestricts.HELMET)
            .rarity(EnchantRarity.UNIQUE)
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
