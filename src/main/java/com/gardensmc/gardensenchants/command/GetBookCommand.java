package com.gardensmc.gardensenchants.command;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GetBookCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!commandSender.hasPermission("gardens.getbook") || !(commandSender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            return false;
        }

        ItemStack stack = null; // todo re-impl
//        ItemStack stack = EnchantUtils.getEnchantBook(args[0]);
        if (stack == null) {
            player.sendMessage(ChatColor.RED + "Could not find a valid book by that name");
        }
        else {
            player.getInventory().addItem(stack);
            player.sendMessage(ChatColor.GREEN + "Enchantment received");
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        List<String> values = new ArrayList<>();

        for (Enchantment enchantment : Enchantment.values()) {
            String key = enchantment.getKey().getKey();
            values.add(key);
        }

        return values;
    }
}
