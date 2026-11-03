package fr.fidorial.world.dimension.types;

import fr.fidorial.registry.keys.DimensionTypeKeys;
import fr.fidorial.world.dimension.DimensionTypeDefinition;

/**
 * Provides the built-in Minecraft dimension type definitions.
 *
 * <p>These definitions correspond to the vanilla dimension types registered
 * by Minecraft and can be used when constructing or referencing vanilla
 * dimensions.</p>
 *
 * @apiNote The dimension types provided by this class mirror those registered
 * by vanilla and are not guaranteed to remain stable. New dimension types may
 * be added and existing dimension types may be removed in future Minecraft
 * versions without notice.
 *
 * @since 0.1.0
 */
public final class VanillaDimensionTypes {
    /**
     * The {@code minecraft:overworld} dimension type.
     *
     * @since 0.1.0
     */
    public static final DimensionTypeDefinition OVERWORLD = DimensionTypeDefinition.builder(DimensionTypeKeys.OVERWORLD).build();

    /**
     * The {@code minecraft:overworld_caves} dimension type.
     *
     * @since 0.1.0
     */
    public static final DimensionTypeDefinition OVERWORLD_CAVES = DimensionTypeDefinition.builder(DimensionTypeKeys.OVERWORLD_CAVES).build();

    /**
     * The {@code minecraft:the_end} dimension type.
     *
     * @since 0.1.0
     */
    public static final DimensionTypeDefinition THE_END = DimensionTypeDefinition.builder(DimensionTypeKeys.THE_END).build();

    /**
     * The {@code minecraft:the_nether} dimension type.
     *
     * @since 0.1.0
     */
    public static final DimensionTypeDefinition THE_NETHER = DimensionTypeDefinition.builder(DimensionTypeKeys.THE_NETHER).build();
}
