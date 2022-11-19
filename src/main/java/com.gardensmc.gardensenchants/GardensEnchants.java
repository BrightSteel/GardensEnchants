package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.events.Events;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GardensEnchants extends JavaPlugin {

    public static GardensEnchants plugin;
    private static final List<CustomEnchantment> gardensEnchants = new ArrayList<>();

    // book_namespace -> custom_enchantment
    private static HashMap<String, CustomEnchantment> gardensEnchantsMap = new HashMap<>();

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
        gardensEnchants.add(DecapitationEnchant.INSTANCE);
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
        }
    }
}
