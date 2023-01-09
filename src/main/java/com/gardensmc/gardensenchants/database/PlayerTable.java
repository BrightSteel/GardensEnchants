package com.gardensmc.gardensenchants.database;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerTable {

    @Nullable
    public String getLastUsername(String uuid) throws SQLException {
        String query = "SELECT PlayerName FROM players WHERE UUID = ?";
        Connection con = DatabaseConnector.INSTANCE.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, uuid);

        ResultSet rs = ps.executeQuery();
        String username = null;
        if (rs.next()) {
            username = rs.getString(1);
        }

        rs.close();
        ps.close();
        con.close();
        return username;
    }

    /**
     * Using Optifine, used for modified gui offsets
     * @param uuid
     * @return 0 if false, 1 if true
     * @throws SQLException
     */
    public static int usingOptifine(String uuid) throws SQLException {
        String query = "SELECT Optifine FROM players WHERE UUID = ?";
        Connection con = DatabaseConnector.INSTANCE.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, uuid);

        int optifine = 0;
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            optifine = rs.getInt(1);
        }

        rs.close();
        ps.close();
        con.close();
        return optifine;
    }
}
