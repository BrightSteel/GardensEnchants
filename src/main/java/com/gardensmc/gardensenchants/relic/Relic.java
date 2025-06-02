package com.gardensmc.gardensenchants.relic;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Relic {

    @Getter
    private final String id;
    // equipment_furniture_id -> result_furniture_id
    private final Map<String, String> relicEquipmentMap;
    @Getter
    private final int baseCost;

    public Relic(String id, ConfigurationSection relicSection) {
        this.id = id;
        this.baseCost = relicSection.getInt("baseCost", 3);
        var equipmentSection = relicSection.getConfigurationSection("equipment");
        this.relicEquipmentMap = Objects.requireNonNull(equipmentSection).getKeys(false)
                .stream()
                .map(equipmentKey -> Map.entry(
                        equipmentKey,
                        Objects.requireNonNull(equipmentSection.getString(equipmentKey))
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String getRelicResult(String equipmentId) {
        return relicEquipmentMap.get(equipmentId);
    }

    public boolean hasRelicResult(String equipmentId) {
        return relicEquipmentMap.containsKey(equipmentId);
    }
}
