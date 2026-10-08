package com.gardensmc.gardensenchants.events;

import com.enderbeam.enderwardrobe.api.WardrobeApi;
import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantment;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemContainerContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.COMPACTOR;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.QUICK_DEPOSIT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.RESTOCK;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.SNACK_POUCH;

/**
 * Enchantments on the backpack worn in EnderWardrobe's backpack slot (only that one), working on what it holds:
 * Restock tops the hotbar up from it, an item at a time like a hopper; Quick Deposit puts what a container already holds
 * into it; Compactor packs nines into their block form; Snack Pouch eats from it when hunger runs low. None touch a
 * backpack while one is open, since that edit would be overwritten when it closes.
 */
public final class BackpackEnchants implements Listener {

    /** Restock's pace, like a hopper's (ticks), and Compactor's. */
    private static final int RESTOCK_TICKS = 8, COMPACT_TICKS = 40;
    /** Snack Pouch eats when food drops to this (of 20), and no more often than this (ms). */
    private static final int SNACK_AT = 6;
    private static final long SNACK_GAP_MS = 2000;
    /** Food Snack Pouch never eats: harmful, teleporting, precious, or leaving a bowl or bottle behind. */
    private static final Set<Material> NOT_SNACKS = Set.of(Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.POISONOUS_POTATO,
            Material.PUFFERFISH, Material.CHICKEN, Material.SUSPICIOUS_STEW, Material.CHORUS_FRUIT, Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.MUSHROOM_STEW, Material.RABBIT_STEW,
            Material.BEETROOT_SOUP, Material.HONEY_BOTTLE);

    private final WardrobeApi wardrobe;
    /** Each player's hotbar as Restock last saw it, to tell a slot used up from one moved away. */
    private final Map<UUID, ItemStack[]> lastHotbar = new HashMap<>();
    private final Map<UUID, Long> lastSnack = new HashMap<>();
    /** What a nine of each material packs into, from the server's own 3x3 recipes. */
    private Map<Material, Material> compacts;

    private BackpackEnchants(WardrobeApi wardrobe) {
        this.wardrobe = wardrobe;
    }

    public static void enable() {
        if (Bukkit.getPluginManager().getPlugin("EnderWardrobe") == null) return;
        var api = Bukkit.getServicesManager().load(WardrobeApi.class);
        if (api == null) return;
        var listener = new BackpackEnchants(api);
        Bukkit.getPluginManager().registerEvents(listener, GardensEnchants.plugin);
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, listener::restockAll, 20, RESTOCK_TICKS);
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, listener::compactAll, 40, COMPACT_TICKS);
    }

    // ---- The worn backpack ----

    /** The backpack in the backpack slot, when it has the enchantment and isn't open; null otherwise. */
    @Nullable
    private ItemStack worn(Player player, CustomEnchantment enchantment) {
        if (wardrobe.isBackpackOpen(player)) return null;
        var backpack = wardrobe.worn(player, "backpack");
        if (backpack == null || wardrobe.backpackRows(backpack) == 0 || !enchantment.hasEnchantment(backpack)) return null;
        return backpack;
    }

    /** Its slots, padded to its size (empty slots null). */
    private List<ItemStack> slots(ItemStack backpack) {
        var data = backpack.getData(DataComponentTypes.CONTAINER);
        var slots = new ArrayList<ItemStack>();
        if (data != null) for (var item : data.contents()) slots.add(item == null || item.isEmpty() ? null : item.clone());
        int size = wardrobe.backpackRows(backpack) * 9;
        while (slots.size() < size) slots.add(null);
        return slots;
    }

    private boolean save(Player player, ItemStack backpack, List<ItemStack> slots) {
        var items = slots.stream().map(item -> item == null ? ItemStack.empty() : item).toList();
        backpack.setData(DataComponentTypes.CONTAINER, ItemContainerContents.containerContents(items));
        return wardrobe.setWorn(player, "backpack", backpack);
    }

    /** Takes one item like `like` out of the slots; whether there was one. */
    private static boolean takeOne(List<ItemStack> slots, ItemStack like) {
        for (int i = 0; i < slots.size(); i++) {
            var there = slots.get(i);
            if (there != null && there.isSimilar(like)) {
                slots.set(i, there.getAmount() > 1 ? there.asQuantity(there.getAmount() - 1) : null);
                return true;
            }
        }
        return false;
    }

    /** Puts the stack in, onto stacks of it first then empty slots; how many didn't fit. */
    private static int put(List<ItemStack> slots, ItemStack stack) {
        int left = stack.getAmount();
        for (int pass = 0; pass < 2 && left > 0; pass++) {
            for (int i = 0; i < slots.size() && left > 0; i++) {
                var there = slots.get(i);
                if (pass == 0 && there != null && there.isSimilar(stack)) {
                    int moved = Math.min(left, there.getMaxStackSize() - there.getAmount());
                    if (moved > 0) {
                        slots.set(i, there.asQuantity(there.getAmount() + moved));
                        left -= moved;
                    }
                } else if (pass == 1 && there == null) {
                    int moved = Math.min(left, stack.getMaxStackSize());
                    slots.set(i, stack.asQuantity(moved));
                    left -= moved;
                }
            }
        }
        return left;
    }

    // ---- Restock ----

    /**
     * An item at a time into each hotbar stack that isn't full, from the backpack; a slot used up (its last item gone)
     * gets the same item back. A stack moved away isn't replaced: only one that ran out.
     */
    private void restockAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            var id = player.getUniqueId();
            var backpack = player.getGameMode() == GameMode.CREATIVE ? null : worn(player, RESTOCK);
            var inventory = player.getInventory();
            var last = lastHotbar.get(id);
            if (backpack != null) {
                var slots = slots(backpack);
                boolean changed = false;
                for (int slot = 0; slot < 9; slot++) {
                    var here = inventory.getItem(slot);
                    boolean empty = here == null || here.isEmpty();
                    var was = last == null ? null : last[slot];
                    var want = !empty ? here : was != null && was.getAmount() == 1 ? was : null;
                    if (want == null || want.getMaxStackSize() <= 1) continue;
                    if (!empty && here.getAmount() >= here.getMaxStackSize()) continue;
                    if (!takeOne(slots, want)) continue;
                    if (empty) inventory.setItem(slot, want.asOne());
                    else here.setAmount(here.getAmount() + 1);
                    changed = true;
                }
                if (changed) save(player, backpack, slots);
            }
            var now = new ItemStack[9];
            for (int slot = 0; slot < 9; slot++) {
                var here = inventory.getItem(slot);
                now[slot] = here == null || here.isEmpty() ? null : here.clone();
            }
            lastHotbar.put(id, now);
        }
    }

    // ---- Quick Deposit ----

    /**
     * Sneaking, empty-handed, using a container they may open: what it already holds goes in from the backpack, or
     * into an empty chest, barrel or shulker box everything (somewhere new to empty it into; a furnace or a hopper would
     * take anything into the wrong slots), and the container doesn't open (it's a quick deposit; without sneaking it opens as ever). Last but for watchers, so
     * a claim plugin has said whether they may use it.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseContainer(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND || e.getClickedBlock() == null) return;
        if (e.useInteractedBlock() == Event.Result.DENY) return;   // a claim, or anything else, says no
        var player = e.getPlayer();
        if (!player.isSneaking() || !player.getInventory().getItemInMainHand().isEmpty()) return;
        if (!(e.getClickedBlock().getState() instanceof Container container)) return;
        var backpack = worn(player, QUICK_DEPOSIT);
        if (backpack == null) return;
        e.setUseInteractedBlock(Event.Result.DENY);
        e.setUseItemInHand(Event.Result.DENY);
        var chest = container.getInventory();
        var slots = slots(backpack);
        int moved = 0;
        boolean everything = chest.isEmpty() && switch (chest.getType()) {
            case CHEST, BARREL, SHULKER_BOX -> true;
            default -> false;
        };
        for (int i = 0; i < slots.size(); i++) {
            var item = slots.get(i);
            if (item == null) continue;
            boolean holds = everything;
            for (var there : everything ? new ItemStack[0] : chest.getContents()) {
                if (there != null && there.isSimilar(item)) {
                    holds = true;
                    break;
                }
            }
            if (!holds) continue;
            var left = chest.addItem(item.clone()).values().stream().mapToInt(ItemStack::getAmount).sum();
            moved += item.getAmount() - left;
            slots.set(i, left == 0 ? null : item.asQuantity(left));
        }
        if (moved == 0) {
            player.sendActionBar(Component.text("Nothing here to deposit from your backpack", NamedTextColor.GRAY));
            return;
        }
        if (!save(player, backpack, slots)) {
            GardensEnchants.plugin.getLogger().warning("Quick Deposit couldn't save " + player.getName() + "'s backpack.");
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 0.8f);
        player.sendActionBar(Component.text("Deposited " + moved + " item" + (moved == 1 ? "" : "s") + " from your backpack", NamedTextColor.GREEN));
    }

    // ---- Compactor ----

    private Map<Material, Material> compacts() {
        if (compacts != null) return compacts;
        var map = new EnumMap<Material, Material>(Material.class);
        var iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            var result = recipe.getResult();
            if (result.getAmount() != 1 || result.hasItemMeta() && !result.isSimilar(new ItemStack(result.getType()))) continue;
            Material from = null;
            if (recipe instanceof ShapedRecipe shaped && shaped.getShape().length == 3 && String.join("", shaped.getShape()).length() == 9) {
                var choices = shaped.getChoiceMap().values().stream().distinct().toList();
                if (choices.size() == 1) from = single(choices.getFirst());
            } else if (recipe instanceof ShapelessRecipe shapeless && shapeless.getChoiceList().size() == 9) {
                var choices = shapeless.getChoiceList().stream().distinct().toList();
                if (choices.size() == 1) from = single(choices.getFirst());
            }
            if (from != null && from != result.getType()) map.putIfAbsent(from, result.getType());
        }
        return compacts = map;
    }

    @Nullable
    private static Material single(@Nullable RecipeChoice choice) {
        return choice instanceof RecipeChoice.MaterialChoice materials && materials.getChoices().size() == 1 ? materials.getChoices().getFirst() : null;
    }

    /** Nines of plain items into what they pack into. Only what fits: nothing's lost if a result has nowhere to go. */
    private void compactAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            var backpack = worn(player, COMPACTOR);
            if (backpack == null) continue;
            var slots = slots(backpack);
            boolean changed = false;
            for (var entry : compacts().entrySet()) {
                var plain = new ItemStack(entry.getKey());
                int total = 0;
                for (var item : slots) if (item != null && item.isSimilar(plain)) total += item.getAmount();
                int packs = total / 9;
                if (packs == 0) continue;
                var attempt = new ArrayList<>(slots);
                for (int n = 0; n < packs * 9; n++) takeOne(attempt, plain);
                if (put(attempt, new ItemStack(entry.getValue(), packs)) > 0) continue;   // no room for the result
                slots = attempt;
                changed = true;
            }
            if (changed) save(player, backpack, slots);
        }
    }

    // ---- Snack Pouch ----

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHungry(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player) || e.getFoodLevel() > SNACK_AT || e.getFoodLevel() >= player.getFoodLevel()) return;
        long now = System.currentTimeMillis();
        if (now - lastSnack.getOrDefault(player.getUniqueId(), 0L) < SNACK_GAP_MS) return;
        // after the change has happened
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> snack(player));
    }

    /** The least filling safe food in the backpack, eaten. */
    private void snack(Player player) {
        if (!player.isOnline() || player.getFoodLevel() > SNACK_AT) return;
        var backpack = worn(player, SNACK_POUCH);
        if (backpack == null) return;
        var slots = slots(backpack);
        ItemStack best = null;
        int bestNutrition = Integer.MAX_VALUE;
        for (var item : slots) {
            if (item == null || NOT_SNACKS.contains(item.getType())) continue;
            var food = item.getData(DataComponentTypes.FOOD);
            if (food == null || food.nutrition() <= 0) continue;
            if (food.nutrition() < bestNutrition) {
                best = item;
                bestNutrition = food.nutrition();
            }
        }
        if (best == null) return;
        var food = best.getData(DataComponentTypes.FOOD);
        var eaten = best.asOne();
        if (!takeOne(slots, eaten) || !save(player, backpack, slots)) return;
        int level = Math.min(20, player.getFoodLevel() + food.nutrition());
        player.setFoodLevel(level);
        player.setSaturation(Math.min(level, player.getSaturation() + food.saturation()));
        lastSnack.put(player.getUniqueId(), System.currentTimeMillis());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.6f, 1.1f);
        player.sendActionBar(Component.text("Your Snack Pouch fed you", NamedTextColor.GREEN));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastHotbar.remove(e.getPlayer().getUniqueId());
        lastSnack.remove(e.getPlayer().getUniqueId());
    }
}
