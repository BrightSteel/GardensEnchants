package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.books.BooksHandler;
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

    public static String[] layout = new String[]{
            " bbbbbbb ",
            " bbbbbbb ",
            " bbbbbbb ",
            "   p n   "
    };

    public EnchantingBooksMenu(Player viewer) {
        super(layout, "<white><font:gardens:default>" + getShift(-8) + "\u0121", viewer);
    }

    @Override
    protected void initializeElements() {
        GuiElementGroup grp = new GuiElementGroup('b');
        Collection<GuiElement> books = Arrays.stream(EnchantRarity.values())
                .flatMap(rarity -> GardensEnchantments.INSTANCE.getEnchantments(rarity)
                        .stream()
                        .sorted(Comparator.comparing(GardensEnchantment::getId)))
                .map(enchant -> new StaticGuiElement('a', buildBook(enchant)))
                .collect(Collectors.toList());
        grp.addElements(books);
        gui.addElement(grp);
        gui.addElement(buildBackButton(() -> new EnchantingMenu(viewer), "<gold>Back to Enchanting Table"));
        gui.addElement(buildNextButton());
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
