package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VeinMinerEnchant extends CustomEnchantment {

    public static CustomEnchantment INSTANCE = new VeinMinerEnchant("gardens:vein_miner", EnchantRarity.ORDINARY);

    public VeinMinerEnchant(String bookNameSpace, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, "vein_miner"), bookNameSpace, rarity);
    }

    @NotNull
    @Override
    public String getName() {
        return "Vein Miner";
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }


    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().name().endsWith("PICKAXE");
    }
}
