package com.gardensmc.gardensenchants.database;

import com.gardensmc.gardensenchants.database.entry.EnchantEntry;
import com.gardensmc.gardensenchants.database.entry.nested.EnchantJsonEntry;

import java.sql.SQLException;

public final class EnchantTable implements Table {

    @Override
    public String getPrepareStatement() {
        return """
            CREATE TABLE IF NOT EXISTS enchant(
                uuid varchar(36),
                type varchar(20),
                enchants json,
                PRIMARY KEY (uuid, type)
            )
            """;
    }

    public EnchantEntry getEnchantEntry(String uuid, String type) {
        String query = "SELECT enchants FROM enchant WHERE uuid = ? AND type = ?";
        try {
            return DatabaseConnector.INSTANCE.querySingleton(
                    query,
                    EnchantEntry::new,
                    uuid,
                    type
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts or replaces the stored offers for this player and type.
     */
    public void saveEnchantEntry(String uuid, String type, EnchantJsonEntry enchantJson) {
        String update = "INSERT INTO enchant (uuid, type, enchants) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE enchants = VALUES(enchants)";
        try {
            DatabaseConnector.INSTANCE.executeUpdate(update, uuid, type, enchantJson.toJson());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
