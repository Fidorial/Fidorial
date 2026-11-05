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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class HexColorArgument<T> implements ArgumentType<T> {

    public static final DynamicCommandExceptionType ERROR_INVALID_HEX = ExceptionFactory.dynamic("argument.hexcolor.invalid");

    private final Function<Integer, T> converter;

    private HexColorArgument(final Function<Integer, T> converter) {
        this.converter = converter;
    }

    public static HexColorArgument<Integer> hexColor() {
        return hexColor(Function.identity());
    }

    public static <T> HexColorArgument<T> hexColor(final Function<Integer, T> converter) {
        return new HexColorArgument<>(converter);
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        final String colorString = reader.readUnquotedString();

        final int rgb = switch (colorString.length()) {
            case 3 ->
                    rgb(
                            duplicate(hexDigit(colorString, 0)),
                            duplicate(hexDigit(colorString, 1)),
                            duplicate(hexDigit(colorString, 2)));
            case 6 ->
                    rgb(
                            Integer.parseInt(colorString.substring(0, 2), 16),
                            Integer.parseInt(colorString.substring(2, 4), 16),
                            Integer.parseInt(colorString.substring(4, 6), 16));
            default -> throw ERROR_INVALID_HEX.createWithContext(reader, colorString);
        };

        return converter.apply(rgb);
    }

    private static int hexDigit(final String s, final int index) {
        return Integer.parseInt(s.substring(index, index + 1), 16);
    }

    private static int duplicate(final int digit) {
        return digit * 17;
    }

    private static int rgb(final int r, final int g, final int b) {
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context,
            final SuggestionsBuilder builder
    ) {
        builder.suggest("FFFFFF");
        builder.suggest("D6BCFB");
        return builder.buildFuture();
    }

    public static final class Info implements ArgumentTypeRegistrar<HexColorArgument<?>, Info.Spec> {

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
        public Spec access(final HexColorArgument<?> argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<HexColorArgument<?>> {
            @Override
            public HexColorArgument<?> instantiate() {
                return HexColorArgument.hexColor();
            }

            @Override
            public ArgumentTypeRegistrar<HexColorArgument<?>, ?> type() {
                return new Info();
            }
        }
    }
}
