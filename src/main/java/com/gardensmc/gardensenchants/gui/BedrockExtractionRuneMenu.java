package com.gardensmc.gardensenchants.gui;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.rune.ExtractionRune;
import com.gardensmc.gardensenchants.utils.AnvilUtil;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensenchants.utils.RomanNumber;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Geyser/Bedrock equivalent of the Java {@link ExtractionRuneMenu}, which Bedrock can't render. The
 * anvil hands the item and rune back to the player's inventory and this form lists the enchants to
 * extract. Nothing is held while the form is open: picking one looks the item and rune up in the
 * inventory again, so closing the form (or disconnecting) can't lose anything.
 */
public class BedrockExtractionRuneMenu {

    private BedrockExtractionRuneMenu() {
    }

    public static void open(Player viewer, ItemStack item, @Nullable Location anvilLocation) {
        var options = ExtractionRune.getExtractableEnchantments(item);
        if (options.isEmpty()) {
            return;
        }
        var builder = SimpleForm.builder()
                .title("Extract Enchantment")
                .content("Choose an enchant to move onto a book. You keep the item without it.");
        options.forEach(option -> builder.button(
                buttonText(viewer, option),
                FormImage.Type.PATH,
                BedrockEnchantingMenu.bookIconPath(option.getEnchantment())
        ));
        builder.validResultHandler(response -> {
            int index = response.clickedButtonId();
            if (index < 0 || index >= options.size()) {
                return;
            }
            // the response arrives off the main thread
            Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> extract(viewer, item, options.get(index), anvilLocation));
        });
        FloodgateApi.getInstance().sendForm(viewer.getUniqueId(), builder.build());
    }

    private static void extract(Player viewer, ItemStack original, EnchantmentInstance option, @Nullable Location anvilLocation) {
        if (!viewer.isOnline()) {
            return;
        }
        if (!AnvilUtil.canStillUse(viewer, anvilLocation)) {
            message(viewer, "<red>You're no longer at the anvil!");
            return;
        }
        var inventory = viewer.getInventory();
        int itemSlot = findSlot(inventory.getStorageContents(), original);
        int runeSlot = findRuneSlot(inventory.getStorageContents());
        if (itemSlot == -1 || runeSlot == -1) {
            message(viewer, "<red>You need the item and an extraction rune in your inventory!");
            return;
        }
        var item = inventory.getItem(itemSlot);
        var instance = ExtractionRune.findExtractable(item, option.getEnchantment().getId());
        if (instance == null || instance.getLevel() != option.getLevel()) {
            message(viewer, "<red>That item has changed. Use the anvil again!");
            return;
        }
        int cost = ExtractionRune.getCost(instance);
        boolean creative = viewer.getGameMode() == GameMode.CREATIVE;
        if (!creative && viewer.getLevel() < cost) {
            message(viewer, "<red>You don't have enough XP! This extraction costs <white>" + cost + "<red> levels.");
            return;
        }
        var result = ExtractionRune.extract(item, instance);
        // take the inputs first, then hand out the results
        inventory.setItem(itemSlot, null);
        var rune = inventory.getItem(runeSlot);
        rune.setAmount(rune.getAmount() - 1);
        if (!creative) {
            viewer.setLevel(viewer.getLevel() - cost);
        }
        PlayerUtil.giveItems(viewer, List.of(result.item(), result.book()));
        AnvilUtil.use(viewer, anvilLocation);
        message(viewer, "<green>Extracted <white>" + instance.getEnchantment().getDisplayName() + "<green> onto a book!");
    }

    private static int findSlot(ItemStack[] contents, ItemStack original) {
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getAmount() == 1 && contents[i].isSimilar(original)) {
                return i;
            }
        }
        return -1;
    }

    private static int findRuneSlot(ItemStack[] contents) {
        for (int i = 0; i < contents.length; i++) {
            if (ExtractionRune.isExtractionRune(contents[i])) {
                return i;
            }
        }
        return -1;
    }

    private static String buttonText(Player viewer, EnchantmentInstance option) {
        var enchantment = option.getEnchantment();
        var name = enchantment.getDisplayName() + (enchantment.getMaxLevel() == 1 ? "" : " " + RomanNumber.toRoman(option.getLevel()));
        int cost = ExtractionRune.getCost(option);
        boolean canAfford = viewer.getGameMode() == GameMode.CREATIVE || viewer.getLevel() >= cost;
        return name + "\n" + (canAfford ? "§a" : "§c") + cost + " XP";
    }

    private static void message(Player viewer, String miniMessage) {
        viewer.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage));
    }
}
