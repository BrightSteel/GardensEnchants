package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.*;

/**
 * Enchantments for the tools that had none: Shepherd's Bounty, Topiary and Gentle on shears; Grapple and Yank on
 * fishing rods; Reflect, Bash and Bulwark on shields; Homing and Tidecaller on tridents; Shockwave on maces; Impale on
 * spears.
 */
public final class ToolEnchants implements Listener {

    // ---- Shears ----

    /** How long a sheared sheep takes to grow its wool back on its own, by Shepherd's Bounty level (I-III). */
    private static final long[] REGROW_TICKS = {20 * 180, 20 * 120, 20 * 60};

    /** Shepherd's Bounty: a wool more a level, and the sheep grows it back without needing grass. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent e) {
        int level = SHEPHERDS_BOUNTY.getEnchantmentLevel(e.getItem());
        if (level <= 0 || !(e.getEntity() instanceof Sheep sheep)) return;
        List<ItemStack> drops = new ArrayList<>(e.getDrops());
        var wool = drops.stream().filter(it -> Tag.WOOL.isTagged(it.getType())).findFirst();
        wool.ifPresent(it -> drops.add(new ItemStack(it.getType(), level)));
        e.setDrops(drops);
        long delay = REGROW_TICKS[Math.min(level, REGROW_TICKS.length) - 1];
        sheep.getScheduler().runDelayed(GardensEnchants.plugin, task -> {
            if (sheep.isValid() && sheep.isSheared()) sheep.setSheared(false);
        }, null, delay);
    }

    /** Topiary: cutting a plant with shears cuts the plants in a 3x3x3 around it too. */
    @EventHandler(ignoreCancelled = true)
    public void onCut(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (AreaBreaker.isBreaking(player)) return;
        var tool = player.getInventory().getItemInMainHand();
        if (tool.getType() != Material.SHEARS || !TOPIARY.hasEnchantment(tool) || !isPlant(e.getBlock())) return;
        List<Block> targets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block target = e.getBlock().getRelative(x, y, z);
                    if (isPlant(target)) targets.add(target);
                }
            }
        }
        AreaBreaker.breakBlocks(player, e.getBlock(), targets);
    }

    private static boolean isPlant(Block block) {
        var type = block.getType();
        return Tag.LEAVES.isTagged(type) || Tag.REPLACEABLE_BY_TREES.isTagged(type) || Tag.CAVE_VINES.isTagged(type)
                || type == Material.COBWEB || type == Material.VINE || type == Material.GLOW_LICHEN
                || type == Material.TWISTING_VINES || type == Material.TWISTING_VINES_PLANT
                || type == Material.WEEPING_VINES || type == Material.WEEPING_VINES_PLANT || type == Material.HANGING_ROOTS;
    }

    /**
     * Gentle: shearing a full hive or nest. Done here instead of by vanilla, which angers the bees nearby unless
     * there's a campfire under it.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onShearHive(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) return;
        Block block = e.getClickedBlock();
        ItemStack item = e.getItem();
        if (block == null || item == null || item.getType() != Material.SHEARS || !GENTLE.hasEnchantment(item)) return;
        if (!(block.getBlockData() instanceof Beehive hive) || hive.getHoneyLevel() < hive.getMaximumHoneyLevel()) return;
        e.setCancelled(true);
        hive.setHoneyLevel(0);
        block.setBlockData(hive);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 1.1, 0.5), new ItemStack(Material.HONEYCOMB, 3));
        block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_BEEHIVE_SHEAR, 1, 1);
        e.getPlayer().damageItemStack(e.getHand() == null ? EquipmentSlot.HAND : e.getHand(), 1);
    }

    // ---- Fishing rods ----

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();
        ItemStack rod = rod(player, e.getHand());
        if (rod == null) return;
        if (e.getState() == PlayerFishEvent.State.IN_GROUND && GRAPPLE.hasEnchantment(rod)) {
            grapple(player, e.getHook().getLocation().toVector());
        } else if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && YANK.hasEnchantment(rod)
                && e.getCaught() instanceof LivingEntity caught && !(caught instanceof Player)) {
            // after vanilla's own (small) tug
            Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> yank(player, caught));
        }
    }

    private static ItemStack rod(Player player, EquipmentSlot hand) {
        var item = player.getInventory().getItem(hand == null ? EquipmentSlot.HAND : hand);
        return item.getType() == Material.FISHING_ROD ? item : null;
    }

    /** Grapple: pulls its caster toward the hook, a little up so it clears the edge. */
    private static void grapple(Player player, Vector hook) {
        Vector toHook = hook.subtract(player.getLocation().toVector());
        double distance = toHook.length();
        if (distance < 1) return;
        Vector pull = toHook.normalize().multiply(Math.min(1.8, 0.4 + distance * 0.12));
        player.setVelocity(pull.setY(pull.getY() + 0.35));
        com.gardensmc.gardensenchants.api.AirTime.allow(player);
        player.setFallDistance(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1, 0.7f);
    }

    /** Yank: hauls the mob most of the way to its caster. */
    private static void yank(Player player, LivingEntity caught) {
        if (!caught.isValid()) return;
        Vector toPlayer = player.getLocation().toVector().subtract(caught.getLocation().toVector());
        Vector pull = toPlayer.multiply(0.18);
        if (pull.length() > 2) pull.normalize().multiply(2);
        caught.setVelocity(pull.setY(Math.max(pull.getY(), 0) + 0.35));
    }

    // ---- Shields ----

    /** The shield a player is blocking with, or null. */
    private static ItemStack blockingShield(Player player) {
        if (!player.isBlocking()) return null;
        var item = player.getActiveItem();
        return item.getType() == Material.SHIELD ? item : null;
    }

    /** Whether something moving this way is coming at the player's front, where a shield blocks. */
    private static boolean fromFront(Player player, Vector movement) {
        Vector facing = player.getLocation().getDirection().setY(0);
        Vector coming = movement.clone().setY(0);
        if (facing.lengthSquared() < 1e-6 || coming.lengthSquared() < 1e-6) return true;
        return facing.normalize().dot(coming.normalize()) < 0;
    }

    /** Reflect: a blocked arrow (or trident) flies back at whoever shot it, as its blocker's now. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowHit(ProjectileHitEvent e) {
        if (!(e.getHitEntity() instanceof Player player) || !(e.getEntity() instanceof AbstractArrow arrow)) return;
        ItemStack shield = blockingShield(player);
        if (shield == null || !REFLECT.hasEnchantment(shield) || !fromFront(player, arrow.getVelocity())) return;
        double speed = Math.max(1.5, arrow.getVelocity().length());
        var shooter = arrow.getShooter() instanceof LivingEntity living && living != player ? living : null;
        // next tick, once the shield has deflected it (an arrow that got past is gone or stuck by then)
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> {
            if (!arrow.isValid() || arrow.isInBlock()) return;
            Vector back = shooter != null && shooter.isValid() && shooter.getWorld() == arrow.getWorld()
                    ? shooter.getEyeLocation().toVector().subtract(arrow.getLocation().toVector())
                    : player.getLocation().getDirection();
            arrow.setVelocity(back.normalize().multiply(speed));
            arrow.setShooter(player);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1.4f);
        });
    }

    /** How long Bash takes to come back, in milliseconds. */
    private static final long BASH_COOLDOWN = 4000;
    private final Map<UUID, Long> lastBash = new HashMap<>();

    /** Bash: raising a shield while sneaking shoves back everything in front, harder at level II. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRaise(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (!player.isSneaking() || item == null || item.getType() != Material.SHIELD) return;
        int level = BASH.getEnchantmentLevel(item);
        if (level <= 0) return;
        long now = System.currentTimeMillis();
        if (now - lastBash.getOrDefault(player.getUniqueId(), 0L) < BASH_COOLDOWN) return;
        lastBash.put(player.getUniqueId(), now);
        Vector facing = player.getLocation().getDirection().setY(0).normalize();
        boolean hit = false;
        for (Entity entity : player.getNearbyEntities(3.5, 2, 3.5)) {
            if (!(entity instanceof LivingEntity living) || entity instanceof ArmorStand) continue;
            Vector to = living.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
            if (to.lengthSquared() < 1e-4 || to.normalize().dot(facing) < 0.5) continue;
            living.setVelocity(to.multiply(0.6 + 0.4 * level).setY(0.35));
            hit = true;
        }
        if (hit) player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 0.7f);
    }

    /** Bulwark: a hit blocked from the front doesn't push its blocker back. */
    @EventHandler(ignoreCancelled = true)
    public void onPushed(EntityKnockbackEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        ItemStack shield = blockingShield(player);
        // knockback pushes away from the hit, so a hit from the front pushes backward
        if (shield != null && BULWARK.hasEnchantment(shield) && fromFront(player, e.getKnockback())) e.setCancelled(true);
    }

    // ---- Tridents ----

    /** Tridents thrown with Homing, until they hit something, and the mob each has locked onto (once it has one). */
    private final java.util.Map<UUID, UUID> homing = new java.util.HashMap<>();
    /** Nothing locked onto yet. */
    private static final UUID NO_TARGET = new UUID(0, 0);
    /** How far Homing looks, and a trident's gravity a tick, made up for each turn so it flies where it's aimed. */
    private static final double HOMING_RANGE = 10, TRIDENT_GRAVITY = 0.05;
    /**
     * How far off its flight a mob may be for Homing to choose it, by level (I, II, III): 20, 30 and 40 degrees either
     * side, as the cosine (a mob is in the cone when its direction's dot with the heading is at least this).
     */
    private static final double[] HOMING_CONE = {Math.cos(Math.toRadians(20)), Math.cos(Math.toRadians(30)), Math.cos(Math.toRadians(40))};
    /** Each homing trident's cone, by its Homing level. */
    private final java.util.Map<UUID, Double> homingCone = new java.util.HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onThrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident) || !(trident.getShooter() instanceof Player)) return;
        if (!HOMING.hasEnchantment(trident.getItemStack())) return;
        homing.put(trident.getUniqueId(), NO_TARGET);
        int level = Math.max(1, Math.min(HOMING.getEnchantmentLevel(trident.getItemStack()), HOMING_CONE.length));
        homingCone.put(trident.getUniqueId(), HOMING_CONE[level - 1]);
        trident.getScheduler().runAtFixedRate(GardensEnchants.plugin, task -> {
            if (!trident.isValid() || trident.isInBlock() || !homing.containsKey(trident.getUniqueId()) || trident.getTicksLived() > 60) {
                homing.remove(trident.getUniqueId());
                homingCone.remove(trident.getUniqueId());
                task.cancel();
                return;
            }
            steer(trident);
        }, () -> {
            homing.remove(trident.getUniqueId());
            homingCone.remove(trident.getUniqueId());
        }, 2, 1);
    }

    @EventHandler
    public void onTridentHit(ProjectileHitEvent e) {
        homing.remove(e.getEntity().getUniqueId());
        homingCone.remove(e.getEntity().getUniqueId());
    }

    /**
     * Turns a homing trident toward a hostile mob: the nearest ahead of it, kept once chosen (close up it may no longer
     * be ahead, and turning to another would miss both). It leads the mob's movement, turns harder the closer it gets
     * and aims straight at it for the last couple of ticks: a gentle turn alone flies past at a trident's speed.
     */
    private void steer(Trident trident) {
        Vector velocity = trident.getVelocity();
        double speed = velocity.length();
        if (speed < 0.3) return;
        Vector heading = velocity.clone().normalize();
        var target = locked(trident, heading);
        if (target == null) return;
        Vector aimAt = target.getLocation().add(0, target.getHeight() / 2, 0).toVector().add(target.getVelocity());
        Vector to = aimAt.subtract(trident.getLocation().toVector());
        double distance = to.length();
        if (distance < 0.1) return;
        double turn = distance < speed * 2 ? 1 : Math.max(0.25, Math.min(1, 1.25 - distance / HOMING_RANGE));
        Vector turned = heading.multiply(1 - turn).add(to.normalize().multiply(turn)).normalize().multiply(speed);
        trident.setVelocity(turned.setY(turned.getY() + TRIDENT_GRAVITY));
    }

    /**
     * The mob a trident is locked onto, choosing the nearest hostile one in its cone ahead (narrower at lower levels) if
     * it has none (or it's gone).
     */
    @org.jetbrains.annotations.Nullable
    private Mob locked(Trident trident, Vector heading) {
        var id = homing.get(trident.getUniqueId());
        if (id != null && !id.equals(NO_TARGET) && Bukkit.getEntity(id) instanceof Mob mob && !mob.isDead()
                && mob.getWorld() == trident.getWorld() && mob.getLocation().distance(trident.getLocation()) <= HOMING_RANGE * 1.5) {
            return mob;
        }
        Mob best = null;
        double bestDistance = Double.MAX_VALUE;
        double cone = homingCone.getOrDefault(trident.getUniqueId(), HOMING_CONE[0]);
        for (Entity entity : trident.getNearbyEntities(HOMING_RANGE, HOMING_RANGE, HOMING_RANGE)) {
            if (!(entity instanceof Mob mob) || !(entity instanceof Enemy) || mob.isDead()) continue;
            Vector to = mob.getLocation().add(0, mob.getHeight() / 2, 0).toVector().subtract(trident.getLocation().toVector());
            double distance = to.length();
            if (distance < 0.5 || to.clone().normalize().dot(heading) < cone || distance >= bestDistance) continue;
            best = mob;
            bestDistance = distance;
        }
        homing.put(trident.getUniqueId(), best == null ? NO_TARGET : best.getUniqueId());
        return best;
    }

    /**
     * How long Tidecaller takes to recharge (ms), and when each player's is ready. Kept here, not as the trident's item
     * cooldown: that would stop the trident being used at all, Riptide in water included.
     */
    private static final long TIDECALLER_COOLDOWN = 5000;
    private final java.util.Map<java.util.UUID, Long> tidecallerReady = new java.util.HashMap<>();

    @EventHandler
    public void onQuitTidecaller(org.bukkit.event.player.PlayerQuitEvent e) {
        tidecallerReady.remove(e.getPlayer().getUniqueId());
    }

    /** Tidecaller: right-clicking a Riptide trident out of water launches its holder as Riptide would in it. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRiptideOnLand(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        ItemStack trident = e.getItem();
        if (trident == null || trident.getType() != Material.TRIDENT || !TIDECALLER.hasEnchantment(trident)) return;
        int riptide = trident.getEnchantmentLevel(Enchantment.RIPTIDE);
        if (riptide <= 0 || player.isInWaterOrRain()) return;
        long now = System.currentTimeMillis();
        if (tidecallerReady.getOrDefault(player.getUniqueId(), 0L) > now) {
            player.sendActionBar(net.kyori.adventure.text.Component.text("Tidecaller is recharging",
                    net.kyori.adventure.text.format.NamedTextColor.GRAY));
            return;
        }
        // vanilla's push: 3 * (1 + level) / 4 along where you look
        float strength = 3f * (1 + riptide) / 4f;
        player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().multiply(strength)));
        player.startRiptideAttack(20, 8f, trident);
        com.gardensmc.gardensenchants.api.AirTime.allow(player);
        tidecallerReady.put(player.getUniqueId(), now + TIDECALLER_COOLDOWN);
        player.damageItemStack(e.getHand() == null ? EquipmentSlot.HAND : e.getHand(), 1);
        Sound sound = riptide >= 3 ? Sound.ITEM_TRIDENT_RIPTIDE_3 : riptide == 2 ? Sound.ITEM_TRIDENT_RIPTIDE_2 : Sound.ITEM_TRIDENT_RIPTIDE_1;
        player.getWorld().playSound(player.getLocation(), sound, 1, 1);
    }

    // ---- Maces and spears ----

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !(e.getEntity() instanceof LivingEntity target)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.MACE) {
            int level = SHOCKWAVE.getEnchantmentLevel(weapon);
            // a smash attack: the mace's bonus needs a fall of more than 1.5 blocks
            if (level > 0 && player.getFallDistance() > 1.5) shockwave(player, target, level);
        } else if (com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts.SPEAR.isEnchantable(weapon)) {
            int level = IMPALE.getEnchantmentLevel(weapon);
            // a charged hit: the spear held raised, as it is while charging
            if (level > 0 && player.isHandRaised()) impale(target, level);
        }
    }

    /** Shockwave: the mobs around a smash (and the one hit) are thrown up, higher at level II. */
    private static void shockwave(Player player, LivingEntity target, int level) {
        double radius = 3 + level;
        List<LivingEntity> thrown = new ArrayList<>();
        thrown.add(target);
        for (Entity entity : target.getNearbyEntities(radius, 2, radius)) {
            if (entity instanceof LivingEntity living && entity != player && !(entity instanceof ArmorStand)
                    && !(entity instanceof Tameable tame && player.equals(tame.getOwner()))) {
                thrown.add(living);
            }
        }
        // after the hit's own knockback
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> thrown.forEach(living -> {
            if (living.isValid()) living.setVelocity(living.getVelocity().setY(0.6 + 0.25 * level));
        }));
    }

    /** How long a target can't be impaled again after one ends, in milliseconds, so nothing's pinned for good. */
    private static final long IMPALE_IMMUNITY = 3000;
    private final Map<UUID, Long> impaledUntil = new HashMap<>();

    /** Impale: what a charged spear hits can barely move for a second (a second and a half at level II). */
    private void impale(LivingEntity target, int level) {
        long now = System.currentTimeMillis();
        if (now < impaledUntil.getOrDefault(target.getUniqueId(), 0L)) return;
        int ticks = 20 + 10 * (level - 1);
        impaledUntil.put(target.getUniqueId(), now + ticks * 50L + IMPALE_IMMUNITY);
        impaledUntil.values().removeIf(until -> until < now);
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> {
            if (!target.isValid()) return;
            target.setVelocity(new Vector(0, Math.min(0, target.getVelocity().getY()), 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 9, false, true, true));
            target.getWorld().playSound(target.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 0.8f);
        });
    }
}
