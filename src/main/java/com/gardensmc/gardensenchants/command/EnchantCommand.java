package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.enchantment.VanillaEnchantment;
import com.gardensmc.gardensenchants.gui.EnchantingMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnchantCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!commandSender.hasPermission("gardens.getbook") || !(commandSender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            if (player.hasPermission("gardens.enchant.open")) {
                new EnchantingMenu(player).open();
            } else {
                player.sendMessage(ChatColor.RED + "No permission to use this command");
            }
            return true;
        }

        var enchantName = args[0].toLowerCase();
        var enchant = GardensEnchantments.INSTANCE.getEnchantment(enchantName);
        if (enchant == null) {
            player.sendMessage(ChatColor.RED + "Could not find the enchant " + enchantName);
            return true;
        }

        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Second argument should be a number");
                return true;
            }
        }

        var force = args.length >= 3 && args[2].equalsIgnoreCase("force");
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            if (!force && !enchant.canEnchant(item)) {
                player.sendMessage(ChatColor.RED + "You cannot apply " + enchantName + " to this item!");
                return true;
            }
            new GardensEnchantHandler(item).enchant(enchant, level, force);
            player.sendMessage(ChatColor.GREEN + "Added " + enchantName + " to your item");
        }
        else {
            player.sendMessage(ChatColor.RED + "Cannot enchant air!");
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 2) {
            return List.of("level");
        }
        if (args.length == 3) {
            return List.of("force");
        }

        return GardensEnchantments.INSTANCE.getEnchantments().stream().map(GardensEnchantment::getId).toList();
    }
}
