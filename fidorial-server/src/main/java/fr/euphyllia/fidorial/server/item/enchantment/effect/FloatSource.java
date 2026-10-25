package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.random.RandomGenerator;

/**
 * A number provider, either a loot number provider ({@code constant}, {@code uniform},
 * {@code enchantment_level}) or a float provider ({@code uniform}, {@code trapezoid},
 * {@code clamped_normal}).
 */
public interface FloatSource {

    /**
     * {@return a value}
     *
     * @param level  the level of the enchantment
     * @param random the random source
     */
    float sample(int level, RandomGenerator random);

    static FloatSource parse(final JsonElement element) {
        if (EffectJson.isNumber(element)) {
            final float value = element.getAsFloat();
            return (level, random) -> value;
        }
        final JsonObject object = EffectJson.object(element, "A number provider");
        return switch (LevelValue.vanillaType(object)) {
            case "constant" -> {
                final float value = EffectJson.number(object, "value");
                yield (level, random) -> value;
            }
            case "uniform" -> {
                if (object.has("min_inclusive")) {
                    final float min = EffectJson.number(object, "min_inclusive");
                    final float max = EffectJson.number(object, "max_exclusive");
                    yield (level, random) -> between(random, min, max);
                }
                final FloatSource min = parse(object.get("min"));
                final FloatSource max = parse(object.get("max"));
                yield (level, random) -> between(random, min.sample(level, random), max.sample(level, random));
            }
            case "enchantment_level" -> {
                final LevelValue amount = LevelValue.parse(object.get("amount"));
                yield (level, random) -> amount.at(level);
            }
            case "trapezoid" -> {
                final float min = EffectJson.number(object, "min");
                final float max = EffectJson.number(object, "max");
                final float plateau = EffectJson.number(object, "plateau");
                yield (level, random) -> {
                    // Sum of two uniform draws, flat over the middle of the range.
                    final float range = max - min;
                    final float ramp = (range - plateau) / 2.0f;
                    final float spread = range - ramp;
                    return min + random.nextFloat() * spread + random.nextFloat() * ramp;
                };
            }
            case "clamped_normal" -> {
                final float mean = EffectJson.number(object, "mean");
                final float deviation = EffectJson.number(object, "deviation");
                final float min = EffectJson.number(object, "min");
                final float max = EffectJson.number(object, "max");
                yield (level, random) -> Math.clamp(mean + (float) random.nextGaussian() * deviation, min, Math.max(min, max));
            }
            default -> throw new UnsupportedEffectException("the number provider " + object.get("type") + " is not supported");
        };
    }

    static float between(final RandomGenerator random, final float min, final float max) {
        return max <= min ? min : min + random.nextFloat() * (max - min);
    }
}
