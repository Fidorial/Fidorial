package fr.euphyllia.fidorial.server.world.gamerule;

import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRuleType;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

    public void load(final CompoundBinaryTag tag, final Set<String> ignoredNames) {
        final CompoundBinaryTag source = tag.get(SAVED_DATA_WRAPPER) instanceof final CompoundBinaryTag wrapped
                ? wrapped
                : tag;

        for (final String name : source.keySet()) {
            if (name.equals(DATA_VERSION) || ignoredNames.contains(name)) {
                continue;
            }
            final BinaryTag raw = source.get(name);
            if (raw == null) {
                continue;
            }

            int index = VanillaGameRules.indexOf(name);
            Integer value = index < 0 ? null : readValue(raw);

            if (index < 0) {
                final VanillaGameRules.Legacy legacy = VanillaGameRules.legacy(name);
                if (legacy != null) {
                    index = VanillaGameRules.indexOf(legacy.target().key());
                    final Integer legacyValue = readValue(raw);
                    value = legacyValue == null ? null : legacy.convert().applyAsInt(legacyValue);
                } else if (Key.parseable(name)) {
                    unknown.put(name, raw);
                    LOGGER.debug("Unknown game rule '{}' kept as-is", name);
                    continue;
                } else {
                    LOGGER.warn("Ignoring unknown legacy game rule '{}'", name);
                    continue;
                }
            }

            if (value == null) {
                LOGGER.warn("Ignoring unreadable value {} for game rule '{}'", raw, name);
                continue;
            }
            final GameRuleDefinition definition = VanillaGameRules.ALL.get(index);
            values.set(index, sanitize(definition, value, name));
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

    private static @Nullable Integer readValue(final BinaryTag raw) {
        if (raw instanceof final NumberBinaryTag number) {
            return number.intValue();
        }
        if (raw instanceof final StringBinaryTag string) {
            final String text = string.value().trim();
            if (text.equalsIgnoreCase("true")) {
                return 1;
            }
            if (text.equalsIgnoreCase("false")) {
                return 0;
            }
            try {
                return Integer.parseInt(text);
            } catch (final NumberFormatException e) {
                return null;
            }
        }
        return null;
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
