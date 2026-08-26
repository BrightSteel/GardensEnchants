package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BookInstance;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.books.TypedBooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantEntryHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;
import java.util.Objects;

/**
 * Geyser/Bedrock equivalent of the Java {@link EnchantingMenu}. Bedrock players can't render the
 * Java resource-pack enchanting-table menu (which relies on custom fonts and inventory slots), so
 * instead of inserting a book into a GUI they are required to <b>hold an unenchanted book</b> when
 * right-clicking the enchanting table. We then send a Cumulus {@link SimpleForm} listing the same
 * randomly-generated enchantment options the Java menu would show (keyed by book type, persisted via
 * {@link EnchantEntryHandler}); selecting one closes the form, charges the player, and turns the held
 * book into the matching enchanted book.
 */
public class BedrockEnchantingMenu {

    /**
     * Hidden private-use glyph placed at the start of the form title, rendered invisibly by the
     * Bedrock pack's blank glyph sheet. It gates the pack's {@code gardens_enchant_form} layout
     * (an enchanting-table-style list of option rows) — see {@code GardensPlayers/bedrock-resourcepack}.
     */
    private static final String ENCHANT_TOKEN = "\uE117";

    /** Fixed number of option rows the pack renders; padded so button indices line up with cells. */
    private static final int SLOTS = BooksHandler.ENCHANT_GENERATE_COUNT;

    /** Button index (after the option rows) for the "View Enchanting Books" footer button. */
    private static final int BOOKS_BUTTON_INDEX = SLOTS;

    /** Book icons in the Bedrock pack, one per enchant id (see {@code textures/items/books/}). */
    private static final String BOOK_ICON_DIR = "textures/items/books/";

    private BedrockEnchantingMenu() {
    }

    /**
     * True if the player is connecting through Geyser/Floodgate (a Bedrock client). Guarded so the
     * plugin still works when Floodgate isn't installed (it's a softdepend).
     */
    public static boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable ignored) {
            // Floodgate not present on the server.
            return false;
        }
    }

    public static void open(Player viewer) {
        var held = viewer.getInventory().getItemInMainHand();
        if (!isEnchantableBook(held)) {
            message(viewer, "<red>Hold an unenchanted book to enchant at the table!");
            return;
        }

        var type = TypedBooksHandler.getTypeFromBook(held);
        List<BookInstance> options = new EnchantEntryHandler(viewer.getUniqueId(), type)
                .getEnchantmentInstances()
                .stream()
                .filter(Objects::nonNull)
                .map(BooksHandler.INSTANCE::buildBook)
                .toList();
        if (options.isEmpty()) {
            message(viewer, "<red>No enchantments are available right now. Try again later!");
            return;
        }

        var builder = SimpleForm.builder()
                // ENCHANT_TOKEN gates the pack's enchanting-table layout; invisible to the player.
                .title(ENCHANT_TOKEN + "Enchant Book")
                .content("Choose an enchantment to apply to your held book:");
        // Send exactly SLOTS buttons so indices line up with the pack's fixed rows; pad with empty
        // (blank text) buttons, which the pack hides. Real rows carry the enchant's book icon.
        for (int i = 0; i < SLOTS; i++) {
            if (i < options.size()) {
                var option = options.get(i);
                builder.button(buttonText(viewer, option), FormImage.Type.PATH, bookIconPath(option.enchantment()));
            } else {
                builder.button("");
            }
        }
        // Index SLOTS: opens the read-only book catalogue (maps to the pack's footer button).
        builder.button("View Enchanting Books");
        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx == BOOKS_BUTTON_INDEX) {
                BedrockEnchantingBooksMenu.open(viewer);
                return;
            }
            if (idx < 0 || idx >= options.size()) {
                return;
            }
            var chosen = options.get(idx);
            // The form response fires off the main thread; inventory + economy mutations must be sync.
            Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> purchase(viewer, type, chosen));
        });
        FloodgateApi.getInstance().sendForm(viewer.getUniqueId(), builder.build());
    }

    private static void purchase(Player viewer, String type, BookInstance bookInstance) {
        // Re-check the held item — it may have changed while the form was open.
        var held = viewer.getInventory().getItemInMainHand();
        if (!isEnchantableBook(held)) {
            message(viewer, "<red>You're no longer holding a book to enchant!");
            return;
        }
        var enchantment = bookInstance.enchantment();
        // Check each cost separately so the player knows exactly what they're short on.
        int cost = enchantment.getTableCost();
        if (!GardensEnchants.econ.has(viewer, cost)) {
            message(viewer, "<red>You don't have enough acorns! This enchantment costs <white>" + cost + "<red> acorns.");
            return;
        }
        int xpCost = enchantment.getTableCostInXp();
        if (viewer.getLevel() < xpCost) {
            message(viewer, "<red>You don't have enough XP! This enchantment costs <white>" + xpCost + "<red> levels.");
            return;
        }

        chargePlayer(viewer, enchantment);
        // Consume one book from the held stack and hand over the enchanted book.
        if (held.getAmount() == 1) {
            viewer.getInventory().setItemInMainHand(bookInstance.book());
        } else {
            held.setAmount(held.getAmount() - 1);
            var leftover = viewer.getInventory().addItem(bookInstance.book());
            leftover.values().forEach(item ->
                    viewer.getWorld().dropItemNaturally(viewer.getLocation(), item));
        }
        // Regenerate the options for next time, matching the Java menu's behaviour on purchase.
        new EnchantEntryHandler(viewer.getUniqueId(), type).resetEnchantEntry();
        message(viewer, "<green>Enchanted your book with <white>" + enchantment.getDisplayName() + "<green>!");
    }

    private static boolean isEnchantableBook(ItemStack item) {
        return item != null && item.getType() == Material.BOOK;
    }

    private static void chargePlayer(Player viewer, GardensEnchantment enchantment) {
        GardensEnchants.econ.withdrawPlayer(viewer, enchantment.getTableCost());
        viewer.setLevel(viewer.getLevel() - enchantment.getTableCostInXp());
    }

    /**
     * Two-line button label: the coloured enchantment name (reused from the built book so it matches
     * the Java menu exactly), then a cost line whose XP / money segments turn red when unaffordable.
     */
    private static String buttonText(Player viewer, BookInstance bookInstance) {
        var meta = bookInstance.book().getItemMeta();
        var displayName = meta != null ? meta.displayName() : null;
        String name = displayName != null
                ? LegacyComponentSerializer.legacySection().serialize(displayName)
                : bookInstance.enchantment().getDisplayName();

        var enchantment = bookInstance.enchantment();
        int xpCost = enchantment.getTableCostInXp();
        int cost = enchantment.getTableCost();
        boolean canAffordXp = viewer.getLevel() >= xpCost;
        boolean canAffordMoney = GardensEnchants.econ.has(viewer, cost);
        String costLine = (canAffordXp ? "§a" : "§c") + xpCost + " XP §7· "
                + (canAffordMoney ? "§6" : "§c") + cost + "$";
        return name + "\n" + costLine;
    }

    /** Bedrock pack texture path for an enchant's book icon (matches {@code textures/items/books/<id>.png}). */
    private static String bookIconPath(GardensEnchantment enchantment) {
        return BOOK_ICON_DIR + enchantment.getId();
    }

    private static void message(Player viewer, String miniMessage) {
        viewer.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage));
    }
}
