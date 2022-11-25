package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomEnchantments {

    // used to produce mappings, cleared after
    public static final List<CustomEnchantment> customEnchantments = new ArrayList<>();

    // book_namespace -> custom_enchantment, for anvil usage
    private static final HashMap<String, CustomEnchantment> gardensEnchantsMap = new HashMap<>();

    public static final List<CustomEnchantment>
            ordinaryEnchants = new ArrayList<>(),
            uniqueEnchants = new ArrayList<>(),
            mysticalEnchants = new ArrayList<>(),
            divineEnchants = new ArrayList<>();



    /**
     * Ordinary
     */
    public static final CustomEnchantment SKULL_STEALER = new WeaponEnchant("skull_stealer", EnchantRarity.ORDINARY);
    public static final CustomEnchantment TIMBER = new ToolEnchant( "timber", EnchantRarity.ORDINARY, "axe");
    public static final CustomEnchantment VEIN_MINER = new ToolEnchant("vein_miner", EnchantRarity.ORDINARY, "pickaxe");
    public static final CustomEnchantment UNBREAKING = new CustomWrapper(Enchantment.DURABILITY, "unbreaking", EnchantRarity.ORDINARY);
    public static final CustomEnchantment EFFICIENCY = new CustomWrapper(Enchantment.DIG_SPEED, "efficiency", EnchantRarity.ORDINARY);
    public static final CustomEnchantment SHARPNESS = new CustomWrapper(Enchantment.DAMAGE_ALL, "sharpness", EnchantRarity.ORDINARY);
    public static final CustomEnchantment SWEEPING_EDGE = new CustomWrapper(Enchantment.SWEEPING_EDGE, "sweeping_edge", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FIRE_ASPECT = new CustomWrapper(Enchantment.FIRE_ASPECT, "fire_aspect", EnchantRarity.ORDINARY);
    public static final CustomEnchantment KNOCKBACK = new CustomWrapper(Enchantment.KNOCKBACK, "knockback", EnchantRarity.ORDINARY);
    public static final CustomEnchantment LOYALTY = new CustomWrapper(Enchantment.LOYALTY, "loyalty", EnchantRarity.ORDINARY);
    public static final CustomEnchantment POWER = new CustomWrapper(Enchantment.ARROW_DAMAGE, "power", EnchantRarity.ORDINARY);
    public static final CustomEnchantment PUNCH = new CustomWrapper(Enchantment.ARROW_KNOCKBACK, "punch", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FLAME = new CustomWrapper(Enchantment.ARROW_FIRE, "flame", EnchantRarity.ORDINARY);
    public static final CustomEnchantment QUICK_CHARGE = new CustomWrapper(Enchantment.QUICK_CHARGE, "quick_charge", EnchantRarity.ORDINARY);
    public static final CustomEnchantment RESPIRATION = new CustomWrapper(Enchantment.OXYGEN, "respiration", EnchantRarity.ORDINARY);
    public static final CustomEnchantment PROTECTION = new CustomWrapper(Enchantment.PROTECTION_ENVIRONMENTAL, "protection", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FIRE_PROTECTION = new CustomWrapper(Enchantment.PROTECTION_FIRE, "fire_protection", EnchantRarity.ORDINARY);

    public static final CustomEnchantment DEPTH_STRIDER = new CustomWrapper(Enchantment.DEPTH_STRIDER, "depth_strider", EnchantRarity.ORDINARY);
    public static final CustomEnchantment THORNS = new CustomWrapper(Enchantment.THORNS, "thorns", EnchantRarity.ORDINARY);

    /**
     * Unique
     */
    public static final CustomEnchantment BUBBLE_HEAD = new HelmetEnchant( "bubble_head", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LIFE_STEAL = new WeaponEnchant("life_steal", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FORTUNE = new CustomWrapper(Enchantment.LOOT_BONUS_BLOCKS, "fortune", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SILK_TOUCH = new CustomWrapper(Enchantment.SILK_TOUCH, "silk_touch", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LURE = new CustomWrapper(Enchantment.LURE, "lure", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LUCK_OF_THE_SEA = new CustomWrapper(Enchantment.LUCK, "luck_of_the_sea", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SMITE = new CustomWrapper(Enchantment.DAMAGE_UNDEAD,"smite", EnchantRarity.UNIQUE);
    public static final CustomEnchantment BANE_OF_ARTHROPODS = new CustomWrapper(Enchantment.DAMAGE_ARTHROPODS,"bane_of_arthropods", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LOOTING = new CustomWrapper(Enchantment.LOOT_BONUS_MOBS,"looting", EnchantRarity.UNIQUE);
    public static final CustomEnchantment IMPALING = new CustomWrapper(Enchantment.IMPALING,"impaling", EnchantRarity.UNIQUE);
    public static final CustomEnchantment RIPTIDE = new CustomWrapper(Enchantment.RIPTIDE,"riptide", EnchantRarity.UNIQUE);
    public static final CustomEnchantment PIERCING = new CustomWrapper(Enchantment.PIERCING,"piercing", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FEATHER_FALLING = new CustomWrapper(Enchantment.PROTECTION_FALL,"feather_falling", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FROST_WALKER = new CustomWrapper(Enchantment.FROST_WALKER,"frost_walker", EnchantRarity.UNIQUE);
    public static final CustomEnchantment BLAST_PROTECTION = new CustomWrapper(Enchantment.LUCK,"blast_protection", EnchantRarity.UNIQUE);
    public static final CustomEnchantment PROJECTILE_PROTECTION = new CustomWrapper(Enchantment.PROTECTION_PROJECTILE, "projectile_protection", EnchantRarity.UNIQUE);
    public static final CustomEnchantment INFINITY = new CustomWrapper(Enchantment.ARROW_INFINITE, "infinity", EnchantRarity.UNIQUE);

    /**
     * Mystical
     */
    public static final CustomEnchantment CHANNELING = new CustomWrapper(Enchantment.CHANNELING,"channeling", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment AQUA_AFFINITY = new CustomWrapper(Enchantment.WATER_WORKER,"aqua_affinity", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment SOUL_SPEED = new CustomWrapper(Enchantment.SOUL_SPEED,"soul_speed", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment SWIFT_SNEAK = new CustomWrapper(Enchantment.SWIFT_SNEAK,"swift_sneak", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment MENDING = new CustomWrapper(Enchantment.MENDING,"mending", EnchantRarity.MYSTICAL);

    /**
     * Divine
     */
    public static final CustomEnchantment ETERNAL = new ToolEnchant( "eternal", EnchantRarity.DIVINE);




    private static void compileList() {
        customEnchantments.add(ETERNAL);
        customEnchantments.add(SKULL_STEALER);
        customEnchantments.add(TIMBER);
        customEnchantments.add(VEIN_MINER);

        customEnchantments.add(UNBREAKING);
        customEnchantments.add(EFFICIENCY);
        customEnchantments.add(SHARPNESS);
        customEnchantments.add(SWEEPING_EDGE);
        customEnchantments.add(FIRE_ASPECT);
        customEnchantments.add(KNOCKBACK);
        customEnchantments.add(LOYALTY);
        customEnchantments.add(POWER);
        customEnchantments.add(PUNCH);
        customEnchantments.add(FLAME);
        customEnchantments.add(QUICK_CHARGE);
        customEnchantments.add(RESPIRATION);
        customEnchantments.add(PROTECTION);
        customEnchantments.add(FIRE_PROTECTION);
        customEnchantments.add(DEPTH_STRIDER);
        customEnchantments.add(THORNS);

        customEnchantments.add(BUBBLE_HEAD);
        customEnchantments.add(LIFE_STEAL);
        customEnchantments.add(SILK_TOUCH);
        customEnchantments.add(FORTUNE);
        customEnchantments.add(LURE);
        customEnchantments.add(LUCK_OF_THE_SEA);
        customEnchantments.add(SMITE);
        customEnchantments.add(BANE_OF_ARTHROPODS);
        customEnchantments.add(LOOTING);
        customEnchantments.add(IMPALING);
        customEnchantments.add(RIPTIDE);
        customEnchantments.add(PIERCING);
        customEnchantments.add(FEATHER_FALLING);
        customEnchantments.add(FROST_WALKER);
        customEnchantments.add(BLAST_PROTECTION);
        customEnchantments.add(PROJECTILE_PROTECTION);
        customEnchantments.add(INFINITY);

        customEnchantments.add(CHANNELING);
        customEnchantments.add(AQUA_AFFINITY);
        customEnchantments.add(SOUL_SPEED);
        customEnchantments.add(SWIFT_SNEAK);
        customEnchantments.add(MENDING);


    }

    public static void initialize() {
        compileList();
        registerEnchantments();
        mapEnchants();
    }

    private static void registerEnchantments() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Enchantment enchantment : customEnchantments) {
            if (!(enchantment instanceof CustomWrapper))
                Enchantment.registerEnchantment(enchantment);
        }

        Enchantment.stopAcceptingRegistrations();
    }

    private static void mapEnchants() {
        for (CustomEnchantment enchantment : customEnchantments) {

            gardensEnchantsMap.put(enchantment.getBookNamespace(), enchantment);

            switch (enchantment.getRarity()) {
                case ORDINARY -> ordinaryEnchants.add(enchantment);
                case UNIQUE -> uniqueEnchants.add(enchantment);
                case MYSTICAL -> mysticalEnchants.add(enchantment);
                case DIVINE -> divineEnchants.add(enchantment);
            }
        }
        customEnchantments.clear();
    }

    public static CustomEnchantment getRandomEnchant(EnchantRarity rarity) {
        switch (rarity) {
            default -> {
                return randEnchantGrabber(ordinaryEnchants);
            }
            case UNIQUE -> {
                return randEnchantGrabber(uniqueEnchants);
            }
            case MYSTICAL -> {
                return randEnchantGrabber(mysticalEnchants);
            }
            case DIVINE -> {
                return randEnchantGrabber(divineEnchants);
            }
        }
    }

    public static int getRandomDestruction(EnchantRarity rarity) {
        int min, max;
        switch (rarity) {
            default -> {min = 15; max = 25;}
            case UNIQUE -> {min = 30; max = 45;}
            case MYSTICAL -> {min = 50; max = 65;}
            case DIVINE -> {min = 70; max = 85;}
        }
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    private static CustomEnchantment randEnchantGrabber(List<CustomEnchantment> list) {
        return list.get(ProbabilityUtil.getRandomIndex(list.size()));
    }

    @Nullable
    public static Enchantment getGardensEnchant(String bookNamespace) {
        return gardensEnchantsMap.get(bookNamespace);
    }


}
