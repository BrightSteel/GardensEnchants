package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.rune.ExtractionRune;
import com.gardensmc.gardensenchants.utils.AnvilUtil;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * Anvil-style menu for the extraction rune, opened when one is placed in an anvil. It has a second
 * result slot under the usual one: the item without the selected enchant, and the book holding it.
 * Taking either result extracts and leaves the other in its slot to be taken; the buttons above and
 * below the rune pick which enchant. The cost is drawn under the results like a vanilla anvil's.
 */
public class ExtractionRuneMenu extends Menu {

    // a = item, b = rune, r = item result, s = book result, u / d = previous / next enchant.
    // matches the slots drawn in the background (resource pack: gardens:default font,
    // extraction_rune_menu.png), which starts 11px above the window (ascent 24) to keep vanilla's
    // hammer spacing
    private static final String[] layout = new String[] {
            "    u    ",
            " a  b  r ",
            "    d  s "
    };
    private static final String BG = "\uE500";
    // the same background with a strip behind the cost label, used while there's a cost to show
    private static final String BG_COST = "\uE503";
    // the carets are separate glyphs so the title can leave one out when there's nothing that way.
    // their ascents put them over the u / d slots, these place them across
    private static final String CARET_UP = "\uE501";
    private static final String CARET_DOWN = "\uE502";
    private static final int CARET_X = 83;
    private static final int CARET_ADVANCE = 10;
    // the background is 176px wide (177 advance); vanilla anvils draw their title at x = 60
    private static final int BG_ADVANCE = 177;
    private static final int TITLE_X = 60;
    // the cost label is right-aligned like vanilla's (imageWidth - 8 - 2), in fonts that are vanilla's
    // text moved down to the strip under the book slot, and 1px further for the shadow
    private static final int COST_LABEL_RIGHT = 166;
    private static final String COST_FONT = "gardens:extraction_cost";
    private static final String COST_SHADOW_FONT = "gardens:extraction_cost_shadow";
    // vanilla's anvil cost colours, and their text shadows
    private static final String COST_COLOR = "#80FF20";
    private static final String COST_SHADOW = "#203F08";
    private static final String TOO_EXPENSIVE_COLOR = "#FF6060";
    private static final String TOO_EXPENSIVE_SHADOW = "#3F1818";

    // open menus by gui, so an xp change can recolour the cost
    private static final Map<InventoryGui, ExtractionRuneMenu> menus = new WeakHashMap<>();
    private static final int ITEM_SLOT = 0;
    private static final int RUNE_SLOT = 1;

    // where the menu was opened from, so using it can wear the anvil down. null for virtual anvils
    @Nullable
    private final Location anvilLocation;
    private GuiStorageElement itemStorage, runeStorage;
    private int selected;
    // built once per selection, so the book handed over is the one that was shown
    @Nullable
    private Preview preview;
    private boolean reverting;
    private String title;
    // results of an extraction still waiting to be taken out of their slots
    @Nullable
    private ItemStack pendingItem, pendingBook;

    public ExtractionRuneMenu(Player viewer, ItemStack item, ItemStack rune, @Nullable Location anvilLocation) {
        this(viewer, item, rune, anvilLocation, initialTitle(viewer, item));
    }

    private static String initialTitle(Player viewer, ItemStack item) {
        var options = ExtractionRune.getExtractableEnchantments(item);
        int cost = ExtractionRune.getCost(options.getFirst());
        return buildTitle(false, options.size() > 1, cost, canAfford(viewer, cost));
    }

    private ExtractionRuneMenu(Player viewer, ItemStack item, ItemStack rune, @Nullable Location anvilLocation, String title) {
        super(layout, title, viewer);
        this.title = title;
        this.anvilLocation = anvilLocation;
        menus.put(gui, this);
        initElements();
        itemStorage.setStorageItem(viewer, ITEM_SLOT, item);
        runeStorage.setStorageItem(viewer, RUNE_SLOT, rune);
    }

    private void initElements() {
        var inventory = Bukkit.createInventory(null, InventoryType.CHEST);
        itemStorage = new GuiStorageElement('a', inventory, ITEM_SLOT, this::onApplyStorage, null);
        runeStorage = new GuiStorageElement('b', inventory, RUNE_SLOT, this::onApplyStorage, null);
        gui.addElement(itemStorage);
        gui.addElement(runeStorage);
        gui.addElement(new DynamicGuiElement('r', viewer -> getResultElement('r', false)));
        gui.addElement(new DynamicGuiElement('s', viewer -> getResultElement('s', true)));
        gui.addElement(new DynamicGuiElement('u', viewer -> getSelectButton('u', -1)));
        gui.addElement(new DynamicGuiElement('d', viewer -> getSelectButton('d', 1)));
        gui.setCloseAction(close -> onClose());
    }

    private boolean onClose() {
        // empty the slots before handing the items back, so a late click can't take a copy
        var items = new ArrayList<ItemStack>();
        takeStorageItem(itemStorage, ITEM_SLOT, items);
        takeStorageItem(runeStorage, RUNE_SLOT, items);
        Stream.of(pendingItem, pendingBook).filter(Objects::nonNull).forEach(items::add);
        pendingItem = null;
        pendingBook = null;
        PlayerUtil.giveItems(viewer, items);
        return true;
    }

    private void takeStorageItem(GuiStorageElement storage, int slot, List<ItemStack> into) {
        var item = storage.getStorageItem(viewer, slot);
        if (item != null) {
            storage.setStorageItem(viewer, slot, null);
            into.add(item);
        }
    }

    private void onApplyStorage() {
        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
            if (isOpen()) {
                refresh();
            }
        }, 1);
    }

    private void refresh() {
        preview = null;
        gui.draw();
        updateTitle();
        if (!hasPending() && getOptions().isEmpty()) {
            // results taken and nothing left to extract (rune taken out, item swapped): back to a plain anvil
            revertToAnvil();
        }
    }

    /**
     * Swaps this menu back for the anvil it came from, carrying over the inputs and the cursor. Runs a
     * tick later, as another inventory can't be opened from inside a click.
     */
    private void revertToAnvil() {
        if (reverting) {
            return;
        }
        reverting = true;
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> {
            reverting = false;
            // closed meanwhile (the close already returned the items), or valid again
            if (!isOpen() || hasPending() || !getOptions().isEmpty()) {
                return;
            }
            if (anvilLocation != null && !AnvilUtil.canStillUse(viewer, anvilLocation)) {
                viewer.closeInventory();
                return;
            }
            var item = itemStorage.getStorageItem(viewer, ITEM_SLOT);
            var rune = runeStorage.getStorageItem(viewer, RUNE_SLOT);
            var cursor = viewer.getItemOnCursor();
            // empty the slots and cursor first, so closing this menu doesn't hand them out as well
            itemStorage.setStorageItem(viewer, ITEM_SLOT, null);
            runeStorage.setStorageItem(viewer, RUNE_SLOT, null);
            viewer.setItemOnCursor(null);
            var builder = MenuType.ANVIL.builder().checkReachable(anvilLocation != null);
            if (anvilLocation != null) {
                builder = builder.location(anvilLocation);
            }
            var anvil = builder.build(viewer);
            anvil.open();
            if (viewer.getOpenInventory() != anvil) {
                // the anvil didn't open, so nothing would hand these back
                PlayerUtil.giveItems(viewer, Stream.of(item, rune, cursor).filter(i -> i != null && !i.isEmpty()).toList());
                return;
            }
            viewer.setItemOnCursor(cursor);
            // may open this menu again if they still make an extraction (see AnvilListener)
            anvil.getTopInventory().setFirstItem(item);
            anvil.getTopInventory().setSecondItem(rune);
        });
    }

    private static String buildTitle(boolean hasPrevious, boolean hasNext, @Nullable Integer cost, boolean canAfford) {
        var title = new StringBuilder("<white><font:gardens:default>" + getShift(-8) + (cost == null ? BG : BG_COST));
        int x = BG_ADVANCE;
        if (hasPrevious) {
            title.append(getShift(CARET_X - x)).append(CARET_UP);
            x = CARET_X + CARET_ADVANCE;
        }
        if (hasNext) {
            title.append(getShift(CARET_X - x)).append(CARET_DOWN);
            x = CARET_X + CARET_ADVANCE;
        }
        if (cost != null) {
            var label = "Extraction Cost: " + cost;
            int width = getLabelWidth(label);
            int labelX = COST_LABEL_RIGHT - width;
            // shadow 1px right (its font puts it 1px lower), then back to draw the text over it
            title.append(getShift(labelX + 1 - x))
                    .append("<font:").append(COST_SHADOW_FONT).append("><color:")
                    .append(canAfford ? COST_SHADOW : TOO_EXPENSIVE_SHADOW).append('>').append(label)
                    .append("<font:gardens:default>").append(getShift(-(width + 1)))
                    .append("<font:").append(COST_FONT).append("><color:")
                    .append(canAfford ? COST_COLOR : TOO_EXPENSIVE_COLOR).append('>').append(label)
                    .append("<font:gardens:default>");
            x = labelX + width;
        }
        return title.append(getShift(TITLE_X - x))
                .append("<font:minecraft:default><#404040>Extract Enchantment")
                .toString();
    }

    // advances of vanilla's ascii glyphs, for the characters the cost label uses
    private static int getLabelWidth(String label) {
        int width = 0;
        for (char c : label.toCharArray()) {
            width += switch (c) {
                case ' ', 't' -> 4;
                case ':', 'i' -> 2;
                default -> 6;
            };
        }
        return width;
    }

    /**
     * Redraws the carets and cost label. Only sent when they change, as it resends the whole screen.
     */
    private void updateTitle() {
        var options = getOptions();
        int current = getSelected(options);
        // no choosing while an extraction's results are waiting to be taken
        boolean choosing = !hasPending() && !options.isEmpty();
        Integer cost = choosing ? ExtractionRune.getCost(options.get(current)) : null;
        var newTitle = buildTitle(
                choosing && current > 0,
                choosing && current < options.size() - 1,
                cost,
                cost != null && canAfford(cost)
        );
        if (!newTitle.equals(title) && isOpen()) {
            title = newTitle;
            PlayerUtil.sendInventoryTitleChange(viewer.getOpenInventory(), MiniMessage.miniMessage().deserialize(title));
        }
    }

    /**
     * Recolours the cost label of the player's open extraction menu, if any, after their xp changed.
     */
    public static void onLevelChange(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryGui.Holder holder) {
            var menu = menus.get(holder.getGui());
            if (menu != null) {
                menu.updateTitle();
            }
        }
    }

    private boolean hasPending() {
        return pendingItem != null || pendingBook != null;
    }

    private boolean isOpen() {
        return viewer.isOnline()
                && viewer.getOpenInventory().getTopInventory().getHolder() instanceof InventoryGui.Holder holder
                && holder.getGui() == gui;
    }

    /**
     * The enchants the inputs allow extracting, or an empty list if the item or rune is missing.
     */
    private List<EnchantmentInstance> getOptions() {
        var item = itemStorage.getStorageItem(viewer, ITEM_SLOT);
        var rune = runeStorage.getStorageItem(viewer, RUNE_SLOT);
        if (item == null || item.getAmount() != 1 || !ExtractionRune.isExtractionRune(rune)) {
            return List.of();
        }
        return ExtractionRune.getExtractableEnchantments(item);
    }

    @Nullable
    private Preview getPreview() {
        if (preview == null) {
            var options = getOptions();
            if (options.isEmpty()) {
                return null;
            }
            var instance = options.get(getSelected(options));
            var item = itemStorage.getStorageItem(viewer, ITEM_SLOT);
            preview = new Preview(instance, ExtractionRune.extract(item, instance));
        }
        return preview;
    }

    /**
     * A result slot: what's left of the last extraction if there is one, otherwise the preview.
     */
    private GuiElement getResultElement(char slot, boolean book) {
        if (hasPending()) {
            return new StaticGuiElement(slot, book ? pendingBook : pendingItem, click -> {
                takePending(click, book);
                refresh();
                return true;
            });
        }
        var preview = getPreview();
        var display = preview == null ? null : (book ? preview.result().book() : preview.result().item()).clone();
        return new StaticGuiElement(slot, display, click -> extract(click, book));
    }

    /**
     * Hands one waiting result to the player like a vanilla result slot: onto an empty cursor, or into
     * the inventory on a shift click. False if it can't go anywhere and stays put.
     */
    private boolean takePending(GuiElement.Click click, boolean book) {
        var item = book ? pendingBook : pendingItem;
        if (item == null || !canTake(click)) {
            return false;
        }
        if (click.getType().isShiftClick()) {
            PlayerUtil.giveItem(viewer, item);
        } else {
            click.setCursor(item);
        }
        if (book) {
            pendingBook = null;
        } else {
            pendingItem = null;
        }
        return true;
    }

    private static boolean canTake(GuiElement.Click click) {
        var cursor = click.getCursor();
        return click.getType().isShiftClick() || cursor == null || cursor.isEmpty();
    }

    // the selection stays in range when the item changes under it
    private int getSelected(List<EnchantmentInstance> options) {
        return Math.max(0, Math.min(selected, options.size() - 1));
    }

    /**
     * The previous (-1) or next (1) enchant button, or nothing at the start / end of the list. The
     * caret is drawn by the title; the invisible item takes the click and shows the tooltip.
     */
    private GuiElement getSelectButton(char slot, int direction) {
        var options = getOptions();
        int target = getSelected(options) + direction;
        if (hasPending() || target < 0 || target >= options.size()) {
            return new StaticGuiElement(slot, (ItemStack) null);
        }
        return new StaticGuiElement(
                slot,
                blankItem.clone(),
                click -> {
                    selected = target;
                    refresh();
                    return true;
                },
                direction < 0 ? "<gold>Previous Enchant" : "<gold>Next Enchant",
                "<gray>" + options.get(target).getEnchantment().getDisplayName()
                        + " <dark_gray>(" + (target + 1) + "/" + options.size() + ")"
        );
    }

    private boolean extract(GuiElement.Click click, boolean bookClicked) {
        var preview = getPreview();
        if (preview == null || !canTake(click)) {
            return true;
        }
        if (!AnvilUtil.canStillUse(viewer, anvilLocation)) {
            message("<red>You're no longer at the anvil!");
            viewer.closeInventory();
            return true;
        }
        var item = itemStorage.getStorageItem(viewer, ITEM_SLOT);
        var rune = runeStorage.getStorageItem(viewer, RUNE_SLOT);
        // the preview is rebuilt whenever the inputs change, but check it still matches before taking anything
        var instance = item == null ? null : ExtractionRune.findExtractable(item, preview.instance().getEnchantment().getId());
        if (instance == null || instance.getLevel() != preview.instance().getLevel() || !ExtractionRune.isExtractionRune(rune)) {
            refresh();
            return true;
        }
        int cost = ExtractionRune.getCost(instance);
        if (!canAfford(cost)) {
            message("<red>You don't have enough XP! This extraction costs <white>" + cost + "<red> levels.");
            return true;
        }
        // take the inputs first, then hand out the results
        itemStorage.setStorageItem(viewer, ITEM_SLOT, null);
        if (rune.getAmount() > 1) {
            var remaining = rune.clone();
            remaining.setAmount(rune.getAmount() - 1);
            runeStorage.setStorageItem(viewer, RUNE_SLOT, remaining);
        } else {
            runeStorage.setStorageItem(viewer, RUNE_SLOT, null);
        }
        if (viewer.getGameMode() != GameMode.CREATIVE) {
            viewer.setLevel(viewer.getLevel() - cost);
        }
        // both results wait in their slots; the clicked one is taken straight away
        pendingItem = preview.result().item();
        pendingBook = preview.result().book();
        takePending(click, bookClicked);
        AnvilUtil.use(viewer, anvilLocation);
        selected = 0;
        refresh();
        if (AnvilUtil.getAnvil(anvilLocation) == null && anvilLocation != null) {
            // the anvil broke, like vanilla the menu goes with it
            viewer.closeInventory();
        }
        return true;
    }

    private boolean canAfford(int cost) {
        return canAfford(viewer, cost);
    }

    private static boolean canAfford(Player player, int cost) {
        return player.getGameMode() == GameMode.CREATIVE || player.getLevel() >= cost;
    }

    private void message(String miniMessage) {
        viewer.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    private record Preview(EnchantmentInstance instance, ExtractionRune.Result result) {
    }
}
