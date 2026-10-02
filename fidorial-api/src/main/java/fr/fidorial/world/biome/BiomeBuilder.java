package fr.fidorial.world.biome;

import com.google.common.base.Preconditions;
import fr.fidorial.world.environment.AmbientParticle;
import fr.fidorial.world.environment.AmbientSounds;
import fr.fidorial.world.environment.BackgroundMusic;
import fr.fidorial.world.environment.EnvironmentAttributes;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Fluent builder for {@link BiomeDefinition}.
 *
 * @since 0.1.0
 */
public final class BiomeBuilder {

    private Key key;
    private boolean hasPrecipitation = true;
    private float temperature = 0.8F;
    private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
    private float downfall = 0.4F;
    private BiomeEffects effects = BiomeEffects.DEFAULT;
    private EnvironmentAttributes.Builder attributes = EnvironmentAttributes.builder();

    BiomeBuilder(final Key key) {
        Preconditions.checkArgument(key != null, "The key of a biome builder must not be null");
        this.key = key;
    }

    BiomeBuilder(final BiomeDefinition definition) {
        this.key = definition.key();
        this.hasPrecipitation = definition.hasPrecipitation();
        this.temperature = definition.temperature();
        this.temperatureModifier = definition.temperatureModifier();
        this.downfall = definition.downfall();
        this.effects = definition.effects();
        this.attributes = EnvironmentAttributes.builder(definition.attributes());
    }

    /**
     * @param key the namespaced identifier of the biome
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder key(final Key key) {
        Preconditions.checkArgument(key != null, "The key passed to BiomeBuilder.key() must not be null");
        this.key = key;
        return this;
    }

    /**
     * @param hasPrecipitation whether rain or snow may fall in the biome
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder hasPrecipitation(final boolean hasPrecipitation) {
        this.hasPrecipitation = hasPrecipitation;
        return this;
    }

    /**
     * Sets the biome temperature.
     *
     * <p>Below {@code 0.15} water freezes and precipitation falls as snow; above {@code 1.0} the
     * client stops rendering rain entirely.</p>
     *
     * @param temperature the biome temperature
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder temperature(final float temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * @param modifier how the client reinterprets the temperature
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder temperatureModifier(final TemperatureModifier modifier) {
        Preconditions.checkArgument(modifier != null, "The modifier passed to BiomeBuilder.temperatureModifier() must not be null");
        this.temperatureModifier = modifier;
        return this;
    }

    /**
     * @param downfall humidity within {@code [0, 1]}
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder downfall(final float downfall) {
        this.downfall = downfall;
        return this;
    }

    /**
     * @param effects the client-side effects
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder effects(final BiomeEffects effects) {
        Preconditions.checkArgument(effects != null, "The effects passed to BiomeBuilder.effects() must not be null");
        this.effects = effects;
        return this;
    }

    /**
     * Configures the effects in place, starting from the ones currently set.
     *
     * @param configurer callback receiving a pre-filled effects builder
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder effects(final Consumer<BiomeEffects.Builder> configurer) {
        final BiomeEffects.Builder builder = BiomeEffects.builder(this.effects);
        configurer.accept(builder);
        this.effects = builder.build();
        return this;
    }

    /**
     * Replaces the whole environment attribute map.
     *
     * @param attributes the attributes this biome sets
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder attributes(final EnvironmentAttributes attributes) {
        this.attributes = EnvironmentAttributes.builder(attributes);
        return this;
    }

    /**
     * Configures the environment attributes in place, starting from what is already set.
     *
     * @param configurer callback receiving the attribute builder
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder attributes(final Consumer<EnvironmentAttributes.Builder> configurer) {
        configurer.accept(this.attributes);
        return this;
    }

    /**
     * Shorthand for {@code minecraft:visual/sky_color}.
     *
     * @param color packed RGB color of the sky
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder skyColor(final @Nullable Integer color) {
        this.attributes.skyColor(color);
        return this;
    }

    /**
     * Shorthand for {@code minecraft:visual/fog_color}.
     *
     * @param color packed RGB color of the distance fog
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder fogColor(final @Nullable Integer color) {
        this.attributes.fogColor(color);
        return this;
    }

    /**
     * Shorthand for {@code minecraft:visual/water_fog_color}.
     *
     * @param color packed RGB color of the underwater fog
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder waterFogColor(final @Nullable Integer color) {
        this.attributes.waterFogColor(color);
        return this;
    }

    /**
     * Shorthand for {@code minecraft:visual/ambient_particles}.
     *
     * @param particle the particle to add
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder addAmbientParticle(final AmbientParticle particle) {
        this.attributes.addAmbientParticle(particle);
        return this;
    }

    /**
     * Shorthand for {@code minecraft:audio/ambient_sounds}.
     *
     * @param sounds the ambient sounds, or {@code null} to leave unset
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder ambientSounds(final @Nullable AmbientSounds sounds) {
        this.attributes.ambientSounds(sounds);
        return this;
    }

    /**
     * Shorthand for {@code minecraft:audio/background_music}.
     *
     * @param music the background music, or {@code null} to leave unset
     * @return this builder
     */
    @Contract("_ -> this")
    public BiomeBuilder backgroundMusic(final @Nullable BackgroundMusic music) {
        this.attributes.backgroundMusic(music);
        return this;
    }

    /**
     * Sets the sky color to the value vanilla would derive from the current temperature.
     *
     * <p>Call this after {@link #temperature(float)}.</p>
     *
     * @return this builder
     */
    @Contract("-> this")
    public BiomeBuilder vanillaSkyColor() {
        return skyColor(EnvironmentAttributes.skyColorFor(this.temperature));
    }

    /**
     * {@return the immutable definition described by this builder}
     */
    @Contract(value = "-> new", pure = true)
    public BiomeDefinition build() {
        return new BiomeDefinition(
                key, hasPrecipitation, temperature, temperatureModifier, downfall, effects, attributes.build());
    }
}
