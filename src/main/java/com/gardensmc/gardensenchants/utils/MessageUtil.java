package com.gardensmc.gardensenchants.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class MessageUtil {

    // Text typed by players (anvil renames, /lore): colours and basic formatting only. Fonts, glyph
    // tags, obfuscation, click/hover etc. stay literal so items can't spoof rarity tags or UI glyphs.
    private static final MiniMessage PLAYER_MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(TextDecoration.BOLD),
                    StandardTags.decorations(TextDecoration.ITALIC),
                    StandardTags.decorations(TextDecoration.UNDERLINED),
                    StandardTags.decorations(TextDecoration.STRIKETHROUGH),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.reset()
            ))
            .build();

    public static Component fromPlayerInput(String message) {
        return PLAYER_MINI_MESSAGE.deserialize(message);
    }

    public static String readableId(String id) {
        var capitalized = Arrays
                .stream(id.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .toList();
        return String.join(" ", capitalized);
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
