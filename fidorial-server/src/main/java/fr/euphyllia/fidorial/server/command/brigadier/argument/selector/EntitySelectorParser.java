package fr.euphyllia.fidorial.server.command.brigadier.argument.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntitySelector;
import fr.euphyllia.fidorial.server.command.brigadier.argument.range.RangeBounds;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.options.EntitySelectorOptions;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.options.SelectorSetState;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.options.SingleUseOption;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.fidorial.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EntitySelectorParser {

    public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = ExceptionFactory.simple("argument.entity.invalid");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = ExceptionFactory.dynamic("argument.entity.selector.unknown");
    public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = ExceptionFactory.simple("argument.entity.selector.missing");
    public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = ExceptionFactory.simple("argument.entity.options.unterminated");
    public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = ExceptionFactory.dynamic("argument.entity.options.valueless");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = ExceptionFactory.dynamic("argument.entity.options.unknown");
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = ExceptionFactory.dynamic("argument.entity.options.inapplicable");

    private static final int MAX_NAME_LENGTH = 16;
    private static final List<String> SELECTORS = List.of("@p", "@a", "@r", "@s", "@e", "@n");

    /**
     * Suggests from the cursor; {@code names} adds the names valid there.
     */
    @FunctionalInterface
    public interface Suggester {
        Suggester NONE = (builder, _) -> builder.buildFuture();

        CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> names);
    }

    private final StringReader reader;
    private int startPosition;
    private Suggester suggestions = Suggester.NONE;

    private int maxResults = 1;
    private boolean includesEntities;
    private boolean selfSelector;
    private boolean usesSelector;

    private @Nullable Double x;
    private @Nullable Double y;
    private @Nullable Double z;
    private @Nullable Double deltaX;
    private @Nullable Double deltaY;
    private @Nullable Double deltaZ;
    private RangeBounds.@Nullable Doubles distance;

    private @Nullable String targetName;
    private @Nullable UUID targetUuid;
    private EntitySelector.SortType sort = EntitySelector.SortType.ARBITRARY;
    private final List<Predicate<Entity>> predicates = new ArrayList<>();

    private final SelectorSetState nameOption = new SelectorSetState();
    private final SingleUseOption limitedOption = new SingleUseOption();
    private final SingleUseOption sortedOption = new SingleUseOption();
    private final SelectorSetState gamemodeOption = new SelectorSetState();
    private final SelectorSetState typeOption = new SelectorSetState();

    public EntitySelectorParser(final StringReader reader) {
        this.reader = reader;
    }

    public EntitySelector parse() throws CommandSyntaxException {
        startPosition = reader.getCursor();
        suggestions = this::suggestNameOrSelector;

        if (reader.canRead() && reader.peek() == '@') {
            reader.skip();
            parseSelector();
        } else {
            parseNameOrUuid();
        }

        return new EntitySelector(maxResults, includesEntities, selfSelector, usesSelector, predicates,
                x, y, z, distance, deltaX, deltaY, deltaZ, sort, targetName, targetUuid);
    }

    private void parseSelector() throws CommandSyntaxException {
        usesSelector = true;
        suggestions = this::suggestSelector;

        if (!reader.canRead()) {
            throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
        }

        final int start = reader.getCursor();
        final char type = reader.read();
        switch (type) {
            case 'p' -> { maxResults = 1; sort = EntitySelector.SortType.NEAREST; }
            case 'a' -> maxResults = Integer.MAX_VALUE;
            case 'r' -> { maxResults = 1; sort = EntitySelector.SortType.RANDOM; }
            case 's' -> { maxResults = 1; selfSelector = true; }
            case 'e' -> { maxResults = Integer.MAX_VALUE; includesEntities = true; }
            case 'n' -> { maxResults = 1; includesEntities = true; sort = EntitySelector.SortType.NEAREST; }
            default -> {
                reader.setCursor(start);
                throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(reader, "@" + type);
            }
        }

        suggestions = this::suggestOpenOptions;
        if (reader.canRead() && reader.peek() == '[') {
            reader.skip();
            suggestions = this::suggestOptionsKeyOrClose;
            parseOptions();
        }
    }

    private void parseOptions() throws CommandSyntaxException {
        suggestions = this::suggestOptionsKey;
        reader.skipWhitespace();

        while (reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace();
            final int start = reader.getCursor();
            final String key = reader.readString();
            final EntitySelectorOptions.Modifier modifier = EntitySelectorOptions.get(this, key, start);

            reader.skipWhitespace();
            if (!reader.canRead() || reader.peek() != '=') {
                reader.setCursor(start);
                throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(reader, key);
            }
            reader.skip();
            reader.skipWhitespace();

            suggestions = Suggester.NONE;
            modifier.handle(this);
            reader.skipWhitespace();
            suggestions = this::suggestOptionsNextOrClose;

            if (!reader.canRead()) {
                break;
            }
            if (reader.peek() == ',') {
                reader.skip();
                suggestions = this::suggestOptionsKey;
            } else if (reader.peek() != ']') {
                throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(reader);
            }
        }

        if (!reader.canRead()) {
            throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(reader);
        }
        reader.skip();
        suggestions = Suggester.NONE;
    }

    private void parseNameOrUuid() throws CommandSyntaxException {
        if (reader.canRead()) {
            suggestions = this::suggestName;
        }

        final int start = reader.getCursor();
        final String value = reader.readString();
        maxResults = 1;

        try {
            targetUuid = UUID.fromString(value);
            includesEntities = true;
        } catch (final IllegalArgumentException notUuid) {
            if (value.isEmpty() || value.length() > MAX_NAME_LENGTH) {
                reader.setCursor(start);
                throw ERROR_INVALID_NAME_OR_UUID.createWithContext(reader);
            }
            targetName = value;
        }
    }

    public StringReader getReader() {
        return reader;
    }

    /**
     * {@return an error of {@code type} for {@code argument}, with the cursor moved back to {@code start}}
     */
    public CommandSyntaxException rollback(final int start, final DynamicCommandExceptionType type, final Object argument) {
        reader.setCursor(start);
        return type.createWithContext(reader, argument);
    }

    /**
     * {@return an error of {@code type}, with the cursor moved back to {@code start}}
     */
    public CommandSyntaxException rollback(final int start, final SimpleCommandExceptionType type) {
        reader.setCursor(start);
        return type.createWithContext(reader);
    }

    public boolean shouldInvertValue() {
        reader.skipWhitespace();
        if (reader.canRead() && reader.peek() == '!') {
            reader.skip();
            reader.skipWhitespace();
            return true;
        }
        return false;
    }

    public boolean isCurrentEntity() {
        return selfSelector;
    }

    public void addPredicate(final Predicate<Entity> predicate) {
        predicates.add(predicate);
    }

    public void setSuggestions(final Suggester suggestions) {
        this.suggestions = suggestions;
    }

    public RangeBounds.@Nullable Doubles getDistance() { return distance; }
    public void setDistance(final RangeBounds.Doubles distance) { this.distance = distance; }

    public @Nullable Double getX() { return x; }
    public @Nullable Double getY() { return y; }
    public @Nullable Double getZ() { return z; }
    public void setX(final double x) { this.x = x; }
    public void setY(final double y) { this.y = y; }
    public void setZ(final double z) { this.z = z; }

    public @Nullable Double getDeltaX() { return deltaX; }
    public @Nullable Double getDeltaY() { return deltaY; }
    public @Nullable Double getDeltaZ() { return deltaZ; }
    public void setDeltaX(final double deltaX) { this.deltaX = deltaX; }
    public void setDeltaY(final double deltaY) { this.deltaY = deltaY; }
    public void setDeltaZ(final double deltaZ) { this.deltaZ = deltaZ; }

    public void setMaxResults(final int maxResults) { this.maxResults = maxResults; }
    public void setIncludesEntities(final boolean includesEntities) { this.includesEntities = includesEntities; }
    public void setOrder(final EntitySelector.SortType sort) { this.sort = sort; }

    public SelectorSetState nameOption() { return nameOption; }
    public SingleUseOption limitedOption() { return limitedOption; }
    public SingleUseOption sortedOption() { return sortedOption; }
    public SelectorSetState gamemodeOption() { return gamemodeOption; }
    public SelectorSetState typeOption() { return typeOption; }

    public CompletableFuture<Suggestions> fillSuggestions(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        return suggestions.suggest(builder.createOffset(reader.getCursor()), names);
    }

    private CompletableFuture<Suggestions> suggestNameOrSelector(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        names.accept(builder);
        SELECTORS.forEach(builder::suggest);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestName(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        final SuggestionsBuilder sub = builder.createOffset(startPosition);
        names.accept(sub);
        return builder.add(sub).buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSelector(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        final SuggestionsBuilder sub = builder.createOffset(builder.getStart() - 1);
        SELECTORS.forEach(sub::suggest);
        return builder.add(sub).buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenOptions(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        return builder.suggest("[").buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOptionsKey(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        EntitySelectorOptions.suggestNames(this, builder);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        builder.suggest("]");
        return suggestOptionsKey(builder, names);
    }

    private CompletableFuture<Suggestions> suggestOptionsNextOrClose(final SuggestionsBuilder builder, final Consumer<SuggestionsBuilder> names) {
        return builder.suggest(",").suggest("]").buildFuture();
    }
}
