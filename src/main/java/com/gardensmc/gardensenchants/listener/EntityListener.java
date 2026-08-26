package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.cache.OnlinePlayerCache;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.enchantment.custom.LingeringEnchantHandler;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EntityListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onShootArrow(EntityShootBowEvent e) {
        var bow = e.getBow();
        if (e.getProjectile() instanceof Arrow arrow && e.getEntity() instanceof Player player) {
            handleBowExplosion(arrow, bow, player);
            handleBowBarrage(arrow, bow);
        }
    }

    private void handleBowBarrage(Arrow arrow, ItemStack bow) {
        var barrage = CustomEnchantments.BARRAGE;
        var level = barrage.getEnchantmentLevel(bow);
        if (level > 0) {
            for (int i = 0; i < 3; i++) {
                Bukkit.getScheduler().runTaskLater(
                        GardensEnchants.plugin,
                        () -> duplicateArrow(arrow), (i + 1) * 5
                );
            }
        }
    }

    private void duplicateArrow(Arrow arrow) {
        Arrow clone = (Arrow) arrow.getWorld().spawnEntity(
                arrow.getLocation(),
                arrow.getType()
        );
        clone.setVelocity(arrow.getVelocity());
        // Copy the whole pickup item stack so the clone inherits the exact potion
        // contents AND the tipped-arrow duration scale (POTION_DURATION_SCALE).
        // Rebuilding via setBasePotionType leaves the scale at 1.0, which makes
        // potion effects last 8x longer than the real arrow (e.g. 5s -> 40s).
        clone.setItemStack(arrow.getItemStack());
        clone.setCritical(arrow.isCritical());
        clone.setShooter(arrow.getShooter());
        // Barrage clones are bonus projectiles, not free ammo: never let them be
        // collected, otherwise one arrow (especially with Infinity) becomes several.
        clone.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
    }

    private void handleBowExplosion(Entity arrow, ItemStack bow, Player source) {
        var explosive = CustomEnchantments.EXPLOSIVE;
        var explosiveLevel = explosive.getEnchantmentLevel(bow);
        if (explosiveLevel > 0) {
            new LingeringEnchantHandler(arrow).putLingeringEnchant(explosive.getId(), explosiveLevel, source.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArrowHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow arrow) {
            var lingeringExplosive = new LingeringEnchantHandler(arrow).getLingeringEnchant(
                    CustomEnchantments.EXPLOSIVE.getId()
            );
            if (lingeringExplosive != null) {
                var source = Bukkit.getPlayer(lingeringExplosive.source());
                if (source != null) {
                    arrow.getWorld().createExplosion(
                            source,
                            e.getEntity().getLocation(),
                            lingeringExplosive.level(),
                            true,
                            false
                    );
                }
            }
        }
    }





}
