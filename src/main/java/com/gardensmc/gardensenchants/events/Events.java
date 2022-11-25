package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.gui.EnchantingGUI;
import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import com.gardensmc.gardensenchants.gui.InventoryUpdate;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import com.gardensmc.gardensenchants.utils.ItemUtils;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;

import java.util.*;

public class Events implements Listener {

    @EventHandler
    public static void onRepair(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        if (contents.length > 1) {

            if (contents[0] == null || contents[1] == null) return;

            ItemStack item = contents[0].clone();
            ItemStack book = contents[1];

            // our items adder category
            if (book.getType() != Material.GOLDEN_HORSE_ARMOR) return;

            CustomStack customStack = CustomStack.byItemStack(book);
            if (customStack == null) return;

            Enchantment enchantment = CustomEnchantments.getGardensEnchant(customStack.getNamespacedID());
            if (enchantment == null) return;

            if (enchantment.canEnchantItem(item)) {
                EnchantUtils.enchantItem(item, enchantment, EnchantUtils.getLevelFromName(book));
                inv.setRepairCost(1);
                e.setResult(item);
            }
        }
    }

    @EventHandler
    public void onVeinMine(PlayerVeinMineEvent e) {
        ItemStack itemStack = e.getItem();

        String block_name = e.getBlock().getType().name();
        boolean isWood =  (block_name.endsWith("WOOD") || block_name.endsWith("LOG") || block_name.endsWith("ROOTS"));
        if (itemStack != null) {
            String item_name = itemStack.getType().name();
            if (isWood) {
                if (item_name.endsWith("AXE")) {
                    if (EnchantUtils.hasEnchantment(itemStack, CustomEnchantments.TIMBER)) {
                        return;
                    }
                }
            }
            else {
                if (item_name.endsWith("PICKAXE")) {
                    if (EnchantUtils.hasEnchantment(itemStack, CustomEnchantments.VEIN_MINER)) {
                        return;
                    }
                }
            }
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        if (EnchantUtils.hasEnchantment(e.getItem(), CustomEnchantments.ETERNAL)) {
            int ench_level = EnchantUtils.getEnchantLevel(e.getItem(), CustomEnchantments.ETERNAL);
            if (ProbabilityUtil.roll((ench_level*10)*2)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.SKULL_STEALER)) {
                int ench_level = EnchantUtils.getEnchantLevel(weapon, CustomEnchantments.SKULL_STEALER);
                int percent = ench_level == 1 ? 15 : 30;
                if (ProbabilityUtil.roll(percent)) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), ItemUtils.getHead(e.getEntity().getUniqueId()));
                }
            }
        }
    }

//    @EventHandler
//    public void onInteract(PlayerInteractEvent e) {
//        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
//            e.setCancelled(true);
//            EnchantMenu enchantMenu = new EnchantMenu(e.getPlayer());
//            enchantMenu.open();
//        }
//    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getType() == InventoryType.ENCHANTING && e.getPlayer() instanceof Player player) {
            e.setCancelled(true);
            EnchantingGUI enchantingGUI = new EnchantingGUI();
            player.openInventory(enchantingGUI.getInventory());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;

            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                Inventory clicked = e.getClickedInventory();
                if (clicked != null && clicked.getType() == InventoryType.PLAYER) {
                    System.out.println(e.getInventory().getType().name());
                    if (e.getInventory().getItem(1) != null) {
                        e.setCancelled(true);
                        return;
//                e.getInventory().setItem(0, null);
//                e.getInventory().setItem(1, stack);
                    }
//                else if (e.getSlot() != 1){
//                    e.setCancelled(true);
//                }
                } else if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof EnchantingGUI) {
                    if (e.getSlot() != 1) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            else if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof EnchantingGUI) {
                ItemStack selection = null;
                if (e.getSlot() != 1 && e.getWhoClicked() instanceof Player player) {
                    if (e.getSlot() > 2 && e.getSlot() < 9) {
                        selection = e.getInventory().getItem(8);
                    } else if (e.getSlot() > 11 && e.getSlot() < 18) {
                        selection = e.getInventory().getItem(17);

                    } else if (e.getSlot() > 20 && e.getSlot() < 27) {
                        selection = e.getInventory().getItem(26);

                    } else if (e.getSlot() == 19) {
                        new EnchantingMenu(player).open();
                    }

                    if (selection != null) {
                        e.getInventory().setItem(1, selection);
                        updateEnchantingGUI(player, e.getInventory());
                    }

                    e.setCancelled(true);
                }
            }



        if (e.getWhoClicked() instanceof Player player) {
            if (e.getRawSlot() == 1) {
                if (e.getAction().name().contains("PLACE") || e.getAction().name().contains("PICKUP") || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    updateEnchantingGUI(player, e.getInventory());
                }

            }
            else if (e.getRawSlot() >= 27 && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                updateEnchantingGUI(player, e.getInventory());
            }
        }

        System.out.println("Clicked: " + e.getRawSlot() + "slot, " + e.getCurrentItem() + "item, Action: " + e.getAction().name());


    }

    private void updateEnchantingGUI(Player player, Inventory inv) {
        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
            String title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:&f\uE094");
            if (itemIsEnchantable(inv.getItem(1))) {
                ItemStack[] books = genBooks(inv);
                String book[] = new String[3];
                for (int i = 0; i < book.length; i++) {
                    book[i] = (books[i] != null && books[i].getItemMeta() != null) ? books[i].getItemMeta().getDisplayName() : " ";
                }

                HashMap<Character, Character>[] fontMaps = new HashMap[]{FontMappings.INSTANCE.fontMap1, FontMappings.INSTANCE.fontMap2, FontMappings.INSTANCE.fontMap3};
                String[] sample = new String[3];
                for (int i = 0; i < 3; i++) {
                     String raw = stripFormatting(book[i]);
                     String padding = FontMappings.getPadding(raw, 100);
                     sample[i] = FontMappings.INSTANCE.replaceAll(stripFormatting(book[i]), fontMaps[i]) + padding;
                }

                title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:&f\uE05F:offset_-195:&f" + sample[0] + "&f:offset_-100:&f" + sample[1] + "&f:offset_-100:&f" + sample[2]);
            }
            else {
                removeBooks(inv);
            }

            InventoryUpdate.updateInventory(GardensEnchants.plugin, player, title);

        }, 1);
        // we placed something into an empty
//        if (bookSlot == null) {
//            String title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:&f\uE094");
//
//            InventoryUpdate.updateInventory(GardensEnchants.plugin, player, title);
//
//        }
//         else if (bookSlot.getType() == Material.BOOK){
//

//        }
    }

    // assumes we have a formatted hex code at beginning of display name
    private String stripFormatting(String formatted) {
        if (formatted.length() < 14) {
            return formatted;
        }
        return formatted.substring(14);
    }

    private ItemStack[] genBooks(Inventory inv) {
        ItemStack display = ItemUtils.getCustomItem("gardens:blank_item");
        ItemStack[] books = EnchantUtils.getRandomBooks();
        inv.setItem(8, books[0]);
        inv.setItem(17, books[1]);
        inv.setItem(26, books[2]);

        int j = 0;
        for (ItemStack book : books) {
            ItemMeta displayMeta = display.getItemMeta();
            ItemMeta bookMeta = (book != null) ? book.getItemMeta() : null;
            if (displayMeta != null && bookMeta != null) {
                displayMeta.setDisplayName(bookMeta.getDisplayName());
                displayMeta.setLore(bookMeta.getLore());
                display.setItemMeta(displayMeta);
            }
            for (int i = 3 + (j * 9); i < 8 + (j * 9); i++) {
                inv.setItem(i, display);
            }
            j++;
        }
        return books;
    }

    private void removeBooks(Inventory inv) {
        for (int j = 0; j < 3; j++) {
            for (int i = 3; i < 9; i++) {
                inv.setItem(i + (j * 9), null);
            }
        }
    }

    private boolean itemIsEnchantable(ItemStack item) {
        return item != null && item.getType() == Material.BOOK;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;
//
        if (e.getRawSlots().stream().anyMatch(integer -> integer < 27 && integer != 1)) {
            e.setCancelled(true);
        }

        if (e.getWhoClicked() instanceof Player player) {
            if (e.getRawSlots().contains(1)) {
                updateEnchantingGUI(player, e.getInventory());
            }
        }


        System.out.print("Dragged: ");
        for (Integer value : e.getRawSlots()) {
            System.out.print("Slot: " + value + ", ");
        }
        System.out.println();

        for (Map.Entry<Integer, ItemStack> value : e.getNewItems().entrySet()) {
            System.out.print("new item: " + value.getValue().getType().name() + " at " + value.getKey() + ", ");
        }
        System.out.println();

//        if (e.getWhoClicked() instanceof Player player) {
//            ItemStack bookSlot = e.getInventory().getItem(1);
//            if (bookSlot != null)
//                updateEnchantingGUI(bookSlot, player);
//        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;
        ItemStack item = e.getInventory().getItem(1);
        if (item != null) {
            e.getPlayer().getInventory().addItem(item);
        }
    }


//    @EventHandler
//    public void onMove(InventoryMoveItemEvent e) {
//        if (!(e.getDestination().getHolder() instanceof EnchantingGUI)) return;
//        System.out.println("moved " + e.getItem().getType().name());
//    }



}
