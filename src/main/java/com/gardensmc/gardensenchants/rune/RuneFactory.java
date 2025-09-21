package com.gardensmc.gardensenchants.rune;

import com.gardensmc.gardensfurniture.GardensFurniture;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class RuneFactory {

    @Nullable
    public static RuneHandler createRuneHandler(ItemStack rune) {
        var customRuneItem = GardensFurniture.itemRegistry.getCustomItem(rune);
        if (customRuneItem == null) {
            return null;
        }
        return switch (customRuneItem.getIdentifier()) {
            case "power_up_rune" -> new PowerUpRune(rune);
            case "strength_rune" -> new RestorationRune(rune);
            case "slot_rune" -> new SlotIncreaserRuneHandler(rune);
            default -> null;
        };
    }

}
