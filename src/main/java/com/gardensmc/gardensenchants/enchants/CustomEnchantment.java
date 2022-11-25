package com.gardensmc.gardensenchants.enchants;

import com.gardensmc.gardensenchants.GardensEnchants;
import com.gardensmc.gardensenchants.utils.ChatUtils;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomEnchantment extends Enchantment {

    private final String bookNamespace;
    private EnchantRarity rarity;

    public CustomEnchantment(String key, EnchantRarity rarity) {
        super(new NamespacedKey(GardensEnchants.plugin, key));
        this.bookNamespace = "gardens:" + key;
        this.rarity = rarity;
    }

    public String getBookNamespace() {
        return bookNamespace;
    }

    public ItemStack getBook() {
        CustomStack customStack = CustomStack.getInstance(bookNamespace);
        ItemStack book;
        if (customStack != null) {
            book = customStack.getItemStack();
        }
        else {
            book = new ItemStack(Material.ENCHANTED_BOOK);
        }

        ItemMeta meta = book.getItemMeta();
        String colorPrefix = EnchantUtils.getColorFromRarity(rarity);

        meta.setDisplayName(colorPrefix + meta.getDisplayName().substring(2));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + EnchantUtils.getRarityTag(rarity));
        for (String s : meta.getLore()) {
            lore.add(ChatUtils.translateHexColorCodes("#", "", "#555555") + s);
        }
        meta.setLore(lore);
//            ItemMeta meta = book.getItemMeta();
//            meta.setDisplayName(colorPrefix + meta.getDisplayName());
            book.setItemMeta(meta);
            return book;

    }


    public EnchantRarity getRarity() {
        return rarity;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return bookNamespace;
    }
}
