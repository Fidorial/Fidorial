package fr.fidorial.world.dimension;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * A value, or a random distribution of values, used by
 * {@link DimensionTypeDefinition#monsterSpawnLightLevel()}.
 *
 * @since 0.1.0
 */
public sealed interface IntProvider
        permits IntProvider.Constant, IntProvider.Uniform, IntProvider.BiasedToBottom,
        IntProvider.Clamped, IntProvider.ClampedNormal, IntProvider.WeightedList {

    /**
     * {@return a provider always yielding {@code value}}
     *
     * @param value the constant value
     */
    @Contract(value = "_ -> new", pure = true)
    static IntProvider constant(final int value) {
        return new Constant(value);
    }

    /**
     * {@return a provider picking uniformly within {@code [minInclusive, maxInclusive]}}
     *
     * @param minInclusive the minimum possible value
     * @param maxInclusive the maximum possible value
     */
    @Contract(value = "_, _ -> new", pure = true)
    static IntProvider uniform(final int minInclusive, final int maxInclusive) {
        return new Uniform(minInclusive, maxInclusive);
    }

    /**
     * {@return a provider within {@code [minInclusive, maxInclusive]}, weighted towards the bottom}
     *
     * @param minInclusive the minimum possible value
     * @param maxInclusive the maximum possible value
     */
    @Contract(value = "_, _ -> new", pure = true)
    static IntProvider biasedToBottom(final int minInclusive, final int maxInclusive) {
        return new BiasedToBottom(minInclusive, maxInclusive);
    }

    /**
     * {@return {@code source}, clamped to {@code [minInclusive, maxInclusive]}}
     *
     * @param minInclusive the minimum allowed value
     * @param maxInclusive the maximum allowed value
     * @param source       the provider being clamped
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static IntProvider clamped(final int minInclusive, final int maxInclusive, final IntProvider source) {
        return new Clamped(minInclusive, maxInclusive, source);
    }

    /**
     * {@return a provider following a normal distribution, clamped to {@code [minInclusive, maxInclusive]}}
     *
     * @param mean         the mean of the normal distribution
     * @param deviation    the deviation of the normal distribution
     * @param minInclusive the minimum allowed value
     * @param maxInclusive the maximum allowed value
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    static IntProvider clampedNormal(
            final float mean, final float deviation, final int minInclusive, final int maxInclusive) {
        return new ClampedNormal(mean, deviation, minInclusive, maxInclusive);
    }

    /**
     * {@return a provider picking randomly among {@code distribution}, honoring each entry's weight}
     *
     * @param distribution the pool to pick from, never empty
     */
    @Contract(value = "_ -> new", pure = true)
    static IntProvider weightedList(final List<WeightedList.Entry> distribution) {
        return new WeightedList(distribution);
    }

    /**
     * @param value the constant value
     * @since 0.1.0
     */
    record Constant(int value) implements IntProvider {
    }

    /**
     * @param minInclusive the minimum possible value
     * @param maxInclusive the maximum possible value
     * @since 0.1.0
     */
    record Uniform(int minInclusive, int maxInclusive) implements IntProvider {
        public Uniform {
            requireOrdered(minInclusive, maxInclusive);
        }
    }

    /**
     * @param minInclusive the minimum possible value
     * @param maxInclusive the maximum possible value
     * @since 0.1.0
     */
    record BiasedToBottom(int minInclusive, int maxInclusive) implements IntProvider {
        public BiasedToBottom {
            requireOrdered(minInclusive, maxInclusive);
        }
    }

    /**
     * @param minInclusive the minimum allowed value that the result will be
     * @param maxInclusive the maximum allowed value that the result will be
     * @param source       the provider being clamped
     * @since 0.1.0
     */
    record Clamped(int minInclusive, int maxInclusive, IntProvider source) implements IntProvider {
        public Clamped {
            requireOrdered(minInclusive, maxInclusive);
            Preconditions.checkArgument(source != null, "The source of an int provider clamped must not be null");
        }
    }

    /**
     * @param mean         the mean of the normal distribution
     * @param deviation    the deviation of the normal distribution
     * @param minInclusive the minimum allowed value that the result will be
     * @param maxInclusive the maximum allowed value that the result will be
     * @since 0.1.0
     */
    record ClampedNormal(float mean, float deviation, int minInclusive, int maxInclusive) implements IntProvider {
        public ClampedNormal {
            requireOrdered(minInclusive, maxInclusive);
        }
    }

    /**
     * @param distribution the pool of weighted entries to pick from, never empty
     * @since 0.1.0
     */
    record WeightedList(List<Entry> distribution) implements IntProvider {

        public WeightedList {
            distribution = List.copyOf(distribution);
            if (distribution.isEmpty()) {
                throw new IllegalArgumentException("A weighted list int provider needs at least one entry");
            }
        }

        /**
         * One entry of a {@link WeightedList}.
         *
         * @param data   the value or nested provider this entry yields
         * @param weight this entry's weight; must be positive
         * @since 0.1.0
         */
        public record Entry(IntProvider data, int weight) {
            public Entry {
                Preconditions.checkArgument(data != null, "The data of an int provider weighted list entry must not be null");
                if (weight <= 0) {
                    throw new IllegalArgumentException("The weight of an int provider entry must be positive, got " + weight);
                }
            }
        }
    }

    private static void requireOrdered(final int minInclusive, final int maxInclusive) {
        if (maxInclusive < minInclusive) {
            throw new IllegalArgumentException(
                    "maxInclusive (" + maxInclusive + ") must not be less than minInclusive (" + minInclusive + ")");
        }
    }
}
