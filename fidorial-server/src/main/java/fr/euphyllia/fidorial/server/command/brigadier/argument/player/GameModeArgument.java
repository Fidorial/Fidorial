package fr.euphyllia.fidorial.server.command.brigadier.argument.player;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.entity.GameMode;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class GameModeArgument implements ArgumentType<GameMode> {

    private static final DynamicCommandExceptionType ERROR_INVALID = ExceptionFactory.dynamic("argument.gamemode.invalid");

    public static GameModeArgument gameMode() {
        return new GameModeArgument();
    }

    public GameModeArgument() {
    }

    @Override
    public GameMode parse(final StringReader reader) throws CommandSyntaxException {
        final String input = reader.readUnquotedString();

        final GameMode mode = GameMode.byName(input);
        if (mode == null) {
            throw ERROR_INVALID.createWithContext(reader, input);
        }

        return mode;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (final GameMode mode : GameMode.values()) {
            final String name = mode.name().toLowerCase(Locale.ROOT);
            if (name.startsWith(remaining)) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }

    public static final class Info implements ArgumentTypeRegistrar<GameModeArgument, Info.Spec> {

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
        public Spec access(final GameModeArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<GameModeArgument> {
            @Override
            public GameModeArgument instantiate() {
                return new GameModeArgument();
            }

            @Override
            public ArgumentTypeRegistrar<GameModeArgument, ?> type() {
                return new Info();
            }
        }
    }
}
