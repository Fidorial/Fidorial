package fr.fidorial.chat;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link ChatTypeDefinition}.
 *
 * @since 0.1.0
 */
public final class ChatTypeBuilder {

    private Key key;
    private ChatTypeDecoration chat = ChatTypeDecoration.builder("chat.type.text")
            .parameters("sender", "content")
            .build();
    private ChatTypeDecoration narration = ChatTypeDecoration.builder("chat.type.text.narrate")
            .parameters("sender", "content")
            .build();

    ChatTypeBuilder(final Key key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    ChatTypeBuilder(final ChatTypeDefinition definition) {
        this.key = definition.key();
        this.chat = definition.chat();
        this.narration = definition.narration();
    }

    /**
     * @param key the namespaced identifier of the chat type
     * @return this builder
     */
    @Contract("_ -> this")
    public ChatTypeBuilder key(final Key key) {
        this.key = Objects.requireNonNull(key, "key");
        return this;
    }

    /**
     * @param chat how the message is shown in the chat log
     * @return this builder
     */
    @Contract("_ -> this")
    public ChatTypeBuilder chat(final ChatTypeDecoration chat) {
        this.chat = Objects.requireNonNull(chat, "chat");
        return this;
    }

    /**
     * Configures the chat-log decoration in place, starting from what is already set.
     *
     * @param configurer callback receiving a pre-filled decoration builder
     * @return this builder
     */
    @Contract("_ -> this")
    public ChatTypeBuilder chat(final Consumer<ChatTypeDecoration.Builder> configurer) {
        final ChatTypeDecoration.Builder builder = ChatTypeDecoration.builder(this.chat);
        configurer.accept(builder);
        this.chat = builder.build();
        return this;
    }

    /**
     * @param narration how the message is read out for narration/accessibility
     * @return this builder
     */
    @Contract("_ -> this")
    public ChatTypeBuilder narration(final ChatTypeDecoration narration) {
        this.narration = Objects.requireNonNull(narration, "narration");
        return this;
    }

    /**
     * Configures the narration decoration in place, starting from what is already set.
     *
     * @param configurer callback receiving a pre-filled decoration builder
     * @return this builder
     */
    @Contract("_ -> this")
    public ChatTypeBuilder narration(final Consumer<ChatTypeDecoration.Builder> configurer) {
        final ChatTypeDecoration.Builder builder = ChatTypeDecoration.builder(this.narration);
        configurer.accept(builder);
        this.narration = builder.build();
        return this;
    }

    /**
     * Shorthand: sets both {@link #chat(ChatTypeDecoration)} and
     * {@link #narration(ChatTypeDecoration)} to identical decorations built from
     * {@code translationKey} and {@code parameters}, with no style.
     *
     * <p>Matches vanilla's {@code emote_command} chat type, which narrates itself
     * the same way it's shown in chat.</p>
     *
     * @param translationKey the translation key used to format the message, supporting placeholders
     * @param parameters     which named slots are substituted into the translation, in order
     * @apiNote the translation key is resolved purely client-side using language files from resource packs; fallbacks to plaintext with resolved placeholders when it can't be found.
     * @return this builder
     */
    @Contract("_, _ -> this")
    public ChatTypeBuilder sameForBoth(final String translationKey, final List<String> parameters) {
        final ChatTypeDecoration decoration = ChatTypeDecoration.builder(translationKey)
                .parameters(parameters)
                .build();
        this.chat = decoration;
        this.narration = decoration;
        return this;
    }

    /**
     * {@return the immutable definition described by this builder}
     */
    @Contract(value = "-> new", pure = true)
    public ChatTypeDefinition build() {
        return new ChatTypeDefinition(key, chat, narration);
    }
}
