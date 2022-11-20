package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.enchants.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerSchedules {

    public static void cancelOxygenDepletion() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GardensEnchants.plugin, () -> {
            for (Player player: Bukkit.getServer().getOnlinePlayers()) {
                if (player.isInWater()) {
                    ItemStack helmet = player.getInventory().getHelmet();

                    if (helmet != null && EnchantUtils.hasEnchantment(helmet, CustomEnchantments.BUBBLE_HEAD)) {
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
