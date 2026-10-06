package com.gardensmc.gardensenchants.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.gardensmc.gardensenchants.cache.OnlinePlayerCache;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.FoodMath;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class PlayerListener implements Listener {

    // players holding the jump key, so Rocket Boots launch them from water once per press
    private final Set<UUID> holdingJump = new HashSet<>();

    // the ways a mob notices a player on its own - the only targeting Cloak hides you from. A mob you hit, or a
    // pet sent after you, still comes for you.
    private static final Set<EntityTargetEvent.TargetReason> NATURAL_TARGET_REASONS = Set.of(
            EntityTargetEvent.TargetReason.CLOSEST_ENTITY,
            EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
            EntityTargetEvent.TargetReason.RANDOM_TARGET,
            EntityTargetEvent.TargetReason.DEFEND_VILLAGE,
            EntityTargetEvent.TargetReason.COLLISION
    );

    @EventHandler(ignoreCancelled = true)
    public void onFoodDrain(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (e.getItem() != null) { // ignore eating
            return;
        }
        var foodLevel = e.getEntity().getFoodLevel();
        var resultLevel = e.getFoodLevel();
        if (resultLevel >= foodLevel) { // ignore food increases
            return;
        }
        var modifiedFoodLevel = getModifiedFoodLevel(player, foodLevel, resultLevel);
        if (modifiedFoodLevel != resultLevel) {
            e.setFoodLevel(modifiedFoodLevel);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEatFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (e.getItem() == null) {
            return;
        }
        var foodLevel = e.getEntity().getFoodLevel();
        var resultLevel = e.getFoodLevel();
        if (resultLevel <= foodLevel) { // ignore decreases
            return;
        }
        var helmet = player.getInventory().getHelmet();
        if (helmet == null) {
            return;
        }
        var muncherLvl = CustomEnchantments.MUNCHER.getEnchantmentLevel(helmet);
        if (muncherLvl > 0) {
            e.setFoodLevel(FoodMath.muncherFoodLevel(foodLevel, resultLevel, muncherLvl));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTargetPlayer(EntityTargetLivingEntityEvent e) {
        if (!(e.getTarget() instanceof Player player)) {
            return;
        }
        if (OnlinePlayerCache.INSTANCE.getPlayerData(player.getUniqueId()).isInCombat()) {
            return;
        }
        if (e.getEntity().getType() == EntityType.ENDERMAN) {
            return;
        }
        if (!NATURAL_TARGET_REASONS.contains(e.getReason())) {
            return;
        }
        if (shouldCancelTarget(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        var player = e.getPlayer();
        if (player.isSneaking()) {
            handleFlutter(player);
        }
        handleFlippers(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKillEntity(EntityDeathEvent e) {
        if (e instanceof PlayerDeathEvent) {
            // PvP: the victim's drops stay on the ground. Taking them here ignored keepInventory,
            // so a later keepInventory handler (e.g. GPFlags) left the victim a copy.
            return;
        }
        var damager = e.getDamageSource().getCausingEntity();
        if (damager instanceof Player player) {
            var tool = PlayerUtil.getHeldItem(player);
            if (CustomEnchantments.SIPHON.hasEnchantment(tool)) {
                PlayerUtil.giveItems(player, e.getDrops());
                var amount = e.getDroppedExp();
                var expToGive = simulateMending(player, getModifiedExp(player, amount));
                if (expToGive > 0) {
                    player.giveExp(expToGive);
                }
                // cancel drops
                e.setDroppedExp(0);
                e.getDrops().clear();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTakeFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        var boots = player.getInventory().getBoots();
        if (boots == null) {
            return;
        }
        var rocketLvl = CustomEnchantments.ROCKET_BOOTS.getEnchantmentLevel(boots);
        if (rocketLvl > 0) {
            int damageableBlocksFell = (int) e.getDamage(); // players take 1 dmg per block after first 3
            var padding = rocketLvl - 1; // give extra block of padding starting at level 2, since level 1 is a three block jump and this is already not damageable
            if (damageableBlocksFell <= padding) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCollectExp(PlayerExpChangeEvent e) {
        var amount = e.getAmount();
        if (amount < 1) {
            return;
        }
        var modifiedAmount = getModifiedExp(e.getPlayer(), amount);
        if (modifiedAmount != amount) {
            e.setAmount(modifiedAmount);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e) {
        if (CustomEnchantments.NIGHT_VISION.hasEnchantment(e.getPlayer().getInventory().getHelmet())) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
        }
    }

    private int getModifiedExp(Player player, int amount) {
        var chestplate = player.getInventory().getChestplate();
        if (chestplate != null) {
            var orbBoostLvl = CustomEnchantments.ORB_BOOST.getEnchantmentLevel(chestplate);
            if (orbBoostLvl > 0) {
                int n = (int) Math.round(amount + (amount * orbBoostLvl * 0.1));
                return n;
            }
        }
        return amount;
    }

    /**
     * Rocket Boots in shallow water. The boots work by giving Jump Boost while sneaking, but a jump from
     * water is a swim upwards, which ignores Jump Boost - so for a sneaking player standing in water with
     * their head above it, we make the boosted jump ourselves.
     */
    @EventHandler
    public void onJumpInWater(PlayerInputEvent e) {
        var player = e.getPlayer();
        var input = e.getInput();
        // only on pressing jump: holding it and changing another key fires this again
        boolean pressed = input.isJump() && holdingJump.add(player.getUniqueId());
        if (!input.isJump()) {
            holdingJump.remove(player.getUniqueId());
        }
        if (!pressed || !input.isSneak() || !isStandingInShallowWater(player)) {
            return;
        }
        var boots = player.getInventory().getBoots();
        int level = boots == null ? 0 : CustomEnchantments.ROCKET_BOOTS.getEnchantmentLevel(boots);
        if (level <= 0) {
            return;
        }
        // vanilla's jump with the boots' Jump Boost (amplifier = level)
        double jump = 0.42 + 0.1 * (level + 1);
        var velocity = player.getVelocity();
        player.setVelocity(velocity.setY(Math.max(velocity.getY(), jump)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        holdingJump.remove(e.getPlayer().getUniqueId());
    }

    // feet in water, head out of it, standing on something
    private static boolean isStandingInShallowWater(Player player) {
        if (!player.isInWater() || player.getEyeLocation().getBlock().getType() == Material.WATER) {
            return false;
        }
        return player.isOnGround() || player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }

    private void handleFlutter(Player player) {
        var velocity = player.getVelocity();
        if (velocity.getY() >= 0) {
            return;
        }
        if (player.isInWater() || player.isGliding() || player.isOnGround()) {
            return;
        }
        var chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.ELYTRA) {
            return;
        }
        if (!CustomEnchantments.FLUTTER.hasEnchantment(chestplate)) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 1));
        // if player already gained falling velocity, it'll take a while to lower. Let's reset it immediately to max slow falling speed
        if (velocity.getY() <= -0.5) {
            player.setVelocity(velocity.setY(-0.49)); // avg velocity seems to be -0.49 with slow falling
        }
    }

    private void handleFlippers(Player player) {
        if (!player.isInWater() || !player.isSwimming()) {
            return;
        }
        var boots = player.getInventory().getBoots();
        if (boots == null) {
            return;
        }
        if (!CustomEnchantments.FLIPPERS.hasEnchantment(boots)) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 5, 1));
    }

    private boolean shouldCancelTarget(Player player) {
        return Arrays.stream(player.getInventory().getArmorContents()).anyMatch(armor -> {
            if (armor == null) {
                return false;
            }
            return CustomEnchantments.CLOAK.hasEnchantment(armor);
        });
    }

    private int getModifiedFoodLevel(Player player, int foodLevel, int resultLevel) {
        var boots = player.getInventory().getBoots();
        if (boots == null) {
            return resultLevel;
        }
        var sprinterLvl = CustomEnchantments.SPRINTER.getEnchantmentLevel(boots);
        if (sprinterLvl == 0) {
            return resultLevel;
        }
        return FoodMath.sprinterFoodLevel(foodLevel, resultLevel, sprinterLvl, ThreadLocalRandom.current()::nextDouble);
    }

    // returns leftover exp to be added to player, if any
    private int simulateMending(Player player, int expAmount) {
        var inv = player.getInventory();
        var mendables = Stream.concat(
                Stream.of(inv.getItemInMainHand(), inv.getItemInOffHand()),
                Arrays.stream(inv.getArmorContents())
        )
        .filter(this::canBeRepaired)
        .toList();
        if (mendables.isEmpty()) {
            return expAmount;
        }
        var randomItem = mendables.get(ProbabilityUtil.getRandomIndex(mendables.size()));
        Damageable meta = (Damageable) randomItem.getItemMeta();
        int damage = meta.getDamage();
        int repairableDurability = Math.min(damage, 2 * expAmount);
        meta.setDamage(damage - repairableDurability);
        randomItem.setItemMeta(meta);
        return expAmount - (repairableDurability / 2);
    }

    private boolean canBeRepaired(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        // like vanilla mending, only items with Mending are repaired by the xp
        if (!itemStack.containsEnchantment(Enchantment.MENDING)) {
            return false;
        }
        var meta = itemStack.getItemMeta();
        return meta instanceof Damageable damageable && damageable.hasDamage();
    }
}
