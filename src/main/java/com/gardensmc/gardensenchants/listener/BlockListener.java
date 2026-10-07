package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.ItemUtil;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensfurniture.events.CustomBlockInteractEvent;
import com.gardensmc.gardensfurniture.events.CustomDropItemEvent;
import com.gardensmc.gardensfurniture.util.ProtectionUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent e) {
        var player = e.getPlayer();
        var tool = PlayerUtil.getHeldItem(player);
        if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
            e.setCancelled(true);
            PlayerUtil.giveSiphoned(player, e.getItems().stream().map(Item::getItemStack).toList());
        }
    }

    // GardensFurniture's custom blocks and furniture drop their items themselves, so they never reach
    // BlockDropItemEvent; it fires CustomDropItemEvent for them instead
    @EventHandler(ignoreCancelled = true)
    public void onCustomDropItem(CustomDropItemEvent e) {
        var player = e.getPlayer();
        if (CustomEnchantments.SIPHON.hasEnchantment(PlayerUtil.getHeldItem(player))) {
            e.setCancelled(true);
            PlayerUtil.giveSiphoned(player, e.getItems());
        }
    }

    // Drops and XP are handed out at MONITOR, once no other plugin can still cancel the break.
    // Doing it earlier let a later cancel keep the block and the reward (repeatable).
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        var player = e.getPlayer();
        var tool = PlayerUtil.getHeldItem(player);
        if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
            player.giveExp(e.getExpToDrop());
            e.setExpToDrop(0);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSilkAmethyst(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.BUDDING_AMETHYST) {
            var tool = PlayerUtil.getHeldItem(e.getPlayer());
            if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                var drop = new ItemStack(Material.BUDDING_AMETHYST);
                if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
                    PlayerUtil.giveSiphoned(e.getPlayer(), List.of(drop));
                } else {
                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);
                }
            }
        }
    }

    // zero the spawner XP before Siphon (MONITOR) would pay it out
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpawnerBreakXp(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.SPAWNER
                && CustomEnchantments.SILK_TOUCH_SPAWNER.hasEnchantment(e.getPlayer().getInventory().getItemInMainHand())) {
            e.setExpToDrop(0);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSpawnerBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.SPAWNER) {
            return; // avoid a block state snapshot on every break
        }
        if (e.getBlock().getState() instanceof CreatureSpawner spawner) {
            ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
            if (CustomEnchantments.SILK_TOUCH_SPAWNER.hasEnchantment(tool)) {
                EntityType spawnedType = spawner.getSpawnedType();
                if (spawnedType != null) {
                    var item = ItemUtil.buildSpawnerItem(spawnedType.getKey().toString());
                    e.getPlayer().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.SPAWNER) {
            applySpawnerEntity(e.getItemInHand(), e.getBlockPlaced());
        }
    }

    // Right-clicking a note block (GardensFurniture custom block) is consumed by furniture, which
    // cancels the vanilla PlayerInteractEvent, so BlockPlaceEvent never fires. Furniture instead
    // exposes CustomBlockInteractEvent for these clicks, so we place the spawner ourselves here and
    // cancel the event to stop furniture running its default item placement.
    @EventHandler(ignoreCancelled = true)
    public void onSpawnerPlaceOnCustomBlock(CustomBlockInteractEvent e) {
        ItemStack heldItem = e.getHeldItem();
        if (heldItem == null || heldItem.getType() != Material.SPAWNER) return;

        NBTItem spawnerItem = new NBTItem(heldItem);
        if (!spawnerItem.hasKey("entity_id")) return;

        Player player = e.getPlayer();
        Block target = e.getBlock().getRelative(e.getBlockFace());
        if (!target.isReplaceable()) return;
        if (!ProtectionUtil.canBuild(target.getLocation(), player)) return;

        target.setType(Material.SPAWNER);
        if (!applySpawnerEntity(heldItem, target)) {
            // NBT was malformed; don't leave a stray empty spawner or consume the item
            target.setType(Material.AIR);
            return;
        }

        e.setCancelled(true); // stop furniture's default placement from also running

        EquipmentSlot hand = e.getHand() == null ? EquipmentSlot.HAND : e.getHand();
        if (hand == EquipmentSlot.OFF_HAND) {
            player.swingOffHand();
        } else {
            player.swingMainHand();
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack used = player.getInventory().getItem(hand);
            if (used != null) {
                used.setAmount(used.getAmount() - 1);
                player.getInventory().setItem(hand, used);
            }
        }
    }

    private static boolean applySpawnerEntity(ItemStack spawnerStack, Block placed) {
        NBTItem spawnerItem = new NBTItem(spawnerStack);

        if (!spawnerItem.hasKey("entity_id")) return false;
        String entity_id = spawnerItem.getString("entity_id");

        var key = NamespacedKey.fromString(entity_id);
        var spawnedType = key == null ? null : Registry.ENTITY_TYPE.get(key);
        if (spawnedType == null) return false;

        if (placed.getState() instanceof CreatureSpawner spawner) {
            // setSpawnedType also clears SpawnPotentials/nextSpawnData, so the placed
            // spawner isn't left empty (raw SpawnData NBT edits don't do this).
            spawner.setSpawnedType(spawnedType);
            spawner.update(true, false);
            return true;
        }
        return false;
    }
}
