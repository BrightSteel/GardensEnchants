package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import de.themoep.inventorygui.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantingBooksMenu extends Menu {

    private Player viewer;

    public static String[] layout = new String[]{
            "bbbbbbbbb",
            "bbbbbbbbb",
            "bbbbbbbbb",
            "   p n   "
    };

    public EnchantingBooksMenu(Player viewer) {
        super(layout, "Enchanting Books", viewer);
//        gui.setOutsideAction();
        this.viewer = viewer;
    }

    @Override
    protected void initializeElements() {
        GuiElementGroup grp = new GuiElementGroup('b');
        for (var rarity : EnchantRarity.values()) {
            var enchantList = GardensEnchantments.INSTANCE.getEnchantments(rarity);
            for (var enchant : enchantList) {
                var book = BooksHandler.INSTANCE.getBookItem(enchant);
                var meta = book.getItemMeta();
                var minLvl = RomanNumber.toRoman(1);
                var maxLvl = RomanNumber.toRoman(enchant.getMaxLevel());
                if (meta != null) {
                    var display = meta.displayName();
                    if (display == null) {
                        display = Component.text(enchant.getDisplayName());
                    }
                    if (minLvl.equals(maxLvl)) {
                        display = display.append(Component.text(" (" + minLvl + ")"));
                    }
                    else {
                        display = display.append(Component.text(" (" + minLvl + "-" + maxLvl + ")"));
                    }
                    meta.displayName(display);
                    book.setItemMeta(meta);
                }
                grp.addElement(new StaticGuiElement('a', book));
            }
        }

        gui.addElement(grp);
        gui.addElement(new GuiPageElement('p', new ItemStack(Material.PAPER), GuiPageElement.PageAction.PREVIOUS, "Previous"));
        gui.addElement(new GuiPageElement('n', new ItemStack(Material.PAPER), GuiPageElement.PageAction.NEXT, "Next"));

    }

    public void open() {
        gui.show(viewer);
    }



}
