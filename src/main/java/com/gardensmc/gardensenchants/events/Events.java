package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.OnlinePlayerCache;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.gui.BedrockEnchantingMenu;
import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import com.gardensmc.gardensenchants.utils.*;
import com.gardensmc.gardensfurniture.custom.CustomBlockManager;
import com.gardensmc.gardensfurniture.util.ProtectionUtil;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.*;

public class Events implements Listener {

    private static final int MAX_TIMBER_COUNT = 40;

    @EventHandler
    public void onEquip(EntityEquipmentChangedEvent e) {
        if (e.getEntity() instanceof Player player) {
            e.getEquipmentChanges().forEach(((equipmentSlot, equipmentChange) -> onEquipmentChange(player, equipmentSlot, equipmentChange)));
        }
    }

    private void onEquipmentChange(Player player, EquipmentSlot slot, EntityEquipmentChangedEvent.EquipmentChange change) {
        // equipping
        var oldItem = change.oldItem();
        var newItem = change.newItem();

        // handle old item changes first to remove effects
        if (oldItem.getType() != Material.AIR) {
            if (slot == EquipmentSlot.FEET) {
                if (player.getWalkSpeed() > 0.2f) {
                    player.setWalkSpeed(0.2f);
                }
//                if (player.getAllowFlight() && !e.getPlayer().hasPermission("gardens.fly.anywhere")) {
//                    String[] anywhere_req = new String[]{
//                            "resource_world",
//                            "nether",
//                            "end"
//                    };
//                    if (Arrays.asList(anywhere_req).contains(e.getPlayer().getWorld().getName().toLowerCase()) || !e.getPlayer().hasPermission("gardens.fly")) {
//                        e.getPlayer().setFlying(false);
//                        e.getPlayer().setAllowFlight(false);
//                    }
//
//                }
            }
            else if (slot == EquipmentSlot.HEAD) {
                if (CustomEnchantments.NIGHT_VISION.hasEnchantment(oldItem)) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }

        // handle new item second to add / re-add effect
        if (newItem.getType() != Material.AIR) {
            if (slot == EquipmentSlot.FEET) {
                if (SPEEDY.hasEnchantment(newItem)) {
                    int lvl = SPEEDY.getEnchantmentLevel(newItem);
                    float newSpeed = 0.2f + (lvl * 0.025f);
                    player.setWalkSpeed(newSpeed);
                }
//                if (CustomEnchantments.WINGED_BOOTS.hasEnchantment(newItem)) {
//                    NBTItem nbtItem = new NBTItem(e.getNewArmorPiece());
//                    if (nbtItem.hasKey("charge")) {
//                        if (nbtItem.getInteger("charge") > 0) {
//                            e.getPlayer().setAllowFlight(true);
//                        }
//                    }
//                }

            }
            else if (slot == EquipmentSlot.HEAD) {
                if (CustomEnchantments.NIGHT_VISION.hasEnchantment(newItem)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                }
            }
        }
    }

    @EventHandler
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.isCancelled()) return;

        if (e.getEntity() instanceof Player player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                ItemStack boots = player.getInventory().getBoots();
                if (boots != null && JELLY_LEGS.hasEnchantment(boots)) {
                    int level = JELLY_LEGS.getEnchantmentLevel(boots);
                    if (level < 1) level = 1;
                    if (ProbabilityUtil.roll(level * 15)) {
                        e.setCancelled(true);
                        player.playSound(player, Sound.ENTITY_SLIME_SQUISH, 1, 1);
                    }
                }
            }
        }
        else if (e.getEntity() instanceof Item item) {
            if (ETERNAL.hasEnchantment(item.getItemStack())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {

        if (e.isCancelled()) return;

        EntityType[] netherTypes = new EntityType[]{
                EntityType.ZOGLIN,
                EntityType.PIGLIN,
                EntityType.PIGLIN_BRUTE,
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.WITHER_SKELETON,
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE,
                EntityType.STRIDER,
                EntityType.GHAST
        };
        if (e.getDamager() instanceof Player player) {
            if (e.getEntity() instanceof Enemy) {
                OnlinePlayerCache.INSTANCE.getPlayerData(player.getUniqueId()).startCombatTimer();
            }
            ItemStack weapon = PlayerUtil.getHeldItem(player);
            if (weapon == null) {
                return;
            }

            if (Arrays.asList(netherTypes).contains(e.getEntity().getType())) {
                if (NETHERLING.hasEnchantment(weapon)) {
                    e.setDamage(e.getDamage()*2);
                }
            }

            if (LIFE_STEAL.hasEnchantment(weapon)) {
                double enchant_level = LIFE_STEAL.getEnchantmentLevel(weapon);
                if (enchant_level < 1) enchant_level = 1;
                double hp = e.getDamage() * ((enchant_level + 1)/30);
                double newHP = player.getHealth() + hp;
                if (newHP > 20) {
                    newHP = 20;
                }
                player.setHealth(newHP);
            }

            var playerData = OnlinePlayerCache.INSTANCE.getPlayerData(player.getUniqueId());
            if (DOUBLE_STRIKE.hasEnchantment(weapon) && !playerData.hasDoubleStrikeCooldown()) {
                double enchant_level = DOUBLE_STRIKE.getEnchantmentLevel(weapon);
                if (enchant_level < 1) enchant_level = 1;
                if (ProbabilityUtil.roll(20*enchant_level)) {
                    if (e.getEntity() instanceof LivingEntity livingEntity) {
                        playerData.setDoubleStrikeCooldown(10); // ensures double strike doesn't loop itself
                        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
                            // run with delay so player can visually see the second hit and hear it
                            if (!livingEntity.isDead()) {
                                livingEntity.damage(e.getDamage(), player);
                                livingEntity.getLocation().getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
                            }
                        }, 5);
                    }
                }
            }
        }
    }


//    @EventHandler
//    public void onPlayerJoin(PlayerJoinEvent e) {
//        new EnchantCache(e.getPlayer().getUniqueId()).loadCache();
//
//        EnchantUtils.refreshEnchants(e.getPlayer());
//    }

//    @EventHandler
//    public void onDisconnect(PlayerQuitEvent e) {
//        EnchantCache.remove(e.getPlayer().getUniqueId());
//    }

//    @EventHandler
//    public static void onApplyRune(PrepareAnvilEvent e) {
//        // todo update these to use new anvilView?
//        AnvilInventory inv = e.getInventory();
//        ItemStack[] contents = inv.getContents();
//
//        ItemStack item = contents[0];
//        ItemStack rune = contents[1];
//        if (item != null && rune != null) {
//            CustomItem customItem = GardensFurniture.itemRegistry.getCustomItem(rune);
//            if (customItem == null || (!customItem.getIdentifier().equalsIgnoreCase("amber") && rune.getAmount() != 1)) return;
////            if (rune.getAmount() != 1) return;
//            // attempt to get rune
////            CustomItem customItem = CustomRegister.INSTANCE.itemRegistry.getCustomItem(rune);
////            if (customItem != null) {
//            ItemStack result = null;
//                switch (customItem.getIdentifier()) {
//                    case "slot_increaser_rune" -> result = RuneUtil.getRuneResult(Rune.SLOT_INCREASER, item);
//                    case "power_up_rune" -> result = RuneUtil.getRuneResult(Rune.POWER_UP, item);
//                    case "restoration_rune" -> result = RuneUtil.getRuneResult(Rune.RESTORATION, item);
//                    case "amber" -> result = RuneUtil.amberUpgrade(item, rune);
//                }
//                if (result != null) {
//                    inv.setRepairCost(3);
//                    e.setResult(result);
//                }
////            }
//        }
//    }




//    @EventHandler
//    public void onSilkTouchSpawner(SilkSpawnersSpawnerBreakEvent e) {
//        e.getPlayer().sendMessage("WAP");
//        if (!EnchantUtils.hasEnchantment(e.getPlayer().getInventory().getItemInMainHand(), CustomEnchantments.SILK_TOUCH_SPAWNER)) {
////            e.setCancelled(true);
//            e.getPlayer().sendMessage("Cancelling drop");
//            e.setDrop(new ItemStack(Material.AIR));
//        }
//    }

    // we need to run this prior to furniture removing the furniture store, so low priority (runs first)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onTimber(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("Timber_TBD")) {
            return;
        }

        Player player = e.getPlayer();


        if (!TIMBER.hasEnchantment(player.getInventory().getItemInMainHand())) {
            return;
        }

        if (isTimberable(e.getBlock())) {

            List<Block> blocks = new ArrayList<>();
            blocks.add(e.getBlock());

            List<Block> destroy = new ArrayList<>();
            destroy.add(e.getBlock());

            BlockFace[] faces = new BlockFace[]{
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST,
                    BlockFace.UP,
                    BlockFace.DOWN
            };

            for (int i = 0; i < blocks.size(); i++) {
                if (i >= MAX_TIMBER_COUNT) break;
                Block curr = blocks.get(i);
                for (BlockFace blockFace : faces) {
                    for (int j = -1; j < 2; j++) {
                        for (int l = -1; l < 2; l++) {
                            Block potential = curr.getRelative(blockFace);
                            potential = potential.getRelative(j, 0, l);
                            Block finalPotential = potential;
                            if (potential.getType() == e.getBlock().getType()
                                    && Objects.equals(CustomBlockManager.getItemFromLeaves(potential), CustomBlockManager.getItemFromLeaves(e.getBlock()))
                                    && blocks.stream().noneMatch(block -> block.getLocation().equals(finalPotential.getLocation()))
                            ) {
                                blocks.add(potential);
                                destroy.add(potential);
                            }
                        }
                    }

                }
            }

            // do block breaking
            for (Block block : destroy) {
                block.setMetadata("Timber_TBD", new FixedMetadataValue(GardensEnchants.plugin, true));
                breakIfAble(player, block);
            }
        }
    }

    private static final Set<Material> TIMBER_MATERIALS = Set.of(
            Material.MUSHROOM_STEM,
            Material.BROWN_MUSHROOM_BLOCK,
            Material.RED_MUSHROOM_BLOCK,
            Material.MANGROVE_ROOTS
    );

    private boolean isTimberable(Block block) {
        String block_name = block.getType().toString().toUpperCase();
        var isVanillaBreakable = TIMBER_MATERIALS.contains(block.getType()) || block_name.endsWith("LOG") || block_name.endsWith("LEAVES") || block_name.endsWith("VINE");
        if (isVanillaBreakable) {
            return true;
        }
        if (block.getType() == Material.NOTE_BLOCK) {
            var customItem = CustomBlockManager.getItemFromLeaves(block);
            return customItem != null && customItem.getIdentifier().contains("log");
        }
       return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onVeinMiner(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("Vein_Miner_TBD")) {
            return;
        }

        Player player = e.getPlayer();
        String block_name = e.getBlock().getType().toString().toUpperCase();
        if (block_name.endsWith("ORE")) {
            if (!VEIN_MINER.hasEnchantment(player.getInventory().getItemInMainHand())) {
                return;
            }

            List<Block> blocks = new ArrayList<>();
            blocks.add(e.getBlock());

            List<Block> destroy = new ArrayList<>();
            destroy.add(e.getBlock());

            BlockFace[] faces = new BlockFace[]{
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST,
                    BlockFace.UP,
                    BlockFace.DOWN
            };

            for (int i = 0; i < blocks.size(); i++) {
                if (i >= 15) break;
                Block curr = blocks.get(i);
                for (BlockFace blockFace : faces) {
                    Block potential = curr.getRelative(blockFace);
                    if (potential.getType() == e.getBlock().getType()) {
                        blocks.add(potential);
                        destroy.add(potential);
                    }
                }
            }

            // do block breaking
            for (Block block : destroy) {
                block.setMetadata("Vein_Miner_TBD", new FixedMetadataValue(GardensEnchants.plugin, true));
                breakIfAble(player, block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmeltMineBlock(BlockBreakEvent e) {
        Player player = e.getPlayer();
        String block_name = e.getBlock().getType().toString().toUpperCase();
        if (block_name.endsWith("ORE")) {
            var heldItem = PlayerUtil.getHeldItem(player);
            if (heldItem == null || !CustomEnchantments.SMELTER.hasEnchantment(heldItem)) {
                return;
            }
            int fortune = heldItem.getEnchantmentLevel(Enchantment.FORTUNE);
            Material drop = null;
            int amount = 1 + ThreadLocalRandom.current().nextInt(0, fortune + 1);

            switch (e.getBlock().getType()) {
                case IRON_ORE, DEEPSLATE_IRON_ORE ->
                    drop = Material.IRON_INGOT;
                case GOLD_ORE, DEEPSLATE_GOLD_ORE ->
                    drop = Material.GOLD_INGOT;
                case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE ->
                    drop = Material.DIAMOND;
                case COPPER_ORE, DEEPSLATE_COPPER_ORE -> {
                    drop = Material.COPPER_INGOT;
                    amount = ThreadLocalRandom.current().nextInt(2, 6) + (ThreadLocalRandom.current().nextInt(2, 6) * fortune);
                }
            }
            if (drop != null) {
                e.setDropItems(false);
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(drop, amount));
            }
        }
    }
    
    private static final Set<Material> SUPER_BREAK_STONE_MATERIALS = Set.of(
            Material.AMETHYST_BLOCK,
            Material.ANCIENT_DEBRIS,
            Material.ANDESITE,
            Material.BASALT,
            Material.BRICK,
            Material.CALCITE,
            Material.CLAY,
            Material.CRIMSON_NYLIUM,
            Material.DEEPSLATE,
            Material.DIORITE,
            Material.DRIPSTONE_BLOCK,
            Material.GRANITE,
            Material.NETHER_BRICK,
            Material.NETHERRACK,
            Material.MAGMA_BLOCK,
            Material.OBSIDIAN,
            Material.SMOOTH_BASALT,
            Material.STONE,
            Material.TUFF,
            Material.WARPED_NYLIUM
    );

    private static final Set<Material> SUPER_BREAK_DIRT_MATERIALS = Set.of(
            Material.DIRT,
            Material.GRASS_BLOCK,
            Material.COARSE_DIRT,
            Material.PODZOL,
            Material.MYCELIUM,
            Material.MUD,
            Material.MUDDY_MANGROVE_ROOTS,
            Material.ROOTED_DIRT,
            Material.SAND,
            Material.RED_SAND,
            Material.GRAVEL,
            Material.SNOW_BLOCK,
            Material.POWDER_SNOW,
            Material.CLAY,
            Material.SOUL_SAND,
            Material.SOUL_SOIL,
            Material.MOSS_BLOCK,
            Material.MOSS_CARPET
    );

    private static final Set<String> SUPER_BREAK_ENDS_WITH = Set.of(
            "ORE",
            "TERRACOTTA",
            "STONE"
    );

    private static final Set<String> SUPER_BREAK_EXC = Set.of(
            "STAIR",
            "WALL",
            "SLAB"
    );

    @EventHandler(ignoreCancelled = true)
    public void onMineBlock(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("Super_Breaker_TBD") || e.getBlock().hasMetadata("Vein_Miner_TBD")) {
            return;
        }

        Player player = e.getPlayer();
        var tool = player.getInventory().getItemInMainHand();
        if (canSuperBreak(e.getBlock(), tool)) {
            if (!SUPER_BREAKER.hasEnchantment(tool)) {
                return;
            }
            BlockFace facing = PlayerUtil.getBlockFaceLooking(player);
            if (facing == null) {
                return;
            }

            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if (i == 0 && j == 0) continue;
                    Block target = null;
                    String face = facing.toString();
                    if (face.startsWith("NORTH") || face.startsWith("SOUTH")) { // X, Y area
                        target = e.getBlock().getRelative(i, j, 0);
                    }
                    else if (face.startsWith("EAST") || face.startsWith("WEST")) {
                        target = e.getBlock().getRelative(0, j, i);
                    }
                    else if (face.startsWith("UP") || face.startsWith("DOWN")) {
                        target = e.getBlock().getRelative(i, 0, j);
                    }

                    if (target != null && canSuperBreak(target, tool)) {
                        // give metadata so we can avoid calling this event on this block again
                        target.setMetadata("Super_Breaker_TBD", new FixedMetadataValue(GardensEnchants.plugin, true));
                        breakIfAble(player, target);
                    }
                }
            }
        }
    }

    private boolean canSuperBreak(Block block, ItemStack tool) {
        String block_name = block.getType().toString().toUpperCase();
        if (tool.getType().toString().toUpperCase().contains("SHOVEL")) {
            return SUPER_BREAK_DIRT_MATERIALS.contains(block.getType());
        }
        return (SUPER_BREAK_STONE_MATERIALS.contains(block.getType()) || SUPER_BREAK_ENDS_WITH.stream().anyMatch(block_name::endsWith)) && !SUPER_BREAK_EXC.contains(block_name);
    }

    private void breakIfAble(Player player, Block target) {
        if (!ProtectionUtil.canBuild(target.getLocation(), player)) {
            return;
        }

        // break block, this calls block break event so need 2 ensure it doesnt keep calling our firehammer hting
        // which we do by giving it metadata to mark it as such
        player.breakBlock(target);
        if (target.hasMetadata("Super_Breaker_TBD")) {
            target.removeMetadata("Super_Breaker_TBD", GardensEnchants.plugin);
        }
        if (target.hasMetadata("Vein_Miner_TBD")) {
            target.removeMetadata("Vein_Miner_TBD", GardensEnchants.plugin);
        }
        if (target.hasMetadata("Timber_TBD")) {
            target.removeMetadata("Timber_TBD", GardensEnchants.plugin);
        }
    }

//    @EventHandler
//    public void onVeinMine(PlayerVeinMineEvent e) {
//        ItemStack itemStack = e.getItem();
//
//        String block_name = e.getBlock().getType().name();
//        boolean isWood =  (block_name.endsWith("WOOD") || block_name.endsWith("LOG") || block_name.endsWith("ROOTS"));
//        if (itemStack != null) {
//            String item_name = itemStack.getType().name();
//            if (isWood) {
//                if (item_name.endsWith("AXE")) {
//                    if (EnchantUtils.hasEnchantment(itemStack, CustomEnchantments.TIMBER)) {
//                        return;
//                    }
//                }
//            }
//            else {
//                if (item_name.endsWith("PICKAXE")) {
//                    if (EnchantUtils.hasEnchantment(itemStack, CustomEnchantments.VEIN_MINER)) {
//                        return;
//                    }
//                }
//            }
//        }
//
//        e.setCancelled(true);
//    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        if (CustomEnchantments.ETERNAL.hasEnchantment(e.getItem())) {
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            ItemStack weapon = PlayerUtil.getHeldItem(killer);
            if (weapon != null && CustomEnchantments.SKULL_STEALER.hasEnchantment(weapon)) {
                int ench_level = SKULL_STEALER.getEnchantmentLevel(weapon);
//                int percent = ench_level == 1 ? 15 : 15 * ench_level;
                if (ProbabilityUtil.roll(25)) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), ItemUtils.getHead(e.getEntity().getUniqueId()));

                    killer.sendMessage(ChatColor.YELLOW + "Dropped " + e.getEntity().getName() + "'s head");
                }
            }
        }
    }

//    @EventHandler
//    public void onInteract(PlayerInteractEvent e) {
//        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
//            e.setCancelled(true);
//            EnchantMenu enchantMenu = new EnchantMenu(e.getPlayer());
//            enchantMenu.open();
//        }
//    }

//    @EventHandler(ignoreCancelled = true)
//    public void onInventoryOpen(InventoryOpenEvent e) {
//        Inventory inv = e.getInventory();
//        if (inv.getType() == InventoryType.ENCHANTING && e.getPlayer() instanceof Player player) {
//            e.setCancelled(true);
////            EnchantingGUI enchantingGUI = new EnchantingGUI(player);
////            player.openInventory(enchantingGUI.getInventory());
//
//            new EnchantingMenu(player).open();
//        }
//    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOpenEnchanting(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
            e.setCancelled(true);
            var player = e.getPlayer();
            if (BedrockEnchantingMenu.isBedrockPlayer(player)) {
                // Bedrock clients can't render the Java menu; require a held book and use a Geyser form.
                BedrockEnchantingMenu.open(player);
            } else {
                new EnchantingMenu(player, isFullBookSetup(e.getClickedBlock())).open();
            }
        }
    }

    private boolean isFullBookSetup(Block enchantingTableBlock) {
        var numBookShelves = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y < 3; y++) {
                    if (Math.abs(x) < 2 && Math.abs(z) < 2) {
                        continue;
                    }
                    var block = enchantingTableBlock.getRelative(x, y, z);
                    if (block.getType() == Material.BOOKSHELF) {
                        numBookShelves++;
                    }
                    if (numBookShelves == 15) {
                        break;
                    }
                }
            }
        }
        return numBookShelves == 15;
    }

//    @EventHandler(ignoreCancelled = true)
//    public void onClickEnchanting(InventoryClickEvent e) {
//        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;
//        var slot = e.getSlot();
//        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
//            Inventory clicked = e.getClickedInventory();
//            if (clicked != null && clicked.getType() == InventoryType.PLAYER) {
//                if (e.getInventory().getItem(1) != null) {
//                    e.setCancelled(true);
//                    return;
//                }
//            } else if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof EnchantingGUI) {
//                if (slot != 1) {
//                    e.setCancelled(true);
//                    return;
//                }
//            }
//        } else if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof EnchantingGUI) {
//            ItemStack selection = null;
//            if (slot != 1 && e.getWhoClicked() instanceof Player player) {
//                if (slot > 2 && slot < 9) {
//                    selection = e.getInventory().getItem(8);
//                } else if (slot > 11 && slot < 18) {
//                    selection = e.getInventory().getItem(17);
//                } else if (slot > 20 && slot < 27) {
//                    selection = e.getInventory().getItem(26);
//                } else if (slot == 19) {
//                    new EnchantingBooksMenu(player).open();
//                }
//                // todo account for xp and money
//                if (selection != null) {
//                    int playerLevels = ExpUtil.getExp(player);
//                    NBTItem nbtItem = new NBTItem(selection);
//                    int amber = nbtItem.getInteger("amber");
//                    int xp = nbtItem.getInteger("xp");
//
//                    if (playerLevels >= xp && GardensEnchants.econ.getBalance(player) >= amber) {
//                        ExpUtil.changeExp(player, -xp);
//                        GardensEnchants.econ.withdrawPlayer(player, amber);
//
////                            new EnchantCache(player.getUniqueId()).resetEnchant();
//                        ItemStack enchantTarget = e.getInventory().getItem(1);
//                        if (enchantTarget != null) {
//                            int amount = enchantTarget.getAmount();
//                            if (amount > 1) {
//                                enchantTarget.setAmount(amount - 1);
//                                e.getInventory().getViewers().get(0).getInventory().addItem(enchantTarget);
//                            }
//                            // remove costs from lore before enchanting target
//                            ItemMeta meta = selection.getItemMeta();
//                            if (meta != null) {
//                                List<String> lore = meta.getLore();
//                                if (lore != null) {
//                                    // remove last 3 lines
//                                    for (int i = 0; i < 3; i++) {
//                                        lore.remove(lore.size() - 1);
//                                    }
//                                }
//                                meta.setLore(lore);
//                                selection.setItemMeta(meta);
//                            }
//                            e.getInventory().setItem(1, selection);
//                        }
//
//                        updateEnchantingGUI(player, e.getInventory());
//                    }
//                }
//
//                e.setCancelled(true);
//            }
//        }
//
//        if (e.getWhoClicked() instanceof Player player) {
//            if (e.getRawSlot() == 1) {
//                if (e.getAction().name().contains("PLACE") || e.getAction().name().contains("PICKUP") || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
//                    updateEnchantingGUI(player, e.getInventory());
//                }
//
//            }
//            else if (e.getRawSlot() >= 27 && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
//                updateEnchantingGUI(player, e.getInventory());
//            }
//        }
//    }

//    private void updateEnchantingGUI(Player player, Inventory inv) {
//
//        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
//
//
//            String title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE094");
//
//            if (itemIsEnchantable(inv.getItem(1))) {
//                BookInstance[] books = genBooks(inv, player);
//                String[] book = new String[3];
//                for (int i = 0; i < book.length; i++) {
//                    var b = books[i];
//                    book[i] = (b != null) ? b.enchantment().getEnchantment().getDisplayName() + " " + RomanNumber.toRoman(b.enchantment().getLevel()) : " ";
//                }
//
//                HashMap<Character, Character>[] fontMaps = new HashMap[]{FontMappings.INSTANCE.fontMap1, FontMappings.INSTANCE.fontMap2, FontMappings.INSTANCE.fontMap3};
//                String[] sample = new String[3];
//                for (int i = 0; i < 3; i++) {
//                     String raw = stripFormatting(book[i]);
//                     String padding = FontMappings.getPadding(raw);
//                     sample[i] = FontMappings.INSTANCE.replaceAll(raw, fontMaps[i]) + padding;
//                }
//                title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE05F" + OraxenShift.INSTANCE.getShift(193, true) + sample[0]  + sample[1]  + sample[2]);
//
//            }
//            else {
//                removeBooks(inv);
//            }
//            player.getOpenInventory().setTitle(title);
//
//        }, 1);
//    }

//    private String stripFormatting(String formatted) {
//        var maxLength = 12;
//        if (formatted.length() < maxLength) {
//            return formatted; // already small enough
//        }
//        var numeralIndex = formatted.lastIndexOf(" ") + 1;
//        var numeral = formatted.substring(numeralIndex);
//        return formatted.substring(0, Math.min(maxLength, numeralIndex)) + " " + numeral;
//    }

//    // places books into inventory
//    private BookInstance[] genBooks(Inventory inv, Player player) {
//        ItemStack display = ItemUtils.getBlankItem();
////        int playerLevels = ExpUtil.getExp(player);
////        double playerBal = GardensEnchants.econ.getBalance(player);
//        // cost and amber
//        // costs
//        var books = new BookInstance[3];
//        for (int i = 0; i < books.length; i++) {
//            var bookInstance = BooksHandler.INSTANCE.generateRandomBook();
//            var book = bookInstance.book();
//            int amber = 1;
//            int xp = 1;
//            var meta = book.getItemMeta();
//            if (meta != null) {
//                var lore = meta.lore() != null ? meta.lore() : new ArrayList<Component>();
//                lore.add(Component
//                        .text("\uE01F " + amber)
//                        .color(NamedTextColor.WHITE)
//                        .decoration(TextDecoration.ITALIC, false));
//                lore.add(Component
//                        .text("\uE10F " + xp)
//                        .color(NamedTextColor.WHITE)
//                        .decoration(TextDecoration.ITALIC, false));
//                meta.lore(lore);
//            }
//            book.setItemMeta(meta);
//            NBT.modify(book, nbt -> {
//                nbt.setInteger("amber", amber);
//                nbt.setInteger("xp", xp);
//            });
//            books[i] = bookInstance;
//        }
//
//        inv.setItem(8, books[0].book());
//        inv.setItem(17, books[1].book());
//        inv.setItem(26, books[2].book());
//
//        int row = 0;
//        for (BookInstance bookInstance : books) {
//            var bookClone = bookInstance.book().clone().withType(display.getType());
//            bookClone.getItemMeta().setCustomModelData(display.getItemMeta().getCustomModelData());
//            var rowOffset = row * 9;
//            for (int i = rowOffset + 3; i < rowOffset + 8; i++) {
//                inv.setItem(i, bookClone);
//            }
//            row++;
//        }
//        return books;
//    }
//
//    private void removeBooks(Inventory inv) {
//        for (int j = 0; j < 3; j++) {
//            for (int i = 3; i < 9; i++) {
//                inv.setItem(i + (j * 9), null);
//            }
//        }
//    }
//
//    private boolean itemIsEnchantable(ItemStack item) {
//        return item != null && item.getType() == Material.BOOK;
//    }

//    @EventHandler
//    public void onDrag(InventoryDragEvent e) {
//        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;
////
//        if (e.getRawSlots().stream().anyMatch(integer -> integer < 27 && integer != 1)) {
//            e.setCancelled(true);
//        }
//
//        if (e.getWhoClicked() instanceof Player player) {
//            if (e.getRawSlots().contains(1)) {
//                updateEnchantingGUI(player, e.getInventory());
//            }
//        }
//
////        if (e.getWhoClicked() instanceof Player player) {
////            ItemStack bookSlot = e.getInventory().getItem(1);
////            if (bookSlot != null)
////                updateEnchantingGUI(bookSlot, player);
////        }
//
//    }

    @EventHandler(ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent e) {
        Player player = e.getEntity().getKiller();
        if (player == null) return;

        if (!CustomEnchantments.SKULL_STEALER.hasEnchantment(player.getInventory().getItemInMainHand())) {
            return;
        }

        if (ProbabilityUtil.roll(10)) {
            String typeEncoder = getEntityTypeVariant(e.getEntity(), e.getEntityType().toString().toLowerCase());
            ItemStack head = ItemUtils.getMobHead(typeEncoder);
            if (head != null) {
                e.getDrops().add(head);
            }
        }
    }

    private String getEntityTypeVariant(Entity entity, String type) {
        String variant = "";
        if (entity instanceof Axolotl axolotl) {
            variant =  axolotl.getVariant().toString().toLowerCase();
        }
        else if (entity instanceof Sheep sheep) {
            if (sheep.getColor() != null) {
                variant = sheep.getColor().toString().toLowerCase();
            }
        }
        else if (entity instanceof Villager villager) {
            variant = villager.getProfession().toString().toLowerCase();
        }
        else if (entity instanceof Cat cat) {
            variant = cat.getCatType().toString().toLowerCase();
        }
        else if (entity instanceof MushroomCow mushroomCow) {
            variant = mushroomCow.getVariant().toString().toLowerCase();
        }
        else if (entity instanceof Fox fox) {
            variant = fox.getFoxType().toString().toLowerCase();
        }
        else if (entity instanceof Frog frog) {
            variant = frog.getVariant().toString().toLowerCase();
        }
        else if (entity instanceof Horse horse) {
            variant = horse.getColor().toString().toLowerCase();
        }
        else if (entity instanceof Rabbit rabbit) {
            variant = rabbit.getRabbitType().toString().toLowerCase();
        }

        if (!variant.isEmpty()) {
            variant += "_";
        }

        return variant + type;
    }


//    @EventHandler
//    public void onMove(InventoryMoveItemEvent e) {
//        if (!(e.getDestination().getHolder() instanceof EnchantingGUI)) return;
//        System.out.println("moved " + e.getItem().getType().name());
//    }

//    @EventHandler
//    private void onPrepareGrindstone(PrepareResultEvent e) {
//        if (e.getInventory() instanceof GrindstoneInventory inv) {
//            if (Arrays.stream(e.getInventory().getContents()).anyMatch(itemStack ->
//                itemStack.getType() == Material.ENCHANTED_BOOK && itemStack.hasItemMeta() && itemStack.getItemMeta().hasCustomModelData()
//            )) {
//                if (e.getResult() != null) {
//                    ItemMeta meta = e.getResult().getItemMeta();
//                    if (!meta.getDisplayName().equalsIgnoreCase("Book")) {
//                        meta.setDisplayName("Book");
//                        e.getResult().setItemMeta(meta);
//                        e.setResult(e.getResult());
//                    }
//                }
//            }
//        }
//    }

//    @EventHandler
//    public void onClickGrindstone(InventoryClickEvent e) {
//        if (e.getClickedInventory() != null && e.getClickedInventory() instanceof GrindstoneInventory inv) {
//            if (e.getSlotType() == InventoryType.SlotType.RESULT) {
//                ItemStack result = inv.getItem(e.getSlot());
//                if (result != null && result.getType() == Material.BOOK) {
//                    System.out.println("Grindstone resulted!");
//                }
//            }
//        }
//    }

    @EventHandler
    public void arrowFire(EntityDamageByEntityEvent e) {

        if (e.getDamager() instanceof Arrow arrow) {
            Color color = arrow.getColor();
            if (color == null) return;
            int rgb = color.asRGB();
            if (rgb == 15558708) { // explosive arrow
                arrow.getWorld().createExplosion(e.getDamager().getLocation(), 3, true, true);
            }
            else if (rgb == 15587124) {
                arrow.getWorld().strikeLightning(e.getDamager().getLocation());
            }
        }
    }

//    @EventHandler
//    public void onVillagerInteract(PlayerInteractAtEntityEvent e) {
//        if (e.getRightClicked() instanceof Villager villager) {
//            List<MerchantRecipe> recipes = villager.getRecipes();
//            MerchantRecipe[] array = recipes.toArray(new MerchantRecipe[0]);
//            for (int i = 0; i < array.length; i++) {
//                MerchantRecipe recipe = array[i];
//                ItemStack result = recipe.getResult();
//                if (result.getType() == Material.ENCHANTED_BOOK) {
//                    ItemMeta meta = result.getItemMeta();
//                    if (meta != null) {
//                        if (meta.hasEnchants()) {
//                            meta.getEnchants()
//                        }
//                    }
//                }
//            }
//        }
//    }

}
