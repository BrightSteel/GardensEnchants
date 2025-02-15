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
    }

    private static void register(Listener listener) {
        GardensEnchants.plugin.getServer().getPluginManager().registerEvents(listener, GardensEnchants.plugin);
    }

}
