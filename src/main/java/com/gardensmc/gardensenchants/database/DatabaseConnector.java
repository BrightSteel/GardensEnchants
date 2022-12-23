package com.gardensmc.gardensenchants.database;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseConnector {

    public static final DatabaseConnector INSTANCE = new DatabaseConnector();
    private final HikariDataSource dataSource;
    public HikariConfig hikariConfig = new HikariConfig();

    public DatabaseConnector() {
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

    public void executeUpdate(String string, Object... obj) throws SQLException {

        Connection connection = dataSource.getConnection();

        PreparedStatement ps = connection.prepareStatement(string);
        for(int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }

        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public boolean queryPresence(String string, Object... obj) throws SQLException {
        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(string);
        for (int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }
        ResultSet rs = ps.executeQuery();
        boolean present = rs.next();
        rs.close();
        ps.close();
        con.close();
        return present;
    }

    public boolean connectTest() {
        try {
            dataSource.getConnection();
        } catch (SQLException throwables) {
            return false;
        }
        return true;
    }
}
