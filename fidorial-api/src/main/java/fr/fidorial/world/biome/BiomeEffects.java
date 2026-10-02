package fr.fidorial.world.biome;

import com.google.common.base.Preconditions;
import fr.fidorial.world.environment.EnvironmentAttributes;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;


/**
 * The client-side tinting a biome applies.
 *
 * @param waterColor         tint applied to water and cauldrons
 * @param foliageColor       leaf and vine tint, or {@code null} to let the client compute it
 * @param grassColor         grass tint, or {@code null} to let the client compute it
 * @param dryFoliageColor    leaf litter tint, or {@code null} for the default
 * @param grassColorModifier post-processing applied on top of the grass color
 * @since 0.1.0
 */
public record BiomeEffects(
        int waterColor,
        @Nullable Integer foliageColor,
        @Nullable Integer grassColor,
        @Nullable Integer dryFoliageColor,
        GrassColorModifier grassColorModifier
) {

    /**
     * Overworld temperate defaults, matching {@code minecraft:plains}.
     */
    public static final BiomeEffects DEFAULT = builder().build();

    public BiomeEffects {
        Preconditions.checkArgument(grassColorModifier != null, "The grass color modifier of a biome effects must not be null");
    }

    /**
     * {@return a new builder pre-filled with vanilla overworld values}
     */
    @Contract(value = "-> new", pure = true)
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@return a new builder pre-filled with the values of {@code effects}}
     *
     * @param effects the effects to copy
     */
    @Contract(value = "_ -> new", pure = true)
    public static Builder builder(final BiomeEffects effects) {
        return new Builder(effects);
    }

    /**
     * Computes the sky color the vanilla generator derives from a given temperature.
     *
     * @param temperature the biome temperature
     * @return the packed RGB sky color
     * @deprecated moved alongside the attribute it feeds; use
     * {@link EnvironmentAttributes#skyColorFor(float)}
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public static int skyColorFor(final float temperature) {
        return EnvironmentAttributes.skyColorFor(temperature);
    }

    /**
     * Mutable builder for {@link BiomeEffects}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private int waterColor = 0x3F76E4;
        private @Nullable Integer foliageColor;
        private @Nullable Integer grassColor;
        private @Nullable Integer dryFoliageColor;
        private GrassColorModifier grassColorModifier = GrassColorModifier.NONE;

        private Builder() {
        }

        private Builder(final BiomeEffects effects) {
            this.waterColor = effects.waterColor;
            this.foliageColor = effects.foliageColor;
            this.grassColor = effects.grassColor;
            this.dryFoliageColor = effects.dryFoliageColor;
            this.grassColorModifier = effects.grassColorModifier;
        }

        /**
         * @param color packed RGB tint applied to water
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder waterColor(final int color) {
            this.waterColor = color;
            return this;
        }

        /**
         * @param color packed RGB leaf tint, or {@code null} to let the client compute it
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder foliageColor(final @Nullable Integer color) {
            this.foliageColor = color;
            return this;
        }

        /**
         * @param color packed RGB grass tint, or {@code null} to let the client compute it
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder grassColor(final @Nullable Integer color) {
            this.grassColor = color;
            return this;
        }

        /**
         * @param color packed RGB leaf litter tint, or {@code null} for the default
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder dryFoliageColor(final @Nullable Integer color) {
            this.dryFoliageColor = color;
            return this;
        }

        /**
         * @param modifier post-processing applied on top of the grass color
         * @return this builder
         */
        @Contract("_ -> this")
        public Builder grassColorModifier(final GrassColorModifier modifier) {
            Preconditions.checkArgument(modifier != null, "The modifier passed to BiomeEffects.Builder.grassColorModifier() must not be null");
            this.grassColorModifier = modifier;
            return this;
        }

        /**
         * {@return the immutable effects described by this builder}
         */
        @Contract(value = "-> new", pure = true)
        public BiomeEffects build() {
            return new BiomeEffects(
                    waterColor,
                    foliageColor,
                    grassColor,
                    dryFoliageColor,
                    grassColorModifier);
        }
    }
}
