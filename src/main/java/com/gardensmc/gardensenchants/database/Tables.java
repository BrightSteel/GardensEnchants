package com.gardensmc.gardensenchants.database;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Level;

public class Tables {

    public static final EnchantTable enchant = register(EnchantTable::new);

    private static <T extends Table> T register(Supplier<T> tableSupplier) {
        var logger = GardensEnchants.plugin.getLogger();
        var table = tableSupplier.get();
        var tableName =  table.getClass().getSimpleName();
        try {
            // run on main thread as we should wait for initialization to complete
            DatabaseConnector.INSTANCE.executeUpdate(table.getPrepareStatement());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize " + tableName, e);
            Bukkit.getServer().getPluginManager().disablePlugin(GardensEnchants.plugin);
        }
        return table;
    }

    /**
     * Load class into memory, ensuring tables are registered and set up prior to their first use.
     * Could skip this step but means there may be some delay when user firsts interacts with table.
     */
    public static void initializeClass() {
        GardensEnchants.plugin.getLogger().info("Initialized tables");
    }
}
