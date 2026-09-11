package fr.euphyllia.fidorial.server.world.structure.jigsaw;

import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.pool.PoolElement;
import fr.euphyllia.fidorial.server.world.structure.pool.Projection;
import fr.fidorial.world.structure.StructureRotation;

public record PlacedPiece(
        PoolElement element,
        int x,
        int y,
        int z,
        StructureRotation rotation,
        Box box,
        int groundLevelDelta
) {

    public Projection projection() {
        return element.projection();
    }

    public PlacedPiece moved(final int dy) {
        return new PlacedPiece(element, x, y + dy, z, rotation, box.moved(0, dy, 0), groundLevelDelta);
    }
}
