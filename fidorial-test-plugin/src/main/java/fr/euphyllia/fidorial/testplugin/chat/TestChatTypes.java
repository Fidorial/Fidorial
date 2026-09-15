package fr.euphyllia.fidorial.testplugin.terrain;

import fr.fidorial.chat.ChatTypeDecoration;
import fr.fidorial.chat.ChatTypeDefinition;
import fr.fidorial.chat.ChatTypeRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;

public final class TestChatTypes {

    public static final ChatTypeDefinition ARRIVAL = ChatTypeDefinition
            .builder(Key.key("fidorial", "arrival"))
            .chat(ChatTypeDecoration.builder("%s has joined the server: %s")
                    .parameters("sender", "content")
                    .style(Style.style(NamedTextColor.GREEN, TextDecoration.ITALIC))
                    .build())
            .narration(ChatTypeDecoration.builder("%s has joined and said %s")
                    .parameters("sender", "content")
                    .build())
            .build();

    public static final ChatTypeDefinition DEPARTURE = ChatTypeDefinition
            .builder(Key.key("fidorial", "departure"))
            .chat(ChatTypeDecoration.builder("%s has left the server: %s")
                    .parameters("sender", "content")
                    .style(Style.style(NamedTextColor.YELLOW, TextDecoration.ITALIC))
                    .build())
            .narration(ChatTypeDecoration.builder("%s has left and said %s")
                    .parameters("sender", "content")
                    .build())
            .build();

    public static final ChatTypeDefinition SERVER_WHISPER = ChatTypeDefinition
            .builder(Key.key("fidorial", "server_whisper"))
            .chat(ChatTypeDecoration.builder("[%s -> %s] %s")
                    .parameters("sender", "target", "content")
                    .style(Style.style(NamedTextColor.AQUA))
                    .build())
            .narration(ChatTypeDecoration.builder("%s privately told %s: %s")
                    .parameters("sender", "target", "content")
                    .build())
            .build();

    public static final ChatTypeDefinition ANNOUNCEMENT = ChatTypeDefinition
            .builder(Key.key("fidorial", "announcement"))
            .chat(ChatTypeDecoration.builder("[!] %s")
                    .parameters("content")
                    .style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
                    .build())
            .narration(ChatTypeDecoration.builder("Announcement from %s: %s")
                    .parameters("sender", "content")
                    .build())
            .build();

    public static final ChatTypeDefinition EMOTE = ChatTypeDefinition
            .builder(Key.key("fidorial", "emote"))
            .chat(ChatTypeDecoration.builder("* %s %s")
                    .parameters("sender", "content")
                    .style(Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC))
                    .build())
            .narration(ChatTypeDecoration.builder("%s %s")
                    .parameters("sender", "content")
                    .build())
            .build();

    private static final List<ChatTypeDefinition> ALL = List.of(
            ARRIVAL, DEPARTURE, SERVER_WHISPER, ANNOUNCEMENT, EMOTE);

    private TestChatTypes() {
        throw new UnsupportedOperationException("TestChatTypes cannot be instantiated.");
    }

    public static List<ChatTypeDefinition> all() {
        return ALL;
    }

    public static void registerAll(final ChatTypeRegistry chatTypes, final ComponentLogger logger) {
        for (final ChatTypeDefinition chatType : ALL) {
            chatTypes.overwrite(chatType);
            logger.info("[TestPlugin] chat type {} registered (network id {})",
                    chatType.key().asString(), chatTypes.networkId(chatType.key()));
        }

        logger.info("[TestPlugin] {} chat types registered, {} of them defined by the server",
                chatTypes.totalRegistered(), chatTypes.definitions().size());
    }

    public static void unregisterAll(final ChatTypeRegistry chatTypes) {
        for (final ChatTypeDefinition chatType : ALL) {
            chatTypes.unregister(chatType.key());
        }
    }
}
