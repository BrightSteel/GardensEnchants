package com.gardensmc.gardensenchants.events;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.EnchantCache;
import com.gardensmc.gardensfurniture.custom.CustomItem;
import com.gardensmc.gardensfurniture.register.CustomRegister;
import com.gardensmc.gardensplayers.GardensPlayers;
import com.gardensmc.gardensplayers.player.PlayerCache;
import com.gardensmc.gardensenchants.enchants.*;
import com.gardensmc.gardensenchants.gui.EnchantingGUI;
import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import com.gardensmc.gardensenchants.gui.InventoryUpdate;
import com.gardensmc.gardensenchants.utils.*;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.tr7zw.nbtapi.NBTItem;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Events implements Listener {

    @EventHandler
    public void onEquip(ArmorEquipEvent e) {

        // equipping
        if (e.getNewArmorPiece() != null && e.getNewArmorPiece().getType() != Material.AIR) {
            if (e.getType() == ArmorType.BOOTS) {
                if (EnchantUtils.hasEnchantment(e.getNewArmorPiece(), CustomEnchantments.SPEEDY)) {
                    int lvl = EnchantUtils.getEnchantLevel(e.getNewArmorPiece(), CustomEnchantments.SPEEDY);
                    float newSpeed = 0.2f + (lvl * 0.025f);
                    e.getPlayer().setWalkSpeed(newSpeed);
                }
                if (EnchantUtils.hasEnchantment(e.getNewArmorPiece(), CustomEnchantments.WINGED_BOOTS)) {
                    NBTItem nbtItem = new NBTItem(e.getNewArmorPiece());
                    if (nbtItem.hasKey("charge")) {
                        if (nbtItem.getInteger("charge") > 0) {
                            e.getPlayer().setAllowFlight(true);
                        }
                    }
                }

            }
            else if (e.getType() == ArmorType.HELMET) {
                if (EnchantUtils.hasEnchantment(e.getNewArmorPiece(), CustomEnchantments.NIGHT_VISION)) {
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                }

            }
        }

        // unequipping
        if (e.getOldArmorPiece() != null && e.getOldArmorPiece().getType() != Material.AIR) {
            if (e.getType() == ArmorType.BOOTS) {
                if (e.getPlayer().getWalkSpeed() > 0.2f) {
                    e.getPlayer().setWalkSpeed(0.2f);
                }
                if (e.getPlayer().getAllowFlight() && !e.getPlayer().hasPermission("gardens.fly.anywhere")) {
                    String[] anywhere_req = new String[]{
                            "resource_world",
                            "nether",
                            "end"
                    };
                    if (Arrays.asList(anywhere_req).contains(e.getPlayer().getWorld().getName().toLowerCase()) || !e.getPlayer().hasPermission("gardens.fly")) {
                        e.getPlayer().setFlying(false);
                        e.getPlayer().setAllowFlight(false);
                    }

                }
            }
            else if (e.getType() == ArmorType.HELMET) {
                if (EnchantUtils.hasEnchantment(e.getOldArmorPiece(), CustomEnchantments.NIGHT_VISION)){
                    e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
                    e.getPlayer().sendMessage("Removed night vision effect");
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
                if (boots != null && EnchantUtils.hasEnchantment(boots, CustomEnchantments.JELLY_LEGS)) {
                    int level = boots.getEnchantmentLevel(CustomEnchantments.JELLY_LEGS);
                    if (level < 1) level = 1;
                    if (ProbabilityUtil.roll(level * 15)) {
                        e.setCancelled(true);
                        player.playSound(player, Sound.ENTITY_SLIME_SQUISH, 1, 1);
                    }
                }
            }
        }
        else if (e.getEntity() instanceof Item item) {
            if (EnchantUtils.hasEnchantment(item.getItemStack(), CustomEnchantments.ETERNAL)) {
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
            ItemStack weapon = player.getInventory().getItemInMainHand();

            if (Arrays.asList(netherTypes).contains(e.getEntity().getType())) {
                if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.NETHERLING)) {
                    e.setDamage(e.getDamage()*2);
                }
            }

            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.LIFE_STEAL)) {
                // todo check if this actually works with custom enchantments, i doubt it
                double enchant_level = weapon.getEnchantmentLevel(CustomEnchantments.LIFE_STEAL);
                if (enchant_level < 1) enchant_level = 1;
                double hp = e.getDamage() * ((enchant_level + 1)/30);
                double newHP = player.getHealth() + hp;
                if (newHP > 20) {
                    newHP = 20;
                }
                player.setHealth(newHP);
            }

            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.DOUBLE_STRIKE)) {
                double enchant_level = weapon.getEnchantmentLevel(CustomEnchantments.DOUBLE_STRIKE);
                if (enchant_level < 1) enchant_level = 1;
                if (ProbabilityUtil.roll(20*enchant_level)) {
                    if (e.getEntity() instanceof LivingEntity livingEntity) {
                        // assuming this won't call EntityDamageByEntityEvent again?
                        livingEntity.damage(e.getDamage());
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        new EnchantCache(e.getPlayer().getUniqueId()).loadCache();

        EnchantUtils.refreshEnchants(e.getPlayer());
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        EnchantCache.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public static void onApplyRune(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();

        ItemStack item = contents[0];
        ItemStack rune = contents[1];
        if (item != null && rune != null) {
            CustomItem customItem = CustomRegister.INSTANCE.itemRegistry.getCustomItem(rune);
            if (customItem == null || (!customItem.getIdentifier().equalsIgnoreCase("amber") && rune.getAmount() != 1)) return;
//            if (rune.getAmount() != 1) return;
            // attempt to get rune
//            CustomItem customItem = CustomRegister.INSTANCE.itemRegistry.getCustomItem(rune);
//            if (customItem != null) {
            ItemStack result = null;
                switch (customItem.getIdentifier()) {
                    case "slot_increaser_rune" -> result = RuneUtil.getRuneResult(Rune.SLOT_INCREASER, item);
                    case "power_up_rune" -> {}
                    case "restoration_rune" -> result = RuneUtil.getRuneResult(Rune.RESTORATION, item);
                    case "amber" -> result = RuneUtil.amberUpgrade(item, rune);
                }
                if (result != null) {
                    inv.setRepairCost(3);
                    e.setResult(result);
                }
//            }
        }
    }

    @EventHandler
    public static void onCombine(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();

        if (contents[0] != null && contents[1] != null) {
            if (e.getResult() == null || e.getResult().getType() == Material.AIR) {
                return;
            }
            else if (contents[0].getType() == contents[1].getType()) {
                // clone first item
                ItemStack og = contents[0].clone();
                // upgrade matching enchants
                for (Map.Entry<Enchantment, Integer> entry : contents[0].getEnchantments().entrySet()) {
                    if (contents[1].getEnchantments().containsKey(entry.getKey())) {
                        // matching, attempt upgrade
                        int level_a = entry.getValue();
                        int level_b = contents[1].getEnchantments().get(entry.getKey());
                        int level_max = entry.getKey().getMaxLevel();
                        int upgrade = level_a + 1;

                        if (level_a == level_b && upgrade <= level_max) {
                            og.addEnchantment(entry.getKey(), upgrade);
                        }
                    }
                }
                // add in missing enchants from second item
                for (Map.Entry<Enchantment, Integer> entry : contents[1].getEnchantments().entrySet()) {
                    if (!og.getEnchantments().containsKey(entry.getKey())) {
                        og.addEnchantment(entry.getKey(), entry.getValue());
                    }
                }

                // give resulting durability
                ItemMeta og_meta = og.getItemMeta();
                ItemMeta result_meta = e.getResult().getItemMeta();
                if (result_meta instanceof Damageable damageable && og_meta instanceof Damageable og_damageable) {
                    og_damageable.setDamage(damageable.getDamage());
                    og.setItemMeta(og_meta);
                }
                e.setResult(og);
            }
        }
    }

    @EventHandler
    public static void onRepairOrRename(PrepareAnvilEvent e) {
        Material[] repair_ores = new Material[]{
                Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT
        };

        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        if (e.getResult() == null || e.getResult().getType() == Material.AIR) {
            return;
        }
        if (contents[0] != null && contents[1] == null) { // renaming
            ItemStack og = contents[0].clone();
            ItemMeta renamed = e.getResult().getItemMeta();
            ItemMeta meta = og.getItemMeta();
            if (meta != null && renamed != null) {
                meta.setDisplayName(renamed.getDisplayName());
                og.setItemMeta(meta);
                e.setResult(og);
            }

        }
        else if (contents[0] != null && Arrays.asList(repair_ores).contains(contents[1].getType())) { // repairing
            ItemStack og = contents[0].clone();
            ItemMeta meta = og.getItemMeta();
            ItemMeta repaired = e.getResult().getItemMeta();
            if (repaired instanceof Damageable damageable && meta instanceof Damageable og_damageable) {
                og_damageable.setDamage(damageable.getDamage());
                og.setItemMeta(damageable);
                e.setResult(og);
            }
        }
    }

    @EventHandler
    public static void onRepair(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack[] contents = inv.getContents();
        if (contents.length > 1) {

            if (contents[0] == null || contents[1] == null) return;

            ItemStack item = contents[0].clone();
            ItemStack book = contents[1];

            // our items adder category
            if (book.getType() != Material.ENCHANTED_BOOK) return;

            CustomItem customItem = CustomRegister.INSTANCE.itemRegistry.getCustomItem(book);
//            String item_id = OraxenItems.getIdByItem(book);
            String item_id = null;
            if (customItem == null) {
                // attempt use model data
                ItemMeta meta = book.getItemMeta();
                if (meta != null && meta.hasCustomModelData()) {
                    int model = meta.getCustomModelData();
                    item_id = CustomEnchantments.conversionMap.get(model);
                }
            }
            else {
                item_id = customItem.getIdentifier();
            }
//            CustomStack customStack = CustomStack.byItemStack(book);
            if (item_id == null) {
                // hopefully disallows vanilla books?
                e.setResult(new ItemStack(Material.AIR));
                return;
            }

            Enchantment enchantment = CustomEnchantments.getGardensEnchant(item_id);
            if (enchantment == null) return;

            if (enchantment.canEnchantItem(item) && !hasConflicts(enchantment, item)) {

                NBTItem nbtItem = new NBTItem(item);
                if (!nbtItem.hasKey("slots")) {
                    nbtItem.setInteger("slots", 4);
                }

                int slots = nbtItem.getInteger("slots");
                int currEnchants = nbtItem.getItem().getEnchantments().size();

                if (currEnchants >= slots) {
                    return;
                }

                int level = new NBTItem(book).getInteger("level");

                // check if item already contains enchantment at same level, then we should attempt to upgrade it
                if (EnchantUtils.getEnchantLevel(item, enchantment) == level) {
                    int upgrade = level + 1;
                    if (enchantment.getMaxLevel() >= upgrade) {
                        level = upgrade;
                    }
                }

                if (level != 0) {
                    ItemStack final_item = nbtItem.getItem();
                    EnchantUtils.enchantItem(final_item, enchantment, level);
                    inv.setRepairCost(3);
                    e.setResult(final_item);
                }
            }
        }
    }

    public static boolean hasConflicts(Enchantment enchantment, ItemStack item) {
        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (ench.conflictsWith(enchantment)) {
                return true;
            }
        }
        return false;
    }

//    @EventHandler
//    public void onSilkTouchSpawner(SilkSpawnersSpawnerBreakEvent e) {
//        e.getPlayer().sendMessage("WAP");
//        if (!EnchantUtils.hasEnchantment(e.getPlayer().getInventory().getItemInMainHand(), CustomEnchantments.SILK_TOUCH_SPAWNER)) {
////            e.setCancelled(true);
//            e.getPlayer().sendMessage("Cancelling drop");
//            e.setDrop(new ItemStack(Material.AIR));
//        }
//    }

    @EventHandler(ignoreCancelled = true)
    public void onTimber(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("Timber_TBD")) {
            return;
        }

        Player player = e.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        String block_name = e.getBlock().getType().toString().toUpperCase();
        if (block_name.endsWith("LOG") || block_name.endsWith("ROOTS") || block_name.endsWith("STEM") || block_name.endsWith("LEAVES") || block_name.endsWith("VINE")) {
            if (!EnchantUtils.hasEnchantment(player.getInventory().getItemInMainHand(), CustomEnchantments.TIMBER)) {
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
                if (i >= 30) break;
                Block curr = blocks.get(i);
                for (BlockFace blockFace : faces) {
                    for (int j = -1; j < 2; j++) {
                        for (int l = -1; l < 2; l++) {
                            Block potential = curr.getRelative(blockFace);
                            potential = potential.getRelative(j, 0, l);
                            Block finalPotential = potential;
                            if (potential.getType() == e.getBlock().getType() && blocks.stream().noneMatch(block -> block.getLocation().equals(finalPotential.getLocation()))) {
                                blocks.add(potential);
                                destroy.add(potential);
                            }
                        }
                    }

                }
            }

            // do block breaking
            for (Block block : destroy) {
                block.setMetadata("Timber_TBD", new FixedMetadataValue(GardensPlayers.plugin, true));
                breakIfAble(player, block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVeinMiner(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("Vein_Miner_TBD")) {
            return;
        }

        Player player = e.getPlayer();
        String block_name = e.getBlock().getType().toString().toUpperCase();
        if (block_name.endsWith("ORE")) {
            if (!EnchantUtils.hasEnchantment(player.getInventory().getItemInMainHand(), CustomEnchantments.VEIN_MINER)) {
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
                if (i >= 12) break;
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
                block.setMetadata("Vein_Miner_TBD", new FixedMetadataValue(GardensPlayers.plugin, true));
                breakIfAble(player, block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmeltMineBlock(BlockBreakEvent e) {
        Player player = e.getPlayer();
        String block_name = e.getBlock().getType().toString().toUpperCase();
        if (block_name.endsWith("ORE")) {
            if (!EnchantUtils.hasEnchantment(player.getInventory().getItemInMainHand(), CustomEnchantments.SMELTER)) {
                return;
            }
            int fortune = EnchantUtils.getEnchantLevel(player.getInventory().getItemInMainHand(), CustomEnchantments.FORTUNE);
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

    @EventHandler(ignoreCancelled = true)
    public void onMineBlock(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("Super_Breaker_TBD") || e.getBlock().hasMetadata("Vein_Miner_TBD")) {
            return;
        }

        Player player = e.getPlayer();
        String block_name = e.getBlock().getType().toString().toUpperCase();
        if (block_name.endsWith("ORE") || block_name.endsWith("STONE") || block_name.endsWith("DEEPSLATE")) {
            if (!EnchantUtils.hasEnchantment(player.getInventory().getItemInMainHand(), CustomEnchantments.SUPER_BREAKER)) {
                return;
            }
            BlockFace facing = PlayerUtils.getBlockFaceLooking(player);
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

                    if (target != null) {
                        // give metadata so we can avoid calling this event on this block again
                        target.setMetadata("Super_Breaker_TBD", new FixedMetadataValue(GardensPlayers.plugin, true));
                        breakIfAble(player, target);
                    }
                }
            }
        }
    }

    private void breakIfAble(Player player, Block target) {

        LocalPlayer  lp = WorldGuardPlugin.inst().wrapPlayer(player);
        Location loc = new Location(lp.getWorld(), target.getX(), target.getY(), target.getZ());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (query.testBuild(loc, lp)) {
            EntityPlayer human = ((CraftPlayer) player).getHandle();
            BlockPosition blockPosition = new BlockPosition(target.getX(), target.getY(), target.getZ());

            // break block, this calls block break event so need 2 ensure it doesnt keep calling our firehammer hting
            // which we do by giving it metadata to mark it as such
            new PlayerInteractManager(human).a(blockPosition);

//            target.breakNaturally(player.getInventory().getItemInMainHand());
        }
        if (target.hasMetadata("Super_Breaker_TBD")) {
            target.removeMetadata("Super_Breaker_TBD", GardensPlayers.plugin);
        }
        if (target.hasMetadata("Vein_Miner_TBD")) {
            target.removeMetadata("Vein_Miner_TBD", GardensPlayers.plugin);
        }
        if (target.hasMetadata("Timber_TBD")) {
            target.removeMetadata("Timber_TBD", GardensPlayers.plugin);
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
        if (EnchantUtils.hasEnchantment(e.getItem(), CustomEnchantments.ETERNAL)) {
//            int ench_level = EnchantUtils.getEnchantLevel(e.getItem(), CustomEnchantments.ETERNAL);
//            if (ProbabilityUtil.roll((ench_level*10)*2)) {
                e.setCancelled(true);
//            }
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (EnchantUtils.hasEnchantment(weapon, CustomEnchantments.SKULL_STEALER)) {
                int ench_level = EnchantUtils.getEnchantLevel(weapon, CustomEnchantments.SKULL_STEALER);
                int percent = ench_level == 1 ? 15 : 15 * ench_level;
                if (ProbabilityUtil.roll(percent)) {
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

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getType() == InventoryType.ENCHANTING && e.getPlayer() instanceof Player player) {
            e.setCancelled(true);
            if (player.hasPermission("gardens.enchant")) {
                EnchantingGUI enchantingGUI = new EnchantingGUI(player);
                player.openInventory(enchantingGUI.getInventory());
            }
            else {
                e.getPlayer().sendMessage(ChatColor.RED + "Enchanting is currently disabled!");
            }
//
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getClickedInventory() instanceof AnvilInventory) {
            if (e.getSlot() == 2) {
                ItemStack item = e.getInventory().getItem(e.getSlot());
                ItemStack currentItem = e.getCurrentItem();
                if (item != null && currentItem != null) {
                    ItemStack book = e.getInventory().getItem(1);
                    NBTItem nbtItem = new NBTItem(book);
                    if (nbtItem.hasKey("destruction")) {
                        int destruction = nbtItem.getInteger("destruction");

                        if (ProbabilityUtil.roll(destruction)) {
                            String enchant = nbtItem.getString("destroy_enchant");
                            System.out.println("destroy enchant: " + enchant);

                            Enchantment customEnchantment = CustomEnchantments.getCustomEnchant(enchant);
//                            if (customEnchantment instanceof CustomWrapper w) {
//                                customEnchantment = w.enchantment;
//                                currentItem.removeEnchantment(customEnchantment);
//                            }
//                            else {
                            ItemStack fresh_clone = EnchantUtils.removeEnchant(currentItem, customEnchantment);
//                            }
                            EnchantUtils.updateLore(fresh_clone);

                            // send to their inventory directly
                            e.getWhoClicked().getInventory().addItem(fresh_clone);
                            ItemStack air = new ItemStack(Material.AIR);
                            e.setCurrentItem(air);
                            for (int i = 0; i < 3; i++) {
                                e.getInventory().setItem(i, air);
                            }

                            e.getWhoClicked().sendMessage(ChatColor.RED + "Enchantment Destructed!");
//                            e.getWhoClicked().sendMessage(ChatColor.RED + "Attempted removal of " + customEnchantment.getKey() + " found using " + enchant);

                        }
                    }
                    else if (book.getType() == Material.PAPER) {
                        CustomItem customItem = CustomRegister.INSTANCE.itemRegistry.getCustomItem(book);
                        if (customItem != null && customItem.getIdentifier().equalsIgnoreCase("amber")) {
                            // 50 percent chance failure
                            if (ProbabilityUtil.roll(50)) {

//                            if (customEnchantment instanceof CustomWrapper w) {
//                                customEnchantment = w.enchantment;
//                                currentItem.removeEnchantment(customEnchantment);
//                            }
//                            else {
                                ItemStack fresh_clone = EnchantUtils.removeEnchant(currentItem, CustomEnchantments.WINGED_BOOTS);
//                            }
                                EnchantUtils.updateLore(fresh_clone);

                                // send to their inventory directly
                                e.getWhoClicked().getInventory().addItem(fresh_clone);
                                ItemStack air = new ItemStack(Material.AIR);
                                e.setCurrentItem(air);
                                for (int i = 0; i < 3; i++) {
                                    e.getInventory().setItem(i, air);
                                }

                                e.getWhoClicked().sendMessage(ChatColor.RED + "Enchantment Destructed!");
//                            e.getWhoClicked().sendMessage(ChatColor.RED + "Attempted removal of " + customEnchantment.getKey() + " found using " + enchant);

                            }
                            int excess_amber = book.getAmount() - 24;
                            if (excess_amber > 0) {
                                ItemStack amber = CustomRegister.INSTANCE.itemRegistry.buildItem("amber");
                                if (amber != null) {
                                    amber.setAmount(excess_amber);
                                    e.getWhoClicked().getInventory().addItem(amber);
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;

            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                Inventory clicked = e.getClickedInventory();
                if (clicked != null && clicked.getType() == InventoryType.PLAYER) {
                    System.out.println(e.getInventory().getType().name());
                    if (e.getInventory().getItem(1) != null) {
                        e.setCancelled(true);
                        return;
//                e.getInventory().setItem(0, null);
//                e.getInventory().setItem(1, stack);
                    }
//                else if (e.getSlot() != 1){
//                    e.setCancelled(true);
//                }
                } else if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof EnchantingGUI) {
                    if (e.getSlot() != 1) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            else if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof EnchantingGUI) {
                ItemStack selection = null;
                if (e.getSlot() != 1 && e.getWhoClicked() instanceof Player player) {
                    if (e.getSlot() > 2 && e.getSlot() < 9) {
                        selection = e.getInventory().getItem(8);
                    } else if (e.getSlot() > 11 && e.getSlot() < 18) {
                        selection = e.getInventory().getItem(17);

                    } else if (e.getSlot() > 20 && e.getSlot() < 27) {
                        selection = e.getInventory().getItem(26);

                    } else if (e.getSlot() == 19) {
                        new EnchantingMenu(player).open();
                    }

                    // todo account for xp and money
                    if (selection != null) {
                        int playerLevels = ExpUtil.getExp(player);
                        NBTItem nbtItem = new NBTItem(selection);
                        int amber = nbtItem.getInteger("amber");
                        int xp = nbtItem.getInteger("xp");

                        if (playerLevels >= xp && GardensEnchants.econ.getBalance(player) >= amber) {
                            ExpUtil.changeExp(player, -xp);
                            GardensEnchants.econ.withdrawPlayer(player, amber);

                            new EnchantCache(player.getUniqueId()).resetEnchant();
                            ItemStack enchantTarget = e.getInventory().getItem(1);
                            if (enchantTarget != null) {
                                int amount = enchantTarget.getAmount();
                                if (amount > 1) {
                                    enchantTarget.setAmount(amount - 1);
                                    e.getInventory().getViewers().get(0).getInventory().addItem(enchantTarget);
                                }
                                // remove costs from lore before enchanting target
                                ItemMeta meta = selection.getItemMeta();
                                if (meta != null) {
                                    List<String> lore = meta.getLore();
                                    if (lore != null) {
                                        // remove last 3 lines
                                        for (int i = 0; i < 3; i++) {
                                            lore.remove(lore.size() - 1);
                                        }
                                    }
                                    meta.setLore(lore);
                                    selection.setItemMeta(meta);
                                }
                                e.getInventory().setItem(1, selection);
                            }

                            updateEnchantingGUI(player, e.getInventory());
                        }
                    }

                    e.setCancelled(true);
                }
            }



        if (e.getWhoClicked() instanceof Player player) {
            if (e.getRawSlot() == 1) {
                if (e.getAction().name().contains("PLACE") || e.getAction().name().contains("PICKUP") || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    updateEnchantingGUI(player, e.getInventory());
                }

            }
            else if (e.getRawSlot() >= 27 && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                updateEnchantingGUI(player, e.getInventory());
            }
        }

//        System.out.println("Clicked: " + e.getRawSlot() + "slot, " + e.getCurrentItem() + "item, Action: " + e.getAction().name());


    }

    private void updateEnchantingGUI(Player player, Inventory inv) {

        Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
            boolean optifine = false;
            try {
                PlayerCache playerCache = new PlayerCache(player.getUniqueId());
                optifine = playerCache.isUsingOptifine();
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if (optifine) {
//                player.sendMessage("using opti offset");
//            }
//            String offset = (optifine) ? "&f:offset_80:&f" : "";
            String offset = (optifine) ? "&f" + OraxenShift.INSTANCE.getShift(80, false) : "";

            String title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE094");

            if (itemIsEnchantable(inv.getItem(1))) {
                ItemStack[] books = genBooks(inv, player);
                String book[] = new String[3];
                for (int i = 0; i < book.length; i++) {
                    book[i] = (books[i] != null && books[i].getItemMeta() != null) ? books[i].getItemMeta().getDisplayName() : " ";
                }

                HashMap<Character, Character>[] fontMaps = new HashMap[]{FontMappings.INSTANCE.fontMap1, FontMappings.INSTANCE.fontMap2, FontMappings.INSTANCE.fontMap3};
                String[] sample = new String[3];
                for (int i = 0; i < 3; i++) {
                     String raw = stripFormatting(book[i]);
                     String padding = FontMappings.getPadding(raw);
                     sample[i] = FontMappings.INSTANCE.replaceAll(raw, fontMaps[i]) + padding;
                }

//                title = ChatColor.translateAlternateColorCodes('&', "&fꐤ:&f\uE05F:offset_-195:&f" + offset + sample[0] + "&f:offset_-100:&f" + sample[1] + "&f:offset_-100:&f" + sample[2]);
//                String hundred = OraxenShift.INSTANCE.getShift(100, true);
                title = ChatColor.translateAlternateColorCodes('&', "&fꐤ\uE05F" + OraxenShift.INSTANCE.getShift(195, true) + offset + sample[0]  + sample[1]  + sample[2]);

            }
            else {
                removeBooks(inv);
            }

            InventoryUpdate.updateInventory(GardensEnchants.plugin, player, title);

        }, 1);
        // we placed something into an empty
//        if (bookSlot == null) {
//            String title = ChatColor.translateAlternateColorCodes('&', "&f:offset_-8:&f\uE094");
//
//            InventoryUpdate.updateInventory(GardensEnchants.plugin, player, title);
//
//        }
//         else if (bookSlot.getType() == Material.BOOK){
//

//        }
    }

    // assumes we have a formatted hex code at beginning of display name
    private String stripFormatting(String formatted) {
        if (formatted.length() < 14) {
            return formatted;
        }
        return formatted.substring(14);
    }

    // places books into inventory
    private ItemStack[] genBooks(Inventory inv, Player player) {
        ItemStack display = ItemUtils.getBlankItem();
        ItemStack[] books = EnchantUtils.getRandomBooks(player.getUniqueId());
        CustomEnchantment[] enchantments = new EnchantCache(player.getUniqueId()).getEnchants();
        int playerLevels = ExpUtil.getExp(player);
        double playerBal = GardensEnchants.econ.getBalance(player);
        // cost and amber
        // costs
        for (int i = 0; i < 3; i++) {
            EnchantRarity rarity = enchantments[i].getRarity();
            int amber = EnchantUtils.getAmberCost(rarity);
            int xp = EnchantUtils.getXpCost(rarity);


            ItemMeta bookMeta = books[i].getItemMeta();
            if (bookMeta != null) {
                List<String> lore = bookMeta.getLore();
                if (lore != null) {
                    lore.add("");
                    if (playerBal >= amber) {
                        lore.add(ChatColor.WHITE + "\uE01F " + amber + " Amber");
                    }
                    else {
                        lore.add(ChatColor.WHITE + "\uE01F " + ChatColor.RED + amber + " Amber");
                    }

                    if (playerLevels >= xp) {
                        lore.add(ChatColor.WHITE + "\uE10F " + EnchantUtils.translateXpToLevel(xp) + " Levels");
                    }
                    else {
                        lore.add(ChatColor.WHITE + "\uE10F " + ChatColor.RED + EnchantUtils.translateXpToLevel(xp) + " Levels");
                    }
                }

                bookMeta.setLore(lore);
            }
            books[i].setItemMeta(bookMeta);
            NBTItem nbtItem = new NBTItem(books[i]);
            nbtItem.setInteger("amber", amber);
            nbtItem.setInteger("xp", xp);
            books[i] = nbtItem.getItem();
        }

        inv.setItem(8, books[0]);
        inv.setItem(17, books[1]);
        inv.setItem(26, books[2]);

        int j = 0;
        for (ItemStack book : books) {
            ItemMeta displayMeta = display.getItemMeta();
            ItemMeta bookMeta = (book != null) ? book.getItemMeta() : null;
            if (displayMeta != null && bookMeta != null) {
                displayMeta.setDisplayName(bookMeta.getDisplayName());
                displayMeta.setLore(bookMeta.getLore());
                display.setItemMeta(displayMeta);
            }
            for (int i = 3 + (j * 9); i < 8 + (j * 9); i++) {
                inv.setItem(i, display);
            }
            j++;
        }
        return books;
    }

    private void removeBooks(Inventory inv) {
        for (int j = 0; j < 3; j++) {
            for (int i = 3; i < 9; i++) {
                inv.setItem(i + (j * 9), null);
            }
        }
    }

    private boolean itemIsEnchantable(ItemStack item) {
        return item != null && item.getType() == Material.BOOK;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;
//
        if (e.getRawSlots().stream().anyMatch(integer -> integer < 27 && integer != 1)) {
            e.setCancelled(true);
        }

        if (e.getWhoClicked() instanceof Player player) {
            if (e.getRawSlots().contains(1)) {
                updateEnchantingGUI(player, e.getInventory());
            }
        }

//        if (e.getWhoClicked() instanceof Player player) {
//            ItemStack bookSlot = e.getInventory().getItem(1);
//            if (bookSlot != null)
//                updateEnchantingGUI(bookSlot, player);
//        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof EnchantingGUI)) return;
        ItemStack item = e.getInventory().getItem(1);
        if (item != null) {
            e.getPlayer().getInventory().addItem(item);
        }
    }


//    @EventHandler
//    public void onMove(InventoryMoveItemEvent e) {
//        if (!(e.getDestination().getHolder() instanceof EnchantingGUI)) return;
//        System.out.println("moved " + e.getItem().getType().name());
//    }



}
