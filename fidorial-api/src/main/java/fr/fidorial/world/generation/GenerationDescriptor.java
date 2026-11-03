package fr.fidorial.world.generation;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;

/**
 * How a {@link WorldGenerator} is described in {@code level.dat}, so vanilla tools can make sense of the world.
 *
 * @since 0.1.0
 */
public sealed interface GenerationDescriptor {

    /**
     * {@return a descriptor saying nothing about the generator}
     *
     * @since 0.1.0
     */
    static GenerationDescriptor unknown() {
        return Unknown.INSTANCE;
    }

    /**
     * {@return a descriptor of a superflat world}
     *
     * @param floorBlock     the block the floor is made of
     * @param floorThickness the number of floor layers
     * @param biome          the biome of the whole world
     * @since 0.1.0
     */
    static GenerationDescriptor flat(final TypedKey<BlockType> floorBlock, final int floorThickness, final TypedKey<BiomeDefinition> biome) {
        return new Flat(floorBlock, floorThickness, biome);
    }

    /**
     * {@return a descriptor of a noise-based world}
     *
     * @param settings          the noise settings key, for instance {@code minecraft:overworld}
     * @param biomeSourcePreset the biome source preset key
     * @since 0.1.0
     */
    static GenerationDescriptor noise(final Key settings, final Key biomeSourcePreset) {
        return new Noise(settings, biomeSourcePreset);
    }

    /**
     * A superflat world.
     *
     * @param floorBlock     the block the floor is made of
     * @param floorThickness the number of floor layers
     * @param biome          the biome of the whole world
     * @since 0.1.0
     */
    record Flat(TypedKey<BlockType> floorBlock, int floorThickness, TypedKey<BiomeDefinition> biome) implements GenerationDescriptor {
    }

    /**
     * A noise-based world.
     *
     * @param settings          the noise settings key
     * @param biomeSourcePreset the biome source preset key
     * @since 0.1.0
     */
    record Noise(Key settings, Key biomeSourcePreset) implements GenerationDescriptor {
    }

    /**
     * A generator that describes nothing.
     *
     * @since 0.1.0
     */
    record Unknown() implements GenerationDescriptor {
        static final Unknown INSTANCE = new Unknown();
    }
}
