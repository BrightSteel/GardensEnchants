package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.gui.BedrockEnchantingMenu;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Bedrock clients work out the anvil result themselves and show an X unless the book in the second
 * slot stores a vanilla enchantment that fits the item. Our books keep their enchant in NBT, so to
 * Bedrock they look empty. Once we've produced a result, we show the Bedrock player a copy of the
 * book that also stores a vanilla enchantment the item can take. The server's real book is
 * untouched and the result they take is still ours.
 */
public class BedrockAnvilListener implements Listener {

    private static final int MATERIAL_SLOT = 1;
    private static final int LEVEL_COST_PROPERTY = 0;
    // java enchantments bedrock doesn't have, so it would never accept them
    private static final Set<NamespacedKey> JAVA_ONLY = Set.of(NamespacedKey.minecraft("sweeping_edge"));

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player player) || !BedrockEnchantingMenu.isBedrockPlayer(player)) {
            return;
        }
        var result = e.getResult();
        if (result == null || result.isEmpty()) {
            return;
        }
        var view = e.getView();
        // wait for the server to send its own anvil updates, then overwrite the book on the client
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> sendBedrockBook(player, view));
    }

    private static void sendBedrockBook(Player player, AnvilView view) {
        if (player.getOpenInventory() != view) {
            return;
        }
        var inv = view.getTopInventory();
        var item = inv.getFirstItem();
        var book = inv.getSecondItem();
        var result = inv.getResult();
        if (item == null || book == null || result == null || result.isEmpty()
                || BooksHandler.INSTANCE.getBookInstance(book) == null) {
            return;
        }
        var enchantment = findApplicableEnchantment(item);
        if (enchantment == null) {
            return;
        }
        var bedrockBook = book.clone();
        bedrockBook.editMeta(EnchantmentStorageMeta.class, meta ->
                meta.addStoredEnchant(enchantment, currentLevel(item, enchantment) + 1, true));

        var handle = ((CraftPlayer) player).getHandle();
        if (!(handle.containerMenu instanceof AnvilMenu menu)) {
            return;
        }
        handle.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, menu.getStateId(), MATERIAL_SLOT, CraftItemStack.asNMSCopy(bedrockBook)));
        // Geyser recalculates the level cost when the book changes, so send it ours again
        handle.connection.send(new ClientboundContainerSetDataPacket(
                menu.containerId, LEVEL_COST_PROPERTY, view.getRepairCost()));
    }

    /**
     * A vanilla enchantment Bedrock would let this book put on the item: it fits the item, doesn't
     * conflict with what's already there and isn't maxed out on it.
     */
    @Nullable
    private static Enchantment findApplicableEnchantment(ItemStack item) {
        var isBook = item.getType() == Material.ENCHANTED_BOOK;
        var existing = existingEnchants(item);
        for (var enchantment : Registry.ENCHANTMENT) {
            var key = enchantment.getKey();
            if (!key.getNamespace().equals(NamespacedKey.MINECRAFT) || JAVA_ONLY.contains(key)) {
                continue;
            }
            // bedrock lets any enchantment onto a book
            if (!isBook && !enchantment.canEnchantItem(item)) {
                continue;
            }
            if (currentLevel(item, enchantment) >= enchantment.getMaxLevel()) {
                continue;
            }
            if (existing.keySet().stream().anyMatch(other -> !other.equals(enchantment) && other.conflictsWith(enchantment))) {
                continue;
            }
            return enchantment;
        }
        return null;
    }

    private static Map<Enchantment, Integer> existingEnchants(ItemStack item) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            return storage.getStoredEnchants();
        }
        return item.getEnchantments();
    }

    private static int currentLevel(ItemStack item, Enchantment enchantment) {
        return existingEnchants(item).getOrDefault(enchantment, 0);
    }

}
