package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.KeyReader;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class DimensionArgument<T> implements ArgumentType<T> {

    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = ExceptionFactory.dynamic("argument.dimension.invalid");

    private final Function<Key, T> converter;

    private DimensionArgument(final Function<Key, T> converter) {
        this.converter = converter;
    }

    public static DimensionArgument<Key> dimension() {
        return dimension(Function.identity());
    }

    public static <T> DimensionArgument<T> dimension(final Function<Key, T> converter) {
        return new DimensionArgument<>(converter);
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final Key key = KeyReader.readKey(reader);

        final T result = converter.apply(key);
        if (result == null) {
            reader.setCursor(start);
            throw ERROR_INVALID_VALUE.createWithContext(reader, key.asString());
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemainingLowerCase();
        for (final World world : FidorialServer.getInstance().worlds().all()) {
            final Key key = world.key();
            if (key.asString().startsWith(remaining) || key.value().startsWith(remaining)) {
                builder.suggest(key.asString());
            }
        }
        return builder.buildFuture();
    }

    public static final class Info implements ArgumentTypeRegistrar<DimensionArgument<?>, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            return new Spec();
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
        }

        @Override
        public Spec access(final DimensionArgument<?> argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<DimensionArgument<?>> {
            @Override
            public DimensionArgument<?> instantiate() {
                return DimensionArgument.dimension();
            }

            @Override
            public ArgumentTypeRegistrar<DimensionArgument<?>, ?> type() {
                return new Info();
            }
        }
    }
}
