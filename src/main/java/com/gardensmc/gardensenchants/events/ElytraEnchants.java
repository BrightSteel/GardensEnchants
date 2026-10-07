package com.gardensmc.gardensenchants.events;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.PLATED;

/**
 * Plated: an elytra worn as a chestplate, armoring its wearer by level as leather, iron, diamond or netherite would.
 * Transient modifiers, checked twice a second, so nothing's left on the player when it comes off.
 */
public final class ElytraEnchants {

    /** By level (I-IV): armor, toughness, knockback resistance, as those chestplates give. */
    private static final double[][] PLATES = {{3, 0, 0}, {6, 0, 0}, {8, 2, 0}, {8, 3, 0.1}};
    private static final NamespacedKey ARMOR = new NamespacedKey(GardensEnchants.plugin, "plated_armor");
    private static final NamespacedKey TOUGHNESS = new NamespacedKey(GardensEnchants.plugin, "plated_toughness");
    private static final NamespacedKey KNOCKBACK = new NamespacedKey(GardensEnchants.plugin, "plated_knockback");

    private ElytraEnchants() {
    }

    public static void enable() {
        Bukkit.getScheduler().runTaskTimer(GardensEnchants.plugin, ElytraEnchants::tick, 20, 10);
    }

    private static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            var chest = player.getInventory().getChestplate();
            int level = chest != null && chest.getType() == Material.ELYTRA ? Math.min(PLATED.getEnchantmentLevel(chest), PLATES.length) : 0;
            var plate = level > 0 ? PLATES[level - 1] : new double[]{0, 0, 0};
            set(player, Attribute.ARMOR, ARMOR, plate[0]);
            set(player, Attribute.ARMOR_TOUGHNESS, TOUGHNESS, plate[1]);
            set(player, Attribute.KNOCKBACK_RESISTANCE, KNOCKBACK, plate[2]);
        }
    }

    private static void set(Player player, Attribute attribute, NamespacedKey key, double amount) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        var current = instance.getModifier(key);
        if (current != null && current.getAmount() == amount) return;
        if (current != null) instance.removeModifier(key);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER));
    }
}
