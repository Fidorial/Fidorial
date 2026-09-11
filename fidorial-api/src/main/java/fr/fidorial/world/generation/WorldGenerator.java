package fr.fidorial.world.generation;

import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;

@FunctionalInterface
public interface WorldGenerator {

    void generate(GeneratedChunk chunk);

    default DimensionTypeDefinition dimensionType() {
        return VanillaDimensionTypes.OVERWORLD;
    }

    default GenerationDescriptor describeForSave() {
        return GenerationDescriptor.unknown();
    }

    default boolean generatesStructures() {
        return true;
    }
}
