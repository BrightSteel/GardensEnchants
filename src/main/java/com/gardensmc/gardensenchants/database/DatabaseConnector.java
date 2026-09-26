package com.gardensmc.gardensenchants.database;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.database.entry.MySQLEntry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DatabaseConnector {

    public static final DatabaseConnector INSTANCE = new DatabaseConnector();
    private final HikariDataSource dataSource;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "GardensEnchants-DB");
        thread.setDaemon(true);
        return thread;
    });

    private DatabaseConnector() {
        HikariConfig hikariConfig = new HikariConfig();
        ConfigurationSection configSection = Objects.requireNonNull(GardensEnchants.plugin.getConfig().getConfigurationSection("database"));
        String db = configSection.getString("databaseName");
        String host = configSection.getString("host");
        String port = configSection.getString("port");
        String username = configSection.getString("username");
        String password = configSection.getString("password");

        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(3);
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
        hikariConfig.setJdbcUrl(url);
        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Single thread for all table-offer queries. Keeping them in submission order means a save
     * queued after a purchase always lands before any later load of the same row.
     */
    public Executor getExecutor() {
        return executor;
    }

    public void executeUpdate(String string, Object... obj) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(string)) {
            for (int i = 0; i < obj.length; i++) {
                ps.setObject(i + 1, obj[i]);
            }
            ps.executeUpdate();
        }
    }

    public <T extends MySQLEntry> T querySingleton(String query, Supplier<T> supplier, Object... obj) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < obj.length; i++) {
                ps.setObject(i + 1, obj[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                T mySQLEntry = supplier.get();
                mySQLEntry.populate(rs);
                return mySQLEntry;
            }
        }
    }

    /**
     * Lets queued queries finish, then closes the pool.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        dataSource.close();
    }

}
