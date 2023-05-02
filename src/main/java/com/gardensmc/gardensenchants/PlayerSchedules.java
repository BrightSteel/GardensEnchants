package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.enchants.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

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

    public static void tickWingedBoots() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GardensEnchants.plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.isFlying() && !player.hasPermission("gardens.fly.anywhere")) {
                    String[] anywhere_req = new String[]{
                            "resource_world",
                            "nether",
                            "end"
                    };
                    if (Arrays.asList(anywhere_req).contains(player.getWorld().getName().toLowerCase()) || !player.hasPermission("gardens.fly")) {
                        ItemStack boots = player.getInventory().getBoots();
                        if (boots != null && boots.getType() != Material.AIR) {
                            if (EnchantUtils.hasEnchantment(boots, CustomEnchantments.WINGED_BOOTS)) {
                                NBTItem nbtItem = new NBTItem(boots);
                                int charge = 0; // in seconds
                                if (nbtItem.hasKey("charge")) {
                                    charge = nbtItem.getInteger("charge");
                                }
                                if (charge > 0) {
                                    int new_charge = charge - 1;
                                    nbtItem.setInteger("charge", new_charge);
                                    if (new_charge == 0) {
                                        player.sendMessage(ChatColor.YELLOW + "Your boots ran out of charge!");
                                        player.setFlying(false);
                                        player.setAllowFlight(false);
                                    }
                                    player.getInventory().setBoots(nbtItem.getItem());

                                    EnchantUtils.updateLore(player.getInventory().getBoots());
                                }
                            }
                        }
                    }
                }
            }
        }, 0, 20);
    }

    public static void scheduleAll() {
        cancelOxygenDepletion();
        tickWingedBoots();
    }
}
