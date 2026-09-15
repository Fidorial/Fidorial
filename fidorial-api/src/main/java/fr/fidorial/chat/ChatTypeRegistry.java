package fr.fidorial.chat;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChatType;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Optional;

/**
 * Server-wide registry of chat types, holding both the vanilla ones and those added by plugins.
 *
 * @since 0.1.0
 */
public interface ChatTypeRegistry {

    /**
     * Registers a new chat type.
     *
     * @param definition the chat type to add
     * @return the registered definition
     * @throws IllegalStateException if a chat type is already registered under the same key; use
     *                               {@link #overwrite(ChatTypeDefinition)} to replace it on purpose
     * @since 0.1.0
     */
    @Contract("_ -> param1")
    ChatTypeDefinition register(ChatTypeDefinition definition);

    /**
     * Builds and registers a new chat type.
     *
     * @param builder the builder describing the chat type
     * @return the registered definition
     * @throws IllegalStateException if a chat type is already registered under the same key
     * @since 0.1.0
     */
    default ChatTypeDefinition register(final ChatTypeBuilder builder) {
        return register(builder.build());
    }

    /**
     * Reads a chat type from its data pack JSON representation and registers it.
     *
     * @param key  the key to register the chat type under
     * @param json the JSON object describing the chat type
     * @return the registered definition
     * @throws IllegalArgumentException if the JSON is malformed or misses a mandatory field
     * @throws IllegalStateException    if a chat type is already registered under the same key
     * @since 0.1.0
     */
    ChatTypeDefinition registerFromJson(Key key, String json);

    /**
     * Registers a chat type, replacing any existing one sharing its key.
     *
     * @param definition the chat type to add or replace
     * @return the definition previously registered under that key, if any
     * @since 0.1.0
     */
    Optional<ChatTypeDefinition> overwrite(ChatTypeDefinition definition);

    /**
     * Removes a chat type from the registry.
     *
     * @param key the key of the chat type to remove
     * @return {@code true} if a chat type was removed, {@code false} if none was registered
     * @since 0.1.0
     */
    boolean unregister(Key key);

    /**
     * Removes a chat type from the registry.
     *
     * @param key the typed key of the chat type to remove
     * @return {@code true} if a chat type was removed, {@code false} if none was registered
     * @since 0.1.0
     */
    default boolean unregister(final TypedKey<ChatType> key) {
        return unregister(key.key());
    }

    /**
     * {@return the definition registered under {@code key}, if that chat type carries one}
     *
     * @param key the chat type key
     * @since 0.1.0
     */
    Optional<ChatTypeDefinition> definition(Key key);

    /**
     * {@return whether a chat type is registered under {@code key}}
     *
     * @param key the chat type key
     * @since 0.1.0
     */
    boolean contains(Key key);

    /**
     * {@return whether {@code key} maps to a chat type defined by this server rather than a
     * stock vanilla one}
     *
     * @param key the chat type key
     * @since 0.1.0
     */
    boolean isCustom(Key key);

    /**
     * {@return every registered chat type key, in network order}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<Key> keys();

    /**
     * {@return every chat type defined by this server, vanilla overrides included}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<ChatTypeDefinition> definitions();

    /**
     * {@return the network identifier of {@code key}, or {@code -1} if it is not registered}
     *
     * @param key the chat type key
     * @since 0.1.0
     */
    int networkId(Key key);

    /**
     * {@return the number of registered chat types}
     *
     * @since 0.1.0
     */
    int totalRegistered();
}
