package com.gardensmc.gardensenchants.command;

import com.gardensmc.gardensenchants.enchantment.GardensEnchantHandler;
import com.gardensmc.gardensenchants.utils.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.gardensmc.gardensenchants.utils.MessageUtil.sendError;
import static com.gardensmc.gardensenchants.utils.MessageUtil.sendMessage;

public class LoreCommand implements TabExecutor {

    // bounds the markup (colour/gradient tags) around the 50 visible characters
    private static final int MAX_RAW_LORE_LENGTH = 256;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        if (!player.hasPermission("gardens.editlore")) {
            sendError(player, "You don't have permission to use this command!");
            return true;
        }
        var heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            sendError(player, "You must be holding an item to modify its lore");
        } else if (heldItem.getType() == Material.ENCHANTED_BOOK) {
            sendError(player, "Cannot modify lore of enchanted books");
        } else if (strings.length == 0) {
            sendError(player, "Incorrect arguments, should be /lore <add/remove/clear>");
        } else {
            switch (strings[0].toLowerCase()) {
                case "add" -> addLore(player, heldItem, strings);
                case "remove" -> removeLore(player, heldItem, strings);
                case "clear" -> clearLore(player, heldItem);
            }
        }
        return true;
    }

    private void addLore(Player player, ItemStack itemStack, String[] strings) {
        if (strings.length < 2) {
            sendError(player, "Please include a message to add to lore");
            return;
        }
        var text = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));
        var line = MessageUtil.fromPlayerInput(text);
        if (text.length() > MAX_RAW_LORE_LENGTH || MessageUtil.toPlainText(line).length() >= 50) {
            sendError(player, "Lore is too long!");
            return;
        }
        var enchantHandler = new GardensEnchantHandler(itemStack);
        if (enchantHandler.hasEnchantNBT()) {
            if (enchantHandler.getCustomLore().size() >= 5) {
                sendError(player, "Cannot add more than 5 lines of lore!");
                return;
            }
            // store the sanitised line, since custom lore is re-read with the full MiniMessage parser
            enchantHandler.addCustomLore(MessageUtil.toMiniMessage(line));
        } else {
            var meta = itemStack.getItemMeta();
            if (meta.hasLore() && meta.lore().size() >= 5) {
                sendError(player, "Cannot add more than 5 lines of lore!");
                return;
            }
            addLore(itemStack, line);
        }
        sendMessage(player, Component.text().content("Added lore: ").append(line).build());
    }

    private void addLore(ItemStack itemStack, Component line) {
        var meta = itemStack.getItemMeta();
        var lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .append(line)
                .build());
        meta.lore(lore);
        itemStack.setItemMeta(meta);
    }

    private void removeLore(Player player, ItemStack itemStack, String[] strings) {
        int line = -1;
        if (strings.length > 1) {
            try {
                line = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                sendError(player, "Second argument must be a line number");
                return;
            }
        }
        var enchantHandler = new GardensEnchantHandler(itemStack);
        int loreSize = getLoreSize(enchantHandler, itemStack);
        if (loreSize == 0) {
            sendError(player, "This item has no lore!");
            return;
        }
        if (line != -1 && (line < 0 || line >= loreSize)) {
            sendError(player, "Line number must be between 0 and " + (loreSize - 1));
            return;
        }
        Component loreRemoved;
        if (enchantHandler.hasEnchantNBT()) {
            loreRemoved = MessageUtil.fromMiniMessage(enchantHandler.removeCustomLore(line));
        } else {
            loreRemoved = removeLore(itemStack, line);
            if (loreRemoved == null) {
                sendError(player, "This item has no lore!");
                return;
            }
        }
        sendMessage(player, Component.text().content("Removed lore: ").append(loreRemoved).build());
    }

    // the lines /lore works on: NBT custom lore on enchanted items, plain lore otherwise
    private int getLoreSize(GardensEnchantHandler enchantHandler, ItemStack itemStack) {
        if (enchantHandler.hasEnchantNBT()) {
            return enchantHandler.getCustomLore().size();
        }
        var lore = itemStack.getItemMeta().lore();
        return lore == null ? 0 : lore.size();
    }

    private Component removeLore(ItemStack itemStack, int line) {
        var meta = itemStack.getItemMeta();
        var lore = meta.lore();
        if (lore == null) {
            return null;
        } else {
            if (line == -1) {
                // default to last lore line
                line = lore.size() - 1;
            }
            var component = lore.get(line);
            lore.remove(line);
            meta.lore(lore);
            itemStack.setItemMeta(meta);
            return component;
        }
    }

    private void clearLore(Player player, ItemStack itemStack) {
        var enchantHandler = new GardensEnchantHandler(itemStack);
        if (enchantHandler.hasEnchantNBT()) {
            enchantHandler.removeCustomLore();
        } else {
            var meta = itemStack.getItemMeta();
            var lore = meta.lore();
            if (lore == null) {
                sendError(player, "This item has no lore!");
            } else {
                lore.clear();
                meta.lore(lore);
                itemStack.setItemMeta(meta);
            }
        }
        sendMessage(player, "Cleared lore from held item");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return Collections.emptyList();
        }
        var arg = strings[0];
        if (arg.isEmpty()) {
            return List.of("add", "remove", "clear");
        }
        if (strings.length == 1) {
            return Stream.of("add", "remove", "clear").filter(s1 -> s1.startsWith(arg)).toList();
        }
        if (strings.length == 2) {
            if (arg.equalsIgnoreCase("add")) {
                return List.of("<lore>");
            }
            if (arg.equalsIgnoreCase("remove")) {
                var heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType() != Material.AIR) {
                    int loreSize = getLoreSize(new GardensEnchantHandler(heldItem), heldItem);
                    var indices = new ArrayList<String>();
                    for (int i = 0; i < loreSize; i++) {
                        indices.add(String.valueOf(i));
                    }
                    return indices;
                }
            }
        }
        return Collections.emptyList();
    }
}
