package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BookInstance;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.books.TypedBooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantEntryHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
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
import java.util.logging.Level;

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

    public static void open(Player viewer, boolean isFullBookSetup) {
        var held = viewer.getInventory().getItemInMainHand();
        if (!isEnchantableBook(held)) {
            message(viewer, "<red>Hold an unenchanted book to enchant at the table!");
            return;
        }

        var type = TypedBooksHandler.getTypeFromBook(held);
        // load off the main thread, then build the form back on it (it reads inventory, xp and money)
        new EnchantEntryHandler(viewer.getUniqueId(), type).getOffers().whenComplete((offers, throwable) -> {
            if (!GardensEnchants.plugin.isEnabled()) {
                return;
            }
            Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> {
                if (!viewer.isOnline()) {
                    return;
                }
                if (throwable != null) {
                    GardensEnchants.plugin.getLogger().log(Level.SEVERE, "Failed to load enchant offers", throwable);
                    message(viewer, "<red>No enchantments are available right now. Try again later!");
                    return;
                }
                sendForm(viewer, type, offers, isFullBookSetup);
            });
        });
    }

    private static void sendForm(Player viewer, String type, List<EnchantmentInstance> offers, boolean isFullBookSetup) {
        List<BookInstance> options = offers.stream()
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
                builder.button(buttonText(viewer, option, isFullBookSetup), FormImage.Type.PATH, bookIconPath(option.enchantment()));
            } else {
                builder.button("");
            }
        }
        // Index SLOTS: opens the read-only book catalogue (maps to the pack's footer button).
        builder.button("View Enchanting Books");
        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx == BOOKS_BUTTON_INDEX) {
                BedrockEnchantingBooksMenu.open(viewer, isFullBookSetup);
                return;
            }
            if (idx < 0 || idx >= options.size()) {
                return;
            }
            // The form response fires off the main thread; inventory + economy mutations must be sync.
            Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> purchase(viewer, type, idx, offers.get(idx), options.get(idx), isFullBookSetup));
        });
        FloodgateApi.getInstance().sendForm(viewer.getUniqueId(), builder.build());
    }

    private static void purchase(Player viewer, String type, int index, EnchantmentInstance offer, BookInstance bookInstance, boolean isFullBookSetup) {
        // Re-check the held item — it may have changed while the form was open.
        var held = viewer.getInventory().getItemInMainHand();
        if (!isEnchantableBook(held) || !type.equals(TypedBooksHandler.getTypeFromBook(held))) {
            message(viewer, "<red>You're no longer holding a book to enchant!");
            return;
        }
        // The form may be stale (a second form, or a slow response after buying): only sell the
        // offer if it is still the current roll, so each roll can be bought once.
        var entryHandler = new EnchantEntryHandler(viewer.getUniqueId(), type);
        if (!entryHandler.isCurrentOffer(index, offer)) {
            message(viewer, "<red>These enchantments have changed. Use the table again to see the new ones!");
            return;
        }
        var enchantment = bookInstance.enchantment();
        if (!isFullBookSetup && enchantment.getRarity().needsFullBookSetup()) {
            message(viewer, EnchantingMenu.NEEDS_BOOKSHELVES);
            return;
        }
        // Check each cost separately so the player knows exactly what they're short on.
        int cost = enchantment.getTableCost(bookInstance.level());
        if (!GardensEnchants.econ.has(viewer, cost)) {
            message(viewer, "<red>You don't have enough acorns! This enchantment costs <white>" + cost + "<red> acorns.");
            return;
        }
        int xpCost = enchantment.getTableCostInXp(bookInstance.level());
        if (viewer.getLevel() < xpCost) {
            message(viewer, "<red>You don't have enough XP! This enchantment costs <white>" + xpCost + "<red> levels.");
            return;
        }

        if (!chargePlayer(viewer, enchantment, bookInstance.level())) {
            message(viewer, "<red>You don't have enough acorns! This enchantment costs <white>" + cost + "<red> acorns.");
            return;
        }
        // Consume one book from the held stack and hand over the enchanted book.
        if (held.getAmount() == 1) {
            viewer.getInventory().setItemInMainHand(bookInstance.book());
        } else {
            held.setAmount(held.getAmount() - 1);
            PlayerUtil.giveItem(viewer, bookInstance.book());
        }
        // Regenerate the options for next time, matching the Java menu's behaviour on purchase.
        entryHandler.reroll();
        message(viewer, "<green>Enchanted your book with <white>" + enchantment.getDisplayName() + "<green>!");
    }

    private static boolean isEnchantableBook(ItemStack item) {
        return item != null && item.getType() == Material.BOOK;
    }

    /**
     * Takes the table cost. False if the economy refused it (the balance changed since the check), in
     * which case nothing is taken. A free enchant skips the economy, which refuses zero withdrawals.
     */
    static boolean chargePlayer(Player viewer, GardensEnchantment enchantment, int level) {
        var cost = enchantment.getTableCost(level);
        if (cost > 0 && !GardensEnchants.econ.withdrawPlayer(viewer, cost).transactionSuccess()) {
            return false;
        }
        int xp = enchantment.getTableCostInXp(level);
        viewer.setLevel(viewer.getLevel() - xp);
        org.bukkit.Bukkit.getPluginManager().callEvent(new com.gardensmc.gardensenchants.events.TableSpendEvent(
                viewer, "enchant", enchantment.getId() + "_" + level, cost, xp));
        return true;
    }

    /**
     * Two-line button label: the coloured enchantment name (reused from the built book so it matches
     * the Java menu exactly), then a cost line whose XP / money segments turn red when unaffordable.
     */
    private static String buttonText(Player viewer, BookInstance bookInstance, boolean isFullBookSetup) {
        var meta = bookInstance.book().getItemMeta();
        var displayName = meta != null ? meta.displayName() : null;
        String name = displayName != null
                ? LegacyComponentSerializer.legacySection().serialize(displayName)
                : bookInstance.enchantment().getDisplayName();

        var enchantment = bookInstance.enchantment();
        if (!isFullBookSetup && enchantment.getRarity().needsFullBookSetup()) {
            return name + "\n§cNeeds 15 bookshelves";
        }
        int xpCost = enchantment.getTableCostInXp(bookInstance.level());
        int cost = enchantment.getTableCost(bookInstance.level());
        boolean canAffordXp = viewer.getLevel() >= xpCost;
        boolean canAffordMoney = GardensEnchants.econ.has(viewer, cost);
        String costLine = (canAffordXp ? "§a" : "§c") + xpCost + " XP §7· "
                + (canAffordMoney ? "§6" : "§c") + cost + "$";
        return name + "\n" + costLine;
    }

    /** Bedrock pack texture path for an enchant's book icon (matches {@code textures/items/books/<id>.png}). */
    static String bookIconPath(GardensEnchantment enchantment) {
        return BOOK_ICON_DIR + enchantment.getId();
    }

    private static void message(Player viewer, String miniMessage) {
        viewer.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage));
    }
}
