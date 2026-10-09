package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantment;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.MOUNTAINEER;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.SHEPHERD;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.STEADY;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.TREKKER;

/**
 * Enchantments on EnderWardrobe's staffs, held in either hand: Steady resists knockback; Shepherd has farm
 * animals nearby follow, as they'd follow their food; Trekker makes sprinting and jumping cost less hunger
 * (walking costs none to begin with);
 * Mountaineer steps up a block higher (not while sneaking, as the stick's own step); and vanilla's Soul Speed works as
 * it would on boots (vanilla only looks at the feet), though it doesn't wear the stick any faster.
 */
public final class StaffEnchants implements Listener {

    /** Knockback resistance per level of Steady (1 is none at all). */
    private static final double STEADY_PER_LEVEL = 0.2;
    /** How far Shepherd reaches (as far as vanilla's food tempts), how close the animals come, and how fast they walk. */
    private static final double SHEPHERD_RANGE = 10, SHEPHERD_CLOSE = 2.5, SHEPHERD_SPEED = 1.1;
    /** The animals following each player, to let go of when they stop (put the staff away, leave, go out of range). */
    private static final java.util.Map<java.util.UUID, java.util.Set<java.util.UUID>> FOLLOWING = new java.util.HashMap<>();

    /** Less hunger per level of Trekker. */
    private static final double TREKKER_PER_LEVEL = 0.25;
    /** How much higher Mountaineer steps. */
    private static final double MOUNTAINEER_STEP = 1.0;

    private static final NamespacedKey STEADY_KEY = new NamespacedKey(GardensEnchants.plugin, "steady");
    private static final NamespacedKey SOUL_SPEED_KEY = new NamespacedKey(GardensEnchants.plugin, "soul_speed");
    private static final NamespacedKey MOUNTAINEER_KEY = new NamespacedKey(GardensEnchants.plugin, "mountaineer");

    private StaffEnchants() {
    }

    public static void enable() {
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, StaffEnchants::tick, 20, 10);
        // every tick, as vanilla checks what's underfoot
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, () -> Bukkit.getOnlinePlayers().forEach(StaffEnchants::soulSpeed), 20, 1);
        Bukkit.getPluginManager().registerEvents(new StaffEnchants(), GardensEnchants.plugin);
    }

    /** The higher level of the enchantment on a staff in either hand, or 0. */
    private static int held(Player player, CustomEnchantment enchantment) {
        int level = 0;
        for (ItemStack item : new ItemStack[]{player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()}) {
            if (item != null && !item.isEmpty() && EnchantRestricts.STAFF.isEnchantable(item)) {
                level = Math.max(level, enchantment.getEnchantmentLevel(item));
            }
        }
        return level;
    }

    private static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            steady(player, held(player, STEADY));
            mountaineer(player, player.isSneaking());
            var following = held(player, SHEPHERD) > 0 ? shepherd(player) : java.util.Set.<java.util.UUID>of();
            letGo(FOLLOWING.remove(player.getUniqueId()), following);
            if (!following.isEmpty()) FOLLOWING.put(player.getUniqueId(), following);
        }
    }

    private static void steady(Player player, int level) {
        var resistance = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (resistance == null) return;
        double amount = STEADY_PER_LEVEL * level;
        var current = resistance.getModifier(STEADY_KEY);
        if (current != null && current.getAmount() == amount) return;
        if (current != null) resistance.removeModifier(STEADY_KEY);
        if (level > 0) resistance.addTransientModifier(new AttributeModifier(STEADY_KEY, amount, AttributeModifier.Operation.ADD_NUMBER));
    }

    /** Where each player was last tick, to tell whether they're moving (a player's velocity, server-side, isn't). */
    private static final java.util.Map<java.util.UUID, org.bukkit.Location> LAST_AT = new java.util.HashMap<>();

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
        LAST_AT.remove(e.getPlayer().getUniqueId());
        letGo(FOLLOWING.remove(e.getPlayer().getUniqueId()), java.util.Set.of());
    }

    /** Soul Speed's level on a staff in either hand, or 0. */
    private static int heldSoulSpeed(Player player) {
        int level = 0;
        for (ItemStack item : new ItemStack[]{player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()}) {
            if (item != null && !item.isEmpty() && EnchantRestricts.STAFF.isEnchantable(item)) {
                level = Math.max(level, item.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.SOUL_SPEED));
            }
        }
        return level;
    }

    /**
     * Soul Speed as vanilla's on boots: on soul sand or soul soil, no slowing and faster (0.0405, and 0.0105 more a
     * level after the first), with souls kicked up as you go.
     */
    private static void soulSpeed(Player player) {
        var speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        var efficiency = player.getAttribute(Attribute.MOVEMENT_EFFICIENCY);
        if (speed == null || efficiency == null) return;
        int level = player.isFlying() || !player.isOnGround() ? 0 : heldSoulSpeed(player);
        if (level > 0 && !org.bukkit.Tag.SOUL_SPEED_BLOCKS.isTagged(player.getLocation().subtract(0, 0.5, 0).getBlock().getType())) level = 0;
        double amount = level > 0 ? 0.0405 + 0.0105 * (level - 1) : 0;
        var current = speed.getModifier(SOUL_SPEED_KEY);
        if (current == null ? amount != 0 : current.getAmount() != amount) {
            if (current != null) {
                speed.removeModifier(SOUL_SPEED_KEY);
                efficiency.removeModifier(SOUL_SPEED_KEY);
            }
            if (amount != 0) {
                speed.addTransientModifier(new AttributeModifier(SOUL_SPEED_KEY, amount, AttributeModifier.Operation.ADD_NUMBER));
                efficiency.addTransientModifier(new AttributeModifier(SOUL_SPEED_KEY, 1.0, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        var at = player.getLocation();
        var last = LAST_AT.put(player.getUniqueId(), at);
        boolean moving = last != null && last.getWorld() == at.getWorld() && last.distanceSquared(at) > 1e-4;
        if (amount != 0 && moving && Math.random() < 0.25) {
            player.getWorld().spawnParticle(org.bukkit.Particle.SOUL, player.getLocation().add(0, 0.1, 0), 1, 0.3, 0, 0.3, 0.02);
        }
    }

    /** Marks a staff whose own step height has Mountaineer's in it (so its tooltip says what it does). */
    private static final NamespacedKey MOUNTAINEER_ON_ITEM = new NamespacedKey(GardensEnchants.plugin, "mountaineer_step");

    /**
     * Mountaineer's step, on while it's held and its holder isn't sneaking. A staff enchanted since carries it in its
     * own step height, which counts while it's held: that's taken back off while sneaking. One enchanted before adds
     * it while held and not sneaking.
     */
    private static void mountaineer(Player player, boolean sneaking) {
        var step = player.getAttribute(Attribute.STEP_HEIGHT);
        if (step == null) return;
        double want = 0;
        var staff = heldWith(player, MOUNTAINEER);
        if (staff != null) {
            boolean onItem = staff.getPersistentDataContainer().has(MOUNTAINEER_ON_ITEM);
            want = onItem ? (sneaking ? -MOUNTAINEER_STEP : 0) : (sneaking ? 0 : MOUNTAINEER_STEP);
        }
        var current = step.getModifier(MOUNTAINEER_KEY);
        if (current == null ? want == 0 : current.getAmount() == want) return;
        if (current != null) step.removeModifier(MOUNTAINEER_KEY);
        if (want != 0) step.addTransientModifier(new AttributeModifier(MOUNTAINEER_KEY, want, AttributeModifier.Operation.ADD_NUMBER));
    }

    // the staff held (either hand) with this enchantment, or null
    private static ItemStack heldWith(Player player, CustomEnchantment enchantment) {
        for (ItemStack item : new ItemStack[]{player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()}) {
            if (item != null && !item.isEmpty() && EnchantRestricts.STAFF.isEnchantable(item) && enchantment.getEnchantmentLevel(item) > 0) return item;
        }
        return null;
    }

    /**
     * Puts Mountaineer's step into a staff's own step height when it has the enchantment (or takes it back out when
     * it hasn't), so the staff's "When held" line is what it does: +0.4 becomes +1.4. Called whenever its enchantments
     * change: enchanting, combining in an anvil, an Extraction Rune, a grindstone.
     */
    public static void syncMountaineer(ItemStack item) {
        if (item == null || item.isEmpty() || !item.hasItemMeta()) return;
        // an EnderWardrobe staff's attributes are what it does: show them (staffs enchanted before had them hidden)
        if (item.getPersistentDataContainer().has(new NamespacedKey("enderwardrobe", "offhand"))) {
            item.editMeta(meta -> meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES));
        }
        boolean want = MOUNTAINEER.getEnchantmentLevel(item) > 0;
        boolean had = item.getPersistentDataContainer().has(MOUNTAINEER_ON_ITEM);
        double delta = (want ? MOUNTAINEER_STEP : 0) - (had ? MOUNTAINEER_STEP : 0);
        if (delta == 0) return;
        var type = io.papermc.paper.datacomponent.DataComponentTypes.ATTRIBUTE_MODIFIERS;
        var modifiers = item.getData(type);
        var builder = io.papermc.paper.datacomponent.item.ItemAttributeModifiers.itemAttributes();
        boolean done = false;
        if (modifiers != null) {
            for (var entry : modifiers.modifiers()) {
                var m = entry.modifier();
                if (!done && entry.attribute() == Attribute.STEP_HEIGHT && m.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                    m = new AttributeModifier(m.getKey(), m.getAmount() + delta, m.getOperation(), m.getSlotGroup());
                    done = true;
                }
                builder.addModifier(entry.attribute(), m, entry.getGroup(), entry.display());
            }
        }
        if (!done) {
            if (delta < 0) return;
            builder.addModifier(Attribute.STEP_HEIGHT, new AttributeModifier(MOUNTAINEER_KEY, delta,
                    AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.HAND));
        }
        item.setData(type, builder.build());
        item.editMeta(meta -> {
            if (want) meta.getPersistentDataContainer().set(MOUNTAINEER_ON_ITEM, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            else meta.getPersistentDataContainer().remove(MOUNTAINEER_ON_ITEM);
        });
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        mountaineer(e.getPlayer(), e.isSneaking());
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        Bukkit.getScheduler().runTask(GardensEnchants.plugin, () -> mountaineer(e.getPlayer(), e.getPlayer().isSneaking()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExhaustion(EntityExhaustionEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        switch (e.getExhaustionReason()) {
            case WALK, SPRINT, JUMP, JUMP_SPRINT -> {
                int level = held(player, TREKKER);
                if (level > 0) e.setExhaustion((float) (e.getExhaustion() * Math.max(0, 1 - TREKKER_PER_LEVEL * level)));
            }
            default -> {
            }
        }
    }

    /** Farm animals in range walk over, as to someone holding their food, and stop a little way off. */
    /** Draws farm animals within range toward the player; returns the ones following. */
    private static java.util.Set<java.util.UUID> shepherd(Player player) {
        var following = new java.util.HashSet<java.util.UUID>();
        for (var entity : player.getNearbyEntities(SHEPHERD_RANGE, SHEPHERD_RANGE / 2, SHEPHERD_RANGE)) {
            if (!(entity instanceof Animals animal) || entity instanceof Tameable || animal.isLeashed() || animal.isInsideVehicle()) continue;
            double distance = animal.getLocation().distance(player.getLocation());
            if (distance > SHEPHERD_RANGE) continue; // a sphere, as vanilla's food reaches, not the search's box
            following.add(animal.getUniqueId());
            if (distance <= SHEPHERD_CLOSE) {
                animal.getPathfinder().stopPathfinding();
                animal.lookAt(player);
            } else {
                animal.getPathfinder().moveTo(player, SHEPHERD_SPEED);
            }
        }
        return following;
    }

    /** Animals that were following and aren't now stop walking over: back to whatever they'd do on their own. */
    private static void letGo(@org.jetbrains.annotations.Nullable java.util.Set<java.util.UUID> were, java.util.Set<java.util.UUID> now) {
        if (were == null) return;
        for (var id : were) {
            if (now.contains(id)) continue;
            if (Bukkit.getEntity(id) instanceof Animals animal) animal.getPathfinder().stopPathfinding();
        }
    }
}
