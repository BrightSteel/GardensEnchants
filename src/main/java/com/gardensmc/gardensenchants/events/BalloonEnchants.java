package com.gardensmc.gardensenchants.events;

import com.enderbeam.enderwardrobe.api.WardrobeApi;
import com.gardensmc.gardensenchants.GardensEnchants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.DRIFT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.FEATHERWEIGHT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.TETHERED;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.BUOYANT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.LIFT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.UPDRAFT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.WEATHERVANE;

/**
 * Enchantments on the balloon a player wears (EnderWardrobe's balloon slot, read through its API):
 * Featherweight lowers gravity a little more per level; Tethered catches a fall that would kill, then recovers;
 * Updraft gives one more jump in the air (jump is a key the client reports as it's pressed); Drift, while jump is
 * held, lets them down gently; Lift, sneak held in the air, raises them a moment; Buoyant floats them up in water;
 * Weathervane has the wind carry them along in rain.
 */
public final class BalloonEnchants implements Listener {

    /** Gravity taken off per level of Featherweight, on top of the balloon's own. */
    private static final double FEATHERWEIGHT_PER_LEVEL = 0.05;
    /** Minutes Tethered needs to recover after catching a fall, by level (I, II, III). */
    private static final int[] TETHERED_COOLDOWN_MINUTES = {10, 7, 4};

    /** Upward speed an Updraft jump gives (blocks a tick): about a normal jump's. */
    private static final double UPDRAFT_BOOST = 0.55;
    /** The fastest a drifting player falls (blocks a tick). */
    private static final double DRIFT_FALL = 0.12;

    /** Lift: sneak held this long in the air starts it; it lasts at most this long; then it needs this long. */
    private static final int LIFT_HOLD_TICKS = 10, LIFT_MAX_TICKS = 60, LIFT_RECHARGE_TICKS = 400;
    /** Lift's rise, and Buoyant's (blocks a tick). */
    private static final double LIFT_RISE = 0.18, BUOYANT_RISE = 0.1;
    /** Weathervane's push along the way they walk, per level (blocks a tick); doubled in a thunderstorm. */
    private static final double WIND_PER_LEVEL = 0.012;

    private final WardrobeApi wardrobe;
    /** Ticks each player has held sneak in the air; ticks they've been lifted; the tick their Lift is ready again. */
    private final java.util.Map<UUID, Integer> sneakHeld = new java.util.HashMap<>();
    private final java.util.Map<UUID, Integer> lifting = new java.util.HashMap<>();
    private final java.util.Map<UUID, Long> liftReady = new java.util.HashMap<>();
    private long ticks;
    /** Players holding jump. */
    private final Set<UUID> holdingJump = new HashSet<>();
    /** Players who've used their Updraft since they last stood on the ground. */
    private final Set<UUID> updraftUsed = new HashSet<>();
    private final NamespacedKey featherweightKey = new NamespacedKey(GardensEnchants.plugin, "featherweight");
    private final NamespacedKey tetheredReadyKey = new NamespacedKey(GardensEnchants.plugin, "tethered_ready_at");

    private BalloonEnchants(WardrobeApi wardrobe) {
        this.wardrobe = wardrobe;
    }

    /** Starts them when EnderWardrobe is there to wear balloons; nothing otherwise. */
    public static void enable() {
        if (Bukkit.getPluginManager().getPlugin("EnderWardrobe") == null) return;
        var api = Bukkit.getServicesManager().load(WardrobeApi.class);
        if (api == null) return;
        var listener = new BalloonEnchants(api);
        Bukkit.getPluginManager().registerEvents(listener, GardensEnchants.plugin);
        // Featherweight: checked twice a second, as balloons go on and off
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, listener::updateFeatherweight, 20, 10);
        // Drift and Updraft's reset on landing: every tick
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, listener::tickAir, 1, 1);
    }

    @Nullable
    private ItemStack balloon(Player player) {
        return wardrobe.worn(player, "balloon");
    }

    private void updateFeatherweight() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            var gravity = player.getAttribute(Attribute.GRAVITY);
            if (gravity == null) continue;
            var balloon = balloon(player);
            int level = balloon == null ? 0 : FEATHERWEIGHT.getEnchantmentLevel(balloon);
            double amount = -FEATHERWEIGHT_PER_LEVEL * level;
            var current = gravity.getModifier(featherweightKey);
            if (current != null && current.getAmount() == amount) continue;
            if (current != null) gravity.removeModifier(featherweightKey);
            if (level > 0) {
                gravity.addTransientModifier(new AttributeModifier(featherweightKey, amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            }
        }
    }

    /** In the air on their own: not flying, gliding, riding, swimming or climbing. */
    private static boolean airborne(Player player) {
        return !player.isOnGround() && !player.isFlying() && !player.isGliding() && !player.isInsideVehicle()
                && !player.isSwimming() && !player.isInWater() && !player.isClimbing();
    }

    @EventHandler
    public void onInput(PlayerInputEvent e) {
        var player = e.getPlayer();
        var id = player.getUniqueId();
        boolean jump = e.getInput().isJump();
        boolean pressed = jump && !holdingJump.contains(id);
        if (jump) holdingJump.add(id);
        else holdingJump.remove(id);
        // Updraft: jump pressed in the air, once until they land
        if (!pressed || !airborne(player) || updraftUsed.contains(id)) return;
        var balloon = balloon(player);
        if (balloon == null || !UPDRAFT.hasEnchantment(balloon)) return;
        updraftUsed.add(id);
        var velocity = player.getVelocity();
        player.setVelocity(velocity.setY(Math.max(velocity.getY(), UPDRAFT_BOOST)));
        player.setFallDistance(0);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 6, 0.2, 0.05, 0.2, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 0.6f, 1.3f);
    }

    private void tickAir() {
        ticks++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            var id = player.getUniqueId();
            if (player.isOnGround()) updraftUsed.remove(id);
            buoyant(player);
            weathervane(player);
            lift(player, id);
            // Drift: holding jump while falling, the fall is capped and does no damage
            if (!holdingJump.contains(id) || !airborne(player)) continue;
            var velocity = player.getVelocity();
            if (velocity.getY() >= -DRIFT_FALL) continue;
            var balloon = balloon(player);
            if (balloon == null || !DRIFT.hasEnchantment(balloon)) continue;
            player.setVelocity(velocity.setY(-DRIFT_FALL));
            player.setFallDistance(0);
        }
    }

    /** Buoyant: in water, not sneaking (sneak to dive), they float up. */
    private void buoyant(Player player) {
        if (!player.isInWater() || player.isSneaking() || player.isInsideVehicle()) return;
        var velocity = player.getVelocity();
        if (velocity.getY() >= BUOYANT_RISE) return;
        var balloon = balloon(player);
        if (balloon == null || !BUOYANT.hasEnchantment(balloon)) return;
        player.setVelocity(velocity.setY(BUOYANT_RISE));
    }

    /** Weathervane: rain (or snow) on them under open sky, the wind pushes them on the way they're walking. */
    private void weathervane(Player player) {
        var world = player.getWorld();
        if (!world.hasStorm() || player.isFlying() || player.isGliding() || player.isInsideVehicle()) return;
        var at = player.getLocation();
        if (world.getHighestBlockYAt(at) > at.getY()) return;   // under cover
        var velocity = player.getVelocity();
        var horizontal = velocity.clone().setY(0);
        if (horizontal.lengthSquared() < 0.0004) return;          // standing still
        var balloon = balloon(player);
        int level = balloon == null ? 0 : WEATHERVANE.getEnchantmentLevel(balloon);
        if (level <= 0) return;
        double push = WIND_PER_LEVEL * level * (world.isThundering() ? 2 : 1);
        player.setVelocity(velocity.add(horizontal.normalize().multiply(push)));
    }

    /** Lift: sneak held in the air for a moment raises them, for up to a few seconds; then it recharges. */
    private void lift(Player player, UUID id) {
        if (!player.isSneaking() || !airborne(player) || liftReady.getOrDefault(id, 0L) > ticks) {
            if (lifting.remove(id) != null) liftReady.put(id, ticks + LIFT_RECHARGE_TICKS);
            sneakHeld.remove(id);
            return;
        }
        int held = sneakHeld.merge(id, 1, Integer::sum);
        if (held < LIFT_HOLD_TICKS) return;
        var balloon = balloon(player);
        if (balloon == null || !LIFT.hasEnchantment(balloon)) return;
        int lifted = lifting.merge(id, 1, Integer::sum);
        if (lifted > LIFT_MAX_TICKS) {
            lifting.remove(id);
            liftReady.put(id, ticks + LIFT_RECHARGE_TICKS);
            return;
        }
        var velocity = player.getVelocity();
        player.setVelocity(velocity.setY(Math.max(velocity.getY(), LIFT_RISE)));
        player.setFallDistance(0);
        if (lifted == 1) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_IDLE_AIR, 0.5f, 1.5f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        var id = e.getPlayer().getUniqueId();
        holdingJump.remove(id);
        updraftUsed.remove(id);
        sneakHeld.remove(id);
        lifting.remove(id);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL || !(e.getEntity() instanceof Player player)) return;
        if (player.getHealth() - e.getFinalDamage() > 0) return;
        var balloon = balloon(player);
        int level = balloon == null ? 0 : TETHERED.getEnchantmentLevel(balloon);
        if (level <= 0) return;
        var data = player.getPersistentDataContainer();
        long now = System.currentTimeMillis();
        Long readyAt = data.get(tetheredReadyKey, PersistentDataType.LONG);
        if (readyAt != null && readyAt > now) return;
        // caught: left on half a heart, the balloon tugging them up
        e.setDamage(Math.max(0, player.getHealth() - 1));
        int minutes = TETHERED_COOLDOWN_MINUTES[Math.min(level, TETHERED_COOLDOWN_MINUTES.length) - 1];
        data.set(tetheredReadyKey, PersistentDataType.LONG, now + minutes * 60_000L);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_LEAD_TIED, 1f, 1.4f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.2, 0), 12, 0.3, 0.1, 0.3, 0.02);
        player.sendMessage(Component.text("Your balloon caught you! Tethered needs " + minutes + " minutes to recover.", NamedTextColor.YELLOW));
    }
}
