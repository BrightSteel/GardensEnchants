package com.gardensmc.gardensenchants.events;

import com.enderbeam.enderwardrobe.api.WardrobeApi;
import com.gardensmc.gardensenchants.GardensEnchants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
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
 * Updraft gives one more jump in the air (jump is a key the client reports as it's pressed); Drift, jump pressed again
 * in the air and held, early in a fall, lets them down gently for a few seconds; Lift, jump held in the air, raises them a moment; Buoyant floats them up in water;
 * Weathervane has the wind carry them along in rain.
 */
public final class BalloonEnchants implements Listener {

    /** Gravity taken off per level of Featherweight, on top of the balloon's own. */
    private static final double FEATHERWEIGHT_PER_LEVEL = 0.05;
    /** Minutes Tethered needs to recover after catching a fall, by level (I, II, III). */
    private static final int[] TETHERED_COOLDOWN_MINUTES = {10, 7, 4};

    /** Upward speed an Updraft jump gives (blocks a tick): about a normal jump's. */
    private static final double UPDRAFT_BOOST = 0.55;
    /** Updraft's cooldown by level (I, II, III): 8 s, 4 s, and none (just one every jump). */
    private static final int[] UPDRAFT_COOLDOWN_TICKS = {160, 80, 0};
    /** The fastest a drifting player falls (blocks a tick): about 4 blocks a second. */
    private static final double DRIFT_FALL = 0.2;
    /** Drift starts only within this many blocks of a fall's start (and lasts as long as Lift at its level). */
    private static final double DRIFT_START_WITHIN = 3;

    /** Lift: jump held this long in the air starts it; then it needs this long. */
    private static final int LIFT_HOLD_TICKS = 10, LIFT_RECHARGE_TICKS = 400;
    /** How long Lift and Drift last at most, by level (I, II, III): 2, 3 and 4 seconds. */
    private static final int[] LASTS_TICKS = {40, 60, 80};
    /**
     * How hard the balloon is yanked up (blocks a tick) when Updraft or Lift starts or Tethered catches, and each tick
     * Lift goes on, keeping it ahead of its rising wearer.
     */
    private static final double UPDRAFT_TUG = 0.7, LIFT_TUG = 0.8, LIFT_TUG_HOLD = 0.12;
    /** Lift's rise, and Buoyant's (blocks a tick). */
    private static final double LIFT_RISE = 0.18, BUOYANT_RISE = 0.1;
    /** Weathervane's push along the way they walk, per level (blocks a tick); doubled in a thunderstorm. */
    private static final double WIND_PER_LEVEL = 0.012;

    private final WardrobeApi wardrobe;
    /** Ticks each player has held jump in the air; ticks they've been lifted; the tick their Lift is ready again. */
    private final java.util.Map<UUID, Integer> jumpHeld = new java.util.HashMap<>();
    private final java.util.Map<UUID, Integer> lifting = new java.util.HashMap<>();
    private final java.util.Map<UUID, Long> liftReady = new java.util.HashMap<>();
    private long ticks;
    /** Players holding jump. */
    private final Set<UUID> holdingJump = new HashSet<>();
    /** Ticks each drifting player has drifted; players who've had their Drift this fall. */
    private final java.util.Map<UUID, Integer> drifting = new java.util.HashMap<>();
    private final Set<UUID> driftUsed = new HashSet<>();
    /** Players who pressed jump again in the air and are still holding it: Drift starts once they're falling. */
    private final Set<UUID> driftArmed = new HashSet<>();
    /** How long each drifting player's Drift lasts, by its level. */
    private final java.util.Map<UUID, Integer> driftLasts = new java.util.HashMap<>();
    /** The tick each player's Updraft is ready again, after its cooldown (levels I and II). */
    private final java.util.Map<UUID, Long> updraftReady = new java.util.HashMap<>();
    /** Players who've used their Updraft since they last stood on the ground. */
    private final Set<UUID> updraftUsed = new HashSet<>();
    private final NamespacedKey featherweightKey = new NamespacedKey(GardensEnchants.plugin, "featherweight");
    /** The cooldown group balloons are in, so their recharge shows on them. */
    private static final Key COOLDOWN = Key.key("gardens", "balloon");
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
        // Drift: armed by jump pressed again in the air (a double jump), for as long as it's held
        if (pressed && airborne(player)) driftArmed.add(id);
        else if (!jump) driftArmed.remove(id);
        // Updraft: jump pressed in the air, once until they land
        if (!pressed || !airborne(player) || updraftUsed.contains(id) || updraftReady.getOrDefault(id, 0L) > ticks) return;
        var balloon = balloon(player);
        if (balloon == null || !UPDRAFT.hasEnchantment(balloon)) return;
        updraftUsed.add(id);
        int cooldown = UPDRAFT_COOLDOWN_TICKS[Math.max(1, Math.min(UPDRAFT.getEnchantmentLevel(balloon), UPDRAFT_COOLDOWN_TICKS.length)) - 1];
        if (cooldown > 0) {
            updraftReady.put(id, ticks + cooldown);
            showCooldown(player, cooldown);
        }
        var velocity = player.getVelocity();
        player.setVelocity(velocity.setY(Math.max(velocity.getY(), UPDRAFT_BOOST)));
        player.setFallDistance(0);
        // the balloon jerks up first, as if it pulled them; it's a jump, and wears the balloon like one (instead of the
        // jump off the ground before it)
        wardrobe.tugBalloon(player, new org.bukkit.util.Vector(0, UPDRAFT_TUG, 0));
        useBalloon(player);
        puff(player, 6);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 0.6f, 1.3f);
    }

    private void tickAir() {
        ticks++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            var id = player.getUniqueId();
            if (player.isOnGround()) {
                updraftUsed.remove(id);
                driftUsed.remove(id);
                drifting.remove(id);
                driftLasts.remove(id);
            }
            buoyant(player);
            weathervane(player);
            lift(player, id);
            drift(player, id);
        }
    }

    /**
     * Drift: jump pressed again in the air and held, from early in a fall (its first few blocks), caps the fall and does away with its damage, for at
     * most as long as Lift at the same level. Once a fall: letting go of jump, or running out of time, ends it until they land.
     */
    private void drift(Player player, UUID id) {
        boolean holding = holdingJump.contains(id) && airborne(player);
        var velocity = player.getVelocity();
        Integer drifted = drifting.get(id);
        if (drifted == null) {
            if (!holding || !driftArmed.contains(id) || driftUsed.contains(id) || velocity.getY() >= 0
                    || player.getFallDistance() > DRIFT_START_WITHIN) return;
            var balloon = balloon(player);
            if (balloon == null || !DRIFT.hasEnchantment(balloon)) return;
            driftUsed.add(id);
            driftLasts.put(id, lasts(DRIFT.getEnchantmentLevel(balloon)));
            useBalloon(player);
            drifted = 0;
        } else if (!holding || drifted >= driftLasts.getOrDefault(id, LASTS_TICKS[0])) {
            drifting.remove(id);
            return;
        }
        drifting.put(id, drifted + 1);
        if (velocity.getY() < -DRIFT_FALL) player.setVelocity(velocity.setY(-DRIFT_FALL));
        player.setFallDistance(0);
    }

    /**
     * Shows a balloon's recharge on the balloon itself, as an ender pearl shows its: the cooldown sweep over it in
     * /wardrobe and GardensCompanion's slot. The balloon is put in {@link #COOLDOWN}'s group the first time; the
     * longer of two cooldowns (Lift's and Tethered's) shows.
     */
    private void showCooldown(Player player, int ticks) {
        var balloon = balloon(player);
        if (balloon == null) return;
        var cooldown = balloon.getData(DataComponentTypes.USE_COOLDOWN);
        if (cooldown == null || !COOLDOWN.equals(cooldown.cooldownGroup())) {
            balloon.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(1).cooldownGroup(COOLDOWN).build());
            wardrobe.setWorn(player, "balloon", balloon);
        }
        player.setCooldown(COOLDOWN, Math.max(ticks, player.getCooldown(COOLDOWN)));
    }

    /** Tethered's recovery outlasts a log out: shown again on joining. */
    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        var player = e.getPlayer();
        Long readyAt = player.getPersistentDataContainer().get(tetheredReadyKey, PersistentDataType.LONG);
        long left = readyAt == null ? 0 : readyAt - System.currentTimeMillis();
        // after EnderWardrobe has loaded what they wear
        if (left > 0) Bukkit.getScheduler().runTaskLater(GardensEnchants.plugin, () -> {
            if (player.isOnline()) showCooldown(player, (int) ((readyAt - System.currentTimeMillis()) / 50));
        }, 60);
    }

    /**
     * One use of the balloon for what an enchantment made it do. The jump they're in then costs nothing more: only the
     * ability is charged, not the jump off the ground before it too.
     */
    private void useBalloon(Player player) {
        wardrobe.waiveJump(player);
        wardrobe.wearBalloon(player, 1);
    }

    /** How long Lift or Drift at a level lasts, in ticks. */
    private static int lasts(int level) {
        return LASTS_TICKS[Math.max(1, Math.min(level, LASTS_TICKS.length)) - 1];
    }

    /**
     * Cloud puffs from the balloon (its knot, where the lead ties on), drifting down a little as if blown out of it;
     * from the player, should it not be drawn.
     */
    private void puff(Player player, int count) {
        var at = wardrobe.balloonAt(player);
        if (at == null) at = player.getLocation().add(0, 0.2, 0);
        player.getWorld().spawnParticle(Particle.CLOUD, at, count, 0.15, 0.05, 0.15, 0.03);
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

    /**
     * Lift: jump held in the air for a moment raises them, for up to a few seconds; then it recharges. Jump, as the
     * client reports it pressed and let go: sneaking isn't reported reliably in the air, so Lift used to wait for
     * the ground.
     */
    private void lift(Player player, UUID id) {
        if (!holdingJump.contains(id) || !airborne(player) || liftReady.getOrDefault(id, 0L) > ticks) {
            if (lifting.remove(id) != null) {
                liftReady.put(id, ticks + LIFT_RECHARGE_TICKS);
                showCooldown(player, LIFT_RECHARGE_TICKS);
            }
            jumpHeld.remove(id);
            return;
        }
        int held = jumpHeld.merge(id, 1, Integer::sum);
        if (held < LIFT_HOLD_TICKS) return;
        var balloon = balloon(player);
        if (balloon == null || !LIFT.hasEnchantment(balloon)) return;
        int lifted = lifting.merge(id, 1, Integer::sum);
        if (lifted > lasts(LIFT.getEnchantmentLevel(balloon))) {
            lifting.remove(id);
            liftReady.put(id, ticks + LIFT_RECHARGE_TICKS);
            showCooldown(player, LIFT_RECHARGE_TICKS);
            return;
        }
        var velocity = player.getVelocity();
        player.setVelocity(velocity.setY(Math.max(velocity.getY(), LIFT_RISE)));
        player.setFallDistance(0);
        // the balloon shoots up and stays ahead, its lead taut, puffing as it goes: it's what lifts them
        wardrobe.tugBalloon(player, new org.bukkit.util.Vector(0, lifted == 1 ? LIFT_TUG : LIFT_TUG_HOLD, 0));
        // one use as it starts, and one for each second after
        if (lifted % 20 == 1) useBalloon(player);
        if (lifted == 1) {
            puff(player, 8);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_IDLE_AIR, 0.5f, 1.5f);
        } else if (lifted % 4 == 0) {
            puff(player, 2);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        var id = e.getPlayer().getUniqueId();
        holdingJump.remove(id);
        updraftUsed.remove(id);
        updraftReady.remove(id);
        driftUsed.remove(id);
        driftArmed.remove(id);
        drifting.remove(id);
        driftLasts.remove(id);
        jumpHeld.remove(id);
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
        showCooldown(player, minutes * 60 * 20);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_LEAD_TIED, 1f, 1.4f);
        wardrobe.tugBalloon(player, new org.bukkit.util.Vector(0, UPDRAFT_TUG, 0));
        puff(player, 12);
        player.sendMessage(Component.text("Your balloon caught you! Tethered needs " + minutes + " minutes to recover.", NamedTextColor.YELLOW));
    }
}
