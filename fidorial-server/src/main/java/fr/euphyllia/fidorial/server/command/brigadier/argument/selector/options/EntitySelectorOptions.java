package fr.euphyllia.fidorial.server.command.brigadier.argument.selector.options;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntitySelector;
import fr.euphyllia.fidorial.server.command.brigadier.argument.range.RangeBounds;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.EntitySelectorParser;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.KeyReader;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class EntitySelectorOptions {

    public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = ExceptionFactory.simple("argument.entity.options.distance.negative");
    public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = ExceptionFactory.simple("argument.entity.options.limit.toosmall");
    public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = ExceptionFactory.dynamic("argument.entity.options.sort.irreversible");
    public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = ExceptionFactory.dynamic("argument.entity.options.mode.invalid");

    private static final Key PLAYER = Key.key("player");

    private static final Map<String, Option> OPTIONS = options();

    private EntitySelectorOptions() {
    }

    private static Map<String, Option> options() {
        final Map<String, Option> options = new HashMap<>();

        options.put("name", new Option(parser -> {
            final int start = parser.getReader().getCursor();
            final boolean inverted = parser.shouldInvertValue();
            final String name = parser.getReader().readString();
            if (!parser.nameOption().canAdd(inverted)) {
                throw parser.rollback(start, EntitySelectorParser.ERROR_INAPPLICABLE_OPTION, "name");
            }
            parser.nameOption().add(inverted);
            parser.addPredicate(entity -> (entity instanceof final Player player && player.name().equalsIgnoreCase(name)) != inverted);
        }, parser -> parser.nameOption().canAddAny()));

        options.put("distance", new Option(parser -> {
            final int start = parser.getReader().getCursor();
            final RangeBounds.Doubles range = RangeBounds.Doubles.fromReader(parser.getReader());
            if (range.min().filter(min -> min < 0).isPresent() || range.max().filter(max -> max < 0).isPresent()) {
                throw parser.rollback(start, ERROR_RANGE_NEGATIVE);
            }
            parser.setDistance(range);
        }, parser -> parser.getDistance() == null));

        options.put("x", new Option(parser -> parser.setX(parser.getReader().readDouble()), parser -> parser.getX() == null));
        options.put("y", new Option(parser -> parser.setY(parser.getReader().readDouble()), parser -> parser.getY() == null));
        options.put("z", new Option(parser -> parser.setZ(parser.getReader().readDouble()), parser -> parser.getZ() == null));
        options.put("dx", new Option(parser -> parser.setDeltaX(parser.getReader().readDouble()), parser -> parser.getDeltaX() == null));
        options.put("dy", new Option(parser -> parser.setDeltaY(parser.getReader().readDouble()), parser -> parser.getDeltaY() == null));
        options.put("dz", new Option(parser -> parser.setDeltaZ(parser.getReader().readDouble()), parser -> parser.getDeltaZ() == null));

        options.put("limit", new Option(parser -> {
            final int start = parser.getReader().getCursor();
            final int count = parser.getReader().readInt();
            if (count < 1) {
                throw parser.rollback(start, ERROR_LIMIT_TOO_SMALL);
            }
            parser.setMaxResults(count);
            parser.limitedOption().consume();
        }, parser -> !parser.isCurrentEntity() && parser.limitedOption().available()));

        options.put("sort", new Option(parser -> {
            parser.setSuggestions((builder, _) -> suggest(builder, "nearest", "furthest", "random", "arbitrary"));
            final int start = parser.getReader().getCursor();
            final String name = parser.getReader().readUnquotedString();
            parser.setOrder(switch (name) {
                case "nearest" -> EntitySelector.SortType.NEAREST;
                case "furthest" -> EntitySelector.SortType.FURTHEST;
                case "random" -> EntitySelector.SortType.RANDOM;
                case "arbitrary" -> EntitySelector.SortType.ARBITRARY;
                default -> throw parser.rollback(start, ERROR_SORT_UNKNOWN, name);
            });
            parser.sortedOption().consume();
        }, parser -> !parser.isCurrentEntity() && parser.sortedOption().available()));

        options.put("gamemode", new Option(parser -> {
            parser.setSuggestions((builder, _) -> suggestInvertible(builder, parser.gamemodeOption(), gameModeNames()));
            final int start = parser.getReader().getCursor();
            final boolean inverted = parser.shouldInvertValue();
            if (!parser.gamemodeOption().canAdd(inverted)) {
                throw parser.rollback(start, EntitySelectorParser.ERROR_INAPPLICABLE_OPTION, "gamemode");
            }
            final String name = parser.getReader().readUnquotedString();
            final GameMode expected = GameMode.byName(name.toLowerCase(Locale.ROOT));
            if (expected == null) {
                throw parser.rollback(start, ERROR_GAME_MODE_INVALID, name);
            }
            parser.setIncludesEntities(false);
            parser.addPredicate(entity -> entity instanceof final Player player && (player.gameMode() == expected) != inverted);
            parser.gamemodeOption().add(inverted);
        }, parser -> parser.gamemodeOption().canAddAny()));

        options.put("type", new Option(parser -> {
            final int start = parser.getReader().getCursor();
            final boolean inverted = parser.shouldInvertValue();
            if (!parser.typeOption().canAdd(inverted)) {
                throw parser.rollback(start, EntitySelectorParser.ERROR_INAPPLICABLE_OPTION, "type");
            }
            final KeyReader.ParsedKey parsed = KeyReader.readKeyStringDetailed(parser.getReader());
            final Key type = parsed.hasNamespace() ? Key.key(parsed.value()) : Key.key(Key.MINECRAFT_NAMESPACE, parsed.value());
            if (!inverted && type.equals(PLAYER)) {
                parser.setIncludesEntities(false);
            }
            parser.addPredicate(entity -> entity.type().key().equals(type) != inverted);
            parser.typeOption().add(inverted);
        }, parser -> parser.typeOption().canAddAny()));

        return Map.copyOf(options);
    }

    public static Modifier get(final EntitySelectorParser parser, final String key, final int start) throws CommandSyntaxException {
        final Option option = OPTIONS.get(key);
        if (option == null) {
            throw parser.rollback(start, EntitySelectorParser.ERROR_UNKNOWN_OPTION, key);
        }
        if (!option.canUse().test(parser)) {
            throw parser.rollback(start, EntitySelectorParser.ERROR_INAPPLICABLE_OPTION, key);
        }
        return option.modifier();
    }

    public static void suggestNames(final EntitySelectorParser parser, final SuggestionsBuilder builder) {
        final String prefix = builder.getRemainingLowerCase();
        OPTIONS.forEach((name, option) -> {
            if (name.startsWith(prefix) && option.canUse().test(parser)) {
                builder.suggest(name + "=");
            }
        });
    }

    private static String[] gameModeNames() {
        final GameMode[] modes = GameMode.values();
        final String[] names = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
            names[i] = modes[i].name().toLowerCase(Locale.ROOT);
        }
        return names;
    }

    private static CompletableFuture<Suggestions> suggest(final SuggestionsBuilder builder, final String... values) {
        final String prefix = builder.getRemainingLowerCase();
        for (final String value : values) {
            if (value.startsWith(prefix)) {
                builder.suggest(value);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestInvertible(final SuggestionsBuilder builder, final SelectorSetState state, final String... values) {
        final String typed = builder.getRemainingLowerCase();
        final boolean inverted = typed.startsWith("!");
        final String prefix = inverted ? typed.substring(1) : typed;
        for (final String value : values) {
            if (!value.startsWith(prefix)) {
                continue;
            }
            if (!inverted && state.canAdd(false)) {
                builder.suggest(value);
            }
            if (state.canAdd(true)) {
                builder.suggest("!" + value);
            }
        }
        return builder.buildFuture();
    }

    @FunctionalInterface
    public interface Modifier {
        void handle(EntitySelectorParser parser) throws CommandSyntaxException;
    }

    private record Option(Modifier modifier, Predicate<EntitySelectorParser> canUse) {
    }
}
