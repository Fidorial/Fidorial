package fr.fidorial.gamerule;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Optional;

/**
 * A service for reading and changing the game rules of the server, as {@code /gamerule} does.
 *
 * <p>These are the base values, held by the overworld and followed by every world that does not
 * override them; see {@link World#gameRules()}.</p>
 *
 * @since 0.1.0
 */
public interface GameRules {

    /**
     * Gets every known rule.
     *
     * @return the definitions, sorted by identifier
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<GameRuleDefinition> definitions();

    /**
     * Looks a rule up by identifier. The namespace is optional: {@code keep_inventory} and
     * {@code minecraft:keep_inventory} name the same rule.
     *
     * @param id the identifier
     * @return the definition, or empty when no rule has this identifier
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<GameRuleDefinition> definition(String id);

    /**
     * Looks a rule up by registry key.
     *
     * @param key the registry key
     * @return the definition, or empty when no rule has this key
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<GameRuleDefinition> definition(Key key);

    /**
     * Gets the value of a boolean rule.
     *
     * @param rule a boolean rule
     * @return its current value
     * @throws IllegalArgumentException if the rule is unknown or is not a boolean rule
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean getBoolean(TypedKey<GameRule> rule);

    /**
     * Gets the value of an integer rule.
     *
     * @param rule an integer rule
     * @return its current value
     * @throws IllegalArgumentException if the rule is unknown or is not an integer rule
     * @since 0.1.0
     */
    @Contract(pure = true)
    int getInt(TypedKey<GameRule> rule);

    /**
     * Gets the value of any rule, formatted like {@code /gamerule} prints it.
     *
     * @param rule any rule
     * @return {@code "true"}/{@code "false"} for boolean rules, the number otherwise
     * @throws IllegalArgumentException if the rule is unknown
     * @since 0.1.0
     */
    @Contract(pure = true)
    String getAsString(TypedKey<GameRule> rule);

    /**
     * Changes a boolean rule.
     *
     * @param rule  a boolean rule
     * @param value the new value
     * @return {@code true} when the value actually changed, {@code false} when it already had this
     * value or a listener cancelled the change
     * @throws IllegalArgumentException if the rule is unknown or is not a boolean rule
     * @since 0.1.0
     */
    boolean setBoolean(TypedKey<GameRule> rule, boolean value);

    /**
     * Changes an integer rule.
     *
     * @param rule  an integer rule
     * @param value the new value
     * @return {@code true} when the value actually changed, {@code false} when it already had this
     * value or a listener cancelled the change
     * @throws IllegalArgumentException if the rule is unknown, is not an integer rule, or the value
     *                                  is out of range
     * @since 0.1.0
     */
    boolean setInt(TypedKey<GameRule> rule, int value);

    /**
     * Changes any rule from text, the way the client's game rule screen sends values.
     *
     * @param rule  any rule
     * @param value {@code true}/{@code false} (any case) or a number within range
     * @return {@code true} when the value actually changed, {@code false} when it already had this
     * value or a listener cancelled the change
     * @throws IllegalArgumentException if the rule is unknown or the value is not valid for it
     * @since 0.1.0
     */
    boolean set(TypedKey<GameRule> rule, String value);

    /**
     * Puts a rule back to its vanilla default.
     *
     * @param rule any rule
     * @return {@code true} when the value actually changed
     * @throws IllegalArgumentException if the rule is unknown
     * @since 0.1.0
     */
    boolean reset(TypedKey<GameRule> rule);
}
