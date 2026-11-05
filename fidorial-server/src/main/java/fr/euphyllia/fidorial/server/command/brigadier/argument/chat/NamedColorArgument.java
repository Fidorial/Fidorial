package fr.euphyllia.fidorial.server.command.brigadier.argument.chat;

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
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.CompletableFuture;

public final class NamedColorArgument implements ArgumentType<NamedTextColor> {

    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = ExceptionFactory.dynamic("argument.color.invalid");

    public static NamedColorArgument namedColor() {
        return new NamedColorArgument();
    }

    @Override
    public NamedTextColor parse(final StringReader reader) throws CommandSyntaxException {
        final String id = reader.readUnquotedString();
        final NamedTextColor result = NamedTextColor.NAMES.value(id);
        if (result == null) {
            throw ERROR_INVALID_VALUE.createWithContext(reader, id);
        }
        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (final NamedTextColor color : NamedTextColor.NAMES.values()) {
            builder.suggest(NamedTextColor.NAMES.key(color));
        }
        return builder.buildFuture();
    }

    public static final class Info implements ArgumentTypeRegistrar<NamedColorArgument, Info.Spec> {

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
        public Spec access(final NamedColorArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<NamedColorArgument> {
            @Override
            public NamedColorArgument instantiate() {
                return NamedColorArgument.namedColor();
            }

            @Override
            public ArgumentTypeRegistrar<NamedColorArgument, ?> type() {
                return new Info();
            }
        }
    }
}
