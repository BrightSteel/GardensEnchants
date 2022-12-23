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
    public static final CustomEnchantment SKULL_STEALER = new WeaponEnchant("skull_stealer", "Skull Stealer", EnchantRarity.ORDINARY);
    public static final CustomEnchantment TIMBER = new ToolEnchant( "timber", "Timber", EnchantRarity.ORDINARY, "axe");
    public static final CustomEnchantment VEIN_MINER = new ToolEnchant("vein_miner", "Vein Miner", EnchantRarity.ORDINARY, "pickaxe");
    public static final CustomEnchantment UNBREAKING = new CustomWrapper(Enchantment.DURABILITY, "unbreaking", "Unbreaking", EnchantRarity.ORDINARY);
    public static final CustomEnchantment EFFICIENCY = new CustomWrapper(Enchantment.DIG_SPEED, "efficiency", "Efficiency", EnchantRarity.ORDINARY);
    public static final CustomEnchantment SHARPNESS = new CustomWrapper(Enchantment.DAMAGE_ALL, "sharpness", "Sharpness", EnchantRarity.ORDINARY);
    public static final CustomEnchantment SWEEPING_EDGE = new CustomWrapper(Enchantment.SWEEPING_EDGE, "sweeping_edge", "Sweeping Edge", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FIRE_ASPECT = new CustomWrapper(Enchantment.FIRE_ASPECT, "fire_aspect", "Fire Aspect", EnchantRarity.ORDINARY);
    public static final CustomEnchantment KNOCKBACK = new CustomWrapper(Enchantment.KNOCKBACK, "knockback", "Knockback", EnchantRarity.ORDINARY);
    public static final CustomEnchantment LOYALTY = new CustomWrapper(Enchantment.LOYALTY, "loyalty", "Loyalty", EnchantRarity.ORDINARY);
    public static final CustomEnchantment POWER = new CustomWrapper(Enchantment.ARROW_DAMAGE, "power", "Power", EnchantRarity.ORDINARY);
    public static final CustomEnchantment PUNCH = new CustomWrapper(Enchantment.ARROW_KNOCKBACK, "punch", "Punch", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FLAME = new CustomWrapper(Enchantment.ARROW_FIRE, "flame", "Flame", EnchantRarity.ORDINARY);
    public static final CustomEnchantment QUICK_CHARGE = new CustomWrapper(Enchantment.QUICK_CHARGE, "quick_charge", "Quick Charge", EnchantRarity.ORDINARY);
    public static final CustomEnchantment RESPIRATION = new CustomWrapper(Enchantment.OXYGEN, "respiration", "Respiration", EnchantRarity.ORDINARY);
    public static final CustomEnchantment PROTECTION = new CustomWrapper(Enchantment.PROTECTION_ENVIRONMENTAL, "protection", "Protection", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FIRE_PROTECTION = new CustomWrapper(Enchantment.PROTECTION_FIRE, "fire_protection", "Fire Defense", EnchantRarity.ORDINARY);

    public static final CustomEnchantment DEPTH_STRIDER = new CustomWrapper(Enchantment.DEPTH_STRIDER, "depth_strider", "Depth Strider", EnchantRarity.ORDINARY);
    public static final CustomEnchantment THORNS = new CustomWrapper(Enchantment.THORNS, "thorns",  "Thorns", EnchantRarity.ORDINARY);

    /**
     * Unique
     */
    public static final CustomEnchantment BUBBLE_HEAD = new HelmetEnchant( "bubble_head", "Bubble Head", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LIFE_STEAL = new WeaponEnchant("life_steal", "Life Steal", EnchantRarity.UNIQUE, 3);
    public static final CustomEnchantment FORTUNE = new CustomWrapper(Enchantment.LOOT_BONUS_BLOCKS, "fortune", "Fortune", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SILK_TOUCH = new CustomWrapper(Enchantment.SILK_TOUCH, "silk_touch", "Silk Touch", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LURE = new CustomWrapper(Enchantment.LURE, "lure", "Lure", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LUCK_OF_THE_SEA = new CustomWrapper(Enchantment.LUCK, "luck_of_the_sea", "Sea Luck", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SMITE = new CustomWrapper(Enchantment.DAMAGE_UNDEAD,"smite", "Smite", EnchantRarity.UNIQUE);
    public static final CustomEnchantment BANE_OF_ARTHROPODS = new CustomWrapper(Enchantment.DAMAGE_ARTHROPODS,"bane_of_arthropods", "Bane of Spiders", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LOOTING = new CustomWrapper(Enchantment.LOOT_BONUS_MOBS,"looting", "Looting", EnchantRarity.UNIQUE);
    public static final CustomEnchantment IMPALING = new CustomWrapper(Enchantment.IMPALING,"impaling", "Impaling", EnchantRarity.UNIQUE);
    public static final CustomEnchantment RIPTIDE = new CustomWrapper(Enchantment.RIPTIDE,"riptide", "Riptide", EnchantRarity.UNIQUE);
    public static final CustomEnchantment PIERCING = new CustomWrapper(Enchantment.PIERCING,"piercing", "Piercing", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FEATHER_FALLING = new CustomWrapper(Enchantment.PROTECTION_FALL,"feather_falling", "Feather Falling", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FROST_WALKER = new CustomWrapper(Enchantment.FROST_WALKER,"frost_walker", "Frost Walker", EnchantRarity.UNIQUE);
    public static final CustomEnchantment BLAST_PROTECTION = new CustomWrapper(Enchantment.LUCK,"blast_protection", "Blast Defense", EnchantRarity.UNIQUE);
    public static final CustomEnchantment PROJECTILE_PROTECTION = new CustomWrapper(Enchantment.PROTECTION_PROJECTILE, "projectile_protection", "Arrow Defense", EnchantRarity.UNIQUE);
    public static final CustomEnchantment INFINITY = new CustomWrapper(Enchantment.ARROW_INFINITE, "infinity", "Infinity", EnchantRarity.UNIQUE);

    /**
     * Mystical
     */
    public static final CustomEnchantment CHANNELING = new CustomWrapper(Enchantment.CHANNELING,"channeling", "Channeling", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment AQUA_AFFINITY = new CustomWrapper(Enchantment.WATER_WORKER,"aqua_affinity", "Aqua Affinity", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment SOUL_SPEED = new CustomWrapper(Enchantment.SOUL_SPEED,"soul_speed", "Soul Speed", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment SWIFT_SNEAK = new CustomWrapper(Enchantment.SWIFT_SNEAK,"swift_sneak", "Swift Sneak", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment MENDING = new CustomWrapper(Enchantment.MENDING,"mending", "Mending", EnchantRarity.MYSTICAL);

    public static final CustomEnchantment DOUBLE_STRIKE = new WeaponEnchant("double_strike", "Double Strike", EnchantRarity.MYSTICAL, 3);

    public static final CustomEnchantment NIGHT_VISION = new HelmetEnchant("night_vision", "Night Vision", EnchantRarity.MYSTICAL);

    public static final CustomEnchantment JELLY_LEGS = new BootsEnchant("jelly_legs", "Jelly Legs", EnchantRarity.MYSTICAL, 5);

    /**
     * Divine
     */
    public static final CustomEnchantment ETERNAL = new ToolEnchant( "eternal", "Eternal", EnchantRarity.DIVINE);

    public static final CustomEnchantment NETHERLING = new ToolEnchant("netherling", "Netherling", EnchantRarity.DIVINE, "sword");

    public static final CustomEnchantment SILK_TOUCH_SPAWNER = new ToolEnchant("silk_touch_spawner", "Silk Spawner", EnchantRarity.DIVINE);

    public static final CustomEnchantment SPEEDY = new BootsEnchant("speedy", "Speedy", EnchantRarity.DIVINE, 3);

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

        customEnchantments.add(NETHERLING);
        customEnchantments.add(SILK_TOUCH_SPAWNER);
        customEnchantments.add(DOUBLE_STRIKE);
        customEnchantments.add(SPEEDY);
        customEnchantments.add(NIGHT_VISION);
        customEnchantments.add(JELLY_LEGS);
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

    public static Enchantment getGardensEnchant(String bookNamespace) {
        CustomEnchantment c = gardensEnchantsMap.get(bookNamespace);
        if (c instanceof CustomWrapper w) {
            return w.enchantment;
        }
        return c;
    }

    public static CustomEnchantment getCustomEnchant(String bookNamespace) {
        return gardensEnchantsMap.get(bookNamespace);
    }


}
