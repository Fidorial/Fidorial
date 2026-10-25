package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * A level-based value: a constant, or one of {@code linear}, {@code clamped}, {@code fraction},
 * {@code levels_squared}, {@code lookup} and {@code exponent}.
 */
public sealed interface LevelValue {

    /**
     * {@return the value at {@code level}}
     *
     * @param level the level of the enchantment, from {@code 1}
     */
    float at(int level);

    static LevelValue parse(final JsonElement element) {
        if (EffectJson.isNumber(element)) {
            return new Constant(element.getAsFloat());
        }
        final JsonObject object = EffectJson.object(element, "A level-based value");
        return switch (vanillaType(object)) {
            case "linear" -> new Linear(EffectJson.number(object, "base"), EffectJson.number(object, "per_level_above_first"));
            case "clamped" -> new Clamped(parse(object.get("value")),
                    EffectJson.number(object, "min"), EffectJson.number(object, "max"));
            case "fraction" -> new Fraction(parse(object.get("numerator")), parse(object.get("denominator")));
            case "levels_squared" -> new LevelsSquared(EffectJson.number(object, "added"));
            case "lookup" -> {
                final List<Float> values = new ArrayList<>();
                for (final JsonElement value : EffectJson.list(object, "values")) {
                    if (!EffectJson.isNumber(value)) {
                        throw new IllegalArgumentException("A lookup value must be a number, got " + value);
                    }
                    values.add(value.getAsFloat());
                }
                yield new Lookup(List.copyOf(values), parse(object.get("fallback")));
            }
            case "exponent" -> new Exponent(parse(object.get("base")), parse(object.get("power")));
            default -> throw new IllegalArgumentException("Unknown level-based value type " + object.get("type"));
        };
    }

    /**
     * {@return the path of the {@code type} of {@code object}, which must be in the {@code minecraft}
     * namespace}
     */
    static String vanillaType(final JsonObject object) {
        final Key type = EffectJson.type(object);
        if (!Key.MINECRAFT_NAMESPACE.equals(type.namespace())) {
            throw new IllegalArgumentException("Unknown type " + type.asString());
        }
        return type.value();
    }

    record Constant(float value) implements LevelValue {
        @Override
        public float at(final int level) {
            return value;
        }
    }

    record Linear(float base, float perLevelAboveFirst) implements LevelValue {
        @Override
        public float at(final int level) {
            return base + perLevelAboveFirst * (level - 1);
        }
    }

    record Clamped(LevelValue value, float min, float max) implements LevelValue {
        @Override
        public float at(final int level) {
            return Math.clamp(value.at(level), min, Math.max(min, max));
        }
    }

    record Fraction(LevelValue numerator, LevelValue denominator) implements LevelValue {
        @Override
        public float at(final int level) {
            final float divisor = denominator.at(level);
            return divisor == 0f ? 0f : numerator.at(level) / divisor;
        }
    }

    record LevelsSquared(float added) implements LevelValue {
        @Override
        public float at(final int level) {
            return (float) level * level + added;
        }
    }

    record Lookup(List<Float> values, LevelValue fallback) implements LevelValue {
        @Override
        public float at(final int level) {
            return level >= 1 && level <= values.size() ? values.get(level - 1) : fallback.at(level);
        }
    }

    record Exponent(LevelValue base, LevelValue power) implements LevelValue {
        @Override
        public float at(final int level) {
            return (float) Math.pow(base.at(level), power.at(level));
        }
    }
}
