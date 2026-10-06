package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.events.Events;
import org.bukkit.event.Listener;

public class Listeners {

    public static void registerListeners() {
        register(new Events());
        register(new AnvilListener());
        register(new EntityListener());
        register(new VillagerListener());
        register(new LootListener());
        register(new PlayerListener());
        register(new BlockListener());
        register(new GrindstoneListener());
        register(new FarmingListener());
        // QuickShop is optional; only load the listener (and its QuickShop classes) when it's installed
        if (GardensEnchants.plugin.getServer().getPluginManager().getPlugin("QuickShop-Hikari") != null) {
            register(new QuickShopListener());
        }
        // GardensCrops is optional too: its watering can and harvest enchants hook into its events
        if (GardensEnchants.plugin.getServer().getPluginManager().getPlugin("GardensCrops") != null) {
            register(new CropsListener());
        }
        // only Bedrock players (through Geyser/Floodgate) need the anvil workaround
        if (GardensEnchants.plugin.getServer().getPluginManager().getPlugin("floodgate") != null) {
            register(new BedrockAnvilListener());
        }
    }

    private static void register(Listener listener) {
        GardensEnchants.plugin.getServer().getPluginManager().registerEvents(listener, GardensEnchants.plugin);
    }

}
