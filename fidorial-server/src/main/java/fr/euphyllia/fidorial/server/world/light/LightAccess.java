package fr.euphyllia.fidorial.server.world.light;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import org.jspecify.annotations.Nullable;

public interface LightAccess {

    int minY();

    int height();

    default int maxY() {
        return minY() + height();
    }

    BlockState blockAt(int x, int y, int z);

    @Nullable ChunkLightData lightAt(int chunkX, int chunkZ);

    int topNonEmptySectionY(int chunkX, int chunkZ);

    @Nullable BlockColumnAccess columnAt(int chunkX, int chunkZ);

    boolean isLightPopulated(int chunkX, int chunkZ);

    boolean sectionHasEmissiveBlocks(final int chunkX, final int sectionY, final int chunkZ);

    default ColumnSnapshot snapshotAt(final int chunkX, final int chunkZ) {
        return new ColumnSnapshot(
                lightAt(chunkX, chunkZ),
                columnAt(chunkX, chunkZ),
                topNonEmptySectionY(chunkX, chunkZ),
                isLightPopulated(chunkX, chunkZ)
        );
    }

    record ColumnSnapshot(
            @Nullable ChunkLightData lightData,
            @Nullable BlockColumnAccess column,
            int topNonEmptySectionY,
            boolean populated
    ) { }

    interface BlockColumnAccess {
        BlockState blockAt(int localX, int worldY, int localZ);
    }
}
