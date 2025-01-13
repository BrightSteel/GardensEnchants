package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import de.themoep.inventorygui.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EnchantingMenu {

    private InventoryGui gui;
    private Player viewer;

    public static String[] layout = new String[]{
            "bbbbbbbbb",
            "bbbbbbbbb",
            "bbbbbbbbb",
            "   p n   "
    };

    public EnchantingMenu(Player viewer) {
        this.gui = new InventoryGui(GardensEnchants.plugin, viewer, "&fHey", layout);
//        gui.setOutsideAction();
        this.viewer = viewer;
        initializeElements();
    }

    private void initializeElements() {
        GuiElementGroup grp = new GuiElementGroup('b');
        for (var rarity : EnchantRarity.values()) {
            var enchantList = GardensEnchantments.INSTANCE.getEnchantments(rarity);
            for (var enchant : enchantList) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
//                ItemStack book = enchant.getBook(); // todo re-impl
                ItemMeta meta = book.getItemMeta();
                String minLvl = RomanNumber.toRoman(1);
                // todo re-impl
//                String minLvl = RomanNumber.toRoman(enchant.getStartLevel());
                String maxLvl = RomanNumber.toRoman(enchant.getMaxLevel());
                if (meta != null) {
                    if (minLvl.equals(maxLvl)) {
                        meta.setDisplayName(meta.getDisplayName() + " (" + minLvl + ")");
                    }
                    else {
                        meta.setDisplayName(meta.getDisplayName() + " (" + minLvl + "-" + maxLvl + ")");
                    }
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
