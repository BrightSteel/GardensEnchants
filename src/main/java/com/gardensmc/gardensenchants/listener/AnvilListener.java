package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.events.GardensEnchantItemEvent;
import com.gardensmc.gardensenchants.relic.RelicHandler;
import com.gardensmc.gardensenchants.rune.RuneFactory;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.view.AnvilView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnvilListener implements Listener {

    // vanilla adds 2 levels for repairing by combining two damaged items
    private static final int REPAIR_COST = 2;

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
        e.getView().setRepairCost(runeHandler.getApplyCost(itemPrimary));
        // make vanilla consume the rune on take, even if a rename is typed
        e.getView().setRepairItemCountCost(1);
        e.setResult(runeHandler.apply(itemPrimary));
    }

    @EventHandler
    public void onPrepareAnvilWithRelic(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        var itemPrimary = contents[0];
        var itemSecondary = contents[1];
        if (itemPrimary == null || itemSecondary == null) {
            return;
        }
        var relicHandler = RelicHandler.createRelicHandler(itemSecondary);
        if (relicHandler == null || !relicHandler.canApply(itemPrimary)) {
            return;
        }
        e.getView().setRepairCost(relicHandler.getApplyCost(itemPrimary));
        e.getView().setRepairItemCountCost(1);
        e.setResult(relicHandler.apply(itemPrimary));
    }

    @EventHandler
    public void onPrepareAnvilWithBook(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack item = inv.getFirstItem();
        ItemStack book = inv.getSecondItem();
        if (item == null || book == null) {
            return;
        }
        var bookInstance = BooksHandler.INSTANCE.getBookInstance(book);
        if (bookInstance == null) {
            if (book.getType() == Material.ENCHANTED_BOOK) {
                // disable vanilla books
                e.setResult(null);
            }
            return;
        }
        if (item.getType() == Material.ENCHANTED_BOOK || BooksHandler.INSTANCE.getBookInstance(item) != null) {
            return; // book + book is handled by onCombineBooks
        }
        var enchantment = bookInstance.enchantment();
        var itemClone = item.clone();
        var enchantmentHandler = new GardensEnchantHandler(itemClone);
        if (!enchantmentHandler.isEnchantable(enchantment)) {
            e.setResult(null);
            return;
        }
        // same level upgrades the enchant, a higher book replaces it, a lower book is refused
        var level = bookInstance.level();
        var currentLevel = enchantment.getEnchantmentLevel(itemClone);
        if (currentLevel == level) {
            level++;
        }
        if (level <= 0 || level < currentLevel || level > enchantment.getMaxLevel()) {
            e.setResult(null);
            return;
        }
        var rename = e.getView().getRenameText();
        if (rename != null && !rename.isBlank()) {
            itemClone.editMeta(meta -> meta.customName(MessageUtil.fromPlayerInput(rename)));
        }
        enchantmentHandler.enchant(enchantment, level, false);
        e.getView().setRepairCost(bookInstance.getCostInXP());
        // vanilla treats a book + rename as "only renaming" and would leave the book in place
        e.getView().setRepairItemCountCost(1);
        e.setResult(itemClone);
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
            e.getView().setRepairCost(result.getCostInXP());
            e.getView().setRepairItemCountCost(1);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    // run prior to other combinations since this checks for a result and modifies it - only want to recieve vanilla results
    public void onCombineTools(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        var itemPrimary = inv.getFirstItem();
        var itemSecondary = inv.getSecondItem();
        var vanillaResult = e.getResult();
        if (!isCombiningItems(itemPrimary, itemSecondary, vanillaResult)) {
            return;
        }
        var slots = new GardensEnchantHandler(itemPrimary).getSlots();
        // construct result - ignoring vanilla result completely, since sometimes one won't be generated for us
        var result = itemPrimary.clone();
        var combinedEnchantResult = getCombinedEnchantments(itemPrimary, itemSecondary);
        if (combinedEnchantResult.combinedEnchants.size() > slots) {
            // prevent exceeding slot count; the vanilla result would merge vanilla enchants past it
            e.setResult(null);
            return;
        }
        // strip enchantments from result and replace with combined ones
        var resultEnchantHandler = new GardensEnchantHandler(result);
        resultEnchantHandler.clearEnchants(false);
        resultEnchantHandler.enchantAll(combinedEnchantResult.combinedEnchants);
        int cost = combinedEnchantResult.cost;
        if (vanillaResult != null && !vanillaResult.isEmpty()) {
            var resultMeta = result.getItemMeta();
            var vanillaMeta = vanillaResult.getItemMeta();
            // carry over the durability vanilla restored by combining
            if (resultMeta instanceof Damageable resultDamage && vanillaMeta instanceof Damageable vanillaDamage
                    && vanillaDamage.getDamage() < resultDamage.getDamage()) {
                resultDamage.setDamage(vanillaDamage.getDamage());
                cost += REPAIR_COST;
            }
            // and any rename typed in the anvil (vanilla already made it plain text)
            if (!Objects.equals(vanillaMeta.customName(), resultMeta.customName())) {
                resultMeta.customName(vanillaMeta.customName());
                cost += 1;
            }
            result.setItemMeta(resultMeta);
        }
        e.setResult(result);
        // vanilla refuses to hand over a result that costs nothing
        e.getView().setRepairCost(Math.max(1, cost));
        // vanilla skips consuming the second item when it thinks the player is only renaming
        e.getView().setRepairItemCountCost(1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickAnvilResult(InventoryClickEvent e) {
        if (!(e.getView() instanceof AnvilView anvilView) || !(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (e.getRawSlot() != 2 || e.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        var inv = anvilView.getTopInventory();
        var primaryItem = inv.getFirstItem();
        var bookItem = inv.getSecondItem();
        var resultItem = e.getCurrentItem();
        if (resultItem == null || resultItem.isEmpty() || primaryItem == null || bookItem == null) {
            return;
        }
        var bookInstance = BooksHandler.INSTANCE.getBookInstance(bookItem);
        if (bookInstance == null) {
            return;
        }
        if (primaryItem.getType() == Material.ENCHANTED_BOOK) {
            return; // book combining
        }
        if (!canTakeResult(player, anvilView)) {
            // vanilla won't hand the result over, so nothing happens
            return;
        }
        if (!isSimpleTake(e, player)) {
            // only plain and shift clicks are supported, so we know the result really was taken
            e.setCancelled(true);
            return;
        }
        if (ProbabilityUtil.roll(bookInstance.calculateDestructionChance())) {
            // the book is destroyed and the item handed back untouched, without charging xp
            e.setCancelled(true);
            PlayerUtil.giveItem(player, primaryItem.clone());
            inv.setFirstItem(null);
            inv.setSecondItem(null);
            inv.setResult(null);
            player.sendMessage(ChatColor.RED + "Enchantment Destructed!");
            return;
        }
        // tell listeners (e.g. quests) once the result has actually left the anvil
        var primarySnapshot = primaryItem.clone();
        var resultSnapshot = resultItem.clone();
        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
            if (player.getOpenInventory().getTopInventory() != inv) {
                return;
            }
            var currentPrimary = inv.getFirstItem();
            if (currentPrimary == null || !currentPrimary.isSimilar(primarySnapshot)) {
                Bukkit.getPluginManager().callEvent(new GardensEnchantItemEvent(player, resultSnapshot, bookInstance));
            }
        }, 1);
    }

    private static boolean canTakeResult(Player player, AnvilView anvilView) {
        var cost = anvilView.getRepairCost();
        if (cost <= 0) {
            return false;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        return player.getLevel() >= cost && cost < anvilView.getMaximumRepairCost();
    }

    private static boolean isSimpleTake(InventoryClickEvent e, Player player) {
        return switch (e.getClick()) {
            case LEFT, RIGHT -> e.getCursor().isEmpty();
            case SHIFT_LEFT, SHIFT_RIGHT -> player.getInventory().firstEmpty() != -1;
            default -> false;
        };
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
        // add remaining enchantments from secondary, discarding any that conflict with a kept (left) enchant.
        // like vanilla, we still charge for conflicting enchants even though they're dropped from the result
        var compatibleSecondary = enchantmentsOnSecondary
                .stream()
                .filter(secondary -> enchantmentsOnPrimary
                        .stream()
                        .noneMatch(primary -> conflicts(primary, secondary)))
                .toList();
        resultEnchantments.addAll(compatibleSecondary);
        cost += enchantmentsOnSecondary
                .stream()
                .reduce(0,
                        ((accumulator, enchantmentInstance) -> accumulator + enchantmentInstance.getCombineCost()),
                        Integer::sum
                );
        return new CombinedEnchantResult(resultEnchantments, cost);
    }

    private boolean conflicts(EnchantmentInstance left, EnchantmentInstance right) {
        var leftEnchant = left.getEnchantment();
        var rightEnchant = right.getEnchantment();
        return leftEnchant.conflictsWith(rightEnchant) || rightEnchant.conflictsWith(leftEnchant);
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
        if (itemPrimary.getType() == Material.ENCHANTED_BOOK) {
            return false; // combining books instead
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
