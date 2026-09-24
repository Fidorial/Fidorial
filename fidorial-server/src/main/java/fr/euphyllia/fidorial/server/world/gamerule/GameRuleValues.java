package fr.euphyllia.fidorial.server.world.gamerule;

import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class GameRuleValues {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(GameRuleValues.class);

    private static final String DATA_VERSION = "DataVersion";
    private static final String SAVED_DATA_WRAPPER = "data";

    private final AtomicIntegerArray values = new AtomicIntegerArray(VanillaGameRules.ALL.size());
    private final Map<String, BinaryTag> unknown = new ConcurrentHashMap<>();

    public GameRuleValues() {
        for (int i = 0; i < VanillaGameRules.ALL.size(); i++) {
            values.set(i, VanillaGameRules.ALL.get(i).defaultValue());
        }
    }

    public boolean getBoolean(final TypedKey<GameRule> rule) {
        return values.get(index(rule, GameRuleType.BOOLEAN)) != 0;
    }

    public int getInt(final TypedKey<GameRule> rule) {
        return values.get(index(rule, GameRuleType.INTEGER));
    }

    public int get(final GameRuleDefinition definition) {
        return values.get(requireIndex(definition.key().key()));
    }

    int exchange(final GameRuleDefinition definition, final int value) {
        return values.getAndSet(requireIndex(definition.key().key()), value);
    }

    public Map<Key, String> snapshot() {
        final Map<Key, String> snapshot = new LinkedHashMap<>();
        for (int i = 0; i < VanillaGameRules.ALL.size(); i++) {
            final GameRuleDefinition definition = VanillaGameRules.ALL.get(i);
            snapshot.put(definition.key().key(), definition.format(values.get(i)));
        }
        return snapshot;
    }

    static int index(final TypedKey<GameRule> rule, final @Nullable GameRuleType expected) {
        final int index = requireIndex(rule.key());
        final GameRuleDefinition definition = VanillaGameRules.ALL.get(index);
        if (expected != null && definition.type() != expected) {
            throw new IllegalArgumentException("Game rule " + rule.key() + " holds values of type "
                    + definition.type().name().toLowerCase(Locale.ROOT) + ", not "
                    + expected.name().toLowerCase(Locale.ROOT));
        }
        return index;
    }

    private static int requireIndex(final Key key) {
        final int index = VanillaGameRules.indexOf(key);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown game rule: " + key);
        }
        return index;
    }

    public void load(final CompoundBinaryTag tag) {
        final CompoundBinaryTag source = tag.get(SAVED_DATA_WRAPPER) instanceof final CompoundBinaryTag wrapped
                ? wrapped
                : tag;

        for (final String name : source.keySet()) {
            if (name.equals(DATA_VERSION)) {
                continue;
            }
            final BinaryTag raw = source.get(name);
            if (raw == null) {
                continue;
            }

            final int index = VanillaGameRules.indexOf(name);
            if (index < 0) {
                if (Key.parseable(name)) {
                    unknown.put(name, raw);
                    LOGGER.debug("Unknown game rule '{}' kept as-is", name);
                } else {
                    LOGGER.warn("Dropping game rule '{}': not a registry key", name);
                }
                continue;
            }

            final GameRuleDefinition definition = VanillaGameRules.ALL.get(index);
            if (!(raw instanceof final NumberBinaryTag number)) {
                LOGGER.warn("Ignoring non-numeric value {} for game rule '{}', keeping {}",
                        raw, name, definition.format(definition.defaultValue()));
                continue;
            }
            values.set(index, sanitize(definition, number.intValue(), name));
        }
    }

    public void save(final CompoundBinaryTag.Builder root) {
        for (int i = 0; i < VanillaGameRules.ALL.size(); i++) {
            final GameRuleDefinition definition = VanillaGameRules.ALL.get(i);
            final String id = definition.key().key().asString();
            if (definition.isBoolean()) {
                root.putBoolean(id, values.get(i) != 0);
            } else {
                root.putInt(id, values.get(i));
            }
        }
        for (final Map.Entry<String, BinaryTag> entry : unknown.entrySet()) {
            root.put(entry.getKey(), entry.getValue());
        }
    }

    private static int sanitize(final GameRuleDefinition definition, final int value, final String name) {
        if (definition.isBoolean()) {
            return value != 0 ? 1 : 0;
        }
        if (definition.accepts(value)) {
            return value;
        }
        final int clamped = Math.clamp(value, definition.minValue(), definition.maxValue());
        LOGGER.warn("Game rule '{}' had out-of-range value {}, clamped to {}", name, value, clamped);
        return clamped;
    }
}
