package fr.fidorial.gamerule;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

import java.util.Collection;

/**
 * The game rules as one world sees them: the overworld's base values, except for the rules this world overrides
 *
 * @since 0.1.0
 */
public interface WorldGameRules extends GameRules {

    /**
     * Gets the world these rules apply to.
     *
     * @return the world
     * @since 0.1.0
     */
    @Contract(pure = true)
    World world();

    /**
     * Checks whether this world holds the base values, that is whether it is the overworld.
     *
     * @return {@code true} for the overworld, whose setters change the base values
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean holdsBaseValues();

    /**
     * Checks whether this world overrides a rule.
     *
     * @param rule any rule
     * @return {@code true} when the value in effect here comes from this world, not from the base value
     * @throws IllegalArgumentException if the rule is unknown
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean isOverridden(TypedKey<GameRule> rule);

    /**
     * Gets the rules this world overrides.
     *
     * @return the overridden rules, sorted by identifier; always empty for the overworld
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<GameRuleDefinition> overrides();

    /**
     * Drops the override of a rule, so this world follows the base value again.
     *
     * @param rule any rule
     * @return {@code true} when an override was actually removed, {@code false} when there was none
     * or a listener cancelled the change
     * @throws IllegalArgumentException if the rule is unknown
     * @since 0.1.0
     */
    boolean removeOverride(TypedKey<GameRule> rule);
}
