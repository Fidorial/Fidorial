package fr.euphyllia.fidorial.server.command.brigadier.argument.generic;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.annotation.SinceMinecraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record TimeArgument(int minimum, int maximum) implements ArgumentType<Integer> {

    private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = ExceptionFactory.simple("argument.time.invalid_unit");
    private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_LOW = ExceptionFactory.dynamic2Reversed("argument.time.tick_count_too_low");
    private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_HIGH = ExceptionFactory.dynamic2Reversed("argument.time.tick_count_too_high");

    private static final Map<String, Integer> UNITS = new LinkedHashMap<>();

    static {
        UNITS.put("d", 24000);
        UNITS.put("s", 20);
        UNITS.put("t", 1);
        UNITS.put("", 1);
    }

    public static TimeArgument time() {
        return new TimeArgument(0, Integer.MAX_VALUE);
    }

    public static TimeArgument time(final int minimum) {
        return new TimeArgument(minimum, Integer.MAX_VALUE);
    }

    @SinceMinecraft("26.4-snapshot-7")
    public static TimeArgument time(final int minimum, final int maximum) {
        return new TimeArgument(minimum, maximum);
    }

    @Override
    public Integer parse(final StringReader reader) throws CommandSyntaxException {

        final double value = reader.readDouble();

        final String unit = reader.readUnquotedString();

        final int factor = UNITS.getOrDefault(unit, 0);

        if (factor == 0) {
            throw ERROR_INVALID_UNIT.createWithContext(reader);
        }

        final long ticks = Math.round(value * factor);

        if (ticks < minimum) {
            throw ERROR_TICK_COUNT_TOO_LOW.createWithContext(reader, ticks, minimum);
        }

        if (ticks > maximum) {
            throw ERROR_TICK_COUNT_TOO_HIGH.createWithContext(reader, ticks, maximum);
        }

        return (int) ticks;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final StringReader reader = new StringReader(builder.getRemaining());

        try {
            reader.readDouble();
        } catch (final CommandSyntaxException exception) {
            return builder.buildFuture();
        }

        final SuggestionsBuilder offset = builder.createOffset(builder.getStart() + reader.getCursor());

        for (final String unit : UNITS.keySet()) {
            offset.suggest(unit);
        }

        return offset.buildFuture();
    }

    public static final class Info implements ArgumentTypeRegistrar<TimeArgument, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
            buf.writeInt(spec.minimum());
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            return new Spec(buf.readInt());
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
            json.addProperty("min", spec.minimum());
        }

        @Override
        public Spec access(final TimeArgument argument) {
            return new Spec(argument.minimum());
        }

        public record Spec(int minimum) implements ArgumentTypeRegistrar.Spec<TimeArgument> {

            @Override
            public TimeArgument instantiate() {
                return TimeArgument.time(minimum);
            }

            @Override
            public ArgumentTypeRegistrar<TimeArgument, ?> type() {
                return new Info();
            }
        }
    }
}
