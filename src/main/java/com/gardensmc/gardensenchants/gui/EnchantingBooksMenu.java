package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensfurniture.GardensFurniture;
import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import de.themoep.inventorygui.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class EnchantingBooksMenu extends Menu {

    // a row of item type tabs over the books (1-7, and > for more of them), then the books, then back and next
    public static String[] layout = new String[]{
            " 1234567>",
            " bbbbbbb ",
            " bbbbbbb ",
            " bbbbbbb ",
            "   p n   "
    };
    private static final int TABS_PER_PAGE = 7;

    private final boolean isFullBookSetup;
    private final List<GardensEnchantment> enchants;
    private final List<EnchantTypeTabs.Tab> tabs;
    private final GuiElementGroup books = new GuiElementGroup('b');
    private EnchantTypeTabs.Tab selected;
    private int tabPage;

    public EnchantingBooksMenu(Player viewer, boolean isFullBookSetup) {
        super(layout, "<white><font:gardens:default>" + getShift(-8) + "\u0121", viewer);
        this.isFullBookSetup = isFullBookSetup;
        this.enchants = Arrays.stream(EnchantRarity.values())
                .flatMap(rarity -> GardensEnchantments.INSTANCE.getEnchantments(rarity)
                        .stream()
                        .sorted(Comparator.comparing(GardensEnchantment::getId)))
                .toList();
        this.tabs = EnchantTypeTabs.available(enchants);
        this.selected = tabs.getFirst();
        initElements();
    }

    private void initElements() {
        fillBooks();
        gui.addElement(books);
        for (int i = 0; i < TABS_PER_PAGE; i++) gui.addElement(buildTab(i));
        gui.addElement(buildMoreTabsButton());
        gui.addElement(buildBackButton(() -> new EnchantingMenu(viewer, isFullBookSetup), "<gold>Back to Enchanting Table"));
        gui.addElement(buildNextButton());
    }

    /** The books the selected tab shows. */
    private void fillBooks() {
        books.clearElements();
        books.addElements(enchants.stream()
                .filter(selected::shows)
                .map(enchant -> (GuiElement) new StaticGuiElement('a', buildBook(enchant)))
                .toList());
    }

    /** The tab in a slot of the tab row (0-6), on the tab page shown; empty past the last tab. */
    private DynamicGuiElement buildTab(int slot) {
        char at = (char) ('1' + slot);
        return new DynamicGuiElement(at, () -> {
            int index = tabPage * TABS_PER_PAGE + slot;
            if (index >= tabs.size()) return null;
            var tab = tabs.get(index);
            boolean isSelected = tab == selected;
            long count = enchants.stream().filter(tab::shows).count();
            return new StaticGuiElement(at, EnchantTypeTabs.icon(tab, isSelected), click -> {
                if (tab != selected) {
                    selected = tab;
                    fillBooks();
                    gui.setPageNumber(viewer, 0);
                    gui.draw();
                }
                return true;
            }, (isSelected ? "<gold>" : "<yellow>") + tab.name(),
                    "<gray>" + count + " enchantment" + (count == 1 ? "" : "s"),
                    isSelected ? "<dark_gray>Showing" : "<dark_gray>Click to show");
        });
    }

    /** Shows the next tabs, round to the first again; only there when they don't all fit in the row. */
    private DynamicGuiElement buildMoreTabsButton() {
        int pages = (tabs.size() + TABS_PER_PAGE - 1) / TABS_PER_PAGE;
        return new DynamicGuiElement('>', () -> pages <= 1 ? null
                : new StaticGuiElement('>', GardensFurniture.itemRegistry.buildItem("next_button"), click -> {
                    tabPage = (tabPage + 1) % pages;
                    gui.draw();
                    return true;
                }, "<gold>More item types", "<gray>Page " + (tabPage + 1) + " of " + pages));
    }

    private ItemStack buildBook(GardensEnchantment enchant) {
        var book = BooksHandler.INSTANCE.getBookItem(enchant);
        var meta = book.getItemMeta();
        var maxLvl = RomanNumber.toRoman(enchant.getMaxLevel());
        if (meta != null) {
            var display = Component.text(enchant.getDisplayName());
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(enchant.getRarity().getTextColor())
                    .append(display)
                    .build());
            var lore = new ArrayList<Component>();
            var permittedIcons = enchant.getPermittedItemIcons();
            if (!permittedIcons.isEmpty()) {
                lore.add(Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.WHITE)
                        .font(Key.key("gardens:default"))
                        .append(Component.text(StringUtils.join(permittedIcons, " ")))
                        .build()
                );
            }
            lore.addAll(enchant.getLore()
                    .stream()
                    .map(component -> Component.text()
                            .append(component)
                            // remove italics from lore
                            // hopefully gets overridden by italic tags in config file
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.GRAY) // default color
                            .build())
                    .toList());
            lore.add(Component
                    .text(enchant.getRarity().getTag() + "\u011E")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .font(Key.key("gardens:default"))
            );
            lore.add(Component.text("")); // space
            lore.add(Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("Max Level: " + maxLvl))
                    .build());
            if (!enchant.isVisibleInTable()) {
                lore.add(Component
                        .text("Un-Available in Table", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                );
            }
            meta.lore(lore);
            book.setItemMeta(meta);
        }
        return book;
    }

    public void open() {
        gui.show(viewer);
    }

}
