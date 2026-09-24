package fr.euphyllia.fidorial.server.world.gamerule;

import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.storage.Dimension;
import fr.euphyllia.fidorial.server.world.storage.LevelData;
import fr.fidorial.command.CommandSender;
import fr.fidorial.event.EventBus;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.gamerule.GameRules;
import fr.fidorial.gamerule.WorldGameRules;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import fr.fidorial.registry.keys.GameRuleKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

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
        void onChange(@Nullable ServerWorld world, GameRuleDefinition rule);
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
        return apply(null, definitionOf(rule, GameRuleType.BOOLEAN), value ? 1 : 0, GameRuleChangeEvent.Cause.API, null)
                == Result.CHANGED;
    }

    @Override
    public boolean setInt(final TypedKey<GameRule> rule, final int value) {
        return apply(null, definitionOf(rule, GameRuleType.INTEGER), value, GameRuleChangeEvent.Cause.API, null)
                == Result.CHANGED;
    }

    @Override
    public boolean set(final TypedKey<GameRule> rule, final String value) {
        final GameRuleDefinition definition = definitionOf(rule, null);
        return apply(null, definition, parse(definition, value), GameRuleChangeEvent.Cause.API, null) == Result.CHANGED;
    }

    @Override
    public boolean reset(final TypedKey<GameRule> rule) {
        final GameRuleDefinition definition = definitionOf(rule, null);
        return apply(null, definition, definition.defaultValue(), GameRuleChangeEvent.Cause.API, null) == Result.CHANGED;
    }

    public WorldGameRules world(final ServerWorld world) {
        return new FidorialWorldGameRules(this, world);
    }

    public static boolean holdsBaseValues(final ServerWorld world) {
        return world.key().equals(Dimension.OVERWORLD.id());
    }

    public void syncTo(final ServerWorld world, final int entityId, final Consumer<ClientboundPacket> target) {
        final GameRuleOverrides rules = world.gameRuleValues();
        target.accept(ClientboundEntityEventPacket.reducedDebugInfo(
                entityId, rules.getBoolean(GameRuleKeys.REDUCED_DEBUG_INFO)));
        target.accept(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.IMMEDIATE_RESPAWN,
                rules.getBoolean(GameRuleKeys.IMMEDIATE_RESPAWN) ? 1f : 0f));
    }

    public synchronized Result apply(
            final @Nullable ServerWorld world,
            final GameRuleDefinition rule,
            final int value,
            final GameRuleChangeEvent.Cause cause,
            final @Nullable CommandSender source) {
        if (!rule.accepts(value)) {
            throw new IllegalArgumentException("Value " + value + " is out of range for game rule " + rule.key().key()
                    + " [" + rule.minValue() + ", " + rule.maxValue() + "]");
        }
        if (world == null || holdsBaseValues(world)) {
            return applyBase(rule, value, cause, source);
        }

        final GameRuleOverrides overrides = world.gameRuleValues();
        final Integer previousOverride = overrides.override(rule);
        if (previousOverride != null && previousOverride == value) {
            return Result.UNCHANGED;
        }

        final GameRuleChangeEvent event = events.post(new GameRuleChangeEvent(
                rule, world, rule.format(overrides.get(rule)), rule.format(value), false, cause, source));
        if (event.isCancelled()) {
            return Result.CANCELLED;
        }

        final int applied = parseEventValue(rule, event);
        if (previousOverride != null && previousOverride == applied) {
            return Result.UNCHANGED;
        }
        overrides.exchange(rule, applied);
        notifyListeners(world, rule);
        return Result.CHANGED;
    }

    public synchronized Result removeOverride(
            final ServerWorld world,
            final GameRuleDefinition rule,
            final GameRuleChangeEvent.Cause cause,
            final @Nullable CommandSender source) {
        final GameRuleOverrides overrides = world.gameRuleValues();
        final Integer previousOverride = overrides.override(rule);
        if (previousOverride == null) {
            return Result.UNCHANGED;
        }

        final GameRuleChangeEvent event = events.post(new GameRuleChangeEvent(
                rule, world, rule.format(previousOverride), rule.format(values.get(rule)), true, cause, source));
        if (event.isCancelled()) {
            return Result.CANCELLED;
        }

        if (event.removesOverride()) {
            overrides.exchange(rule, null);
        } else {
            final int applied = parseEventValue(rule, event);
            if (applied == previousOverride) {
                return Result.UNCHANGED;
            }
            overrides.exchange(rule, applied);
        }
        notifyListeners(world, rule);
        return Result.CHANGED;
    }

    private Result applyBase(
            final GameRuleDefinition rule,
            final int value,
            final GameRuleChangeEvent.Cause cause,
            final @Nullable CommandSender source) {
        final int previous = values.get(rule);
        if (previous == value) {
            return Result.UNCHANGED;
        }

        final GameRuleChangeEvent event = events.post(new GameRuleChangeEvent(
                rule, null, rule.format(previous), rule.format(value), false, cause, source));
        if (event.isCancelled()) {
            return Result.CANCELLED;
        }

        final int applied = parseEventValue(rule, event);
        if (applied == previous) {
            return Result.UNCHANGED;
        }
        values.exchange(rule, applied);
        notifyListeners(null, rule);
        return Result.CHANGED;
    }

    private void notifyListeners(final @Nullable ServerWorld world, final GameRuleDefinition rule) {
        for (final Listener listener : listeners) {
            try {
                listener.onChange(world, rule);
            } catch (final Throwable t) {
                LOGGER.error("Game rule listener failed for {} in {}", rule.key().key(),
                        world == null ? "every world" : world.key(), t);
            }
        }
    }

    private static int parseEventValue(final GameRuleDefinition rule, final GameRuleChangeEvent event) {
        return Objects.requireNonNull(rule.parse(event.newValue()),
                () -> "GameRuleChangeEvent left an invalid value '" + event.newValue() + "' for game rule " + rule.key().key());
    }

    static int parse(final GameRuleDefinition rule, final String value) {
        final Integer parsed = rule.parse(value);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for game rule " + rule.key().key());
        }
        return parsed;
    }

    static GameRuleDefinition definitionOf(final TypedKey<GameRule> rule, final @Nullable GameRuleType expected) {
        return VanillaGameRules.ALL.get(GameRuleValues.index(rule, expected));
    }
}
