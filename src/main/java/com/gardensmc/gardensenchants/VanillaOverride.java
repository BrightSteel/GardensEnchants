package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;

@Getter
public class VanillaOverride {

    private EnchantRarity enchantRarity = EnchantRarity.ORDINARY;
    private final String display;
    private List<Component> lore = Collections.emptyList();
    private final boolean isDisabled;
    private final String bookIdentifier;

    public VanillaOverride(ConfigurationSection overrideSection) {
        var rarity = overrideSection.getString("rarity");
        if (rarity != null) {
            this.enchantRarity = EnchantRarity.valueOf(rarity.toUpperCase());
        }
        this.display = overrideSection.getString("display");
        var loreSection = overrideSection.getStringList("lore");
        if (!loreSection.isEmpty()) {
            this.lore = loreSection.stream().map(MessageUtil::parseIntoComponent).toList();
        }
        this.isDisabled = overrideSection.getBoolean("isDisabled");
        this.bookIdentifier = overrideSection.getString("bookIdentifier");
    }

}
