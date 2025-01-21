package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensfurniture.GardensFurniture;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RuneFactory {

    @Nullable
    public static RuneHandler createRuneHandler(ItemStack rune) {
        var customRuneItem = GardensFurniture.itemRegistry.getCustomItem(rune);
        if (customRuneItem == null) {
            return null;
        }
        return switch (customRuneItem.getIdentifier()) {
            case "power_up_rune" -> new PowerUpRune();
            case "restoration_rune" -> new RestorationRune();
            case "slot_increaser_rune" -> new SlotIncreaserRuneHandler();
            default -> null;
        };
    }

}
