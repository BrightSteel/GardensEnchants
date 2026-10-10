package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Silk Spawner briefly had a 4-use limit (and before that a 30% drop chance), and the tools and books made then say
 * so in their lore. It's back to dropping every spawner for good, so those items get their lore rewritten (and the
 * tool's use count removed) when a player joins with them or opens a container holding them.
 */
public class SilkSpawnerCleanup implements Listener {

    private static final NamespacedKey USES = new NamespacedKey("gardensenchants", "silk_spawner_uses");

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        clean(e.getPlayer().getInventory());
        clean(e.getPlayer().getEnderChest());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent e) {
        clean(e.getInventory());
    }

    private static void clean(Inventory inventory) {
        var contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            var item = contents[slot];
            if (item != null && !item.isEmpty() && clean(item)) inventory.setItem(slot, item);
        }
    }

    /** Rewrites the item if it's a Silk Spawner tool or book still describing a limit. */
    private static boolean clean(ItemStack item) {
        if (!item.hasItemMeta() || !CustomEnchantments.SILK_TOUCH_SPAWNER.hasEnchantment(item) && item.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }
        boolean counted = item.getItemMeta().getPersistentDataContainer().has(USES);
        if (!counted && !describesLimit(item)) return false;
        if (counted) item.editMeta(meta -> meta.getPersistentDataContainer().remove(USES));
        if (item.getType() == Material.ENCHANTED_BOOK) {
            return BooksHandler.INSTANCE.refreshBook(item) || counted;
        }
        new GardensEnchantHandler(item).refreshLore();
        return true;
    }

    private static boolean describesLimit(ItemStack item) {
        var lore = item.lore();
        if (lore == null) return false;
        return lore.stream().map(PlainTextComponentSerializer.plainText()::serialize)
                .anyMatch(line -> line.contains("uses left") || line.contains("use left")
                        || line.contains("wears off after 4 spawners") || line.contains("30% chance"));
    }
}
