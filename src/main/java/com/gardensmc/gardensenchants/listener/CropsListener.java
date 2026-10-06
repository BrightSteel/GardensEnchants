package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.utils.ProbabilityUtil;
import com.gardensmc.gardenscrops.event.CropHarvestPrepareEvent;
import com.gardensmc.gardenscrops.event.PlanterWaterEvent;
import com.gardensmc.gardenscrops.event.WateringCanFillEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.BOUNTIFUL;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.DEEP_SOAK;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.GREEN_THUMB;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.RESERVOIR;
import static com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments.WIDE_SPRAY;

/**
 * The GardensCrops enchants: Reservoir, Wide Spray and Deep Soak on the watering can, Green Thumb and
 * Bountiful on the hoe. Only registered when GardensCrops is installed.
 */
public class CropsListener implements Listener {

    // 1, 2 and 3 star weights per Green Thumb level (GardensCrops rolls 60/30/10 without it)
    private static final double[][] GREEN_THUMB_CHANCES = {
            {0.52, 0.34, 0.14},
            {0.46, 0.36, 0.18},
            {0.40, 0.40, 0.20}
    };
    // chance of an extra crop per Bountiful level
    private static final double BOUNTIFUL_PERCENT_PER_LEVEL = 25;

    @EventHandler
    public void onFill(WateringCanFillEvent e) {
        int level = RESERVOIR.getEnchantmentLevel(e.getWateringCan());
        if (level > 0) {
            e.setCapacity(e.getCapacity() + level);
        }
    }

    @EventHandler
    public void onWater(PlanterWaterEvent e) {
        var can = e.getWateringCan();
        int spray = WIDE_SPRAY.getEnchantmentLevel(can);
        if (spray > 0) {
            // I: the 3x3 for a charge per planter; II: the whole 3x3 for one charge
            e.setRadius(1);
            e.setChargePerPlanter(spray < 2);
        }
        if (DEEP_SOAK.hasEnchantment(can)) {
            e.setExtraWetDays(1);
        }
    }

    @EventHandler
    public void onHarvest(CropHarvestPrepareEvent e) {
        var tool = e.getPlayer().getInventory().getItemInMainHand();
        int thumb = GREEN_THUMB.getEnchantmentLevel(tool);
        if (thumb > 0) {
            var chances = GREEN_THUMB_CHANCES[Math.min(thumb, GREEN_THUMB_CHANCES.length) - 1];
            e.setStarChances(chances[0], chances[1], chances[2]);
        }
        int bounty = BOUNTIFUL.getEnchantmentLevel(tool);
        if (bounty > 0 && ProbabilityUtil.roll(BOUNTIFUL_PERCENT_PER_LEVEL * bounty)) {
            e.setAmount(e.getAmount() + 1);
        }
    }
}
