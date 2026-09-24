package fr.euphyllia.fidorial.server.world.gamerule;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.gamerule.WorldGameRules;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

final class FidorialWorldGameRules implements WorldGameRules {

    private final FidorialGameRules rules;
    private final ServerWorld world;

    FidorialWorldGameRules(final FidorialGameRules rules, final ServerWorld world) {
        this.rules = Objects.requireNonNull(rules, "The game rule service must not be null");
        this.world = Objects.requireNonNull(world, "The world whose game rules are viewed must not be null");
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public boolean holdsBaseValues() {
        return FidorialGameRules.holdsBaseValues(world);
    }

    @Override
    public boolean isOverridden(final TypedKey<GameRule> rule) {
        return world.gameRuleValues().override(FidorialGameRules.definitionOf(rule, null)) != null;
    }

    @Override
    public Collection<GameRuleDefinition> overrides() {
        return world.gameRuleValues().overridden();
    }

    @Override
    public boolean removeOverride(final TypedKey<GameRule> rule) {
        return rules.removeOverride(world, FidorialGameRules.definitionOf(rule, null), GameRuleChangeEvent.Cause.API, null)
                == FidorialGameRules.Result.CHANGED;
    }

    @Override
    public Collection<GameRuleDefinition> definitions() {
        return rules.definitions();
    }

    @Override
    public Optional<GameRuleDefinition> definition(final String id) {
        return rules.definition(id);
    }

    @Override
    public Optional<GameRuleDefinition> definition(final Key key) {
        return rules.definition(key);
    }

    @Override
    public boolean getBoolean(final TypedKey<GameRule> rule) {
        return world.gameRuleValues().getBoolean(rule);
    }

    @Override
    public int getInt(final TypedKey<GameRule> rule) {
        return world.gameRuleValues().getInt(rule);
    }

    @Override
    public String getAsString(final TypedKey<GameRule> rule) {
        final GameRuleDefinition definition = FidorialGameRules.definitionOf(rule, null);
        return definition.format(world.gameRuleValues().get(definition));
    }

    @Override
    public boolean setBoolean(final TypedKey<GameRule> rule, final boolean value) {
        return apply(FidorialGameRules.definitionOf(rule, GameRuleType.BOOLEAN), value ? 1 : 0);
    }

    @Override
    public boolean setInt(final TypedKey<GameRule> rule, final int value) {
        return apply(FidorialGameRules.definitionOf(rule, GameRuleType.INTEGER), value);
    }

    @Override
    public boolean set(final TypedKey<GameRule> rule, final String value) {
        final GameRuleDefinition definition = FidorialGameRules.definitionOf(rule, null);
        return apply(definition, FidorialGameRules.parse(definition, value));
    }

    @Override
    public boolean reset(final TypedKey<GameRule> rule) {
        final GameRuleDefinition definition = FidorialGameRules.definitionOf(rule, null);
        return apply(definition, definition.defaultValue());
    }

    private boolean apply(final GameRuleDefinition definition, final int value) {
        return rules.apply(world, definition, value, GameRuleChangeEvent.Cause.API, null) == FidorialGameRules.Result.CHANGED;
    }

    @Override
    public String toString() {
        return "FidorialWorldGameRules{world=" + world.key() + ", overrides=" + world.gameRuleValues().overridden().size() + '}';
    }
}
