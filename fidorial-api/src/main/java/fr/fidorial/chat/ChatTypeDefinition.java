package fr.fidorial.chat;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChatType;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * A complete chat type definition, ready to be sent to clients.
 *
 * @param key       the namespaced identifier of the chat type
 * @param chat      how the message is shown in the chat log
 * @param narration how the message is read out for narration/accessibility
 * @since 0.1.0
 */
public record ChatTypeDefinition(
        Key key,
        ChatTypeDecoration chat,
        ChatTypeDecoration narration
) implements ChatType, net.kyori.adventure.chat.ChatType {

    public ChatTypeDefinition {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(chat, "chat");
        Objects.requireNonNull(narration, "narration");
    }

    /**
     * {@return a new builder for the chat type identified by {@code key}}
     *
     * @param key the namespaced identifier of the chat type
     */
    @Contract(value = "_ -> new", pure = true)
    public static ChatTypeBuilder builder(final Key key) {
        return new ChatTypeBuilder(key);
    }

    /**
     * {@return a new builder pre-filled with the values of {@code definition}}
     *
     * @param definition the definition to copy
     */
    @Contract(value = "_ -> new", pure = true)
    public static ChatTypeBuilder builder(final ChatTypeDefinition definition) {
        return new ChatTypeBuilder(definition);
    }

    /**
     * {@return this chat type's key, typed against the {@code minecraft:chat_type} registry}
     */
    @Contract(value = "-> new", pure = true)
    public TypedKey<fr.fidorial.registry.data.ChatType> typedKey() {
        return TypedKey.create(RegistryKey.CHAT_TYPE, key);
    }
}
