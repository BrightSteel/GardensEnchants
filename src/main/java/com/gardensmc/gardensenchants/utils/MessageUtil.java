package com.gardensmc.gardensenchants.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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

    public static boolean isBlank(Component component) {
        var str = PlainTextComponentSerializer.plainText().serialize(component);
        return str.isBlank();
    }

}
