package fr.fidorial.chat;

import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * How a chat message is formatted for the chat log and/or for narration/accessibility.
 *
 * @param translationKey the translation key used to format the message, supporting placeholders
 * @param parameters     which named slots ({@code "sender"}, {@code "target"}, {@code "content"})
 *                       are substituted into the translation, in order
 * @param style          the style applied to the formatted result, or {@code null} for none
 * @apiNote the translation key is resolved purely client-side using language files from resource packs; fallbacks to plaintext with resolved placeholders when it can't be found.
 * @since 0.1.0
 */
public record ChatTypeDecoration(String translationKey, List<String> parameters, @Nullable Style style) {

    public ChatTypeDecoration {
        Objects.requireNonNull(translationKey, "translationKey");
        parameters = List.copyOf(parameters);
    }

    /**
     * {@return a new builder for the decoration identified by {@code translationKey}}
     *
     * @param translationKey the translation key used to format the message, supporting placeholders
     * @apiNote the translation key is resolved purely client-side using language files from resource packs; fallbacks to plaintext with resolved placeholders when it can't be found.
     */
    @Contract(value = "_ -> new", pure = true)
    public static Builder builder(final String translationKey) {
        return new Builder(translationKey);
    }

    /**
     * {@return a new builder pre-filled with the values of {@code decoration}}
     *
     * @param decoration the decoration to copy
     */
    @Contract(value = "_ -> new", pure = true)
    public static Builder builder(final ChatTypeDecoration decoration) {
        return new Builder(decoration);
    }

    /**
     * Fluent builder for {@link ChatTypeDecoration}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private String translationKey;
        private List<String> parameters = List.of();
        private @Nullable Style style;

        private Builder(final String translationKey) {
            this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
        }

        private Builder(final ChatTypeDecoration decoration) {
            this.translationKey = decoration.translationKey;
            this.parameters = decoration.parameters;
            this.style = decoration.style;
        }

        /**
         * @param translationKey the translation key used to format the message, supporting placeholders
         * @apiNote the translation key is resolved purely client-side using language files from resource packs; fallbacks to plaintext with resolved placeholders when it can't be found.
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder translationKey(final String translationKey) {
            this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
            return this;
        }

        /**
         * @param parameters which named slots are substituted into the translation, in order
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder parameters(final String... parameters) {
            this.parameters = List.of(parameters);
            return this;
        }

        /**
         * @param parameters which named slots are substituted into the translation, in order
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder parameters(final List<String> parameters) {
            this.parameters = List.copyOf(parameters);
            return this;
        }

        /**
         * @param style the style applied to the formatted result, or {@code null} for none
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder style(final @Nullable Style style) {
            this.style = style;
            return this;
        }

        /**
         * {@return the immutable decoration described by this builder}
         */
        @Contract(value = "-> new", pure = true)
        public ChatTypeDecoration build() {
            return new ChatTypeDecoration(translationKey, parameters, style);
        }
    }
}
