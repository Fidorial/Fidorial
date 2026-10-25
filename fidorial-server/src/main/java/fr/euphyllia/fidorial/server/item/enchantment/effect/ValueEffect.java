package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * A value effect of an enchantment: {@code add}, {@code multiply}, {@code set},
 * {@code remove_binomial}, {@code exponential} or {@code all_of}.
 */
public sealed interface ValueEffect {

    /**
     * {@return the changed value}
     *
     * @param level  the level of the enchantment
     * @param random the random source
     * @param value  the value so far
     */
    float apply(int level, RandomGenerator random, float value);

    static ValueEffect parse(final JsonElement element) {
        final JsonObject object = EffectJson.object(element, "A value effect");
        return switch (LevelValue.vanillaType(object)) {
            case "add" -> new Add(LevelValue.parse(object.get("value")));
            case "multiply" -> new Multiply(LevelValue.parse(object.get("factor")));
            case "set" -> new Assign(LevelValue.parse(object.get("value")));
            case "remove_binomial" -> new RemoveBinomial(LevelValue.parse(object.get("chance")));
            case "exponential" -> new Exponential(LevelValue.parse(object.get("base")), LevelValue.parse(object.get("exponent")));
            case "all_of" -> {
                final List<ValueEffect> effects = new ArrayList<>();
                for (final JsonElement effect : EffectJson.list(object, "effects")) {
                    effects.add(parse(effect));
                }
                yield new AllOf(List.copyOf(effects));
            }
            default -> throw new IllegalArgumentException("Unknown value effect type " + object.get("type"));
        };
    }

    record Add(LevelValue value) implements ValueEffect {
        @Override
        public float apply(final int level, final RandomGenerator random, final float current) {
            return current + value.at(level);
        }
    }

    record Multiply(LevelValue factor) implements ValueEffect {
        @Override
        public float apply(final int level, final RandomGenerator random, final float current) {
            return current * factor.at(level);
        }
    }

    record Assign(LevelValue value) implements ValueEffect {
        @Override
        public float apply(final int level, final RandomGenerator random, final float current) {
            return value.at(level);
        }
    }

    /**
     * Each unit of the value is taken away with a chance: what Unbreaking does to the durability a stack
     * is about to lose.
     */
    record RemoveBinomial(LevelValue chance) implements ValueEffect {
        @Override
        public float apply(final int level, final RandomGenerator random, final float current) {
            final float probability = chance.at(level);
            int removed = 0;
            for (int trial = 0; trial < (int) current; trial++) {
                if (random.nextFloat() < probability) {
                    removed++;
                }
            }
            return current - removed;
        }
    }

    record Exponential(LevelValue base, LevelValue exponent) implements ValueEffect {
        @Override
        public float apply(final int level, final RandomGenerator random, final float current) {
            return current * (float) Math.pow(base.at(level), exponent.at(level));
        }
    }

    record AllOf(List<ValueEffect> effects) implements ValueEffect {
        @Override
        public float apply(final int level, final RandomGenerator random, final float current) {
            float value = current;
            for (final ValueEffect effect : effects) {
                value = effect.apply(level, random, value);
            }
            return value;
        }
    }
}
