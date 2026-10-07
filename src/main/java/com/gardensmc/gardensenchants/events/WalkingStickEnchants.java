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
 * Enchantments on EnderWardrobe's walking sticks, held in either hand: Steady resists knockback; Shepherd has farm
 * animals nearby follow, as they'd follow their food; Trekker makes walking, sprinting and jumping cost less hunger;
 * Mountaineer steps up a block higher (not while sneaking, as the stick's own step).
 */
public final class WalkingStickEnchants implements Listener {

    /** Knockback resistance per level of Steady (1 is none at all). */
    private static final double STEADY_PER_LEVEL = 0.2;
    /** How far Shepherd reaches, how close the animals come, and how fast they walk. */
    private static final double SHEPHERD_RANGE = 10, SHEPHERD_CLOSE = 2.5, SHEPHERD_SPEED = 1.1;

    /** Less hunger per level of Trekker. */
    private static final double TREKKER_PER_LEVEL = 0.25;
    /** How much higher Mountaineer steps. */
    private static final double MOUNTAINEER_STEP = 1.0;

    private static final NamespacedKey STEADY_KEY = new NamespacedKey(GardensEnchants.plugin, "steady");
    private static final NamespacedKey MOUNTAINEER_KEY = new NamespacedKey(GardensEnchants.plugin, "mountaineer");

    private WalkingStickEnchants() {
    }

    public static void enable() {
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, WalkingStickEnchants::tick, 20, 10);
        Bukkit.getPluginManager().registerEvents(new WalkingStickEnchants(), GardensEnchants.plugin);
    }

    /** The higher level of the enchantment on a walking stick in either hand, or 0. */
    private static int held(Player player, CustomEnchantment enchantment) {
        int level = 0;
        for (ItemStack item : new ItemStack[]{player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()}) {
            if (item != null && !item.isEmpty() && EnchantRestricts.WALKING_STICK.isEnchantable(item)) {
                level = Math.max(level, enchantment.getEnchantmentLevel(item));
            }
        }
        return level;
    }

    private static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            steady(player, held(player, STEADY));
            mountaineer(player, player.isSneaking());
            if (held(player, SHEPHERD) > 0) shepherd(player);
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

    /** Mountaineer's step, on while it's held and its holder isn't sneaking. */
    private static void mountaineer(Player player, boolean sneaking) {
        var step = player.getAttribute(Attribute.STEP_HEIGHT);
        if (step == null) return;
        boolean on = !sneaking && held(player, MOUNTAINEER) > 0;
        boolean has = step.getModifier(MOUNTAINEER_KEY) != null;
        if (on == has) return;
        if (on) step.addTransientModifier(new AttributeModifier(MOUNTAINEER_KEY, MOUNTAINEER_STEP, AttributeModifier.Operation.ADD_NUMBER));
        else step.removeModifier(MOUNTAINEER_KEY);
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
    private static void shepherd(Player player) {
        for (var entity : player.getNearbyEntities(SHEPHERD_RANGE, SHEPHERD_RANGE / 2, SHEPHERD_RANGE)) {
            if (!(entity instanceof Animals animal) || entity instanceof Tameable || animal.isLeashed() || animal.isInsideVehicle()) continue;
            double distance = animal.getLocation().distance(player.getLocation());
            if (distance <= SHEPHERD_CLOSE) {
                animal.getPathfinder().stopPathfinding();
                animal.lookAt(player);
            } else {
                animal.getPathfinder().moveTo(player, SHEPHERD_SPEED);
            }
        }
    }
}
