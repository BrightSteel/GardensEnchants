package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BookInstance;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.books.TypedBooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantEntryHandler;
import com.gardensmc.gardensenchants.enchantment.FontMappings;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.StaticGuiElement;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gardensmc.gardensenchants.books.BooksHandler.ENCHANT_GENERATE_COUNT;

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
    private static final String BG = "\u0116";
    private static final String BG_DARK = "\u0117";

    private final boolean isFullBookSetup;

    public EnchantingMenu(Player viewer, boolean isFullBookSetup) {
        super(layout, "<white><font:gardens:default>" + getShift(-8) + BG_DARK, viewer);
        this.isFullBookSetup = isFullBookSetup;
        initElements();
    }

    private void initElements() {
        // blank item to allow shift clicking book into inventory
        gui.addElement(new StaticGuiElement(
                'a',
                blankHiddenItem,
                " "
        ));
        var inventory = Bukkit.createInventory(null, InventoryType.CHEST);
        this.bookStorage = new GuiStorageElement(
                'b',
                inventory,
                STORAGE_SLOT,
                this::onApplyStorage,
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
                    return open(() -> new EnchantingBooksMenu(viewer, isFullBookSetup));
                },
                "<blue>View Enchanting Books"
        ));
    }

    private boolean onClose() {
        var bookItem = bookStorage.getItem(viewer, STORAGE_SLOT);
        if (bookItem != null) {
            viewer.getInventory().addItem(bookItem);
        }
        return true;
    }

    private void onApplyStorage() {
        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, this::runUpdate, 1);
    }

    private void runUpdate() {
        String title;
        if (storageIsEnchantable()) {
            // fetch enchantment instances
            var enchantments = new EnchantEntryHandler(viewer.getUniqueId(), getInsertedType()).getEnchantmentInstances();
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
            title = "<white><font:gardens:default>" + getShift(-8) + BG_DARK;
        }
        gui.draw();
        PlayerUtil.sendInventoryTitleChange(viewer.getOpenInventory(), MiniMessage.miniMessage().deserialize(title));
    }

    private String getPopulatedTitle() {
        BookInstance[] book = new BookInstance[ENCHANT_GENERATE_COUNT];
        for (int i = 0; i < book.length; i++) {
            book[i] = dynamicRows.get(i).getBookInstance();
//            book[i] = (b != null) ? b.enchantment().getDisplayName() + " " + RomanNumber.toRoman(b.level()) : " ";
        }

        HashMap<Character, Character>[] fontMaps = new HashMap[]{FontMappings.INSTANCE.fontMap1, FontMappings.INSTANCE.fontMap2, FontMappings.INSTANCE.fontMap3};
        String[] sample = new String[ENCHANT_GENERATE_COUNT];
        for (int i = 0; i < ENCHANT_GENERATE_COUNT; i++) {
            String display = getDisplayText(book[i]);
            String padding = FontMappings.getPadding(display);
            sample[i] = FontMappings.INSTANCE.replaceAll(display, fontMaps[i]) + padding;
        }
//        return "<white><font:gardens:default>" + getShift(-8) + BG + getShift(-193);

        return "<white><font:gardens:default>" + getShift(-8) + BG + getShift(-193) + sample[0] + sample[1] + sample[2];
    }

    private String getDisplayText(@Nullable BookInstance bookInstance) {
        if (bookInstance == null) {
            return "";
        }
        var name = bookInstance.enchantment().getDisplayName();
        int maxLength = 13;
        if (name.length() <= maxLength) { // can comfortably fit full name and level on screen
            return name + getLevelText(bookInstance);
        }
        return name.substring(0, maxLength) + "...";
    }

    private String getLevelText(BookInstance bookInstance) {
        return bookInstance.enchantment().getMaxLevel() == 1 ? "" : " " + RomanNumber.toRoman(bookInstance.level());
    }

    private DynamicGuiElement getDynamicBookElement(DynamicRow dynamicRow, Column column) {
        var c = column == Column.DISPLAY ? dynamicRow.display : dynamicRow.book;
        return new DynamicGuiElement(c, viewer -> new StaticGuiElement(
                c,
                getBookDisplayItem(dynamicRow.bookInstance, column),
                click -> {
                    purchase(dynamicRow.getBookInstance());
                    return true;
                }
        ));
    }

    /**
     * Applies the selected enchant to the inserted item. When a book is inserted the item becomes
     * the corresponding enchant book (existing behaviour); when a tool is inserted the enchant is
     * applied directly to the tool.
     */
    private void purchase(@Nullable BookInstance bookInstance) {
        if (bookInstance == null) {
            return;
        }
        var type = getInsertedType();
        var storageItem = bookStorage.getStorageItem(viewer, STORAGE_SLOT);
        if (type == null || storageItem == null) {
            return;
        }
        var enchantment = bookInstance.enchantment();
        if (storageItem.getType() == Material.BOOK) {
            // book -> becomes the corresponding enchant book, left in the slot
            if (!canPurchase(enchantment)) {
                return;
            }
            chargePlayer(enchantment);
            bookStorage.setStorageItem(viewer, STORAGE_SLOT, bookInstance.book());
        } else {
            // tool -> enchant applied directly and left in the slot. once enchanted it no longer
            // generates offers (see getInsertedType), so the player grabs the finished tool out.
            var tool = storageItem.clone();
            var enchantHandler = new GardensEnchantHandler(tool);
            if (!enchantHandler.isEnchantable(enchantment) || !canPurchase(enchantment)) {
                return;
            }
            chargePlayer(enchantment);
            enchantHandler.enchant(enchantment, bookInstance.level(), false);
            bookStorage.setStorageItem(viewer, STORAGE_SLOT, tool);
        }
        new EnchantEntryHandler(viewer.getUniqueId(), type).resetEnchantEntry();
    }

    private boolean canPurchase(GardensEnchantment enchantment) {
        return GardensEnchants.econ.has(viewer, enchantment.getTableCost()) && viewer.getLevel() >= enchantment.getTableCostInXp();
    }

    private void chargePlayer(GardensEnchantment enchantment) {
        GardensEnchants.econ.withdrawPlayer(viewer, enchantment.getTableCost());
        viewer.setLevel(viewer.getLevel() - enchantment.getTableCostInXp());
    }

    private boolean storageIsEnchantable() {
        return getInsertedType() != null;
    }

    /**
     * Resolves the enchant-restrict type for the item in the storage slot, or null if it can't be
     * enchanted here. Books (including typed books) use the typed-book type; tools resolve to the
     * same type their matching typed book would use, so a sword yields the sword enchant pool.
     */
    @Nullable
    private String getInsertedType() {
        var storageItem = bookStorage.getStorageItem(viewer, STORAGE_SLOT);
        if (storageItem == null || storageItem.getAmount() != 1) {
            return null;
        }
        if (storageItem.getType() == Material.BOOK) {
            return TypedBooksHandler.getTypeFromBook(storageItem);
        }
        if (storageItem.getType() == Material.ENCHANTED_BOOK) {
            // vanilla/enchanted books aren't enchantable tools here
            return null;
        }
        var restrict = EnchantRestricts.getMatchingType(storageItem);
        if (restrict == null || hasEnchantments(storageItem)) {
            // only clean, unenchanted tools may be enchanted here (mirrors the vanilla table)
            return null;
        }
        return restrict.getId();
    }

    private boolean hasEnchantments(ItemStack item) {
        return !new GardensEnchantHandler(item).getEnchantments().isEmpty();
    }

    private ItemStack getBookDisplayItem(BookInstance bookInstance, Column column) {
        if (!storageIsEnchantable()) {
            return null;
        }
        var book = bookInstance.book().clone();
        if (column == Column.DISPLAY) {
            book.editMeta(meta -> meta.setItemModel(blankItem.getItemMeta().getItemModel()));
        }
        var lore = book.lore() != null ? book.lore() : new ArrayList<Component>();
        var xpCost = bookInstance.enchantment().getTableCostInXp();
        var cost = bookInstance.enchantment().getTableCost();
        lore.add(toComponent((viewer.getLevel() >= xpCost ? "<#B7E45A>" : "<red>") + bookInstance.enchantment().getTableCostInXp() + " <white><font:gardens:default>\u011F"));
        lore.add(toComponent((GardensEnchants.econ.has(viewer, cost) ? "<gold>" : "<red>") + bookInstance.enchantment().getTableCost() + " <white><font:gardens:default>\u0120"));
        book.editMeta(meta -> meta.lore(lore));
        return book;
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
    }

    private enum Column {
        DISPLAY, BOOK
    }
}
