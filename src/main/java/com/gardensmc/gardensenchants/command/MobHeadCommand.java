package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.utils.ItemUtils;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobHeadCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(commandSender instanceof Player player)) return false;
        if (!player.hasPermission("gardens.mob_heads")) return false;

        if (args.length > 0) {
            String type = args[0];
            ItemStack head = ItemUtils.getMobHead(type);
            if (head == null) {
                player.sendMessage(ChatColor.RED + "Could not find a mob head for: " + type);
            }
            else {
                PlayerUtil.giveItem(player, head);
                player.sendMessage(ChatColor.GREEN + "Received " + type + " head!");
            }
        }
        else {
            player.sendMessage(ChatColor.RED + "Error: Please enter mob type");
        }


        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 1) {
            return List.of();
        }
        return ItemUtils.getMobHeadTypes().stream().filter(type -> type.startsWith(strings[0].toLowerCase())).toList();
    }
}
