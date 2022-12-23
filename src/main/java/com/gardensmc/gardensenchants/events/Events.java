package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.EnchantCache;
import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.gui.EnchantingGUI;
import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import com.gardensmc.gardensenchants.gui.InventoryUpdate;
import com.gardensmc.gardensenchants.utils.*;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.silkspawners.listeners.SilkSpawnersBlockListener;
import de.dustplanet.util.SilkUtil;
import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.bukkit.utils.events.extra.ArmorEquipEvent;
import io.lumine.mythic.bukkit.utils.events.extra.ArmorType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;

import java.util.*;

public class Events implements Listener {

    @EventHandler
    public void onEquip(ArmorEquipEvent e) {
        if (e.getType() == ArmorType.BOOTS) {

            if (e.getNewArmorPiece() != null && EnchantUtils.hasEnchantment(e.getNewArmorPiece(), CustomEnchantments.SPEEDY)) {
                float level = e.getNewArmorPiece().getEnchantmentLevel(CustomEnchantments.SPEEDY);
                if (level < 1) level = 1;
                e.getPlayer().setWalkSpeed(0.2f + (level * 0.025f)); // 0.2 is normal
            }
            else if (e.getPlayer().getWalkSpeed() > 0.2){
                e.getPlayer().setWalkSpeed(0.2f);
               }

            if (e.getNewArmorPiece() != null && EnchantUtils.hasEnchantment(e.getNewArmorPiece(), CustomEnchantments.NIGHT_VISION)) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
            }
            else if (e.getOldArmorPiece() != null && EnchantUtils.hasEnchantment(e.getOldArmorPiece(), CustomEnchantments.NIGHT_VISION)) {
                e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }

        }
    }

    @EventHandler
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.isCancelled()) return;

        if (e.getEntity() instanceof Player player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                ItemStack boots = player.getInventory().getBoots();
                if (boots != null && EnchantUtils.hasEnchantment(boots, CustomEnchantments.JELLY_LEGS)) {
                    int level = boots.getEnchantmentLevel(CustomEnchantments.JELLY_LEGS);
                    if (level < 1) level = 1;
                    if (ProbabilityUtil.roll(level * 15)) {
                        e.setCancelled(true);
                        player.playSound(player, Sound.ENTITY_SLIME_SQUISH, 1, 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {

        if (e.isCancelled()) return;

        EntityType[] netherTypes = new EntityType[]{
                EntityType.ZOGLIN,
                EntityType.PIGLIN,
                EntityType.PIGLIN_BRUTE,
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.WITHER_SKELETON,
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE,
                EntityType.STRIDER,
                EntityType.GHAST
        };
        if (e.getDamager() instanceof Player player) {
            ItemStack weapon = player.getInventory().getItemInMainHand();

            if (Arrays.asList(netherTypes).contains(e.getEntity().getType())) {
                if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.NETHERLING)) {
                    e.setDamage(e.getDamage()*2);
                }
            }

            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.LIFE_STEAL)) {
                // todo check if this actually works with custom enchantments, i doubt it
                double enchant_level = weapon.getEnchantmentLevel(CustomEnchantments.LIFE_STEAL);
                if (enchant_level < 1) enchant_level = 1;
                double hp = e.getDamage() * ((enchant_level + 1)/30);
                player.setHealth(player.getHealth() + hp);
            }

            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.DOUBLE_STRIKE)) {
                double enchant_level = weapon.getEnchantmentLevel(CustomEnchantments.DOUBLE_STRIKE);
                if (enchant_level < 1) enchant_level = 1;
                if (ProbabilityUtil.roll(20*enchant_level)) {
                    if (e.getEntity() instanceof LivingEntity livingEntity) {
                        // assuming this won't call EntityDamageByEntityEvent again?
                        livingEntity.damage(e.getDamage());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        new EnchantCache(e.getPlayer().getUniqueId()).loadCache();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        EnchantCache.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public static void onRepair(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        if (contents.length > 1) {

            if (contents[0] == null || contents[1] == null) return;

            ItemStack item = contents[0].clone();
            ItemStack book = contents[1];

            // our items adder category
            if (book.getType() != Material.ENCHANTED_BOOK) return;

            CustomStack customStack = CustomStack.byItemStack(book);
            if (customStack == null) {
                // hopefully disallows vanilla books?
                e.setResult(new ItemStack(Material.AIR));
                return;
            }

            Enchantment enchantment = CustomEnchantments.getGardensEnchant(customStack.getNamespacedID());
            if (enchantment == null) return;

            if (enchantment.canEnchantItem(item)) {

                NBTItem nbtItem = new NBTItem(item);
                if (!nbtItem.hasKey("slots")) {
                    nbtItem.setInteger("slots", 4);
                }

                int slots = nbtItem.getInteger("slots");
                int currEnchants = nbtItem.getItem().getEnchantments().size();

                if (currEnchants >= slots) {
                    return;
                }

                int level = new NBTItem(book).getInteger("level");
                if (level != 0) {
                    ItemStack final_item = nbtItem.getItem();
                    EnchantUtils.enchantItem(final_item, enchantment, level);
                    inv.setRepairCost(3);
                    e.setResult(final_item);
                }
            }
        }
    }

    @EventHandler
    public void onSilkTouchSpawner(SilkSpawnersSpawnerBreakEvent e) {
        e.getPlayer().sendMessage("WAP");
        if (!EnchantUtils.hasEnchantment(e.getPlayer().getInventory().getItemInMainHand(), CustomEnchantments.SILK_TOUCH_SPAWNER)) {
//            e.setCancelled(true);
            e.getPlayer().sendMessage("Cancelling drop");
            e.setDrop(new ItemStack(Material.AIR));
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
//            int ench_level = EnchantUtils.getEnchantLevel(e.getItem(), CustomEnchantments.ETERNAL);
//            if (ProbabilityUtil.roll((ench_level*10)*2)) {
                e.setCancelled(true);
//            }
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
            if (player.hasPermission("gardens.enchant")) {
                EnchantingGUI enchantingGUI = new EnchantingGUI(player);
                player.openInventory(enchantingGUI.getInventory());
            }
            else {
                e.getPlayer().sendMessage(ChatColor.RED + "Enchanting is currently disabled!");
            }
//
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getClickedInventory() instanceof AnvilInventory) {
            if (e.getSlot() == 2) {
                ItemStack item = e.getInventory().getItem(e.getSlot());
                ItemStack currentItem = e.getCurrentItem();
                if (item != null && currentItem != null) {
                    ItemStack book = e.getInventory().getItem(1);
                    NBTItem nbtItem = new NBTItem(book);
                    if (nbtItem.hasKey("destruction")) {
                        int destruction = nbtItem.getInteger("destruction");

                        if (ProbabilityUtil.roll(destruction)) {
                            String enchant = nbtItem.getString("destroy_enchant");
                            Enchantment customEnchantment = CustomEnchantments.getCustomEnchant(enchant);
                            if (customEnchantment instanceof CustomWrapper w) {
                                customEnchantment = w.enchantment;
                                currentItem.removeEnchantment(customEnchantment);
                            }
                            else {
                                EnchantUtils.removeEnchant(currentItem, customEnchantment);
                            }

                            EnchantUtils.updateLore(currentItem);

                            // send to their inventory directly
                            e.getWhoClicked().getInventory().addItem(currentItem);
                            ItemStack air = new ItemStack(Material.AIR);
                            e.setCurrentItem(air);
                            for (int i = 0; i < 3; i++) {
                                e.getInventory().setItem(i, air);
                            }

                            e.getWhoClicked().sendMessage(ChatColor.RED + "Enchantment Destructed!");
//                            e.getWhoClicked().sendMessage(ChatColor.RED + "Attempted removal of " + customEnchantment.getKey() + " found using " + enchant);

                        }
                    }
                }
            }
            return;
        }

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

                    // todo account for xp and money
                    if (selection != null) {
                        int playerLevels = ExpUtil.getExp(player);
                        NBTItem nbtItem = new NBTItem(selection);
                        int amber = nbtItem.getInteger("amber");
                        int xp = nbtItem.getInteger("xp");

                        if (playerLevels >= xp && GardensEnchants.econ.getBalance(player) >= amber) {
                            ExpUtil.changeExp(player, -xp);
                            GardensEnchants.econ.withdrawPlayer(player, amber);

                            new EnchantCache(player.getUniqueId()).resetEnchant();
                            ItemStack enchantTarget = e.getInventory().getItem(1);
                            if (enchantTarget != null) {
                                int amount = enchantTarget.getAmount();
                                if (amount > 1) {
                                    enchantTarget.setAmount(amount - 1);
                                    e.getInventory().getViewers().get(0).getInventory().addItem(enchantTarget);
                                }
                                // remove costs from lore before enchanting target
                                ItemMeta meta = selection.getItemMeta();
                                if (meta != null) {
                                    List<String> lore = meta.getLore();
                                    if (lore != null) {
                                        // remove last 3 lines
                                        for (int i = 0; i < 3; i++) {
                                            lore.remove(lore.size() - 1);
                                        }
                                    }
                                    meta.setLore(lore);
                                    selection.setItemMeta(meta);
                                }
                                e.getInventory().setItem(1, selection);
                            }


                            updateEnchantingGUI(player, e.getInventory());
                        }
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

//        System.out.println("Clicked: " + e.getRawSlot() + "slot, " + e.getCurrentItem() + "item, Action: " + e.getAction().name());


    }

    private void updateEnchantingGUI(Player player, Inventory inv) {
        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
            String title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:&f\uE094");

            if (itemIsEnchantable(inv.getItem(1))) {
                ItemStack[] books = genBooks(inv, player);
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

    // places books into inventory
    private ItemStack[] genBooks(Inventory inv, Player player) {
        ItemStack display = ItemUtils.getCustomItem("gardens:blank_item");
        ItemStack[] books = EnchantUtils.getRandomBooks(player.getUniqueId());
        CustomEnchantment[] enchantments = new EnchantCache(player.getUniqueId()).getEnchants();
        int playerLevels = ExpUtil.getExp(player);
        double playerBal = GardensEnchants.econ.getBalance(player);
        // cost and amber
        // costs
        for (int i = 0; i < 3; i++) {
            EnchantRarity rarity = enchantments[i].getRarity();
            int amber = EnchantUtils.getAmberCost(rarity);
            int xp = EnchantUtils.getXpCost(rarity);


            ItemMeta bookMeta = books[i].getItemMeta();
            if (bookMeta != null) {
                List<String> lore = bookMeta.getLore();
                if (lore != null) {
                    lore.add("");
                    if (playerBal >= amber) {
                        lore.add(ChatColor.WHITE + "\uE01F " + amber + " Amber");
                    }
                    else {
                        lore.add(ChatColor.WHITE + "\uE01F " + ChatColor.RED + amber + " Amber");
                    }

                    if (playerLevels >= xp) {
                        lore.add(ChatColor.WHITE + "\uE10F " + EnchantUtils.translateXpToLevel(xp) + " Levels");
                    }
                    else {
                        lore.add(ChatColor.WHITE + "\uE10F " + ChatColor.RED + EnchantUtils.translateXpToLevel(xp) + " Levels");
                    }
                }

                bookMeta.setLore(lore);
            }
            books[i].setItemMeta(bookMeta);
            NBTItem nbtItem = new NBTItem(books[i]);
            nbtItem.setInteger("amber", amber);
            nbtItem.setInteger("xp", xp);
            books[i] = nbtItem.getItem();
        }

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
