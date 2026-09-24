package fr.fidorial.event.server;

import fr.fidorial.command.CommandSender;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.registry.keys.GameRuleKeys;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Fired right before a game rule changes
 *
 * @since 0.1.0
 */
public final class GameRuleChangeEvent implements Event, Cancellable {

    private final GameRuleDefinition rule;
    private final String previousValue;
    private final Cause cause;
    private final @Nullable CommandSender source;
    private String newValue;
    private boolean cancelled;


    /**
     * Creates an event.
     *
     * @param rule          the rule about to change
     * @param previousValue its current value
     * @param newValue      the value about to be applied
     * @param cause         what requested the change
     * @param source        who requested the change, or {@code null} when a plugin did
     * @since 0.1.0
     */
    public GameRuleChangeEvent(
            final GameRuleDefinition rule,
            final String previousValue,
            final String newValue,
            final Cause cause,
            final @Nullable CommandSender source) {
        this.rule = Objects.requireNonNull(rule, "The game rule that changes must not be null");
        this.previousValue = Objects.requireNonNull(previousValue, "The previous value of the game rule must not be null");
        this.newValue = Objects.requireNonNull(newValue, "The new value of the game rule must not be null");
        this.cause = Objects.requireNonNull(cause, "The cause of the game rule change must not be null");
        this.source = source;
    }

    /**
     * Gets the rule about to change.
     *
     * @return the rule, compare its {@link GameRuleDefinition#key() key} with
     * {@link GameRuleKeys}
     * @since 0.1.0
     */
    @Contract(pure = true)
    public GameRuleDefinition rule() {
        return rule;
    }

    /**
     * Gets the value of the rule before the change.
     *
     * @return the current value
     * @since 0.1.0
     */
    @Contract(pure = true)
    public String previousValue() {
        return previousValue;
    }

    /**
     * Gets the value about to be applied.
     *
     * @return the new value, possibly replaced by a previous listener
     * @since 0.1.0
     */
    @Contract(pure = true)
    public String newValue() {
        return newValue;
    }

    /**
     * Applies another value instead.
     *
     * @param value {@code true}/{@code false} for boolean rules, a number within range otherwise
     * @throws IllegalArgumentException if the value is not valid for this rule
     * @since 0.1.0
     */
    public void setNewValue(final String value) {
        Objects.requireNonNull(value, "The value to apply to the game rule must not be null");
        final Integer parsed = rule.parse(value);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for game rule " + rule.key().key());
        }
        this.newValue = rule.format(parsed);
    }

    /**
     * Applies another value instead, for a boolean rule.
     *
     * @param value the value to apply
     * @throws IllegalArgumentException if the rule is not a boolean rule
     * @since 0.1.0
     */
    public void setNewValue(final boolean value) {
        requireType(GameRuleType.BOOLEAN);
        this.newValue = Boolean.toString(value);
    }

    /**
     * Applies another value instead, for an integer rule.
     *
     * @param value the value to apply
     * @throws IllegalArgumentException if the rule is not an integer rule, or the value is out of range
     * @since 0.1.0
     */
    public void setNewValue(final int value) {
        requireType(GameRuleType.INTEGER);
        if (!rule.accepts(value)) {
            throw new IllegalArgumentException("Value " + value + " is out of range for game rule " + rule.key().key());
        }
        this.newValue = Integer.toString(value);
    }

    /**
     * Gets what requested the change.
     *
     * @return the cause
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Cause cause() {
        return cause;
    }

    /**
     * Gets who requested the change.
     *
     * @return the player or the console that ran {@code /gamerule} or used the game rule screen,
     * or empty when a plugin changed the rule through the API
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<CommandSender> source() {
        return Optional.ofNullable(source);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Refuses or re-allows the change. A refused change leaves the rule untouched, and
     * {@code /gamerule} tells its sender the change was cancelled.
     *
     * @param cancelled whether the change should be refused
     */
    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    private void requireType(final GameRuleType expected) {
        if (rule.type() != expected) {
            throw new IllegalArgumentException("Game rule " + rule.key().key() + " is not a "
                    + expected.name().toLowerCase(Locale.ROOT) + " rule");
        }
    }

    /**
     * What requested a game rule change.
     *
     * @since 0.1.0
     */
    public enum Cause {
        /**
         * A player or the console ran {@code /gamerule}.
         */
        COMMAND,
        /**
         * An operator confirmed the client's game rule screen.
         */
        GAME_RULE_SCREEN,
        /**
         * A plugin called {@link fr.fidorial.gamerule.GameRules}.
         */
        API
    }
}
