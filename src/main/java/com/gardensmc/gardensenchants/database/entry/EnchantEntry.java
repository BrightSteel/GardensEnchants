package com.gardensmc.gardensenchants.database.entry;

import com.gardensmc.gardensenchants.database.entry.nested.EnchantJsonEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnchantEntry extends MySQLEntry {

    private EnchantJsonEntry enchantJsonEntry;

    @Override
    public void populate(ResultSet resultSet) throws SQLException {
        var enchants = resultSet.getString("enchants");
        if (enchants != null) {
            enchantJsonEntry = new EnchantJsonEntry(enchants);
        }
    }

}
