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
import org.bukkit.inventory.ItemStack;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.SHEPHERD;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.STEADY;

/**
 * Enchantments on EnderWardrobe's walking sticks, held in either hand: Steady resists knockback; Shepherd has farm
 * animals nearby follow, as they'd follow their food.
 */
public final class WalkingStickEnchants {

    /** Knockback resistance per level of Steady (1 is none at all). */
    private static final double STEADY_PER_LEVEL = 0.2;
    /** How far Shepherd reaches, how close the animals come, and how fast they walk. */
    private static final double SHEPHERD_RANGE = 10, SHEPHERD_CLOSE = 2.5, SHEPHERD_SPEED = 1.1;

    private static final NamespacedKey STEADY_KEY = new NamespacedKey(GardensEnchants.plugin, "steady");

    private WalkingStickEnchants() {
    }

    public static void enable() {
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, WalkingStickEnchants::tick, 20, 10);
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
