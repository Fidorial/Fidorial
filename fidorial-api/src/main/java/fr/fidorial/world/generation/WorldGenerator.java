package fr.fidorial.world.generation;

import fr.fidorial.Server;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.WorldSpec;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;

/**
 * Fills the chunks of a world when they are generated for the first time.
 *
 * <p>Plugins provide one through {@link Server#createWorld(WorldSpec)}, or
 * replace the default one by registering it as a {@linkplain ServiceRegistry service}.
 * Generation runs on chunk worker threads: implementations must be thread-safe.</p>
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface WorldGenerator {

    /**
     * Fills a freshly created chunk.
     *
     * @param chunk the chunk to fill
     * @since 0.1.0
     */
    void generate(GeneratedChunk chunk);

    /**
     * {@return the dimension type of the worlds this generator drives, the overworld by default}
     *
     * @since 0.1.0
     */
    default DimensionTypeDefinition dimensionType() {
        return VanillaDimensionTypes.OVERWORLD;
    }

    /**
     * {@return how this generator is described in {@code level.dat}, for vanilla tools to read}
     *
     * @since 0.1.0
     */
    default GenerationDescriptor describeForSave() {
        return GenerationDescriptor.unknown();
    }

    /**
     * {@return {@code true} if datapack structures are placed on top of this generator}
     *
     * @since 0.1.0
     */
    default boolean generatesStructures() {
        return true;
    }
}
