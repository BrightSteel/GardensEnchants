package com.gardensmc.gardensenchants.books;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.tr7zw.nbtapi.NBT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

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

    // restrict id -> rarity -> enchants the table may offer, see buildTablePools
    private final Map<String, Map<EnchantRarity, List<GardensEnchantment>>> tablePools = new ConcurrentHashMap<>();
    public static final int MAX_RESTORATIONS = 3;

    /**
     * Rolls a fresh set of table offers for the type. Never contains nulls, but may hold fewer than
     * {@link #ENCHANT_GENERATE_COUNT} offers if the type has no enchants at all.
     */
    public List<EnchantmentInstance> getRandomEnchantments(String type) {
        List<EnchantmentInstance> enchantments = new ArrayList<>();
        // no enchant twice in one roll; a type with fewer enchants than offers just shows fewer
        Set<GardensEnchantment> shown = new HashSet<>();
        for (int i = 0; i < ENCHANT_GENERATE_COUNT; i++) {
            var enchantment = getRandomEnchantment(type, shown);
            if (enchantment != null) {
                enchantments.add(enchantment);
                shown.add(enchantment.getEnchantment());
            }
        }
        return enchantments;
    }

    /**
     * Precomputes the table pool for every restrict and rarity. Must run on the main thread after
     * enchantments are registered; offers are later generated off-thread from these lists.
     */
    public void buildTablePools() {
        tablePools.clear();
        EnchantRestricts.registeredEnchantRestricts.values().forEach(restrict -> {
            var samples = restrict.getSampleItems();
            Map<EnchantRarity, List<GardensEnchantment>> byRarity = new EnumMap<>(EnchantRarity.class);
            for (var rarity : EnchantRarity.values()) {
                var pool = GardensEnchantments.INSTANCE.getEnchantments(rarity)
                        .stream()
                        // eligible if the enchant can go on any item this type covers
                        // (e.g. a pickaxe or an "all" book can roll "excavation" enchants)
                        .filter(enchantment -> enchantment.isVisibleInTable() && restrict.overlaps(enchantment.getRestrict()))
                        // and really applies to one of them: vanilla overrides like fortune -> "tools" also cover swords
                        .filter(enchantment -> samples.stream().anyMatch(enchantment::canEnchant))
                        .toList();
                byRarity.put(rarity, pool);
            }
            tablePools.put(restrict.getId(), byRarity);
        });
    }

    public List<GardensEnchantment> getTablePool(String type, EnchantRarity rarity) {
        var byRarity = tablePools.get(type);
        return byRarity == null ? List.of() : byRarity.getOrDefault(rarity, List.of());
    }

    /**
     * Whether a stored offer can still be offered for the type (the enchant exists, is in the pool
     * and the level is valid). Offers stored before a config change may no longer be.
     */
    public boolean isValidOffer(String type, @Nullable EnchantmentInstance offer) {
        if (offer == null || offer.getEnchantment() == null) {
            return false;
        }
        var enchantment = offer.getEnchantment();
        return offer.getLevel() >= 1
                && offer.getLevel() <= enchantment.getMaxLevel()
                && getTablePool(type, enchantment.getRarity()).contains(enchantment);
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
                GardensEnchantments.INSTANCE.warnUnknown(enchantId);
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

    @Nullable
    private EnchantmentInstance getRandomEnchantment(String type, Set<GardensEnchantment> exclude) {
        if (!tablePools.containsKey(type)) {
            GardensEnchants.plugin.getLogger().log(Level.SEVERE, "Unable to find enchant restrict with id: " + type);
            return null;
        }
        // an empty rarity used to produce a null offer that bricked the table; fall back instead
        var rarity = OfferRarity.nearestAvailable(ProbabilityUtil.rollRarity(),
                r -> getTablePool(type, r).stream().anyMatch(enchantment -> !exclude.contains(enchantment)));
        if (rarity == null) {
            GardensEnchants.plugin.getLogger().severe("Could not find any table enchant for type %s!".formatted(type));
            return null;
        }
        var enchantmentPool = getTablePool(type, rarity).stream().filter(enchantment -> !exclude.contains(enchantment)).toList();
        var enchantment = enchantmentPool.get(ProbabilityUtil.getRandomIndex(enchantmentPool.size()));
        var level = ProbabilityUtil.rollLevel(enchantment.getMaxLevel());
        return new EnchantmentInstance(enchantment, level);
    }

    /** "Incompatible with:" and the enchantments that can't share an item with this one, a few to a line; none: nothing. */
    public static List<Component> incompatibleLore(GardensEnchantment enchantment) {
        var names = enchantment.getIncompatible().stream().map(GardensEnchantment::getDisplayName).toList();
        if (names.isEmpty()) return List.of();
        var lines = new ArrayList<Component>();
        lines.add(Component.text("Incompatible with:", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        var line = new StringBuilder();
        for (var name : names) {
            if (!line.isEmpty() && line.length() + name.length() > 32) {
                lines.add(Component.text(line + ",", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(", ");
            line.append(name);
        }
        lines.add(Component.text(line.toString(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        return lines;
    }

    /** Rewrites one of our books' lore from its enchant as it is now. False if it isn't one of our books. */
    public boolean refreshBook(ItemStack book) {
        if (book == null || book.getType() != Material.ENCHANTED_BOOK) return false;
        var instance = getBookInstance(book);
        if (instance == null) return false;
        refreshLore(instance);
        return true;
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
            lore.addAll(incompatibleLore(bookInstance.enchantment()));
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
