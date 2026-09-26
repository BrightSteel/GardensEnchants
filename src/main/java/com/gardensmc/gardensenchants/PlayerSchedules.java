package com.gardensmc.gardensenchants;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.*;

public class PlayerSchedules {

    public static void cancelOxygenDepletion() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GardensEnchants.plugin, () -> {
            for (Player player: Bukkit.getServer().getOnlinePlayers()) {
                if (player.isInWater()) {
                    ItemStack helmet = player.getInventory().getHelmet();

                    if (helmet != null && BUBBLE_HEAD.hasEnchantment(helmet)) {
                        player.setRemainingAir(player.getMaximumAir());
                    }
                }
            }
        }, 0, 25);
    }

    public static void scheduleAll() {
        cancelOxygenDepletion();
    }
}
