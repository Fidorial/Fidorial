package fr.fidorial.world;

import com.google.common.base.Preconditions;
import fr.fidorial.Server;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable description of a world to create through {@link Server#createWorld(WorldSpec)}.
 *
 * <p>Instances are obtained from {@link #builder(Key)}; {@link #toBuilder()} derives a modified copy.</p>
 *
 * @since 0.1.0
 */
public final class WorldSpec {

    private final Key key;
    private final long seed;
    private final @Nullable WorldGenerator generator;

    private WorldSpec(final Builder builder) {
        this.key = builder.key;
        this.seed = builder.seed;
        this.generator = builder.generator;
    }

    /**
     * Creates a new builder for a world identified by the given key.
     *
     * @param key the namespaced identifier of the world to create; also used as its
     *            {@link World#key() world key}
     * @return a fresh builder
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    public static Builder builder(final Key key) {
        return new Builder(key);
    }

    /**
     * Returns the namespaced identifier of the world.
     *
     * <p>This is the value that will be exposed as {@link World#key()} once the world exists and the
     * value used to look the world up again through {@link fr.fidorial.Server#world(Key)}.</p>
     *
     * @return the world key
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Key key() {
        return key;
    }

    /**
     * Returns the world seed.
     *
     * @return the world seed, or {@code 0} when none was set
     * @since 0.1.0
     */
    @Contract(pure = true)
    public long seed() {
        return seed;
    }

    /**
     * Returns the custom chunk generator for the world, if one was supplied.
     *
     * <p>When empty, the server falls back to its built-in default generator.</p>
     *
     * @return the generator, or an empty optional to use the server default
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<WorldGenerator> generator() {
        return Optional.ofNullable(generator);
    }

    /**
     * Returns a builder pre-populated with the values of this spec, so a modified copy can be
     * derived without mutating the original.
     *
     * @return a builder seeded with this spec's values
     * @since 0.1.0
     */
    @Contract(value = "-> new", pure = true)
    public Builder toBuilder() {
        return new Builder(key).seed(seed).generator(generator);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof final WorldSpec spec
                && seed == spec.seed
                && key.equals(spec.key)
                && Objects.equals(generator, spec.generator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, seed, generator);
    }

    /**
     * A fluent builder for {@link WorldSpec}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private Key key;
        private long seed;
        private @Nullable WorldGenerator generator;

        private Builder(final Key key) {
            Preconditions.checkArgument(key != null, "The key of a world spec builder must not be null");
            this.key = key;
        }

        /**
         * Sets the namespaced identifier of the world.
         *
         * @param key the world key
         * @return this builder
         * @since 0.1.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder key(final Key key) {
            Preconditions.checkArgument(key != null, "The key passed to WorldSpec.Builder.key() must not be null");
            this.key = key;
            return this;
        }

        /**
         * Sets the world seed. See {@link WorldSpec#seed()} for how the seed is used.
         *
         * @param seed the world seed
         * @return this builder
         * @since 0.1.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder seed(final long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Sets the custom chunk generator for the world.
         *
         * @param generator the generator, or {@code null} to use the server default
         * @return this builder
         * @since 0.1.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder generator(final @Nullable WorldGenerator generator) {
            this.generator = generator;
            return this;
        }

        /**
         * Builds an immutable {@link WorldSpec} from the current builder state.
         *
         * @return a new spec
         * @since 0.1.0
         */
        @Contract(value = "-> new", pure = true)
        public WorldSpec build() {
            return new WorldSpec(this);
        }
    }
}
