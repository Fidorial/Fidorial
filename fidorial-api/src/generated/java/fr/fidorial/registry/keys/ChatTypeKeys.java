package fr.fidorial.registry.keys;

import fr.fidorial.chat.ChatTypeDefinition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:chat_type} registry.
 */
public final class ChatTypeKeys {
    /**
     * Key for {@code minecraft:chat}.
     */
    public static final TypedKey<ChatTypeDefinition> CHAT = create("chat");

    /**
     * Key for {@code minecraft:emote_command}.
     */
    public static final TypedKey<ChatTypeDefinition> EMOTE_COMMAND = create("emote_command");

    /**
     * Key for {@code minecraft:msg_command_incoming}.
     */
    public static final TypedKey<ChatTypeDefinition> MSG_COMMAND_INCOMING = create("msg_command_incoming");

    /**
     * Key for {@code minecraft:msg_command_outgoing}.
     */
    public static final TypedKey<ChatTypeDefinition> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");

    /**
     * Key for {@code minecraft:say_command}.
     */
    public static final TypedKey<ChatTypeDefinition> SAY_COMMAND = create("say_command");

    /**
     * Key for {@code minecraft:team_msg_command_incoming}.
     */
    public static final TypedKey<ChatTypeDefinition> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");

    /**
     * Key for {@code minecraft:team_msg_command_outgoing}.
     */
    public static final TypedKey<ChatTypeDefinition> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");

    private static final List<TypedKey<ChatTypeDefinition>> VALUES = List.of(
        CHAT,
        EMOTE_COMMAND,
        MSG_COMMAND_INCOMING,
        MSG_COMMAND_OUTGOING,
        SAY_COMMAND,
        TEAM_MSG_COMMAND_INCOMING,
        TEAM_MSG_COMMAND_OUTGOING
    );

    private ChatTypeKeys() {
        throw new UnsupportedOperationException("ChatTypeKeys cannot be instantiated.");
    }

    private static TypedKey<ChatTypeDefinition> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.CHAT_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<ChatTypeDefinition>> values() {
        return VALUES.stream();
    }
}
