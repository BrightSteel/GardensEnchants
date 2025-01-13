package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.ChatUtils;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BooksHandler {

    public static final BooksHandler INSTANCE = new BooksHandler();

    public ItemStack generateRandomBook() {
        var enchantmentPool = GardensEnchantments.INSTANCE.getEnchantments(ProbabilityUtil.rollRarity());
        if (enchantmentPool.isEmpty()) { // no enchantments registered under rarity
            return null;
        }
        var level = 1; // todo impl random level
        return buildBook(enchantmentPool.get(ProbabilityUtil.getRandomIndex(enchantmentPool.size())), level);
    }

    private ItemStack buildBook(GardensEnchantment enchantment, int level) {
        ItemStack book = GardensFurniture.itemRegistry.buildItem(enchantment.getId() + "_book");
        if (book == null) {
            return null;
        }
        int destruction = 1; // todo impl random destruction
        // modify displayName and lore
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text().append(meta.displayName()).content(" " + level).build());
            meta.lore(List.of(
                    Component.text()
                            .color(TextColor.fromHexString("FF5555"))
                            .content("Destruction Chance: ")
                            .color(TextColor.fromHexString("AAAAAA"))
                            .content(destruction + "%")
                            .build()
            ));
            book.setItemMeta(meta);
        }
        // modify NBT
        // todo nest this in a enchant component
        NBT.modify(book, nbt -> {
            nbt.setInteger("destruction", destruction);
            nbt.setInteger("level", level);
        });
        return book;
    }

    public static String getColorFromRarity(EnchantRarity rarity) {
        String hex;
        switch (rarity) {
            default -> hex = "#CFD5DD";
            case UNIQUE -> hex = "#F7C941";
            case MYSTICAL -> hex = "#B756FE";
            case DIVINE -> hex = "#91E6DC";
        }
        return ChatUtils.translateHexColorCodes("#", "", hex);
    }

    public static String getRarityTag(EnchantRarity rarity) {
        String tag;
        switch (rarity) {
            default -> tag = "\uE05E";
            case UNIQUE -> tag = "\uE110";
            case MYSTICAL -> tag = "\uE111";
            case DIVINE -> tag = "\uE112";
        }
        return tag;
    }

}
