package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomEnchantments {

    // used to produce mappings, cleared after
    public static final List<CustomEnchantment> customEnchantments = new ArrayList<>();

    // book_namespace -> custom_enchantment, for anvil usage
    private static final HashMap<String, CustomEnchantment> gardensEnchantsMap = new HashMap<>();

    private static final List<CustomEnchantment>
            ordinaryEnchants = new ArrayList<>(),
            uniqueEnchants = new ArrayList<>(),
            mysticalEnchants = new ArrayList<>(),
            divineEnchants = new ArrayList<>();

    public static final CustomEnchantment BUBBLE_HEAD = new HelmetEnchant("Bubble Head", "bubble_head", EnchantRarity.ORDINARY);
    public static final CustomEnchantment ETERNAL = new ToolEnchant("Eternal", "eternal", EnchantRarity.MYSTICAL);
    public static final CustomEnchantment LIFE_STEAL = new WeaponEnchant("Life Steal", "life_steal", EnchantRarity.UNIQUE);
    public static final CustomEnchantment SKULL_STEALER = new WeaponEnchant("Skull Stealer", "skull_stealer", EnchantRarity.UNIQUE);
    public static final CustomEnchantment TIMBER = new ToolEnchant("Timber", "timber", EnchantRarity.UNIQUE, "axe");
    public static final CustomEnchantment VEIN_MINER = new ToolEnchant("Vein Miner", "vein_miner", EnchantRarity.UNIQUE, "pickaxe");

    public static final CustomEnchantment SILK_TOUCH = new CustomWrapper(Enchantment.SILK_TOUCH, "silk_touch", EnchantRarity.MYSTICAL);

    private static void compileList() {
        customEnchantments.add(BUBBLE_HEAD);
        customEnchantments.add(ETERNAL);
        customEnchantments.add(LIFE_STEAL);
        customEnchantments.add(SKULL_STEALER);
        customEnchantments.add(TIMBER);
        customEnchantments.add(VEIN_MINER);
        customEnchantments.add(SILK_TOUCH);
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

    private static CustomEnchantment randEnchantGrabber(List<CustomEnchantment> list) {
        return list.get(ProbabilityUtil.getRandomIndex(list.size()));
    }

    @Nullable
    public static Enchantment getGardensEnchant(String bookNamespace) {
        return gardensEnchantsMap.get(bookNamespace);
    }


}
