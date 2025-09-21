package com.gardensmc.gardensenchants.relic;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensfurniture.GardensFurniture;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@AllArgsConstructor
public class RelicHandler {

    private final Relic relic;

    public static RelicHandler createRelicHandler(ItemStack relic) {
        if (relic.getAmount() != 1) {
            return null;
        }
        var customItem = GardensFurniture.itemRegistry.getCustomItem(relic);
        if (customItem == null) {
            return null;
        }
        var potentialRelic = GardensEnchants.relics.get(customItem.getIdentifier());
        if (potentialRelic == null) {
            return null;
        }
        return new RelicHandler(potentialRelic);
    }

    public ItemStack apply(ItemStack itemStack) {
        var customItem = GardensFurniture.itemRegistry.getCustomItem(itemStack);
        var resultItem = GardensFurniture.itemRegistry.buildItem(
                relic.getRelicResult(Objects.requireNonNull(customItem).getIdentifier())
        );
        // construct merged result
        var clone = itemStack.clone();
        var cloneMeta = clone.getItemMeta();
        cloneMeta.setItemModel(Objects.requireNonNull(resultItem).getItemMeta().getItemModel());
        // upgrade name if default
        var name = clone.getItemMeta().displayName();
        if (name == null || PlainTextComponentSerializer.plainText().serialize(name).equalsIgnoreCase(customItem.getDisplayRawFormatted())) {
            cloneMeta.displayName(resultItem.displayName());
        }
        clone.setItemMeta(cloneMeta);
        return clone;
    }

    public boolean canApply(ItemStack itemStack) {
        var customItem = GardensFurniture.itemRegistry.getCustomItem(itemStack);
        return customItem != null && relic.hasRelicResult(customItem.getIdentifier());
    }

    public int getApplyCost(ItemStack itemStack) {
        // todo offset by equipment slot
        return relic.getBaseCost();
    }

}
