package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
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
    private static final String DESTRUCTION_KEY = "destruction";
    private static final String RESTORATIONS_KEY = "restorations";
    private static final String LEVEL_KEY = "level";

    public static final int ENCHANT_GENERATE_COUNT = 3;
    public static final int MAX_RESTORATIONS = 5;

    public Queue<EnchantmentInstance> getRandomEnchantments() {
        Queue<EnchantmentInstance> enchantments = new LinkedList<>();
        for (int i = 0; i < ENCHANT_GENERATE_COUNT; i++) {
            enchantments.add(getRandomEnchantment());
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

    private EnchantmentInstance getRandomEnchantment() {
        var enchantmentPool = GardensEnchantments.INSTANCE.getEnchantments(ProbabilityUtil.rollRarity())
                .stream()
                .filter(GardensEnchantment::isVisibleInTable)
                .toList();
        if (enchantmentPool.isEmpty()) { // no enchantments registered under rarity
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

    public void reduceDestructionChance(BookInstance bookInstance, int amount) {
        NBT.modify(bookInstance.book(), nbt -> {
            reduceDestructionNBT(nbt, amount);
        });
        // refresh bookInstance
        bookInstance = getBookInstance(bookInstance.book());
        if (bookInstance == null) {
            throw new RuntimeException("Failed to refresh book instance after changing destruction chance!");
        }
        refreshLore(bookInstance);
    }

    private void reduceDestructionNBT(ReadWriteItemNBT nbt, int amount) {
        var bookNBT = nbt.getCompound(BOOK_NBT_KEY);
        if (bookNBT == null) {
            return;
        }
        var destruction = bookNBT.getInteger(DESTRUCTION_KEY);
        if (destruction == null) {
            return;
        }
        bookNBT.setInteger(DESTRUCTION_KEY, Math.max(destruction - amount, 0));
    }

    private void refreshLore(BookInstance bookInstance) {
        var book = bookInstance.book();
        var meta = book.getItemMeta();
        if (meta != null) {
            var lore = bookInstance.enchantment().getLore()
                    .stream()
                    .map(component -> Component.text()
                            .append(component)
                            // remove italics from lore
                            // hopefully gets overridden by italic tags in config file
                            .decoration(TextDecoration.ITALIC, false)
                            .build())
                    .collect(Collectors.toList());
            var destruction = bookInstance.destruction();
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
                        .content("%s/%s Restorations".formatted(restorations, MAX_RESTORATIONS))
                        .color(NamedTextColor.RED)
                        .build()
                );
            }
            meta.lore(lore);
            book.setItemMeta(meta);
        }
    }

    public ItemStack getBookItem(GardensEnchantment enchantment) {
        ItemStack book = GardensFurniture.itemRegistry.buildItem(enchantment.bookIdentifier());
        if (book == null) {
            book = new ItemStack(Material.ENCHANTED_BOOK);
        }
        return book;
    }

    public BookInstance buildBook(EnchantmentInstance enchantmentInstance) {
        var enchantment = enchantmentInstance.getEnchantment();
        var destruction = enchantment.getRarity().getDestructionLevel().generateDestructionChance();
        return buildBook(enchantment, enchantmentInstance.getLevel(), destruction);
    }

    private BookInstance buildBook(GardensEnchantment enchantment, int level, int destruction) {
        var book = getBookItem(enchantment);
        // set displayName
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            var display = meta.displayName();
            if (display == null) {
                display = Component.text(enchantment.getDisplayName());
            }
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(enchantment.getRarity().getTextColor())
                    .append(Component.text(enchantment.getRarity().getTag() + " ").color(NamedTextColor.WHITE))
                    .append(display)
                    .append(Component.text(" " + RomanNumber.toRoman(level)))
                    .build());
            book.setItemMeta(meta);
        }
        // modify NBT
        NBT.modify(book, nbt -> {
            var bookNbt = nbt.getOrCreateCompound(BOOK_NBT_KEY);
            bookNbt.setString(ENCHANTMENT_KEY, enchantment.getId());
            bookNbt.setInteger(LEVEL_KEY, level);
            bookNbt.setInteger(DESTRUCTION_KEY, destruction);
        });
        var bookInstance = new BookInstance(book, enchantment, level, destruction, 0);
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
        return buildBook(bookPrimary.enchantment(), bookPrimary.level() + 1, bookPrimary.destruction());
    }

}
