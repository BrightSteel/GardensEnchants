package com.gardensmc.gardensenchants.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveBookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!commandSender.hasPermission("gardens.givebook")) return false;

        if (args.length > 1) {
            String player_name = args[0];
            String enchant = args[1];
            Player player = Bukkit.getPlayer(player_name);
            if (player != null) {
                // todo re-impl
//                ItemStack book = EnchantUtils.getEnchantBook(enchant, 0);
//                player.getInventory().addItem(book);
            }
        }

        return false;
    }
}
