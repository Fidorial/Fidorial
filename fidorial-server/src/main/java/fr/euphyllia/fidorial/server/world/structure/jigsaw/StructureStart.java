package fr.euphyllia.fidorial.server.world.structure.jigsaw;

import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.worldgen.JigsawStructure;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record StructureStart(@Nullable JigsawStructure structure, List<PlacedPiece> pieces, @Nullable Box bounds) {

    public static final StructureStart EMPTY = new StructureStart(null, List.of(), null);

    public static StructureStart of(final JigsawStructure structure, final List<PlacedPiece> pieces) {
        Box bounds = null;
        for (final PlacedPiece piece : pieces) {
            bounds = bounds == null ? piece.box() : bounds.union(piece.box());
        }
        return new StructureStart(structure, List.copyOf(pieces), bounds);
    }

    public boolean isEmpty() {
        return pieces.isEmpty() || structure == null;
    }
}
