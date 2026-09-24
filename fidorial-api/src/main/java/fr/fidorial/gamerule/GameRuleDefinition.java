package fr.fidorial.gamerule;

import com.google.common.base.Preconditions;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Describes one entry of the {@code minecraft:game_rule} registry: its type, its vanilla default and
 * the range of values it accepts.
 *
 * @param key          the registry key, for instance {@code minecraft:keep_inventory}
 * @param type         the kind of value the rule holds
 * @param defaultValue the vanilla default ({@code 0}/{@code 1} for boolean rules)
 * @param minValue     the smallest accepted value, inclusive
 * @param maxValue     the largest accepted value, inclusive
 * @param implemented  whether the server wires this rule into its gameplay; rules that are not
 *                     implemented are still stored and saved, but have no built-in effect and are
 *                     not offered by {@code /gamerule}
 * @since 0.1.0
 */
public record GameRuleDefinition(
        TypedKey<GameRule> key,
        GameRuleType type,
        int defaultValue,
        int minValue,
        int maxValue,
        boolean implemented
) {

    public GameRuleDefinition {
        Preconditions.checkNotNull(key, "The registry key of a game rule definition must not be null");
        Preconditions.checkNotNull(type, "The value type of a game rule definition must not be null");
        Preconditions.checkArgument(minValue <= maxValue,
                "The minimum value of game rule %s (%s) must not be greater than its maximum value (%s)",
                key.key(), minValue, maxValue);
        Preconditions.checkArgument(defaultValue >= minValue && defaultValue <= maxValue,
                "The default value of game rule %s (%s) must lie between %s and %s",
                key.key(), defaultValue, minValue, maxValue);
    }

    /**
     * Creates a boolean rule definition.
     *
     * @param key          the registry key
     * @param defaultValue the vanilla default
     * @param implemented  whether the server wires the rule into its gameplay
     * @return the definition
     * @since 0.1.0
     */
    public static GameRuleDefinition ofBoolean(
            final TypedKey<GameRule> key, final boolean defaultValue, final boolean implemented) {
        return new GameRuleDefinition(key, GameRuleType.BOOLEAN, defaultValue ? 1 : 0, 0, 1, implemented);
    }

    /**
     * Creates an integer rule definition.
     *
     * @param key          the registry key
     * @param defaultValue the vanilla default
     * @param minValue     the smallest accepted value, inclusive
     * @param maxValue     the largest accepted value, inclusive
     * @param implemented  whether the server wires the rule into its gameplay
     * @return the definition
     * @since 0.1.0
     */
    public static GameRuleDefinition ofInteger(
            final TypedKey<GameRule> key,
            final int defaultValue,
            final int minValue,
            final int maxValue,
            final boolean implemented) {
        return new GameRuleDefinition(key, GameRuleType.INTEGER, defaultValue, minValue, maxValue, implemented);
    }

    /**
     * @return the identifier without its namespace, for instance {@code keep_inventory}
     * @since 0.1.0
     */
    @Contract(pure = true)
    public String id() {
        return key.key().value();
    }

    /**
     * @return {@code true} if the rule holds a boolean
     * @since 0.1.0
     */
    @Contract(pure = true)
    public boolean isBoolean() {
        return type == GameRuleType.BOOLEAN;
    }

    /**
     * @param value a raw value
     * @return {@code true} if {@code value} lies within {@link #minValue()} and {@link #maxValue()}
     * @since 0.1.0
     */
    @Contract(pure = true)
    public boolean accepts(final int value) {
        return value >= minValue && value <= maxValue;
    }

    /**
     * Formats a raw value the way the vanilla command prints it.
     *
     * @param value a raw value
     * @return {@code "true"}/{@code "false"} for boolean rules, the number otherwise
     * @since 0.1.0
     */
    @Contract(pure = true)
    public String format(final int value) {
        return isBoolean() ? Boolean.toString(value != 0) : Integer.toString(value);
    }

    /**
     * Parses a value typed by a player or read from a legacy save.
     *
     * <p>Boolean rules accept {@code true}/{@code false} (any case); integer rules accept a decimal
     * number within range.</p>
     *
     * @param text the text to parse
     * @return the raw value, or {@code null} if the text is not valid for this rule
     * @since 0.1.0
     */
    @Contract(pure = true)
    public @Nullable Integer parse(final String text) {
        final String trimmed = text.trim();
        if (isBoolean()) {
            return switch (trimmed.toLowerCase(Locale.ROOT)) {
                case "true" -> 1;
                case "false" -> 0;
                default -> null;
            };
        }
        try {
            final int value = Integer.parseInt(trimmed);
            return accepts(value) ? value : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}
