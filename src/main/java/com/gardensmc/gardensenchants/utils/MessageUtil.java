package com.gardensmc.gardensenchants.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class MessageUtil {

    public static String readableId(String id) {
        var capitalized = Arrays
                .stream(id.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .toList();
        return String.join(" ", capitalized);
    }

    public static String parseIntoLegacy(String message) {
        var mm = MiniMessage.miniMessage();
        var component = mm.deserialize(message);
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static Component parseIntoComponent(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component fromMiniMessage(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static String toMiniMessage(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static boolean isBlank(Component component) {
        var str = PlainTextComponentSerializer.plainText().serialize(component);
        return str.isBlank();
    }

    public static void sendMessage(CommandSender user, String message) {
        sendMessage(user, MessageUtil.parseIntoComponent(message));
    }

    public static void sendError(CommandSender user, String message) {
        sendError(user, MessageUtil.parseIntoComponent(message));
    }

    public static void sendMessage(CommandSender user, Component message) {
        user.sendMessage(Component.text()
                .content("[GardensEnchants] ")
                .color(NamedTextColor.GREEN)
                .append(message)
                .build()
        );
    }

    public static void sendError(CommandSender user, Component message) {
        user.sendMessage(Component.text()
                .content("[GardensEnchants] ")
                .color(NamedTextColor.RED)
                .append(message)
                .build()
        );
    }

    public static String toPlainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}
