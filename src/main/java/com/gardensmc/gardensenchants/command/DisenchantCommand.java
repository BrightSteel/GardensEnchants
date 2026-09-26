package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisenchantCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(commandSender instanceof Player player) || !commandSender.hasPermission("gardens.disenchant")) return false;

        if (args.length > 0) {
            // accepts "sharpness", "minecraft:sharpness" or a custom enchant id
            String enchant = args[0].toLowerCase().replaceFirst("^minecraft:", "");
            var main = player.getInventory().getItemInMainHand();
            if (main.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "You must be holding an item!");
                return true;
            }
            // goes through the enchant handler so custom enchants and the lore are handled too.
            // (the old NBTItem edit only saw custom_data on 1.20.5+ and changed nothing)
            var enchantHandler = new GardensEnchantHandler(main);
            var instance = enchantHandler.getEnchantments()
                    .stream()
                    .filter(enchantmentInstance -> enchantmentInstance.getEnchantment().getId().equals(enchant))
                    .findFirst()
                    .orElse(null);

            if (instance != null) {
                enchantHandler.removeEnchant(instance.getEnchantment());
                player.sendMessage(ChatColor.GREEN + "Removed " + instance.getEnchantment().getId());
            }
            else {
                player.sendMessage(ChatColor.RED + "Item does not have enchantment " + enchant + " !");
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return List.of();
        }
        var main = player.getInventory().getItemInMainHand();
        if (main.getType() == Material.AIR) {
            return List.of();
        }
        return new GardensEnchantHandler(main).getEnchantments()
                .stream()
                .map(enchantmentInstance -> enchantmentInstance.getEnchantment().getId())
                .toList();
    }
}
