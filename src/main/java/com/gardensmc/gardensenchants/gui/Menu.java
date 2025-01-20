package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensfurniture.GardensFurniture;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

import static com.gardensmc.gardensenchants.utils.MessageUtil.parseIntoLegacy;

public class Menu {

    protected Player viewer;

    protected InventoryGui gui;
    protected ItemStack blankItem;

    public Menu(String[] layout, String title, Player viewer) {
        this.viewer = viewer;
        this.gui = new InventoryGui(
                GardensFurniture.plugin,
                viewer,
                parseIntoLegacy("<white>" + title + "</white>"),
                layout
        );
        this.blankItem = GardensFurniture.itemRegistry.buildItem("blank");
        initializeElements();
    }

    protected void initializeElements() {

    }

    public void open() { gui.show(viewer); }

    public boolean open(Supplier<Menu> menuSupplier) {
        InventoryGui.clearHistory(viewer);
        menuSupplier.get().open();
        return true;
    }

    protected void consoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}