package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        var enchantmentName = args[0].toLowerCase();
        var enchantment = GardensEnchantments.INSTANCE.getEnchantment(enchantmentName);
        if (enchantment == null) {
            player.sendMessage("No enchantment exists with id " + enchantmentName);
            return true;
        }
        ItemStack stack = BooksHandler.INSTANCE.buildBook(new EnchantmentInstance(enchantment, 1)).book();
        PlayerUtil.giveItem(player, stack);
        player.sendMessage(ChatColor.GREEN + "Recieved " + enchantmentName + " book");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return GardensEnchantments.INSTANCE.getEnchantments().stream().map(GardensEnchantment::getId).toList();
    }
}
