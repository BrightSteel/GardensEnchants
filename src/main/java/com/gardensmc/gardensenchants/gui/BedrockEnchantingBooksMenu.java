package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Geyser/Bedrock equivalent of the Java {@link EnchantingBooksMenu} — a read-only, paged catalogue of
 * every enchanting book. The bundled Bedrock pack re-skins this into a grid of book tiles (icon +
 * name), with the book's item lore revealed on hover; Prev/Next page through them and Back returns to
 * {@link BedrockEnchantingMenu}. Reached from the enchant menu's "View Enchanting Books" button.
 *
 * <p><b>Plugin&lt;-&gt;pack contract.</b> The title carries {@link #EBOOKS_TOKEN} to gate the pack's
 * {@code gardens_ebooks_form} layout. Buttons are sent in one fixed block so the pack's hardcoded cells
 * map to the right actions (missing tiles/nav are sent as empty buttons so indices stay aligned):
 * <ul>
 *   <li>{@code 0..SLOTS-1} — the page's book tiles (hover-only; tapping does nothing).</li>
 *   <li>{@code SLOTS} — Prev, {@code SLOTS+1} — Next, {@code SLOTS+2} — Back.</li>
 * </ul>
 */
public class BedrockEnchantingBooksMenu {

    private static final String EBOOKS_TOKEN = "\uE118";

    // Grid is GRID_COLS x GRID_ROWS tiles per page; keep in sync with the pack's hardcoded cells.
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 3;
    private static final int SLOTS = GRID_COLS * GRID_ROWS;

    private static final String BOOK_ICON_DIR = "textures/items/books/";
    private static final String NAV_PREV_ICON = "textures/gardens/shop/nav_prev";
    private static final String NAV_NEXT_ICON = "textures/gardens/shop/nav_next";

    private BedrockEnchantingBooksMenu() {
    }

    public static void open(Player viewer) {
        sendPage(viewer, 0);
    }

    /** All enchantments ordered by rarity then id, matching the Java {@link EnchantingBooksMenu}. */
    private static List<GardensEnchantment> allEnchantments() {
        return Arrays.stream(EnchantRarity.values())
                .flatMap(rarity -> GardensEnchantments.INSTANCE.getEnchantments(rarity)
                        .stream()
                        .sorted(Comparator.comparing(GardensEnchantment::getId)))
                .toList();
    }

    private static void sendPage(Player viewer, int page) {
        var all = allEnchantments();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) SLOTS));
        int p = Math.max(0, Math.min(page, totalPages - 1));
        int from = p * SLOTS;
        var pageItems = all.subList(from, Math.min(from + SLOTS, all.size()));
        boolean hasPrev = p > 0;
        boolean hasNext = p < totalPages - 1;

        var builder = SimpleForm.builder()
                .title(EBOOKS_TOKEN + "Enchanting Books")
                .content("Page " + (p + 1) + " / " + totalPages);

        // 0..SLOTS-1: book tiles (icon + name; lore on hover), padded to keep indices aligned.
        for (int i = 0; i < SLOTS; i++) {
            if (i < pageItems.size()) {
                var enchantment = pageItems.get(i);
                builder.button(tileText(enchantment), FormImage.Type.PATH, BOOK_ICON_DIR + enchantment.getId());
            } else {
                builder.button("");
            }
        }
        // SLOTS, +1: Prev / Next (empty = no image, so the pack hides the tile).
        builder.button(hasPrev ? "Prev" : "", FormImage.Type.PATH, NAV_PREV_ICON);
        builder.button(hasNext ? "Next" : "", FormImage.Type.PATH, NAV_NEXT_ICON);
        // SLOTS+2: Back to the enchant menu (always visible).
        builder.button("§cBack");

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx == SLOTS && hasPrev) {
                sendPage(viewer, p - 1);
            } else if (idx == SLOTS + 1 && hasNext) {
                sendPage(viewer, p + 1);
            } else if (idx == SLOTS + 2) {
                BedrockEnchantingMenu.open(viewer);
            }
        });
        FloodgateApi.getInstance().sendForm(viewer.getUniqueId(), builder.build());
    }

    /**
     * Tile text: the rarity-coloured name (shown on the tile), then two blank lines and the book's lore
     * + max level + table availability. The pack clips the tile to the name and reveals the whole string
     * on hover, so the lore behaves like the Java book's item lore. The two blank lines keep the lore
     * from peeking under the name in the clipped tile (matching the quests menu).
     */
    private static String tileText(GardensEnchantment enchantment) {
        var legacy = LegacyComponentSerializer.legacySection();
        String name = legacy.serialize(Component.text(enchantment.getDisplayName())
                .color(enchantment.getRarity().getTextColor()));
        var sb = new StringBuilder(name).append("\n\n\n");
        for (Component loreLine : enchantment.getLore()) {
            sb.append("§7").append(legacy.serialize(loreLine)).append('\n');
        }
        sb.append("§7Max Level: ").append(RomanNumber.toRoman(enchantment.getMaxLevel()));
        if (!enchantment.isVisibleInTable()) {
            sb.append("\n§8Un-Available in Table");
        }
        return sb.toString();
    }
}
