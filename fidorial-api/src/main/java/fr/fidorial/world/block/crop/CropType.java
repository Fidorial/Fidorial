package fr.fidorial.world.block.crop;

import fr.fidorial.registry.keys.BlockTypeKeys;
import net.kyori.adventure.key.Key;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A plantable crop: the seed that plants it, the block it grows as, and what it
 * accepts as soil.
 *
 * @since 0.1.0
 */
public final class CropType {

    public static final String DEFAULT_AGE_PROPERTY = "age";

    private final Key seed;
    private final Key block;
    private final String ageProperty;
    private final int maxAge;
    private final Set<Key> soils;
    private final boolean requiresMoistSoil;
    private final int averageTicksPerStage;
    private final int minLight;

    private CropType(final Builder builder) {
        this.seed = builder.seed;
        this.block = builder.block;
        this.ageProperty = builder.ageProperty;
        this.maxAge = builder.maxAge;
        this.soils = Set.copyOf(builder.soils);
        this.requiresMoistSoil = builder.requiresMoistSoil;
        this.averageTicksPerStage = builder.averageTicksPerStage;
        this.minLight = builder.minLight;
    }

    /**
     * @param seed  the item that plants this crop
     * @param block the block the crop grows as
     * @return a builder, defaulting to farmland soil and a four-stage {@code age}
     * @since 0.1.0
     */
    public static Builder builder(final Key seed, final Key block) {
        return new Builder(seed, block);
    }

    /**
     * @return the item that plants this crop
     * @since 0.1.0
     */
    public Key seed() {
        return seed;
    }

    /**
     * @return the block this crop grows as
     * @since 0.1.0
     */
    public Key block() {
        return block;
    }

    /**
     * @return the block property carrying the growth stage
     * @since 0.1.0
     */
    public String ageProperty() {
        return ageProperty;
    }

    /**
     * @return the growth stage at which the crop is ripe
     * @since 0.1.0
     */
    public int maxAge() {
        return maxAge;
    }

    /**
     * @return the blocks this crop can be planted on
     * @since 0.1.0
     */
    public Set<Key> soils() {
        return soils;
    }

    /**
     * @return {@code true} when the soil has to be wet for planting to work
     * @since 0.1.0
     */
    public boolean requiresMoistSoil() {
        return requiresMoistSoil;
    }

    /**
     * How long one growth stage takes, on average.
     *
     * @return the average number of ticks one growth stage takes
     * @since 0.1.0
     */
    public int averageTicksPerStage() {
        return averageTicksPerStage;
    }

    /**
     * @return the light level the crop needs to grow; {@code 0} means it grows in the dark
     * @since 0.1.0
     */
    public int minLight() {
        return minLight;
    }

    /**
     * @param soil a block identifier
     * @return {@code true} when this crop accepts that block as soil
     * @since 0.1.0
     */
    public boolean acceptsSoil(final Key soil) {
        return soils.contains(soil);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final CropType other
                && seed.equals(other.seed)
                && block.equals(other.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed, block);
    }

    @Override
    public String toString() {
        return "CropType[seed=" + seed.asString() + ", block=" + block.asString() + "]";
    }

    /**
     * Assembles a {@link CropType}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private final Key seed;
        private final Key block;
        private String ageProperty = DEFAULT_AGE_PROPERTY;
        private int maxAge = 7;
        private final Set<Key> soils = new LinkedHashSet<>(Set.of(BlockTypeKeys.FARMLAND.key()));
        private boolean requiresMoistSoil = true;
        private int averageTicksPerStage = 600;
        private int minLight = 0;

        private Builder(final Key seed, final Key block) {
            this.seed = Objects.requireNonNull(seed, "seed");
            this.block = Objects.requireNonNull(block, "block");
        }

        /**
         * @param ageProperty the block property carrying the growth stage
         * @return this builder
         * @since 0.1.0
         */
        public Builder ageProperty(final String ageProperty) {
            this.ageProperty = Objects.requireNonNull(ageProperty, "ageProperty");
            return this;
        }

        /**
         * @param maxAge the growth stage at which the crop is ripe
         * @return this builder
         * @since 0.1.0
         */
        public Builder maxAge(final int maxAge) {
            if (maxAge < 1) {
                throw new IllegalArgumentException("maxAge must be at least 1, got " + maxAge);
            }
            this.maxAge = maxAge;
            return this;
        }

        /**
         * Replaces the accepted soils. Defaults to farmland alone.
         *
         * @param soils the blocks the crop can be planted on
         * @return this builder
         * @since 0.1.0
         */
        public Builder soils(final Set<Key> soils) {
            if (soils.isEmpty()) {
                throw new IllegalArgumentException("A crop needs at least one soil");
            }
            this.soils.clear();
            this.soils.addAll(soils);
            return this;
        }

        /**
         * @param requiresMoistSoil {@code false} to let the crop be planted on dry soil
         * @return this builder
         * @since 0.1.0
         */
        public Builder requiresMoistSoil(final boolean requiresMoistSoil) {
            this.requiresMoistSoil = requiresMoistSoil;
            return this;
        }

        /**
         * @param averageTicksPerStage the average number of ticks one growth stage should take
         * @return this builder
         * @since 0.1.0
         */
        public Builder averageTicksPerStage(final int averageTicksPerStage) {
            if (averageTicksPerStage < 1) {
                throw new IllegalArgumentException(
                        "averageTicksPerStage must be at least 1, got " + averageTicksPerStage);
            }
            this.averageTicksPerStage = averageTicksPerStage;
            return this;
        }

        /**
         * @param minLight the light level the crop needs to grow; {@code 0} to let it grow in the dark
         * @return this builder
         * @since 0.1.0
         */
        public Builder minLight(final int minLight) {
            this.minLight = Math.clamp(minLight, 0, 15);
            return this;
        }

        /**
         * @return the crop
         * @since 0.1.0
         */
        public CropType build() {
            return new CropType(this);
        }
    }
}
