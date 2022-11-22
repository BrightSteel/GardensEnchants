package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnchantMenu {

    private InventoryGui gui;
    private Player viewer;

    public static String[] layout = new String[]{
            " s aaaaab",
            "   cccccd",
            " r eeeeef"
    };

    public EnchantMenu(Player viewer) {
        this.gui = new InventoryGui(GardensEnchants.plugin, viewer, "&f:offset_-8:\uE05F", layout);
//        gui.setOutsideAction();
        this.viewer = viewer;
        initializeElements();
    }

    private void initializeElements() {
        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST);
        gui.addElement(new GuiStorageElement('s', inv));
        gui.setOutsideAction(action -> {
            return true;
        });
//        gui.setCloseAction(close -> {
//        });
        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.PAPER)));
        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.PAPER)));
        gui.addElement(new StaticGuiElement('e', new ItemStack(Material.PAPER)));
        gui.addElement(new StaticGuiElement('r', new ItemStack(Material.PAPER)));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.PAPER)));
        gui.addElement(new StaticGuiElement('d', new ItemStack(Material.PAPER)));
        gui.addElement(new StaticGuiElement('f', new ItemStack(Material.PAPER)));
    }

    public void open() {
        gui.show(viewer);
    }



}
