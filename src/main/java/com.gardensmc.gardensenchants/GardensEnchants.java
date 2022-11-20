package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.events.Events;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GardensEnchants extends JavaPlugin {

    public static GardensEnchants plugin;

    // only used for mapping purposes, cleared after done
    private static final List<CustomEnchantment> gardensEnchants = new ArrayList<>();

    // book_namespace -> custom_enchantment
    private static final HashMap<String, CustomEnchantment> gardensEnchantsMap = new HashMap<>();

    private static final List<CustomEnchantment>
            ordinaryEnchants = new ArrayList<>(),
            uniqueEnchants = new ArrayList<>(),
            mysticalEnchants = new ArrayList<>(),
            divineEnchants = new ArrayList<>();

    @Override
    public void onLoad() {
        plugin = this;
        enlistEnchantments();
        registerEnchantments();

        // for anvil usage
        mapGardensEnchants();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Events(), this);
        PlayerSchedules.scheduleAll();
    }

    private void enlistEnchantments() {
        gardensEnchants.add(BubbleHeadEnchant.INSTANCE);
        gardensEnchants.add(VeinMinerEnchant.INSTANCE);
        gardensEnchants.add(EternalEnchant.INSTANCE);
        gardensEnchants.add(SkullStealerEnchant.INSTANCE);
        gardensEnchants.add(TimberEnchant.INSTANCE);
    }

    private void registerEnchantments() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Enchantment enchantment : gardensEnchants) {
            Enchantment.registerEnchantment(enchantment);
        }

        Enchantment.stopAcceptingRegistrations();
    }

    @Nullable
    public static Enchantment getGardensEnchant(String bookNamespace) {
        return gardensEnchantsMap.get(bookNamespace);
    }

    public static void mapGardensEnchants() {
        for (CustomEnchantment enchantment : gardensEnchants) {

            gardensEnchantsMap.put(enchantment.getBookNamespace(), enchantment);

            switch (enchantment.getRarity()) {
                case ORDINARY -> ordinaryEnchants.add(enchantment);
                case UNIQUE -> uniqueEnchants.add(enchantment);
                case MYSTICAL -> mysticalEnchants.add(enchantment);
                case DIVINE -> divineEnchants.add(enchantment);
            }
        }
        gardensEnchants.clear();
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


}
