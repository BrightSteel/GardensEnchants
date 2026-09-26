package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.cache.OnlinePlayerCache;
import com.gardensmc.gardensenchants.command.*;
import com.gardensmc.gardensenchants.database.DatabaseConnector;
import com.gardensmc.gardensenchants.database.Tables;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.gui.Menu;
import com.gardensmc.gardensenchants.listener.Listeners;
import com.gardensmc.gardensenchants.relic.Relic;
import com.gardensmc.gardensenchants.utils.ItemUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

public class GardensEnchants extends JavaPlugin {

    public static GardensEnchants plugin;

    public static Economy econ;
    public static Map<String, VanillaOverride> vanillaOverrides;
    public static Map<String, Relic> relics;
    private static boolean databaseAvailable;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        loadVanillaOverrides();
        loadRelics();
        ItemUtils.loadMobHeads();
        GardensEnchantments.INSTANCE.registerEnchantments();
        BooksHandler.INSTANCE.buildTablePools();

        // the table needs both the economy and the database; if either is missing only the table
        // is turned off, instead of the whole plugin (and every enchant effect) failing to enable
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp == null ? null : rsp.getProvider();
        if (econ == null) {
            getLogger().severe("No Vault economy found, the enchanting table is disabled");
        }
        try {
            Tables.initializeClass();
            databaseAvailable = true;
        } catch (Throwable e) { // the pool and table are created in static initialisers
            getLogger().log(Level.SEVERE, "Could not set up the database, the enchanting table is disabled", e);
        }

        Listeners.registerListeners();
        PlayerSchedules.scheduleAll();

        Objects.requireNonNull(Bukkit.getPluginCommand("getbook")).setExecutor(new GetBookCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("givebook")).setExecutor(new GiveBookCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("enchant")).setExecutor(new EnchantCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("mobhead")).setExecutor(new MobHeadCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("disenchant")).setExecutor(new DisenchantCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("lore")).setExecutor(new LoreCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("fixbook")).setExecutor(new FixBookCommand());

        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> Bukkit.getOnlinePlayers().forEach(this::tickPlayer),
                0,
                2
        );
    }

    @Override
    public void onDisable() {
        // close our menus so items in the table slot go back to their owners
        Menu.closeAll();
        if (databaseAvailable) {
            DatabaseConnector.INSTANCE.shutdown();
        }
    }

    /**
     * Whether the enchanting table can be used (economy and database both set up).
     */
    public static boolean isTableAvailable() {
        return econ != null && databaseAvailable;
    }

    private void tickPlayer(Player player) {
        OnlinePlayerCache.INSTANCE.getPlayerData(player.getUniqueId()).tick(2);
        if (player.isSneaking()) {
            if (player.isOnGround()) {
                handleJumpBoost(player);
            }
        }
        // 1 heart = 2 health, matching the lore ("1 heart or lower"); it used to be half a heart
        if (player.getHealth() <= 2.0) {
            var boots = player.getInventory().getBoots();
            if (boots != null && boots.getType() != Material.AIR) {
                var speedLvl = CustomEnchantments.ESCAPE_ARTIST.getEnchantmentLevel(boots);
                if (speedLvl > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, speedLvl));
                }
            }
        }
    }

    private void handleJumpBoost(Player player) {
        var boots = player.getInventory().getBoots();
        if (boots == null || boots.getType() == Material.AIR) {
            return;
        }
        var rocketLvl = CustomEnchantments.ROCKET_BOOTS.getEnchantmentLevel(boots);
        if (rocketLvl > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 5, rocketLvl));
        }
    }

    private void loadVanillaOverrides() {
        var config = loadConfig("vanilla_overrides.yml");
        vanillaOverrides = new HashMap<>();
        for (var key : config.getKeys(false)) {
            // one bad entry (e.g. a rarity typo) is skipped instead of stopping the plugin
            try {
                vanillaOverrides.put(key.toUpperCase(), new VanillaOverride(Objects.requireNonNull(config.getConfigurationSection(key))));
            } catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Skipping invalid vanilla override '%s'".formatted(key), e);
            }
        }
    }

    public void loadRelics() {
        var config = GardensEnchants.plugin.loadConfig("relic_mappings.yml");
        relics = new HashMap<>();
        for (var key : config.getKeys(false)) {
            try {
                var relic = new Relic(key, Objects.requireNonNull(config.getConfigurationSection(key)));
                relics.put(relic.getId(), relic);
            } catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Skipping invalid relic '%s'".formatted(key), e);
            }
        }
    }

    public YamlConfiguration loadConfig(String path) {
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

}
