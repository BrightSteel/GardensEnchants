package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BooksHandler {

    public static final BooksHandler INSTANCE = new BooksHandler();
    private static final String BOOK_NBT_KEY = "GardensEnchantBook";
    private static final String ENCHANTMENT_KEY = "enchantment";
    private static final String DESTRUCTION_KEY = "destructionBase";
    private static final String RESTORATIONS_KEY = "restorations";
    private static final String LEVEL_KEY = "level";
    private static final char RUNE_EMPTY = '\uE246';
    private static final char RUNE_RESTORATION = '\uE244';

    public static final int ENCHANT_GENERATE_COUNT = 3;
    public static final int MAX_RESTORATIONS = 3;

    public Queue<EnchantmentInstance> getRandomEnchantments(String type) {
        Queue<EnchantmentInstance> enchantments = new LinkedList<>();
        for (int i = 0; i < ENCHANT_GENERATE_COUNT; i++) {
            enchantments.add(getRandomEnchantment(type));
        }
        return enchantments;
    }

    @Nullable
    public BookInstance getBookInstance(ItemStack book) {
        return NBT.get(book, nbt -> {
            var bookNBT = nbt.getCompound(BOOK_NBT_KEY);
            if (bookNBT == null) {
                return null;
            }
            var enchantId = bookNBT.getString(ENCHANTMENT_KEY);
            var enchantment = GardensEnchantments.INSTANCE.getEnchantment(enchantId);
            if (enchantment == null) {
                GardensEnchants.plugin.getLogger().log(
                        Level.WARNING,
                        "Enchantment with id %s does not exist".formatted(enchantId)
                );
                return null;
            }
            return new BookInstance(
                    book,
                    enchantment,
                    bookNBT.getInteger(LEVEL_KEY),
                    bookNBT.getInteger(DESTRUCTION_KEY),
                    bookNBT.getInteger(RESTORATIONS_KEY)
            );
        });
    }

    private EnchantmentInstance getRandomEnchantment(String type) {
        var enchantRestrict = EnchantRestricts.registeredEnchantRestricts.get(type);
        if (enchantRestrict == null) {
            GardensEnchants.plugin.getLogger().log(Level.SEVERE, "Unable to \uE11A enchant restrict with id: " + type);
            return null;
        }
        var rarity = ProbabilityUtil.rollRarity();
        var enchantmentPool = GardensEnchantments.INSTANCE.getEnchantments(rarity)
                .stream()
                // eligible if the enchant can go on any item this type covers
                // (e.g. a pickaxe or an "all" book can roll "excavation" enchants)
                .filter(enchantment -> enchantment.isVisibleInTable() && enchantRestrict.overlaps(enchantment.getRestrict()))
                .toList();
        if (enchantmentPool.isEmpty()) { // no enchantments registered under rarity
            GardensEnchants.plugin.getLogger().severe("Could not find enchant with rarity %s for type %s!".formatted(rarity, type));
            return null;
        }
        var enchantment = enchantmentPool.get(ProbabilityUtil.getRandomIndex(enchantmentPool.size()));
        var level = ThreadLocalRandom.current().nextInt(1, enchantment.getMaxLevel() + 1);
        return new EnchantmentInstance(enchantment, level);
    }

//    public BookInstance generateRandomBook() {
//        var enchantmentPool = GardensEnchantments.INSTANCE.getEnchantments(ProbabilityUtil.rollRarity());
//        if (enchantmentPool.isEmpty()) { // no enchantments registered under rarity
//            return null;
//        }
//        var level = 1; // todo impl random level
//        return buildBook(enchantmentPool.get(ProbabilityUtil.getRandomIndex(enchantmentPool.size())), level);
//    }

    public void applyRestoration(BookInstance bookInstance, int amount) {
        NBT.modify(bookInstance.book(), nbt -> {
            increaseRestorationCount(nbt, amount);
        });
        // refresh bookInstance
        bookInstance = getBookInstance(bookInstance.book());
        if (bookInstance == null) {
            throw new RuntimeException("Failed to refresh book instance after changing destructionBase chance!");
        }
        refreshLore(bookInstance);
    }

    private void increaseRestorationCount(ReadWriteItemNBT nbt, int amount) {
        var bookNBT = nbt.getCompound(BOOK_NBT_KEY);
        if (bookNBT == null) {
            return;
        }
        var restorations = bookNBT.getInteger(RESTORATIONS_KEY);
        bookNBT.setInteger(RESTORATIONS_KEY, restorations + amount);
    }

    private void refreshLore(BookInstance bookInstance) {
        var book = bookInstance.book();
        var meta = book.getItemMeta();
        if (meta != null) {
            var lore = new ArrayList<Component>();
            var permittedIcons = bookInstance.enchantment().getPermittedItemIcons();
            if (!permittedIcons.isEmpty()) {
                lore.add(Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.WHITE)
                        .font(Key.key("gardens:default"))
                        .append(Component.text(StringUtils.join(permittedIcons, " ")))
                        .build()
                );
            }
            lore.addAll(bookInstance.enchantment().getLore()
                    .stream()
                    .map(component -> Component.text()
                            .append(component)
                            // remove italics from lore
                            // hopefully gets overridden by italic tags in config file
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.GRAY) // default color
                            .build())
                    .toList());
            var destruction = bookInstance.calculateDestructionChance();
            if (destruction != 0) {
                lore.add(Component.text()
                                .append(Component
                                        .text("Destruction Chance: ", TextColor.color(0xFF5555))
                                        .decoration(TextDecoration.ITALIC, false))
                                .append(Component.
                                        text(destruction + "%", TextColor.color(0xAAAAAA))
                                        .decoration(TextDecoration.ITALIC, false))
                                .build()
                );
            }
            var restorations = bookInstance.restorations();
            if (restorations != 0) {
                lore.add(Component.text()
                        .content(getRestorationDisplay(restorations))
                        .color(NamedTextColor.WHITE)
                        .font(Key.key("gardens:default"))
                        .decoration(TextDecoration.ITALIC, false)
                        .build()
                );
            }
            lore.add(Component
                    .text(bookInstance.enchantment().getRarity().getTag() + "\u011E")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .font(Key.key("gardens:default"))
            );
            meta.lore(lore);
            book.setItemMeta(meta);
        }
    }

    private String getRestorationDisplay(int restorations) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.repeat(RUNE_RESTORATION + " ", restorations);
        if (restorations < MAX_RESTORATIONS) {
            stringBuilder.repeat(RUNE_EMPTY + " ", MAX_RESTORATIONS - restorations);
        }
        return stringBuilder.toString();
    }

    public ItemStack getBookItem(GardensEnchantment enchantment) {
        ItemStack book = GardensFurniture.itemRegistry.buildItem(enchantment.bookIdentifier());
        if (book == null) {
            book = new ItemStack(Material.ENCHANTED_BOOK);
        }
        book.editMeta(meta -> meta.setTooltipStyle(NamespacedKey.fromString("gardens:" + enchantment.getRarity().toString().toLowerCase())));
        return book;
    }

    public BookInstance buildBook(EnchantmentInstance enchantmentInstance) {
        var enchantment = enchantmentInstance.getEnchantment();
        var destruction = enchantment.getRarity().getDestructionLevel().generateDestructionChance();
        return buildBook(enchantment, enchantmentInstance.getLevel(), destruction, 0);
    }

    private BookInstance buildBook(GardensEnchantment enchantment, int level, int destruction, int restorations) {
        var book = getBookItem(enchantment);
        // set displayName
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            var display = Component.text(enchantment.getDisplayName());
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(enchantment.getRarity().getTextColor())
                    .append(display)
                    .append(Component.text(enchantment.getMaxLevel() == 1 ? "" : " " + RomanNumber.toRoman(level)))
                    .build());
            book.setItemMeta(meta);
        }
        // modify NBT
        NBT.modify(book, nbt -> {
            var bookNbt = nbt.getOrCreateCompound(BOOK_NBT_KEY);
            bookNbt.setString(ENCHANTMENT_KEY, enchantment.getId());
            bookNbt.setInteger(LEVEL_KEY, level);
            bookNbt.setInteger(DESTRUCTION_KEY, destruction);
            bookNbt.setInteger(RESTORATIONS_KEY, restorations);
        });
        var bookInstance = new BookInstance(book, enchantment, level, destruction, restorations);
        refreshLore(bookInstance);
        return bookInstance;
    }

    @Nullable
    public BookInstance combineBooks(BookInstance bookPrimary, BookInstance bookSecondary) {
        if (!bookPrimary.enchantment().getId().equals(bookSecondary.enchantment().getId())) {
            return null;
        }
        if (bookPrimary.level() != bookSecondary.level()) {
            return null;
        }
        if (bookPrimary.level() == bookPrimary.enchantment().getMaxLevel()) {
            return null;
        }
        return buildBook(
                bookPrimary.enchantment(),
                bookPrimary.level() + 1,
                bookPrimary.destructionBase(),
                bookPrimary.restorations()
        );
    }

}
