package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.OnlinePlayerCache;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.gui.BedrockEnchantingMenu;
import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import com.gardensmc.gardensenchants.utils.*;
import com.gardensmc.gardensfurniture.custom.CustomBlockManager;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.*;

public class Events implements Listener {

    // how many blocks Timber expands from; the number actually broken is capped by AreaBreaker
    private static final int MAX_TIMBER_COUNT = 40;
    // the helmet's night vision is applied with Integer.MAX_VALUE ticks; potions last minutes
    private static final int HELMET_EFFECT_MIN_DURATION = 1_000_000;
    private static final BlockFace[] AREA_FACES = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

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
                // only undo the speed Speedy set, so other speed changes (e.g. /speed) are kept
                if (SPEEDY.hasEnchantment(oldItem)
                        && player.getWalkSpeed() == getSpeedyWalkSpeed(SPEEDY.getEnchantmentLevel(oldItem))) {
                    player.setWalkSpeed(0.2f);
                }
            }
            else if (slot == EquipmentSlot.HEAD) {
                if (CustomEnchantments.NIGHT_VISION.hasEnchantment(oldItem)) {
                    // only remove the endless effect the helmet gave, not night vision from a potion
                    var effect = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
                    if (effect != null && (effect.isInfinite() || effect.getDuration() > HELMET_EFFECT_MIN_DURATION)) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    }
                }
            }
        }

        // handle new item second to add / re-add effect
        if (newItem.getType() != Material.AIR) {
            if (slot == EquipmentSlot.FEET) {
                if (SPEEDY.hasEnchantment(newItem)) {
                    int lvl = SPEEDY.getEnchantmentLevel(newItem);
                    player.setWalkSpeed(getSpeedyWalkSpeed(lvl));
                }

            }
            else if (slot == EquipmentSlot.HEAD) {
                if (CustomEnchantments.NIGHT_VISION.hasEnchantment(newItem)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                }
            }
        }
    }

    private static float getSpeedyWalkSpeed(int level) {
        return 0.2f + (level * 0.025f);
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
                // cap at the player's real max health; setHealth throws above it
                var maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                double max = maxHealth != null ? maxHealth.getValue() : 20;
                player.setHealth(Math.min(player.getHealth() + hp, max));
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
                                // the target is still invulnerable from the first hit, which made
                                // vanilla discard this one; clear it so the second hit lands
                                livingEntity.setNoDamageTicks(0);
                                livingEntity.damage(e.getDamage(), player);
                                livingEntity.getLocation().getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
                            }
                        }, 5);
                    }
                }
            }
        }
    }

    // we need to run this prior to furniture removing the furniture store, so low priority (runs first)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onTimber(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (AreaBreaker.isBreaking(player)) {
            return;
        }

        if (!TIMBER.hasEnchantment(player.getInventory().getItemInMainHand())) {
            return;
        }

        var origin = e.getBlock();
        if (isTimberable(origin)) {
            var originType = origin.getType();
            var originCustomItem = CustomBlockManager.getItemFromLeaves(origin);

            List<Block> blocks = new ArrayList<>();
            Set<Block> found = new HashSet<>();
            blocks.add(origin);
            found.add(origin);

            for (int i = 0; i < blocks.size(); i++) {
                if (i >= MAX_TIMBER_COUNT) break;
                Block curr = blocks.get(i);
                for (BlockFace blockFace : AREA_FACES) {
                    for (int j = -1; j < 2; j++) {
                        for (int l = -1; l < 2; l++) {
                            Block potential = curr.getRelative(blockFace).getRelative(j, 0, l);
                            if (potential.getType() == originType
                                    && !found.contains(potential)
                                    && Objects.equals(CustomBlockManager.getItemFromLeaves(potential), originCustomItem)
                            ) {
                                found.add(potential);
                                blocks.add(potential);
                            }
                        }
                    }
                }
            }

            AreaBreaker.breakBlocks(player, origin, blocks);
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
        Player player = e.getPlayer();
        if (AreaBreaker.isBreaking(player)) {
            return;
        }

        var origin = e.getBlock();
        String block_name = origin.getType().toString().toUpperCase();
        if (block_name.endsWith("ORE")) {
            if (!VEIN_MINER.hasEnchantment(player.getInventory().getItemInMainHand())) {
                return;
            }

            List<Block> blocks = new ArrayList<>();
            Set<Block> found = new HashSet<>();
            blocks.add(origin);
            found.add(origin);

            for (int i = 0; i < blocks.size(); i++) {
                if (i >= 15) break;
                Block curr = blocks.get(i);
                for (BlockFace blockFace : AREA_FACES) {
                    Block potential = curr.getRelative(blockFace);
                    if (potential.getType() == origin.getType() && found.add(potential)) {
                        blocks.add(potential);
                    }
                }
            }

            AreaBreaker.breakBlocks(player, origin, blocks);
        }
    }

    // MONITOR: the ingots are dropped by hand, so only do it once the break can't be cancelled any more
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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
        Player player = e.getPlayer();
        if (AreaBreaker.isBreaking(player)) {
            return;
        }

        var tool = player.getInventory().getItemInMainHand();
        if (canSuperBreak(e.getBlock(), tool)) {
            if (!SUPER_BREAKER.hasEnchantment(tool)) {
                return;
            }
            BlockFace facing = PlayerUtil.getBlockFaceLooking(player);
            if (facing == null) {
                return;
            }

            List<Block> targets = new ArrayList<>();
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
                        targets.add(target);
                    }
                }
            }
            AreaBreaker.breakBlocks(player, e.getBlock(), targets);
        }
    }

    private boolean canSuperBreak(Block block, ItemStack tool) {
        String block_name = block.getType().toString().toUpperCase();
        if (tool.getType().toString().toUpperCase().contains("SHOVEL")) {
            return SUPER_BREAK_DIRT_MATERIALS.contains(block.getType());
        }
        return (SUPER_BREAK_STONE_MATERIALS.contains(block.getType()) || SUPER_BREAK_ENDS_WITH.stream().anyMatch(block_name::endsWith))
                && SUPER_BREAK_EXC.stream().noneMatch(block_name::contains);
    }

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
                if (ProbabilityUtil.roll(25)) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), ItemUtils.getHead(e.getEntity().getUniqueId()));

                    killer.sendMessage(ChatColor.YELLOW + "Dropped " + e.getEntity().getName() + "'s head");
                }
            }
        }
    }

    // vanilla's maximum bookshelf power; a table this surrounded can sell Epic and Legendary offers
    private static final int FULL_BOOKSHELVES = 15;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOpenEnchanting(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
            e.setCancelled(true);
            var player = e.getPlayer();
            if (!GardensEnchants.isTableAvailable()) {
                player.sendMessage(ChatColor.RED + "The enchanting table is unavailable right now.");
                return;
            }
            if (BedrockEnchantingMenu.isBedrockPlayer(player)) {
                // Bedrock clients can't render the Java menu; require a held book and use a Geyser form.
                BedrockEnchantingMenu.open(player, isFullBookSetup(e.getClickedBlock()));
            } else {
                new EnchantingMenu(player, isFullBookSetup(e.getClickedBlock())).open();
            }
        }
    }

    /**
     * Whether the table has the most bookshelf power vanilla allows (15 shelves), using vanilla's rule:
     * a shelf two blocks out, on the table's level or one up, with the block between it and the table
     * clear (air or anything else replaceable).
     */
    private boolean isFullBookSetup(Block enchantingTableBlock) {
        int shelves = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }
                for (int y = 0; y <= 1; y++) {
                    var shelf = enchantingTableBlock.getRelative(x, y, z).getType();
                    var between = enchantingTableBlock.getRelative(x / 2, y, z / 2).getType();
                    if (Tag.ENCHANTMENT_POWER_PROVIDER.isTagged(shelf) && Tag.ENCHANTMENT_POWER_TRANSMITTER.isTagged(between)
                            && ++shelves >= FULL_BOOKSHELVES) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

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

}
