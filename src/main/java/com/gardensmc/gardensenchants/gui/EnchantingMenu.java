package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BookInstance;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.books.TypedBooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantEntryHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.FontMappings;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import com.gardensmc.gardensfurniture.GardensFurniture;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.gardensmc.gardensenchants.books.BooksHandler.ENCHANT_GENERATE_COUNT;

public class EnchantingMenu extends Menu {

    private static final String[] layout = new String[] {
            "ab cccccd",
            " r eeeeef",
            " i gggggh"
    };
    private static final int STORAGE_SLOT = 1;
    static final String NEEDS_BOOKSHELVES = "<red>Epic and Legendary enchants need 15 bookshelves around the table!";
    // ignores a re-roll click this soon after the last one, so a double click doesn't pay twice
    private static final long REROLL_COOLDOWN_MS = 500;
    private GuiStorageElement bookStorage;
    private final List<DynamicRow> dynamicRows = List.of(
            new DynamicRow('c', 'd'),
            new DynamicRow('e', 'f'),
            new DynamicRow('g', 'h')
    );
    private static final String BG = "\u0116";
    // the offer names' color: a pale version of the red banner's gold (the letters in the pack are white)
    private static final String OFFER_NAME_COLOR = "#F8D877";
    // an offer that can't be taken: the same at half brightness (vanilla's own dimming)
    private static final String OFFER_NAME_COLOR_DIMMED = "#7C6C3B";
    private static final String BG_DARK = "\u0117";

    private final boolean isFullBookSetup;
    // bumped on every refresh so late offer loads for an older item are ignored
    private int updateCount;
    private long lastRerollAt;

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
        // re-roll, shown once there are offers to replace
        gui.addElement(new DynamicGuiElement('r', viewer -> getRerollButton()));
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
        // empty the slot before handing the item back: the "view books" button calls this while
        // the gui is still open for another tick, and a second click could otherwise take a copy
        var bookItem = bookStorage.getStorageItem(viewer, STORAGE_SLOT);
        if (bookItem != null) {
            bookStorage.setStorageItem(viewer, STORAGE_SLOT, null);
            PlayerUtil.giveItem(viewer, bookItem);
        }
        return true;
    }

    private void onApplyStorage() {
        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, this::runUpdate, 1);
    }

    private void runUpdate() {
        if (!isOpen()) {
            return;
        }
        // clear first so rows never show (or sell) offers loaded for a previous item
        dynamicRows.forEach(dynamicRow -> dynamicRow.setBookInstance(null));
        var type = getInsertedType();
        int update = ++updateCount;
        if (type != null) {
            var entryHandler = new EnchantEntryHandler(viewer.getUniqueId(), type);
            var loadedOffers = entryHandler.getLoadedOffers();
            if (loadedOffers != null) {
                showOffers(type, loadedOffers);
                return;
            }
            // load offers off the main thread, then fill the rows if nothing changed meanwhile
            entryHandler.getOffers().whenComplete((offers, throwable) -> {
                if (!GardensEnchants.plugin.isEnabled()) {
                    return;
                }
                Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> {
                    if (update != updateCount || !isOpen() || !type.equals(getInsertedType())) {
                        return;
                    }
                    if (throwable != null) {
                        GardensEnchants.plugin.getLogger().log(Level.SEVERE, "Failed to load enchant offers", throwable);
                        viewer.sendMessage(MiniMessage.miniMessage().deserialize("<red>Could not load enchantments right now. Try again later!"));
                        return;
                    }
                    showOffers(type, offers);
                });
            });
        }
        // blank title until offers arrive (or for items that can't be enchanted)
        redraw("<white><font:gardens:default>" + getShift(-8) + BG_DARK);
    }

    private void showOffers(String type, List<EnchantmentInstance> offers) {
        for (int i = 0; i < dynamicRows.size() && i < offers.size(); i++) {
            var dynamicRow = dynamicRows.get(i);
            dynamicRow.setBookInstance(BooksHandler.INSTANCE.buildBook(offers.get(i)));
            dynamicRow.setOffer(offers.get(i));
            dynamicRow.setType(type);
        }
        redraw(getPopulatedTitle());
    }

    private void redraw(String title) {
        gui.draw();
        PlayerUtil.sendInventoryTitleChange(viewer.getOpenInventory(), MiniMessage.miniMessage().deserialize(title));
    }

    private boolean isOpen() {
        return viewer.isOnline()
                && viewer.getOpenInventory().getTopInventory().getHolder() instanceof InventoryGui.Holder holder
                && holder.getGui() == gui;
    }

    private String getPopulatedTitle() {
        BookInstance[] book = new BookInstance[ENCHANT_GENERATE_COUNT];
        for (int i = 0; i < book.length; i++) {
            book[i] = dynamicRows.get(i).getBookInstance();
        }

        String[] sample = new String[ENCHANT_GENERATE_COUNT];
        for (int i = 0; i < ENCHANT_GENERATE_COUNT; i++) {
            String display = FontMappings.sanitize(getDisplayText(book[i]));
            // dimmed, as vanilla does, when it can't be taken: too dear, or it needs the full bookshelf setup
            var color = book[i] != null && (isLocked(book[i].enchantment()) || !canPurchase(book[i]))
                    ? OFFER_NAME_COLOR_DIMMED : OFFER_NAME_COLOR;
            // typed in the row's font, colored here; then shifted back to where the next row starts
            sample[i] = "<font:" + FontMappings.rowFont(i + 1) + "><" + color + ">" + display
                    + "</" + color + "></font>" + FontMappings.getPadding(display);
        }

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
                    purchase(dynamicRow);
                    return true;
                }
        ));
    }

    /**
     * Applies the selected enchant to the inserted item. When a book is inserted the item becomes
     * the corresponding enchant book (existing behaviour); when a tool is inserted the enchant is
     * applied directly to the tool.
     */
    private void purchase(DynamicRow dynamicRow) {
        var bookInstance = dynamicRow.getBookInstance();
        if (bookInstance == null) {
            return;
        }
        var type = getInsertedType();
        var storageItem = bookStorage.getStorageItem(viewer, STORAGE_SLOT);
        if (type == null || storageItem == null || !type.equals(dynamicRow.getType())) {
            return;
        }
        // the row must still match the stored offer, so each roll can only be bought once
        var entryHandler = new EnchantEntryHandler(viewer.getUniqueId(), type);
        if (!entryHandler.isCurrentOffer(dynamicRows.indexOf(dynamicRow), dynamicRow.getOffer())) {
            runUpdate();
            return;
        }
        var enchantment = bookInstance.enchantment();
        if (isLocked(enchantment)) {
            viewer.sendMessage(MiniMessage.miniMessage().deserialize(NEEDS_BOOKSHELVES));
            return;
        }
        if (storageItem.getType() == Material.BOOK) {
            // book -> becomes the corresponding enchant book, left in the slot
            if (!canPurchase(bookInstance) || !chargePlayer(bookInstance)) {
                return;
            }
            bookStorage.setStorageItem(viewer, STORAGE_SLOT, bookInstance.book());
        } else {
            // tool -> enchant applied directly and left in the slot. once enchanted it no longer
            // generates offers (see getInsertedType), so the player grabs the finished tool out.
            var tool = storageItem.clone();
            var enchantHandler = new GardensEnchantHandler(tool);
            if (!enchantHandler.isEnchantable(enchantment) || !canPurchase(bookInstance) || !chargePlayer(bookInstance)) {
                return;
            }
            enchantHandler.enchant(enchantment, bookInstance.level(), false);
            bookStorage.setStorageItem(viewer, STORAGE_SLOT, tool);
        }
        entryHandler.reroll();
        runUpdate();
    }

    private StaticGuiElement getRerollButton() {
        if (dynamicRows.getFirst().getBookInstance() == null || !storageIsEnchantable()) {
            return new StaticGuiElement('r', (ItemStack) null);
        }
        int cost = getRerollCost();
        int xpCost = getRerollXpCost();
        return new StaticGuiElement(
                'r',
                GardensFurniture.itemRegistry.buildItem("reroll_button"),
                click -> {
                    reroll();
                    return true;
                },
                "<gold>Re-roll Enchantments",
                (viewer.getLevel() >= xpCost ? "<#B7E45A>" : "<red>") + xpCost + " <white><font:gardens:default>\u011F",
                (GardensEnchants.econ.has(viewer, cost) ? "<gold>" : "<red>") + cost + " <white><font:gardens:default>\u0120"
        );
    }

    /**
     * Replaces the offers shown for the inserted item with a fresh roll, for the price of the cheapest offer.
     */
    private void reroll() {
        var type = getInsertedType();
        var row = dynamicRows.getFirst();
        // only the offers the player is looking at, not ones for an item swapped in meanwhile
        if (type == null || row.getBookInstance() == null || !type.equals(row.getType())) {
            return;
        }
        if (System.currentTimeMillis() - lastRerollAt < REROLL_COOLDOWN_MS) {
            return;
        }
        int cost = getRerollCost();
        int xpCost = getRerollXpCost();
        if (!GardensEnchants.econ.has(viewer, cost) || viewer.getLevel() < xpCost) {
            return;
        }
        // the economy refuses zero withdrawals, so a free re-roll skips it
        if (cost > 0 && !GardensEnchants.econ.withdrawPlayer(viewer, cost).transactionSuccess()) {
            return;
        }
        viewer.setLevel(viewer.getLevel() - xpCost);
        lastRerollAt = System.currentTimeMillis();
        Bukkit.getPluginManager().callEvent(new com.gardensmc.gardensenchants.events.TableSpendEvent(
                viewer, "reroll", type, cost, xpCost));
        new EnchantEntryHandler(viewer.getUniqueId(), type).reroll();
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
        runUpdate();
    }

    private int getRerollCost() {
        return getShownBooks().mapToInt(book -> book.enchantment().getTableCost(book.level())).min().orElse(0);
    }

    private int getRerollXpCost() {
        return getShownBooks().mapToInt(book -> book.enchantment().getTableCostInXp(book.level())).min().orElse(0);
    }

    private Stream<BookInstance> getShownBooks() {
        return dynamicRows.stream()
                .map(DynamicRow::getBookInstance)
                .filter(Objects::nonNull);
    }

    // Epic+ offers are shown everywhere, but only sold at a fully surrounded table (or /enchant)
    private boolean isLocked(GardensEnchantment enchantment) {
        return !isFullBookSetup && enchantment.getRarity().needsFullBookSetup();
    }

    private boolean canPurchase(BookInstance book) {
        return GardensEnchants.econ.has(viewer, book.enchantment().getTableCost(book.level()))
                && viewer.getLevel() >= book.enchantment().getTableCostInXp(book.level());
    }

    // false if the economy refused the payment (e.g. the balance changed since canPurchase)
    private boolean chargePlayer(BookInstance book) {
        return BedrockEnchantingMenu.chargePlayer(viewer, book.enchantment(), book.level());
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

    private ItemStack getBookDisplayItem(@Nullable BookInstance bookInstance, Column column) {
        // no offer in this row (still loading, or the item has fewer enchants than rows): an empty row
        if (bookInstance == null || !storageIsEnchantable()) {
            return null;
        }
        var book = bookInstance.book().clone();
        if (column == Column.DISPLAY) {
            book.editMeta(meta -> meta.setItemModel(blankItem.getItemMeta().getItemModel()));
        }
        var lore = book.lore() != null ? book.lore() : new ArrayList<Component>();
        var xpCost = bookInstance.enchantment().getTableCostInXp(bookInstance.level());
        var cost = bookInstance.enchantment().getTableCost(bookInstance.level());
        lore.add(toComponent((viewer.getLevel() >= xpCost ? "<#B7E45A>" : "<red>") + xpCost + " <white><font:gardens:default>\u011F"));
        lore.add(toComponent((GardensEnchants.econ.has(viewer, cost) ? "<gold>" : "<red>") + cost + " <white><font:gardens:default>\u0120"));
        if (isLocked(bookInstance.enchantment())) {
            lore.add(toComponent("<red>Needs 15 bookshelves around the table"));
        }
        book.editMeta(meta -> meta.lore(lore));
        return book;
    }

    @Getter
    private static class DynamicRow {
        private final char display, book;
        @Setter
        private BookInstance bookInstance;
        // the stored offer and item type this row was filled from, re-checked on purchase
        @Setter
        private EnchantmentInstance offer;
        @Setter
        private String type;

        public DynamicRow(char display, char book) {
            this.display = display;
            this.book = book;
        }
    }

    private enum Column {
        DISPLAY, BOOK
    }
}
