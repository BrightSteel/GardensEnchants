package com.gardensmc.gardensenchants.database.entry;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class MySQLEntry {

    public abstract void populate(ResultSet resultSet) throws SQLException;

}
