package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableItemNBT;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class TypedBooksHandler {

    private static final String BOOK_NBT_KEY = "GardensTypedBook";
    private static final String TYPE_KEY = "type";
    private static final String DEFAULT_TYPE = EnchantRestricts.ALL.getId();

    public static ItemStack createTypedBook(String enchantRestrictId) {
        var book = GardensFurniture.itemRegistry.buildItem("typed_book_" + enchantRestrictId);
        if (book == null) {
            book = new ItemStack(Material.ENCHANTED_BOOK);
        }
        NBT.modify(book, nbt -> {
            var compound = nbt.getOrCreateCompound(BOOK_NBT_KEY);
            compound.setString(TYPE_KEY, enchantRestrictId);
        });
        return book;
    }

    public static String getTypeFromBook(ItemStack book) {
        var typeId = NBT.get(book, nbt -> (String) getBookType(nbt));
        return typeId != null ? typeId : DEFAULT_TYPE;
    }

    private static String getBookType(ReadableItemNBT nbt) {
        var compound = nbt.getCompound(BOOK_NBT_KEY);
        return compound != null ? compound.getString(TYPE_KEY) : null;
    }
}
