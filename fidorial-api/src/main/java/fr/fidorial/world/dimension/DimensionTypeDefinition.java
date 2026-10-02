package fr.fidorial.world.dimension;

import com.google.common.base.Preconditions;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DimensionType;
import fr.fidorial.world.environment.EnvironmentAttributes;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A complete dimension type definition, ready to be sent to clients.
 *
 * @param key                         the namespaced identifier of the dimension type
 * @param coordinateScale             multiplier applied to coordinates when leaving the dimension,
 *                                    within {@code [0.00001, 30000000.0]}
 * @param hasSkylight                 whether the dimension has skylight; disables weather when {@code false}
 * @param hasCeiling                  whether the dimension has a logical bedrock ceiling
 * @param hasEnderDragonFight         whether this dimension can host an ender dragon fight
 * @param ambientLight                how much light the dimension has regardless of the light level
 * @param hasFixedTime                whether this dimension has fixed time
 * @param monsterSpawnBlockLightLimit block light must be at or below this, within {@code [0, 15]},
 *                                    for monsters to spawn
 * @param monsterSpawnLightLevel      the computed light level must be at or below this for monsters
 *                                    to spawn
 * @param logicalHeight               the maximum height chorus fruits and Nether portals bring
 *                                    players to; must not exceed {@code height}
 * @param minY                        lowest buildable height, a multiple of 16 within {@code [-2032, 2031]}
 * @param height                      total buildable height, a multiple of 16 within {@code [16, 4064]};
 *                                    {@code minY + height - 1} must not exceed {@code 2031}
 * @param infiniburn                  the block tag whose blocks burn forever
 * @param skybox                      which skybox the client renders
 * @param cardinalLight               which direction cardinal light comes from
 * @param attributes                  the environment attributes this dimension sets, possibly empty
 * @param defaultClock                the world clock used as default for this dimension, or
 *                                    {@code null} if it has none
 * @param timelines                   the timelines active in this dimension, possibly empty
 * @since 0.1.0
 */
public record DimensionTypeDefinition(
        Key key,
        double coordinateScale,
        boolean hasSkylight,
        boolean hasCeiling,
        boolean hasEnderDragonFight,
        float ambientLight,
        boolean hasFixedTime,
        int monsterSpawnBlockLightLimit,
        IntProvider monsterSpawnLightLevel,
        int logicalHeight,
        int minY,
        int height,
        Key infiniburn,
        Skybox skybox,
        CardinalLight cardinalLight,
        EnvironmentAttributes attributes,
        @Nullable Key defaultClock,
        List<TimelineReference> timelines
) implements DimensionType {

    public DimensionTypeDefinition {
        Preconditions.checkArgument(key != null, "The key of a dimension type definition must not be null");
        Preconditions.checkArgument(monsterSpawnLightLevel != null, "The monster spawn light level of a dimension type definition must not be null");
        Preconditions.checkArgument(infiniburn != null, "The infiniburn of a dimension type definition must not be null");
        Preconditions.checkArgument(skybox != null, "The skybox of a dimension type definition must not be null");
        Preconditions.checkArgument(cardinalLight != null, "The cardinal light of a dimension type definition must not be null");
        Preconditions.checkArgument(attributes != null, "The attributes of a dimension type definition must not be null");
        timelines = List.copyOf(timelines);

        if (coordinateScale < 0.00001D || coordinateScale > 30_000_000.0D) {
            throw new IllegalArgumentException(
                    "coordinateScale must be within [0.00001, 30000000.0], got " + coordinateScale);
        }
        if (monsterSpawnBlockLightLimit < 0 || monsterSpawnBlockLightLimit > 15) {
            throw new IllegalArgumentException(
                    "monsterSpawnBlockLightLimit must be within [0, 15], got " + monsterSpawnBlockLightLimit);
        }
        if (minY < -2032 || minY > 2031 || minY % 16 != 0) {
            throw new IllegalArgumentException("The minimum Y of a dimension type must be a multiple of 16 within [-2032, 2031], got " + minY);
        }
        if (height < 16 || height > 4064 || height % 16 != 0) {
            throw new IllegalArgumentException("The height of a dimension type must be a multiple of 16 within [16, 4064], got " + height);
        }
        if (minY + height - 1 > 2031) {
            throw new IllegalArgumentException(
                    "minY + height - 1 must not exceed 2031, got " + (minY + height - 1));
        }
        if (logicalHeight > height) {
            throw new IllegalArgumentException(
                    "logicalHeight (" + logicalHeight + ") must not exceed height (" + height + ")");
        }
    }

    /**
     * {@return a new builder for the dimension type identified by {@code key}}
     *
     * @param key the namespaced identifier of the dimension type
     */
    @Contract(value = "_ -> new", pure = true)
    public static DimensionTypeBuilder builder(final Key key) {
        return new DimensionTypeBuilder(key);
    }

    /**
     * {@return a new builder pre-filled with the values of {@code definition}}
     *
     * @param definition the definition to copy
     */
    @Contract(value = "_ -> new", pure = true)
    public static DimensionTypeBuilder builder(final DimensionTypeDefinition definition) {
        return new DimensionTypeBuilder(definition);
    }

    /**
     * {@return this dimension type's key, typed against the {@code minecraft:dimension_type} registry}
     */
    @Contract(value = "-> new", pure = true)
    public TypedKey<DimensionType> typedKey() {
        return TypedKey.create(RegistryKey.DIMENSION_TYPE, key);
    }
}
