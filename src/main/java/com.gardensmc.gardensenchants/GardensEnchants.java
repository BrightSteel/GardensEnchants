package com.gardensmc.gardensenchants;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.events.Events;
import com.gardensmc.gardensenchants.gui.EnchantingGUI;
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
    public static ProtocolManager protocolManager;

    @Override
    public void onLoad() {
        plugin = this;
        CustomEnchantments.initialize();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Events(), this);
        PlayerSchedules.scheduleAll();
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

}
