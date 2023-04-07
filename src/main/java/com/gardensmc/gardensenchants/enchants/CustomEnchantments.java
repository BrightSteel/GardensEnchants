package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CustomEnchantments {

    // used to produce mappings, cleared after
    public static final List<CustomEnchantment> customEnchantments = new ArrayList<>();

    // book_namespace -> custom_enchantment, for anvil usage
    private static final HashMap<String, CustomEnchantment> gardensEnchantsMap = new HashMap<>();

    // model_data -> book_namespace for using old itemsadder books
    public static HashMap<Integer, String> conversionMap = new HashMap<>();

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
    public static final CustomEnchantment UNBREAKING = new CustomWrapper(Enchantment.DURABILITY, "Unbreaking", EnchantRarity.ORDINARY);
    public static final CustomEnchantment EFFICIENCY = new CustomWrapper(Enchantment.DIG_SPEED, "Efficiency", EnchantRarity.ORDINARY);
    public static final CustomEnchantment SHARPNESS = new CustomWrapper(Enchantment.DAMAGE_ALL, "Sharpness", EnchantRarity.ORDINARY);
    public static final CustomEnchantment SWEEPING_EDGE = new CustomWrapper(Enchantment.SWEEPING_EDGE, "Sweeping Edge", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FIRE_ASPECT = new CustomWrapper(Enchantment.FIRE_ASPECT, "Fire Aspect", EnchantRarity.ORDINARY);
    public static final CustomEnchantment KNOCKBACK = new CustomWrapper(Enchantment.KNOCKBACK, "Knockback", EnchantRarity.ORDINARY);
    public static final CustomEnchantment LOYALTY = new CustomWrapper(Enchantment.LOYALTY, "Loyalty", EnchantRarity.ORDINARY);
    public static final CustomEnchantment POWER = new CustomWrapper(Enchantment.ARROW_DAMAGE,  "Power", EnchantRarity.ORDINARY);
    public static final CustomEnchantment PUNCH = new CustomWrapper(Enchantment.ARROW_KNOCKBACK, "Punch", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FLAME = new CustomWrapper(Enchantment.ARROW_FIRE, "Flame", EnchantRarity.ORDINARY);
    public static final CustomEnchantment QUICK_CHARGE = new CustomWrapper(Enchantment.QUICK_CHARGE, "Quick Charge", EnchantRarity.ORDINARY);
    public static final CustomEnchantment RESPIRATION = new CustomWrapper(Enchantment.OXYGEN, "Respiration", EnchantRarity.ORDINARY);
    public static final CustomEnchantment PROTECTION = new CustomWrapper(Enchantment.PROTECTION_ENVIRONMENTAL, "Protection", EnchantRarity.ORDINARY);
    public static final CustomEnchantment FIRE_PROTECTION = new CustomWrapper(Enchantment.PROTECTION_FIRE, "Fire Defense", EnchantRarity.ORDINARY);

    public static final CustomEnchantment DEPTH_STRIDER = new CustomWrapper(Enchantment.DEPTH_STRIDER, "Depth Strider", EnchantRarity.ORDINARY);
    public static final CustomEnchantment THORNS = new CustomWrapper(Enchantment.THORNS,   "Thorns", EnchantRarity.ORDINARY);

    /**
     * Unique
     */
    public static final CustomEnchantment BUBBLE_HEAD = new HelmetEnchant( "bubble_head", "Bubble Head", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LIFE_STEAL = new WeaponEnchant("life_steal", "Life Steal", EnchantRarity.UNIQUE, 3);
    public static final CustomEnchantment FORTUNE = new CustomWrapper(Enchantment.LOOT_BONUS_BLOCKS, "Fortune", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SILK_TOUCH = new CustomWrapper(Enchantment.SILK_TOUCH, "Silk Touch", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LURE = new CustomWrapper(Enchantment.LURE,  "Lure", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LUCK_OF_THE_SEA = new CustomWrapper(Enchantment.LUCK, "Sea Luck", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SMITE = new CustomWrapper(Enchantment.DAMAGE_UNDEAD,"Smite", EnchantRarity.UNIQUE);
    public static final CustomEnchantment BANE_OF_ARTHROPODS = new CustomWrapper(Enchantment.DAMAGE_ARTHROPODS, "Bane of Spiders", EnchantRarity.UNIQUE);
    public static final CustomEnchantment LOOTING = new CustomWrapper(Enchantment.LOOT_BONUS_MOBS, "Looting", EnchantRarity.UNIQUE);
    public static final CustomEnchantment IMPALING = new CustomWrapper(Enchantment.IMPALING, "Impaling", EnchantRarity.UNIQUE);
//    public static final CustomEnchantment RIPTIDE = new CustomWrapper(Enchantment.RIPTIDE,"riptide", "Riptide", EnchantRarity.UNIQUE);
    public static final CustomEnchantment PIERCING = new CustomWrapper(Enchantment.PIERCING, "Piercing", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FEATHER_FALLING = new CustomWrapper(Enchantment.PROTECTION_FALL, "Feather Falling", EnchantRarity.UNIQUE);
    public static final CustomEnchantment FROST_WALKER = new CustomWrapper(Enchantment.FROST_WALKER, "Frost Walker", EnchantRarity.UNIQUE);
    public static final CustomEnchantment BLAST_PROTECTION = new CustomWrapper(Enchantment.LUCK, "Blast Defense", EnchantRarity.UNIQUE);
    public static final CustomEnchantment PROJECTILE_PROTECTION = new CustomWrapper(Enchantment.PROTECTION_PROJECTILE,  "Arrow Defense", EnchantRarity.UNIQUE);
    public static final CustomEnchantment INFINITY = new CustomWrapper(Enchantment.ARROW_INFINITE,  "Infinity", EnchantRarity.UNIQUE);

    public static final CustomEnchantment MULTISHOT = new CustomWrapper(Enchantment.MULTISHOT,  "Multishot", EnchantRarity.UNIQUE);

    /**
     * Mystical
     */
    public static final CustomEnchantment CHANNELING = new CustomWrapper(Enchantment.CHANNELING, "Channeling", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment AQUA_AFFINITY = new CustomWrapper(Enchantment.WATER_WORKER, "Aqua Affinity", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment SOUL_SPEED = new CustomWrapper(Enchantment.SOUL_SPEED, "Soul Speed", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment SWIFT_SNEAK = new CustomWrapper(Enchantment.SWIFT_SNEAK, "Swift Sneak", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment MENDING = new CustomWrapper(Enchantment.MENDING,"Mending", EnchantRarity.MYSTICAL);

    public static final CustomEnchantment DOUBLE_STRIKE = new WeaponEnchant("double_strike", "Double Strike", EnchantRarity.MYSTICAL, 3);

    public static final CustomEnchantment NIGHT_VISION = new HelmetEnchant("night_vision", "Night Vision", EnchantRarity.MYSTICAL);

    public static final CustomEnchantment JELLY_LEGS = new BootsEnchant("jelly_legs", "Jelly Legs", EnchantRarity.MYSTICAL, 5);

    public static final CustomEnchantment SUPER_BREAKER = new ToolEnchant("super_breaker", "Super Breaker", EnchantRarity.MYSTICAL, "pickaxe");
    public static final CustomEnchantment SMELTER = new ToolEnchant("smelter", "Smelter", EnchantRarity.MYSTICAL, "pickaxe");


    /**
     * Divine
     */
    public static final CustomEnchantment ETERNAL = new UniversalEnchant( "eternal", "Eternal", EnchantRarity.DIVINE);

    public static final CustomEnchantment NETHERLING = new ToolEnchant("netherling", "Netherling", EnchantRarity.DIVINE, "sword");

    public static final CustomEnchantment SILK_TOUCH_SPAWNER = new ToolEnchant("silk_touch_spawner", "Silk Spawner", EnchantRarity.DIVINE);

    public static final CustomEnchantment SPEEDY = new BootsEnchant("speedy", "Speedy", EnchantRarity.DIVINE, 3);

    public static final CustomEnchantment WINGED_BOOTS = new BootsEnchant("winged_boots", "Winged Boots", EnchantRarity.DIVINE);


    private static void compileList() {
        WINGED_BOOTS.setAvailableThroughTable(false);

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
//        customEnchantments.add(RIPTIDE);
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
        customEnchantments.add(SUPER_BREAKER);
        customEnchantments.add(SMELTER);

        customEnchantments.add(NETHERLING);
        customEnchantments.add(SILK_TOUCH_SPAWNER);
        customEnchantments.add(DOUBLE_STRIKE);
        customEnchantments.add(SPEEDY);
        customEnchantments.add(NIGHT_VISION);
        customEnchantments.add(JELLY_LEGS);
        customEnchantments.add(MULTISHOT);
        customEnchantments.add(WINGED_BOOTS);
    }

    public static void mapConversions() {
        conversionMap.put(10000, "bubble_head");
        conversionMap.put(10001, "vein_miner");
        conversionMap.put(10002, "eternal");
        conversionMap.put(10003, "life_steal");
        conversionMap.put(10004, "skull_stealer");
        conversionMap.put(10005, "timber");
        conversionMap.put(10006, "aqua_affinity");
        conversionMap.put(10007, "bane_of_arthropods");
        conversionMap.put(10008, "blast_protection");
        conversionMap.put(10009, "channeling");
        conversionMap.put(10010, "depth_strider");
        conversionMap.put(10011, "efficiency");
        conversionMap.put(10012, "feather_falling");
        conversionMap.put(10013, "fire_aspect");
        conversionMap.put(10014, "fire_protection");
        conversionMap.put(10015, "flame");
        conversionMap.put(10016, "fortune");
        conversionMap.put(10017, "frost_walker");
        conversionMap.put(10018, "impaling");
        conversionMap.put(10019, "infinity");
        conversionMap.put(10020, "knockback");
        conversionMap.put(10021, "looting");
        conversionMap.put(10022, "loyalty");
        conversionMap.put(10023, "luck_of_the_sea");
        conversionMap.put(10024, "lure");
        conversionMap.put(10025, "mending");
        conversionMap.put(10026, "multishot");
        conversionMap.put(10027, "piercing");
        conversionMap.put(10028, "power");
        conversionMap.put(10029, "projectile_protection");
        conversionMap.put(10030, "protection");
        conversionMap.put(10031, "punch");
        conversionMap.put(10032, "quick_charge");
        conversionMap.put(10033, "respiration");
        conversionMap.put(10034, "riptide");
        conversionMap.put(10035, "sharpness");
        conversionMap.put(10036, "silk_touch");
        conversionMap.put(10037, "smite");
        conversionMap.put(10038, "soul_speed");
        conversionMap.put(10039, "sweeping_edge");
        conversionMap.put(10040, "swift_sneak");
        conversionMap.put(10041, "thorns");
        conversionMap.put(10042, "unbreaking");
        conversionMap.put(10043, "silk_touch_spawner");
        conversionMap.put(10044, "netherling");
        conversionMap.put(10045, "double_strike");
        conversionMap.put(10046, "speedy");
        conversionMap.put(10047, "night_vision");
        conversionMap.put(10048, "jelly_legs");
    }

    public static void initialize() {
        compileList();
        registerEnchantments();
        mapEnchants();
        mapConversions();
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
        List<CustomEnchantment> available = list.stream().filter(CustomEnchantment::isAvailableThroughTable).toList();
        return available.get(ProbabilityUtil.getRandomIndex(available.size()));
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
