package com.gardensmc.gardensenchants.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class EnchantTable {

    public static void loadTable() {
        DatabaseConnector connector = DatabaseConnector.INSTANCE;
        try {
            String update = "CREATE TABLE IF NOT EXISTS enchant(uuid varchar(36) PRIMARY KEY, enchants varchar(256), expiration DateTime)";
            connector.executeUpdate(update);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Create player's first enchant
     * @param uuid
     * @param enchants
     * @param expiration
     * @throws SQLException
     */
    public static void createEnchant(String uuid, String enchants, LocalDateTime expiration) throws SQLException {
        String insert = "INSERT INTO enchant(uuid, enchants, expiration) VALUES (?, ?, ?)";
        DatabaseConnector.INSTANCE.executeUpdate(insert, uuid, enchants, expiration);
    }

    /**
     * Modify player's existing enchant
     * @param uuid
     * @param enchants
     * @param expiration
     * @throws SQLException
     */
    public static void updateEnchant(String uuid, String enchants, LocalDateTime expiration) throws SQLException {
        String update = "UPDATE enchant SET enchants = ?, expiration = ? WHERE uuid = ?";
        DatabaseConnector.INSTANCE.executeUpdate(update,enchants, expiration, uuid);
    }
//
//    public static EnchantEntry getEnchantEntry(String uuid) throws SQLException {
//        String query = "SELECT enchants, expiration FROM enchant WHERE uuid = ?";
//        Connection conn = DatabaseConnector.INSTANCE.getConnection();
//        PreparedStatement ps = conn.prepareStatement(query);
//        ps.setString(1, uuid);
//
//        EnchantEntry enchantEntry = null;
//        ResultSet rs = ps.executeQuery();
//        if (rs.next()) {
//            String enchants = rs.getString(1);
//            LocalDateTime expiration = rs.getObject(2, LocalDateTime.class);
//            enchantEntry = new EnchantEntry(enchants, expiration);
//        }
//
//        rs.close();
//        ps.close();
//        conn.close();
//
//        return enchantEntry;
//    }
}
