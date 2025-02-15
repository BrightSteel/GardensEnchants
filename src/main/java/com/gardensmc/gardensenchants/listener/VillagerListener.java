package com.gardensmc.gardensenchants.listener;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
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

// currently just replacing vanilla book results with our custom versions
public class VillagerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onVillagerSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Villager villager && villager.getProfession() == Villager.Profession.LIBRARIAN) {
            villager.setRecipes(regenerateRecipes(villager));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerChangeCareer(VillagerCareerChangeEvent e) {
        if (e.getProfession() == Villager.Profession.LIBRARIAN) {
            e.getEntity().setRecipes(regenerateRecipes(e.getEntity()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerGenerateNewTrade(VillagerAcquireTradeEvent e) {
        if (e.getRecipe().getResult().getType() == Material.ENCHANTED_BOOK) {
            e.setRecipe(replaceBookRecipe(e.getRecipe()));
        }
    }

    private List<MerchantRecipe> regenerateRecipes(Villager villager) {
        return villager.getRecipes().stream().map(merchantRecipe -> {
            if (merchantRecipe.getResult().getType() == Material.ENCHANTED_BOOK) {
                return replaceBookRecipe(merchantRecipe);
            }
            return new MerchantRecipe(merchantRecipe);
        }).toList();
    }

    private MerchantRecipe replaceBookRecipe(MerchantRecipe merchantRecipe) {
        // no setter for result...
        var recipe = new MerchantRecipe(
                getCustomEnchantBook(merchantRecipe.getResult()),
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
            var enchantEntry = meta.getStoredEnchants().entrySet().iterator().next();
            var ench = GardensEnchantments.INSTANCE.getEnchantment(enchantEntry.getKey().getKey().getKey());
            return BooksHandler.INSTANCE.buildBook(new EnchantmentInstance(
                    ench,
                    enchantEntry.getValue()
            )).book();
        }
        System.out.println("Returning book w/o enchantment meta, maybe ran twice");
        return vanillaBook; // shouldn't happen
    }
}
