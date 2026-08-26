package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.listener.VillagerListener;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FixBookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(commandSender instanceof Player player) || !commandSender.hasPermission("gardens.fixbook")) {
            return false;
        }

        var held = PlayerUtil.getHeldItem(player);
        if (held == null || held.getType() != Material.ENCHANTED_BOOK) {
            player.sendMessage(ChatColor.RED + "You must be holding a vanilla enchanted book!");
            return true;
        }

        var customBook = VillagerListener.getCustomEnchantBook(held);
        if (customBook == held) {
            // getCustomEnchantBook returns the original when there is no custom equivalent
            player.sendMessage(ChatColor.RED + "This book has no custom enchant equivalent!");
            return true;
        }

        customBook.setAmount(held.getAmount());
        player.getInventory().setItemInMainHand(customBook);
        player.sendMessage(ChatColor.GREEN + "Replaced vanilla book with custom book");
        return true;
    }
}
