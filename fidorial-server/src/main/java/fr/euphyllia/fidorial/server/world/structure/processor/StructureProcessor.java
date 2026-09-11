package fr.euphyllia.fidorial.server.world.structure.processor;

import org.jspecify.annotations.Nullable;

import java.util.List;

public interface StructureProcessor {

    @Nullable PlacedBlock process(ProcessContext context, int localX, int localY, int localZ, PlacedBlock current);

    default void finish(final ProcessContext context, final List<int[]> originals, final List<PlacedBlock> processed) {
    }

    default boolean needsWholePiece() {
        return false;
    }
}
