package com.gardensmc.gardensenchants;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gardensmc.gardensenchants.command.*;
import com.gardensmc.gardensenchants.database.EnchantTable;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.events.Events;
import com.gardensmc.gardensenchants.listener.Listeners;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GardensEnchants extends JavaPlugin {

    public static GardensEnchants plugin;
    public static ProtocolManager protocolManager;

    public static Economy econ;
    public static Map<String, VanillaOverride> vanillaOverrides;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {

//        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
//        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

//        EnchantTable.loadTable();
        saveDefaultConfig();
        loadVanillaOverrides();
        GardensEnchantments.INSTANCE.registerEnchantments();
        Listeners.registerListeners();
        PlayerSchedules.scheduleAll();
        protocolManager = ProtocolLibrary.getProtocolManager();

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        // todo - enable economy
//        assert rsp != null;
//        econ = rsp.getProvider();

        Objects.requireNonNull(Bukkit.getPluginCommand("getbook")).setExecutor(new GetBookCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("givebook")).setExecutor(new GiveBookCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("enchant")).setExecutor(new EnchantCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("mobhead")).setExecutor(new MobHeadCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("disenchant")).setExecutor(new DisenchantCommand());

    }

    private void loadVanillaOverrides() {
        var config = loadConfig("vanilla_overrides.yml");
        vanillaOverrides = config.getKeys(false).stream().collect(Collectors.toMap(
                String::toUpperCase,
                s -> new VanillaOverride(Objects.requireNonNull(config.getConfigurationSection(s)))
        ));
    }

    private YamlConfiguration loadConfig(String path) {
        var file = new File(this.getDataFolder(), path);
        if (!file.exists()) {
            try (var resource = getClass().getClassLoader().getResourceAsStream(path)) {
                if (resource != null) {
                    Files.copy(resource, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new IllegalStateException("No resource found for " + path);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

//    @Override
//    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
//        if (!channel.equals("BungeeCord")) return;
//        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(message));
//        try {
//            String subchannel = inputStream.readUTF();
//
//            if (subchannel.equals("GardensResetEnchantCache")) {
//                String uuid = inputStream.readUTF();
//                EnchantCache.refreshPacket(UUID.fromString(uuid));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
