package fr.euphyllia.fidorial.server.world.structure.place;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

public interface BlockTarget {

    int minX();

    int minZ();

    int maxX();

    int maxZ();

    int minY();

    int maxY();

    default boolean contains(final int x, final int y, final int z) {
        return x >= minX() && x <= maxX() && z >= minZ() && z <= maxZ() && y >= minY() && y <= maxY();
    }

    BlockState get(int x, int y, int z);

    void set(int x, int y, int z, BlockState state, @Nullable CompoundBinaryTag nbt);

    boolean placedByStructure(int x, int y, int z);

    boolean isGround(BlockState state);

    int written();
}
