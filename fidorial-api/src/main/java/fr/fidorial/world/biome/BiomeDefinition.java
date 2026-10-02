package fr.fidorial.world.biome;

import com.google.common.base.Preconditions;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Biome;
import fr.fidorial.world.environment.EnvironmentAttributes;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;


/**
 * A complete biome definition, ready to be sent to clients.
 *
 * @param key                 the namespaced identifier of the biome
 * @param hasPrecipitation    whether rain or snow may fall
 * @param temperature         biome temperature, driving snow, freezing water and the default colors
 * @param temperatureModifier how the client reinterprets {@code temperature}
 * @param downfall            humidity, within {@code [0, 1]}, driving the default foliage colors
 * @param effects             the client-side tinting
 * @param attributes          the environment attributes this biome sets, possibly empty
 * @since 0.1.0
 */
public record BiomeDefinition(
        Key key,
        boolean hasPrecipitation,
        float temperature,
        TemperatureModifier temperatureModifier,
        float downfall,
        BiomeEffects effects,
        EnvironmentAttributes attributes
) implements Biome {

    public BiomeDefinition {
        Preconditions.checkArgument(key != null, "The key of a biome definition must not be null");
        Preconditions.checkArgument(temperatureModifier != null, "The temperature modifier of a biome definition must not be null");
        Preconditions.checkArgument(effects != null, "The effects of a biome definition must not be null");
        Preconditions.checkArgument(attributes != null, "The attributes of a biome definition must not be null");
        if (downfall < 0F || downfall > 1F) {
            throw new IllegalArgumentException("The downfall of a biome must be within [0, 1], got " + downfall);
        }
    }

    /**
     * {@return a new builder for the biome identified by {@code key}}
     *
     * @param key the namespaced identifier of the biome
     */
    @Contract(value = "_ -> new", pure = true)
    public static BiomeBuilder builder(final Key key) {
        return new BiomeBuilder(key);
    }

    /**
     * {@return a new builder pre-filled with the values of {@code definition}}
     *
     * @param definition the definition to copy
     */
    @Contract(value = "_ -> new", pure = true)
    public static BiomeBuilder builder(final BiomeDefinition definition) {
        return new BiomeBuilder(definition);
    }

    /**
     * {@return this biome's key, typed against the {@code minecraft:worldgen/biome} registry}
     */
    @Contract(value = "-> new", pure = true)
    public TypedKey<Biome> typedKey() {
        return TypedKey.create(RegistryKey.BIOME, key);
    }
}
