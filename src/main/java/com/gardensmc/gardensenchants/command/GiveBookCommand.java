package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.books.BooksHandler;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantment;
import com.gardensmc.gardensenchants.enchantment.GardensEnchantments;
import com.gardensmc.gardensenchants.utils.PlayerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

/**
 * /givebook &lt;player&gt; &lt;enchant&gt; [level] - gives a player an enchant book (events, refunds, testing).
 * Works from the console; the level defaults to 1 and must be within the enchant's levels.
 */
public class GiveBookCommand implements TabExecutor {

    private static final String PERMISSION = "gardens.givebook";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            error(sender, "You don't have permission to do that");
            return true;
        }
        if (args.length < 2) {
            error(sender, "Usage: /givebook <player> <enchant> [level]");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            error(sender, "No online player named " + args[0]);
            return true;
        }
        GardensEnchantment enchantment = GardensEnchantments.INSTANCE.getEnchantment(args[1].toLowerCase());
        if (enchantment == null) {
            error(sender, "No enchantment exists with id " + args[1]);
            return true;
        }
        int level;
        try {
            level = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        } catch (NumberFormatException e) {
            level = -1;
        }
        if (level < 1 || level > enchantment.getMaxLevel()) {
            error(sender, "Level must be from 1 to " + enchantment.getMaxLevel() + " for " + enchantment.getId());
            return true;
        }

        var book = BooksHandler.INSTANCE.buildBook(new EnchantmentInstance(enchantment, level)).book();
        PlayerUtil.giveItem(target, book);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                "<green>Gave " + target.getName() + " a " + enchantment.getId() + " " + level + " book"));
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        var typed = args[args.length - 1].toLowerCase();
        List<String> options = switch (args.length) {
            case 1 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            case 2 -> GardensEnchantments.INSTANCE.getEnchantments().stream().map(GardensEnchantment::getId).toList();
            case 3 -> {
                var enchantment = GardensEnchantments.INSTANCE.getEnchantment(args[1].toLowerCase());
                yield enchantment == null ? List.of()
                        : IntStream.rangeClosed(1, enchantment.getMaxLevel()).mapToObj(String::valueOf).toList();
            }
            default -> List.of();
        };
        return options.stream().filter(option -> option.toLowerCase().startsWith(typed)).toList();
    }

    private static void error(CommandSender sender, String message) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>" + message));
    }
}
