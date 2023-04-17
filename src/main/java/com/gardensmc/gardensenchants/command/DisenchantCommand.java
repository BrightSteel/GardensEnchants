package com.gardensmc.gardensenchants.command;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class DisenchantCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(commandSender instanceof Player player) || !commandSender.hasPermission("gardens.disenchant")) return false;

        if (args.length > 0) {
            String enchant = args[0];
            ItemStack main = player.getInventory().getItemInMainHand();
            List<Enchantment> enchs = main.getEnchantments().keySet().stream().filter(e -> e.getKey().toString().equalsIgnoreCase(enchant)).toList();

            if (!enchs.isEmpty()) {
                NBTItem nbtItem = new NBTItem(main);
                NBTCompoundList lst = nbtItem.getCompoundList("Enchantments");
                lst.removeIf(comp -> comp.getString("id").equals(enchant));
                player.getInventory().setItemInMainHand(nbtItem.getItem());

                player.sendMessage(ChatColor.GREEN + "Removed " + enchs.get(0).getKey().toString());
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
            return null;
        }

        Set<Enchantment> enchants = player.getInventory().getItemInMainHand().getEnchantments().keySet();
        return enchants.stream().map(e -> e.getKey().toString()).toList();
    }
}
