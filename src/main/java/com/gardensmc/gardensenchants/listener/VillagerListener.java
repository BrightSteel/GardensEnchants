package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Objects;

public class VillagerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onVillagerSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Villager villager) {
            villager.setRecipes(regenerateRecipes(villager));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerChangeCareer(VillagerCareerChangeEvent e) {
        e.getEntity().setRecipes(regenerateRecipes(e.getEntity()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerGenerateNewTrade(VillagerAcquireTradeEvent e) {
        var recipe = getReplacementRecipe(e.getRecipe());
        if (recipe != null) {
            e.setRecipe(recipe);
        }
    }

    private MerchantRecipe getReplacementRecipe(MerchantRecipe merchantRecipe) {
        var result = merchantRecipe.getResult();
        if (result.getType() == Material.ENCHANTED_BOOK) {
            return replaceMerchantRecipe(merchantRecipe, getCustomEnchantBook(merchantRecipe.getResult()));
        }
        if (!result.getEnchantments().isEmpty()) {
            var clone = result.clone();
            new GardensEnchantHandler(clone).applyPostEnchantChanges();
            return replaceMerchantRecipe(merchantRecipe, clone);
        }
        return null;
    }

    private List<MerchantRecipe> regenerateRecipes(Villager villager) {
        return villager.getRecipes().stream().map(merchantRecipe -> {
            var replacement = getReplacementRecipe(merchantRecipe);
            return Objects.requireNonNullElseGet(replacement, () -> new MerchantRecipe(merchantRecipe));
        }).toList();
    }

    private MerchantRecipe replaceMerchantRecipe(MerchantRecipe merchantRecipe, ItemStack newResult) {
        // no setter for result...
        var recipe = new MerchantRecipe(
                newResult,
                merchantRecipe.getUses(),
                merchantRecipe.getMaxUses(),
                merchantRecipe.hasExperienceReward(),
                merchantRecipe.getVillagerExperience(),
                merchantRecipe.getPriceMultiplier(),
                merchantRecipe.getDemand(),
                merchantRecipe.getSpecialPrice(),
                merchantRecipe.shouldIgnoreDiscounts()
        );
        recipe.setIngredients(merchantRecipe.getIngredients());
        return recipe;
    }

    public static ItemStack getCustomEnchantBook(ItemStack vanillaBook) {
        if (vanillaBook.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            // ignoring all but first enchantment...
            if (meta.getStoredEnchants().isEmpty()) {
                return vanillaBook; // not sure why this happens ? seeing error log in console
            }
            var enchantEntry = meta.getStoredEnchants().entrySet().iterator().next();
            var ench = GardensEnchantments.INSTANCE.getEnchantment(enchantEntry.getKey().getKey().getKey());
            if (ench == null) {
                // unregistered enchants - ie vanishing
                return vanillaBook;
            }
            return BooksHandler.INSTANCE.buildBook(new EnchantmentInstance(
                    ench,
                    enchantEntry.getValue()
            )).book();
        }
        return vanillaBook; // shouldn't happen
    }
}
