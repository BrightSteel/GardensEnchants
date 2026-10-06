package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.events.AreaBreaker;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensfurniture.GardensFurniture;
import com.gardensmc.gardensfurniture.custom.CustomBlockManager;
import com.gardensmc.gardensfurniture.events.FurnitureBreakEvent;
import com.gardensmc.gardensfurniture.util.ProtectionUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.HARVESTER;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.REPLANTER;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.TILLER;

/**
 * The hoe enchants: Replanter, Harvester and Tiller.
 */
public class FarmingListener implements Listener {

    // vanilla crops and what replants them
    private static final Map<Material, Material> SEEDS = Map.of(
            Material.WHEAT, Material.WHEAT_SEEDS,
            Material.CARROTS, Material.CARROT,
            Material.POTATOES, Material.POTATO,
            Material.BEETROOTS, Material.BEETROOT_SEEDS,
            Material.NETHER_WART, Material.NETHER_WART
    );
    private static final Set<Material> TILLABLE = EnumSet.of(Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH);
    // GardensCrops' crops on planter boxes are furniture named <crop>_stage_<n>
    private static final Pattern PLANTER_CROP = Pattern.compile("(.+)_stage_(\\d+)");

    // players whose neighbouring planter crops are being broken, as that fires FurnitureBreakEvent again
    private final Set<UUID> harvestingPlanters = new HashSet<>();

    /**
     * Replanter: puts a fully grown crop back as a fresh one, paying with a seed from its own drops (before
     * Siphon takes them) or, if none dropped, from the player's inventory.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onReplant(BlockDropItemEvent e) {
        var state = e.getBlockState();
        var seed = SEEDS.get(state.getType());
        if (seed == null || !isFullyGrown(state.getBlockData())) {
            return;
        }
        var player = e.getPlayer();
        if (!REPLANTER.hasEnchantment(PlayerUtil.getHeldItem(player))) {
            return;
        }
        var block = e.getBlock();
        if (!block.getType().isAir() || !canHold(block.getRelative(BlockFace.DOWN), state.getType())) {
            return;
        }
        if (!takeSeed(e.getItems(), seed) && !player.getInventory().removeItem(new ItemStack(seed)).isEmpty()) {
            return;
        }
        // a fresh crop, age 0
        block.setType(state.getType());
    }

    /**
     * Harvester: breaking a fully grown crop also breaks the fully grown crops in the 3x3 around it, as
     * the player, so Replanter and Siphon apply to each.
     */
    @EventHandler(ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent e) {
        var player = e.getPlayer();
        var block = e.getBlock();
        if (AreaBreaker.isBreaking(player) || !isFullyGrownCrop(block)
                || !HARVESTER.hasEnchantment(player.getInventory().getItemInMainHand())) {
            return;
        }
        List<Block> targets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                var target = block.getRelative(x, 0, z);
                if (isFullyGrownCrop(target)) {
                    targets.add(target);
                }
            }
        }
        AreaBreaker.breakBlocks(player, block, targets);
    }

    /**
     * Harvester on planter boxes: breaking a ripe GardensCrops crop also breaks the ripe crops on the
     * planter boxes around it. Each goes through GardensFurniture's normal break, so GardensCrops gives
     * its harvest and replants it from the planter's seeds as usual.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHarvestPlanter(FurnitureBreakEvent e) {
        var player = e.getPlayer();
        if (player == null || harvestingPlanters.contains(player.getUniqueId())
                || !isRipePlanterCrop(e.getCustomItem().getIdentifier())
                || !HARVESTER.hasEnchantment(player.getInventory().getItemInMainHand())) {
            return;
        }
        var origin = e.getItemEntity().getEntity().getLocation().toBlockLocation();
        harvestingPlanters.add(player.getUniqueId());
        try {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    var location = origin.clone().add(x, 0, z);
                    if (isRipePlanterCropAt(location)) {
                        CustomBlockManager.breakFurniture(player, location, false);
                    }
                }
            }
        } finally {
            harvestingPlanters.remove(player.getUniqueId());
        }
    }

    /**
     * Tiller: tilling a block also tills the tillable blocks in the 3x3 around it, for a point of
     * durability each, as tilling them one by one would cost.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTill(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND
                || e.useItemInHand() == Event.Result.DENY || e.getBlockFace() == BlockFace.DOWN) {
            return;
        }
        var block = e.getClickedBlock();
        var hoe = e.getItem();
        if (block == null || hoe == null || !Tag.ITEMS_HOES.isTagged(hoe.getType()) || !canTill(block)
                || !TILLER.hasEnchantment(hoe)) {
            return;
        }
        var player = e.getPlayer();
        int tilled = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue; // vanilla tills the clicked block
                }
                var target = block.getRelative(x, 0, z);
                if (canTill(target) && ProtectionUtil.canBuild(target.getLocation(), player)) {
                    target.setType(Material.FARMLAND);
                    tilled++;
                }
            }
        }
        if (tilled > 0 && player.getGameMode() != GameMode.CREATIVE) {
            player.damageItemStack(EquipmentSlot.HAND, tilled);
        }
    }

    private static boolean isFullyGrown(BlockData data) {
        return data instanceof Ageable ageable && ageable.getAge() >= ageable.getMaximumAge();
    }

    private static boolean isFullyGrownCrop(Block block) {
        return SEEDS.containsKey(block.getType()) && isFullyGrown(block.getBlockData());
    }

    // the block a crop of this type grows on
    private static boolean canHold(Block soil, Material crop) {
        return soil.getType() == (crop == Material.NETHER_WART ? Material.SOUL_SAND : Material.FARMLAND);
    }

    // vanilla only tills a block with air above it
    private static boolean canTill(Block block) {
        return TILLABLE.contains(block.getType()) && block.getRelative(BlockFace.UP).getType().isAir();
    }

    // takes one seed out of the drops; false if none dropped
    private static boolean takeSeed(List<Item> drops, Material seed) {
        for (var iterator = drops.iterator(); iterator.hasNext(); ) {
            var item = iterator.next();
            var stack = item.getItemStack();
            if (stack.getType() != seed) {
                continue;
            }
            if (stack.getAmount() > 1) {
                stack.setAmount(stack.getAmount() - 1);
                item.setItemStack(stack);
            } else {
                iterator.remove();
            }
            return true;
        }
        return false;
    }

    // ripe: the last stage, which is the one with no next stage item
    private static boolean isRipePlanterCrop(@Nullable String identifier) {
        if (identifier == null) {
            return false;
        }
        var matcher = PLANTER_CROP.matcher(identifier);
        return matcher.matches()
                && GardensFurniture.itemRegistry.getItem(matcher.group(1) + "_stage_" + (Integer.parseInt(matcher.group(2)) + 1)) == null;
    }

    private static boolean isRipePlanterCropAt(Location location) {
        var furniture = CustomBlockManager.getFurnitureEntity(location, false);
        if (furniture == null || furniture.getItem() == null) {
            return false;
        }
        var customItem = GardensFurniture.itemRegistry.getCustomItem(furniture.getItem());
        return customItem != null && isRipePlanterCrop(customItem.getIdentifier());
    }
}
