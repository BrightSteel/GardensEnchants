package com.gardensmc.gardensenchants;

import com.gardensmc.gardensenchants.enchantment.EnchantRarity;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestrict;
import com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@Getter
public class VanillaOverride {

    private EnchantRarity enchantRarity = EnchantRarity.COMMON;
    private final String display;
    private List<Component> lore = Collections.emptyList();
    private final boolean isDisabled;
    private final String bookIdentifier;
    private EnchantRestrict enchantRestrict = EnchantRestricts.ALL;

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
        var enchantRestrictId = overrideSection.getString("enchantRestrict");
        if (enchantRestrictId != null) {
            // probably should have these in a map registered by an ID, but im lazy so...
            try {
                this.enchantRestrict = (EnchantRestrict) EnchantRestricts.class
                        .getDeclaredField(enchantRestrictId.toUpperCase())
                        .get(null);
            } catch (Exception e) {
                GardensEnchants.plugin.getLogger().log(
                        Level.SEVERE,
                        "Unknown enchant restrict %s defined in override, ignoring!".formatted(enchantRestrictId)
                );
            }
        }
    }

}
