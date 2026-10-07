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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.FEATHERWEIGHT;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.TETHERED;

/**
 * Enchantments on the balloon a player wears (EnderWardrobe's balloon slot, read through its API):
 * Featherweight lowers gravity a little more per level; Tethered catches a fall that would kill, then recovers.
 */
public final class BalloonEnchants implements Listener {

    /** Gravity taken off per level of Featherweight, on top of the balloon's own. */
    private static final double FEATHERWEIGHT_PER_LEVEL = 0.05;
    /** Minutes Tethered needs to recover after catching a fall, by level (I, II, III). */
    private static final int[] TETHERED_COOLDOWN_MINUTES = {10, 7, 4};

    private final WardrobeApi wardrobe;
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
