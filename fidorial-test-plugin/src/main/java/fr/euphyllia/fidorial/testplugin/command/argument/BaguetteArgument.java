package fr.euphyllia.fidorial.testplugin.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.fidorial.command.serializers.MessageComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static fr.fidorial.command.argument.Arguments.arguments;

public final class BaguetteArgument {

    public enum Baguette {
        TRADITION,
        BANETTE,
        FICELLE,
        EPI,
        RETRODOR,
        MOULEE
    }

    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE =
            new DynamicCommandExceptionType(
                    value -> MessageComponentSerializer.message().serialize(
                            Component.translatable(
                                    "fidorial.baguette.unknown",
                                    Component.text(value.toString())
                            )
                    )
            );

    private BaguetteArgument() {
    }

    public static ArgumentType<Baguette> baguette() {
        return arguments().map(StringArgumentType.word(), BaguetteArgument::parse, BaguetteArgument::suggest);
    }

    private static Baguette parse(final String value, final StringReader reader) throws CommandSyntaxException {
        for (final Baguette baguette : Baguette.values()) {
            if (baguette.name().equalsIgnoreCase(value)) {
                return baguette;
            }
        }
        throw ERROR_INVALID_VALUE.createWithContext(reader, value);
    }

    private static <S> CompletableFuture<Suggestions> suggest(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        for (final Baguette baguette : Baguette.values()) {
            final String name = baguette.name().toLowerCase(Locale.ROOT);
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}
