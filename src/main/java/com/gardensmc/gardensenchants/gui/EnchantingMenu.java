package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BookInstance;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.FontMappings;
import com.gardensmc.gardensenchants.utils.OraxenShift;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.StaticGuiElement;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

import static com.gardensmc.gardensenchants.books.BooksHandler.ENCHANT_GENERATE_COUNT;
import static com.gardensmc.gardensenchants.utils.MessageUtil.parseIntoLegacy;

public class EnchantingMenu extends Menu {

    private static final String[] layout = new String[] {
            "ab cccccd",
            "   eeeeef",
            " i gggggh"
    };
    private static final int STORAGE_SLOT = 1;
    private GuiStorageElement bookStorage;
    private final List<DynamicRow> dynamicRows = List.of(
            new DynamicRow('c', 'd'),
            new DynamicRow('e', 'f'),
            new DynamicRow('g', 'h')
    );

    public EnchantingMenu(Player viewer) {
        super(layout, "ꐤ\uE094", viewer);
        initElements();
    }

    private void initElements() {
        // blank item to allow shift clicking book into inventory
        gui.addElement(new StaticGuiElement(
                'a',
                blankItem,
                " "
        ));
        var inventory = Bukkit.createInventory(null, InventoryType.CHEST);
        this.bookStorage = new GuiStorageElement(
                'b',
                inventory,
                STORAGE_SLOT,
                // run one tick in future to allow storage to properly update
                () -> Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, this::update, 1),
                null
        );
        gui.addElement(bookStorage);
        gui.setCloseAction(close -> onClose());
        // dynamic rows
        dynamicRows.forEach(dynamicRow -> Arrays
                .stream(Column.values())
                .forEach(column -> gui.addElement(getDynamicBookElement(dynamicRow, column))));
        // books menu
        gui.addElement(new StaticGuiElement(
                'i',
                GardensFurniture.itemRegistry.buildItem("enchanting_books_menu"),
                click -> {
                    onClose(); // run on-close since we aren't actually closing the menu
                    return open(() -> new EnchantingBooksMenu(viewer));
                },
                parseIntoLegacy("<blue>View Enchanting Books")
        ));
    }

    private boolean onClose() {
        var bookItem = bookStorage.getItem(viewer, STORAGE_SLOT);
        if (bookItem != null) {
            viewer.getInventory().addItem(bookItem);
        }
        return true;
    }

    private void update() {
        String title;
        if (storageIsEnchantable()) {
            // fetch enchantment instances
            var enchantments = BooksHandler.INSTANCE.getRandomEnchantments();
            for (var dynamicRow : dynamicRows) {
                var enchantment = enchantments.poll();
                if (enchantment == null) {
                    break;
                }
                dynamicRow.bookInstance = BooksHandler.INSTANCE.buildBook(enchantment);
            }
            title = getPopulatedTitle();
        } else {
            // remove enchantment instances
            dynamicRows.forEach(dynamicRow -> dynamicRow.setBookInstance(null));
            // blank title
            title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE094");
        }
        gui.draw();
        // update current title, looks like gui#setTitle requires a show or draw to be called after that re-creates inventory
        viewer.getOpenInventory().setTitle(title);
    }

    private String getPopulatedTitle() {
        String[] book = new String[ENCHANT_GENERATE_COUNT];
        for (int i = 0; i < book.length; i++) {
            var b = dynamicRows.get(i).getBookInstance();
            book[i] = (b != null) ? b.enchantment().getDisplayName() + " " + RomanNumber.toRoman(b.level()) : " ";
        }

        HashMap<Character, Character>[] fontMaps = new HashMap[]{FontMappings.INSTANCE.fontMap1, FontMappings.INSTANCE.fontMap2, FontMappings.INSTANCE.fontMap3};
        String[] sample = new String[ENCHANT_GENERATE_COUNT];
        for (int i = 0; i < ENCHANT_GENERATE_COUNT; i++) {
            String raw = stripFormatting(book[i]);
            String padding = FontMappings.getPadding(raw);
            sample[i] = FontMappings.INSTANCE.replaceAll(raw, fontMaps[i]) + padding;
        }
        return ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE05F" + OraxenShift.INSTANCE.getShift(193, true) + sample[0]  + sample[1]  + sample[2]);
    }

    private String stripFormatting(String formatted) {
        var maxLength = 15;
        if (formatted.length() < maxLength) {
            return formatted; // already small enough
        }
        var numeralIndex = formatted.lastIndexOf(" ") + 1;
        var numeral = formatted.substring(numeralIndex);
        return formatted.substring(0, Math.min(maxLength, numeralIndex - 1)) + " " + numeral;
    }

    private DynamicGuiElement getDynamicBookElement(DynamicRow dynamicRow, Column column) {
        var c = column == Column.DISPLAY ? dynamicRow.display : dynamicRow.book;
        return new DynamicGuiElement(c, viewer -> new StaticGuiElement(
                c,
                getBookDisplayItem(dynamicRow.bookInstance, column),
                click -> {
                    if (dynamicRow.getBookInstance() != null) {
                        bookStorage.setStorageItem(
                                viewer, STORAGE_SLOT, dynamicRow.getBookInstance().book()
                        );
                    }
                    return true;
                },
                dynamicRow.getDisplayText()
        ));
    }

    private boolean storageIsEnchantable() {
        var storageItem = bookStorage.getStorageItem(viewer, STORAGE_SLOT);
        return storageItem != null && storageItem.getType() == Material.BOOK && storageItem.getAmount() == 1;
    }

    private ItemStack getBookDisplayItem(BookInstance bookInstance, Column column) {
        if (!storageIsEnchantable()) {
            return null;
        }
        return column == Column.DISPLAY ? blankItem : bookInstance.book();
    }

    private boolean isValidItem(ItemStack itemStack) {
        var isEmpty = itemStack == null || itemStack.getType() == Material.AIR;
        return isEmpty || (isBook(itemStack.getType()) && itemStack.getAmount() == 1);
    }

    private boolean isBook(Material material) {
        return material == Material.BOOK || material == Material.ENCHANTED_BOOK;
    }

    @Getter
    private static class DynamicRow {
        private final char display, book;
        @Setter
        private BookInstance bookInstance;

        public DynamicRow(char display, char book) {
            this.display = display;
            this.book = book;
        }

        public String[] getDisplayText() {
            if (bookInstance == null) {
                return null;
            }
            var displayName = LegacyComponentSerializer.legacyAmpersand()
                    .serialize(bookInstance.book().displayName());
            // displayName is wrapped with &e[...] since the component has an array...?
            // todo - replace with a build to re-construct name from default book & enchantment
            displayName = displayName.substring("&e[".length(), displayName.length() - 1);
            var lore = bookInstance.book().lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(Component
                    .text("1 Level(s)", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component
                    .text("1 Amber", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            return Stream.concat(
                    Stream.of(displayName),
                    lore.stream().map(component -> LegacyComponentSerializer.legacyAmpersand().serialize(component))
            ).toArray(String[]::new);
        }
    }

    private enum Column {
        DISPLAY, BOOK
    }
}
