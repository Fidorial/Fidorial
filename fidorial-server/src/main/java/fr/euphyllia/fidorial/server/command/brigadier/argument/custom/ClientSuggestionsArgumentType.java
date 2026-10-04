package fr.euphyllia.fidorial.server.command.brigadier.argument.custom;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class ClientSuggestionsArgumentType<T> implements ArgumentType<T>, ClientSuggestions {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ClientSuggestionsArgumentType.class);

    private final ArgumentType<T> delegate;
    private final Key suggestionSource;
    private final @Nullable SuggestionProvider<CommandSource> fallbackSuggestions;

    public ClientSuggestionsArgumentType(final ArgumentType<T> delegate, final Key suggestionSource, final @Nullable SuggestionProvider<CommandSource> fallbackSuggestions) {
        this.delegate = delegate;
        this.suggestionSource = suggestionSource;
        this.fallbackSuggestions = fallbackSuggestions;
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        return delegate.parse(reader);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        if (fallbackSuggestions != null && context.getSource() instanceof CommandSource) {
            try {
                return fallbackSuggestions.getSuggestions((CommandContext<CommandSource>) context, builder);
            } catch (final CommandSyntaxException e) {
                LOGGER.warn("Fallback suggestions for client-suggested argument '{}' threw while computing suggestions", suggestionSource.asString(), e);
                return Suggestions.empty();
            }
        }
        return delegate.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return delegate.getExamples();
    }

    @Override
    public Key suggestionSource() {
        return suggestionSource;
    }

    public ArgumentType<T> delegate() {
        return delegate;
    }

    public static final class Info implements ArgumentTypeRegistrar<ClientSuggestionsArgumentType<?>, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
            buf.writeVarInt(StringArgumentType.StringType.SINGLE_WORD.ordinal());
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            return new Spec();
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
        }

        @Override
        public Spec access(final ClientSuggestionsArgumentType<?> argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<ClientSuggestionsArgumentType<?>> {

            @Override
            public ClientSuggestionsArgumentType<?> instantiate() {
                throw new UnsupportedOperationException(
                        "ClientSuggestionsArgumentType cannot be reconstructed from network data");
            }

            @Override
            public ArgumentTypeRegistrar<ClientSuggestionsArgumentType<?>, ?> type() {
                return new Info();
            }
        }
    }
}
