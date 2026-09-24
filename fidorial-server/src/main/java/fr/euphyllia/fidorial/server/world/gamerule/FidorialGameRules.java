package fr.euphyllia.fidorial.server.world.gamerule;

import fr.euphyllia.fidorial.server.world.storage.LevelData;
import fr.fidorial.command.CommandSender;
import fr.fidorial.event.EventBus;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.gamerule.GameRules;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FidorialGameRules implements GameRules {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialGameRules.class);

    private final GameRuleValues values;
    private final EventBus events;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public FidorialGameRules(final LevelData level, final EventBus events) {
        this.values = Objects.requireNonNull(level, "The level data holding the game rule values must not be null").gameRules;
        this.events = Objects.requireNonNull(events, "The event bus used to fire GameRuleChangeEvent must not be null");
    }

    @FunctionalInterface
    public interface Listener {
        void onChange(GameRuleDefinition rule, int previous, int current);
    }

    public enum Result {
        CHANGED,
        UNCHANGED,
        CANCELLED
    }

    public void addListener(final Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(final Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public Collection<GameRuleDefinition> definitions() {
        return VanillaGameRules.ALL;
    }

    @Override
    public Optional<GameRuleDefinition> definition(final String id) {
        final int index = VanillaGameRules.indexOf(id);
        return index < 0 ? Optional.empty() : Optional.of(VanillaGameRules.ALL.get(index));
    }

    @Override
    public Optional<GameRuleDefinition> definition(final Key key) {
        final int index = VanillaGameRules.indexOf(key);
        return index < 0 ? Optional.empty() : Optional.of(VanillaGameRules.ALL.get(index));
    }

    @Override
    public boolean getBoolean(final TypedKey<GameRule> rule) {
        return values.getBoolean(rule);
    }

    @Override
    public int getInt(final TypedKey<GameRule> rule) {
        return values.getInt(rule);
    }

    @Override
    public String getAsString(final TypedKey<GameRule> rule) {
        final GameRuleDefinition definition = definitionOf(rule, null);
        return definition.format(values.get(definition));
    }

    public int get(final GameRuleDefinition definition) {
        return values.get(definition);
    }

    public Map<Key, String> snapshot() {
        return values.snapshot();
    }

    @Override
    public boolean setBoolean(final TypedKey<GameRule> rule, final boolean value) {
        return apply(definitionOf(rule, GameRuleType.BOOLEAN), value ? 1 : 0, GameRuleChangeEvent.Cause.API, null)
                == Result.CHANGED;
    }

    @Override
    public boolean setInt(final TypedKey<GameRule> rule, final int value) {
        return apply(definitionOf(rule, GameRuleType.INTEGER), value, GameRuleChangeEvent.Cause.API, null)
                == Result.CHANGED;
    }

    @Override
    public boolean set(final TypedKey<GameRule> rule, final String value) {
        final GameRuleDefinition definition = definitionOf(rule, null);
        final Integer parsed = definition.parse(value);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for game rule " + rule.key());
        }
        return apply(definition, parsed, GameRuleChangeEvent.Cause.API, null) == Result.CHANGED;
    }

    @Override
    public boolean reset(final TypedKey<GameRule> rule) {
        final GameRuleDefinition definition = definitionOf(rule, null);
        return apply(definition, definition.defaultValue(), GameRuleChangeEvent.Cause.API, null) == Result.CHANGED;
    }

    public synchronized Result apply(
            final GameRuleDefinition rule,
            final int value,
            final GameRuleChangeEvent.Cause cause,
            final @Nullable CommandSender source) {
        if (!rule.accepts(value)) {
            throw new IllegalArgumentException("Value " + value + " is out of range for game rule " + rule.key().key()
                    + " [" + rule.minValue() + ", " + rule.maxValue() + "]");
        }
        final int previous = values.get(rule);
        if (previous == value) {
            return Result.UNCHANGED;
        }

        final GameRuleChangeEvent event = events.post(
                new GameRuleChangeEvent(rule, rule.format(previous), rule.format(value), cause, source));
        if (event.isCancelled()) {
            return Result.CANCELLED;
        }

        final int applied = Objects.requireNonNull(rule.parse(event.newValue()),
                () -> "GameRuleChangeEvent left an invalid value '" + event.newValue() + "' for game rule " + rule.key().key());
        if (applied == previous) {
            return Result.UNCHANGED;
        }
        values.exchange(rule, applied);

        for (final Listener listener : listeners) {
            try {
                listener.onChange(rule, previous, applied);
            } catch (final Throwable t) {
                LOGGER.error("Game rule listener failed for {}", rule.key().key(), t);
            }
        }
        return Result.CHANGED;
    }

    private static GameRuleDefinition definitionOf(final TypedKey<GameRule> rule, final @Nullable GameRuleType expected) {
        return VanillaGameRules.ALL.get(GameRuleValues.index(rule, expected));
    }
}
