package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.enchants.CustomEnchantment;
import com.gardensmc.gardensenchants.enchants.CustomEnchantments;
import com.gardensmc.gardensenchants.utils.ChatUtils;
import com.gardensmc.gardensenchants.utils.EnchantUtils;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnchantCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!commandSender.hasPermission("gardens.getbook") || !(commandSender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Please specify which enchant you'd like to apply");
            return true;
        }

        CustomEnchantment enchant = CustomEnchantments.getCustomEnchant(args[0]);
        if (enchant == null) {
            player.sendMessage(ChatColor.RED + "Could not find the enchant " + args[0]);
            return true;
        }

        int level = 1;
        if (args.length == 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Second argument should be a number");
                return true;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            if (!enchant.canEnchantItem(item)) {
                player.sendMessage(ChatColor.RED + "You cannot apply " + args[0] + " to this item!");
                return true;
            }
            item.addEnchantment(enchant, level);
            NBTItem nbtItem = new NBTItem(item);
            if (!nbtItem.hasKey("slots")) {
                nbtItem.setInteger("slots", 4);
            }
            EnchantUtils.updateLore(nbtItem.getItem());
            player.sendMessage(ChatColor.GREEN + "Added " + args[0] + " to your item");
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
            return List.of("Level");
        }

        List<String> values = new ArrayList<>();

        for (Enchantment enchantment : Enchantment.values()) {
            String key = enchantment.getKey().getKey();
            values.add(key);
        }

        return values;
    }
}
