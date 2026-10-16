package fr.euphyllia.fidorial.server.command.brigadier.argument.generic;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.Arguments;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a positive amount of time written as a chain of amount/unit pairs, such as
 * {@code 30m}, {@code 7d} or {@code 1w12h}.
 *
 * <p>The unit is always required: a bare number would be ambiguous between ticks, seconds and
 * minutes depending on who reads it.</p>
 */
public final class DurationArgument {

    /**
     * Anything longer than this is a permanent ban with extra steps, and keeps the expiry away
     * from the range where {@link Instant} arithmetic overflows.
     */
    public static final Duration MAXIMUM = Duration.ofDays(36_500);

    private static final List<String> COMMON = List.of("10m", "30m", "1h", "12h", "1d", "7d", "30d");

    private static final List<Character> UNIT_LETTERS = List.of('w', 'd', 'h', 'm', 's');

    private static final Pattern UNITS = Pattern.compile("(\\d{1,9})([wdhms])", Pattern.CASE_INSENSITIVE);

    private static final DynamicCommandExceptionType ERROR_INVALID = ExceptionFactory.dynamic("argument.duration.invalid");
    private static final DynamicCommandExceptionType ERROR_TOO_LONG = ExceptionFactory.dynamic("argument.duration.too_long");

    public static final SuggestionProvider<CommandSource> SUGGESTIONS = DurationArgument::suggest;

    private DurationArgument() {
    }

    public static ArgumentType<Duration> duration() {
        return Arguments.arguments().map(StringArgumentType.word(), DurationArgument::parse, SUGGESTIONS);
    }

    public static Duration getDuration(final CommandContext<CommandSource> context, final String name) {
        return context.getArgument(name, Duration.class);
    }

    private static Duration parse(final String input, final StringReader reader) throws CommandSyntaxException {
        final Matcher matcher = UNITS.matcher(input);
        final int start = reader.getCursor() - input.length();

        Duration total = Duration.ZERO;
        int read = 0;

        while (matcher.find() && matcher.start() == read) {
            total = total.plus(unit(matcher.group(2)).multipliedBy(Long.parseLong(matcher.group(1))));
            read = matcher.end();
        }

        if (read != input.length() || total.isZero()) {
            reader.setCursor(start);
            throw ERROR_INVALID.createWithContext(reader, input);
        }

        if (total.compareTo(MAXIMUM) > 0) {
            reader.setCursor(start);
            throw ERROR_TOO_LONG.createWithContext(reader, input);
        }

        return total;
    }

    private static Duration unit(final String unit) {
        return switch (unit.toLowerCase(Locale.ROOT)) {
            case "w" -> Duration.ofDays(7);
            case "d" -> Duration.ofDays(1);
            case "h" -> Duration.ofHours(1);
            case "m" -> Duration.ofMinutes(1);
            default -> Duration.ofSeconds(1);
        };
    }

    private static <S> CompletableFuture<Suggestions> suggest(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        final String typed = builder.getRemaining();
        final String remaining = builder.getRemainingLowerCase();

        COMMON.stream()
                .filter(value -> value.startsWith(remaining))
                .forEach(builder::suggest);

        final Matcher matcher = UNITS.matcher(remaining);
        int read = 0;
        while (matcher.find() && matcher.start() == read) {
            read = matcher.end();
        }

        final String trailing = remaining.substring(read);

        if (!trailing.isEmpty() && trailing.chars().allMatch(Character::isDigit)) {
            for (final char unitLetter : UNIT_LETTERS) {
                builder.suggest(typed + unitLetter);
            }
        }

        return builder.buildFuture();
    }
}
