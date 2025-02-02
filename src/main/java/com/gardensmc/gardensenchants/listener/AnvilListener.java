package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.rune.RuneFactory;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    @EventHandler
    public void onPrepareAnvilWithRune(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        var itemPrimary = contents[0];
        var itemSecondary = contents[1];
        if (itemPrimary == null || itemSecondary == null) {
            return;
        }
        var runeHandler = RuneFactory.createRuneHandler(itemSecondary);
        if (runeHandler == null || !runeHandler.canApply(itemPrimary)) {
            return;
        }
        inv.setRepairCost(runeHandler.getApplyCost(itemPrimary));
        e.setResult(runeHandler.apply(itemPrimary));
    }

    @EventHandler
    public void onPrepareAnvilWithBook(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        if (contents.length > 1) {
            if (contents[0] == null || contents[1] == null) {
                return;
            };
            ItemStack item = contents[0];
            ItemStack book = contents[1];
            var bookInstance = BooksHandler.INSTANCE.getBookInstance(book);
            if (bookInstance == null) {
                if (book.getType() == Material.ENCHANTED_BOOK) {
                    // disable vanilla books
                    e.setResult(new ItemStack(Material.AIR));
                }
                return;
            }
            var enchantment = bookInstance.enchantment();
            var itemClone = item.clone();
            var enchantmentHandler = new GardensEnchantHandler(itemClone);
            if (enchantmentHandler.isEnchantable(enchantment)) {
                // check if item already contains enchantment at same level, then we should attempt to upgrade it
                var level = bookInstance.level();
                if (enchantment.getEnchantmentLevel(itemClone) == level) {
                    int upgrade = level + 1;
                    if (enchantment.getMaxLevel() >= upgrade) {
                        level = upgrade;
                    } else {
                        return; // fail
                    }
                }
                if (level != 0) {
//                    if (enchantment == WINGED_BOOTS) {
//                        nbtItem.setInteger("charge", 1800);
//                    }
                    enchantmentHandler.enchant(enchantment, level);
                    inv.setRepairCost(bookInstance.getCostInXP());
                    e.setResult(itemClone);
                }
            }
        }
    }

    @EventHandler
    public void onCombineBooks(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        var itemPrimary = contents[0];
        var itemSecondary = contents[1];
        if (!isCombiningBooks(itemPrimary, itemSecondary)) {
            return;
        }
        var bookPrimary = BooksHandler.INSTANCE.getBookInstance(itemPrimary);
        var bookSecondary = BooksHandler.INSTANCE.getBookInstance(itemSecondary);
        if (bookPrimary == null || bookSecondary == null) {
            return;
        }
        var result = BooksHandler.INSTANCE.combineBooks(bookPrimary, bookSecondary);
        if (result != null) {
            e.setResult(result.book());
            inv.setRepairCost(result.getCostInXP());
        }
    }

    @EventHandler
    public void onCombineTools(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        var itemPrimary = contents[0];
        var itemSecondary = contents[1];
        var vanillaResult = e.getResult();
        if (!isCombiningItems(itemPrimary, itemSecondary, vanillaResult)) {
            return;
        }
        // construct result - ignoring vanilla result completely, since sometimes one won't be generated for us
        var result = itemPrimary.clone();
        var combinedEnchantResult = getCombinedEnchantments(itemPrimary, itemSecondary);
        // strip enchantments from result and replace with combined ones
        var resultEnchantHandler = new GardensEnchantHandler(result);
        resultEnchantHandler.clearEnchants(false);
        resultEnchantHandler.enchantAll(combinedEnchantResult.combinedEnchants);
        // give resulting durability if applicable
        var primaryMeta = itemPrimary.getItemMeta();
        if (primaryMeta instanceof Damageable damageable) {
            // if we have no vanilla result, I'm assuming there is no damage to be repaired,
            // otherwise it would've generated one...
            if (damageable.hasDamage() && vanillaResult != null) {
                var resultMeta = result.getItemMeta();
                var vanillaResultMeta = result.getItemMeta();
                if (resultMeta instanceof Damageable resultDamage && vanillaResultMeta instanceof Damageable vanillaDamage) {
                    resultDamage.setDamage(vanillaDamage.getDamage());
                    result.setItemMeta(resultMeta);
                }
            }
        }
        e.setResult(result);
        inv.setRepairCost(combinedEnchantResult.cost);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickAnvilResult(InventoryClickEvent e) {
        if (!(e.getClickedInventory() instanceof AnvilInventory)) {
            return;
        }
        if (e.getSlot() != 2) {
            return;
        }
        var inv = e.getInventory();
        var primaryItem = inv.getItem(0);
        var bookItem = inv.getItem(1);
        if (e.getCurrentItem() == null || primaryItem == null || bookItem == null) {
            return;
        }
        var bookInstance = BooksHandler.INSTANCE.getBookInstance(bookItem);
        if (bookInstance == null) {
            return;
        }
        if (ProbabilityUtil.roll(bookInstance.calculateDestructionChance())) {
            // add original item to inventory
            e.getWhoClicked().getInventory().addItem(primaryItem);
            // remove held item and clear contents of anvil
            var air = new ItemStack(Material.AIR);
            e.setCurrentItem(air);
            for (int i = 0; i < 3; i++) {
                e.getInventory().setItem(i, air);
            }
            // notify user of destructionBase
            e.getWhoClicked().sendMessage(ChatColor.RED + "Enchantment Destructed!");
        }
    }

    private record CombinedEnchantResult(List<EnchantmentInstance> combinedEnchants, int cost) {
    }

    private CombinedEnchantResult getCombinedEnchantments(ItemStack itemPrimary, ItemStack itemSecondary) {
        // upgrade matching enchants - only upgrade if EQUAL levels, if not take the higher one
        var enchantmentsOnPrimary = new GardensEnchantHandler(itemPrimary).getEnchantments();
        // must be mutable, will be removing from secondary list as we process matching enchantments
        var enchantmentsOnSecondary = new ArrayList<>(new GardensEnchantHandler(itemSecondary).getEnchantments());
        List<EnchantmentInstance> resultEnchantments = new ArrayList<>();
        int cost = 0;
        // add all enchants from primary, combining if enchant is present on both
        for (var enchantment : enchantmentsOnPrimary) {
            var matchingEnchantment = enchantmentsOnSecondary
                    .stream()
                    .filter(secondaryEnch -> secondaryEnch.getEnchantment().getId().equals(enchantment.getEnchantment().getId()))
                    .findFirst()
                    .orElse(null);
            if (matchingEnchantment != null) {
                int level = getCombinedLevel(enchantment, matchingEnchantment);
                enchantmentsOnSecondary.remove(matchingEnchantment);
                matchingEnchantment.setLevel(level);
                resultEnchantments.add(matchingEnchantment);
                cost += matchingEnchantment.getCombineCost();
            } else {
                resultEnchantments.add(enchantment);
            }
        }
        // add remaining enchantments from secondary, since we removed any matching
        resultEnchantments.addAll(enchantmentsOnSecondary);
        cost += enchantmentsOnSecondary
                .stream()
                .reduce(0,
                        ((accumulator, enchantmentInstance) -> accumulator + enchantmentInstance.getCombineCost()),
                        Integer::sum
                );
        return new CombinedEnchantResult(resultEnchantments, cost);
    }

    private int getCombinedLevel(EnchantmentInstance enchantment, EnchantmentInstance matchingEnchantment) {
        int level = enchantment.getLevel();
        if (enchantment.getLevel() == matchingEnchantment.getLevel()) {
            var upgradedLevel = level + 1;
            if (upgradedLevel <= enchantment.getEnchantment().getMaxLevel()) {
                level = upgradedLevel;
            }
        } else {
            level = Math.max(enchantment.getLevel(), matchingEnchantment.getLevel());
        }
        return level;
    }

    private boolean isCombiningBooks(ItemStack itemPrimary, ItemStack itemSecondary) {
        if (itemPrimary == null || itemSecondary == null) {
            return false;
        }
        return itemPrimary.getType() == Material.ENCHANTED_BOOK && itemSecondary.getType() == Material.ENCHANTED_BOOK;
    }

    private static boolean isCombiningItems(ItemStack itemPrimary, ItemStack itemSecondary, ItemStack result) {
        if (itemPrimary == null || itemSecondary == null) {
            return false;
        }
        if (itemPrimary.getType() != itemSecondary.getType()) {
            return false;
        }
        // if result is empty AND we have no custom changes to second item that can apply to first,
        // we fail as result is pointless
        boolean hasVanillaResult = result != null && result.getType() != Material.AIR;
        boolean isMissingEnchantments = !new GardensEnchantHandler(itemPrimary).getEnchantments().equals(
                new GardensEnchantHandler(itemSecondary).getEnchantments()
        );
        return hasVanillaResult || isMissingEnchantments;
    }

}
