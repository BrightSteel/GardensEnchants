package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.enchantment.custom.CustomEnchantments;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GardensEnchantments {

    public static final GardensEnchantments INSTANCE = new GardensEnchantments();

    private static final Map<String, GardensEnchantment> gardensEnchantments = new HashMap<>();
    private static final Map<EnchantRarity, List<GardensEnchantment>> enchantmentsByRarity = new HashMap<>();

    @Nullable
    public GardensEnchantment getEnchantment(String id) {
        return gardensEnchantments.get(id);
    }

    public List<GardensEnchantment> getEnchantments(EnchantRarity rarity) {
        return enchantmentsByRarity.containsKey(rarity) ? enchantmentsByRarity.get(rarity) : new ArrayList<>();
    }

    public void registerEnchantments() {
        // register vanilla enchants
        gardensEnchantments.putAll(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                .stream()
                .filter(enchantment -> enchantment.isDiscoverable() && !enchantment.isCursed())
                .map(enchantment -> {
                    var id = enchantment.getKey().getKey().toUpperCase();
                    var vanillaOverride = GardensEnchants.vanillaOverrides.get(id);
                    if (vanillaOverride != null) {
                        if (vanillaOverride.isDisabled()) {
                            return null;
                        }
                        return new VanillaEnchantment(
                                enchantment,
                                vanillaOverride.getEnchantRarity(),
                                vanillaOverride.getDisplay(),
                                vanillaOverride.getLore(),
                                vanillaOverride.getBookIdentifier(),
                                vanillaOverride.getEnchantRestrict()
                        );
                    }
                    return new VanillaEnchantment(enchantment);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(VanillaEnchantment::getId, Function.identity()))
        );
        // register custom enchants
        CustomEnchantments.customEnchantments.values().forEach(customEnchantment -> {
            var id = customEnchantment.getId();
            if (gardensEnchantments.containsKey(id)) {
                GardensEnchants.plugin.getLogger().log(
                        Level.SEVERE,
                        String.format(
                                "Custom enchantment %s is using an existing id '%s' and could not be registered!",
                                customEnchantment.getDisplayName(),
                                customEnchantment.getId())
                );
            } else {
                gardensEnchantments.put(id, customEnchantment);
            }
        });
        // organize enchantment references by rarity for generating books
        populateEnchantmentsByRarity();
    }

    private void populateEnchantmentsByRarity() {
        gardensEnchantments.values().forEach(enchantment -> enchantmentsByRarity
                .computeIfAbsent(enchantment.getRarity(), k -> new ArrayList<>())
                .add(enchantment));
    }
}
