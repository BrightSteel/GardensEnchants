package com.gardensmc.gardensenchants;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gardensmc.gardensenchants.cache.EnchantCache;
import com.gardensmc.gardensenchants.command.EnchantCommand;
import com.gardensmc.gardensenchants.command.GetBookCommand;
import com.gardensmc.gardensenchants.command.GiveBookCommand;
import com.gardensmc.gardensenchants.database.EnchantTable;
import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.events.Events;
import com.gardensmc.gardensenchants.gui.EnchantingGUI;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GardensEnchants extends JavaPlugin implements PluginMessageListener {

    public static GardensEnchants plugin;
    public static ProtocolManager protocolManager;

    public static Economy econ;

    @Override
    public void onLoad() {
        plugin = this;
        CustomEnchantments.initialize();
    }

    @Override
    public void onEnable() {

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        EnchantTable.loadTable();
        getServer().getPluginManager().registerEvents(new Events(), this);
        PlayerSchedules.scheduleAll();
        protocolManager = ProtocolLibrary.getProtocolManager();

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        assert rsp != null;
        econ = rsp.getProvider();

        Objects.requireNonNull(Bukkit.getPluginCommand("getbook")).setExecutor(new GetBookCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("givebook")).setExecutor(new GiveBookCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("enchant")).setExecutor(new EnchantCommand());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) return;
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subchannel = inputStream.readUTF();

            if (subchannel.equals("GardensResetEnchantCache")) {
                String uuid = inputStream.readUTF();
                EnchantCache.refreshPacket(UUID.fromString(uuid));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
