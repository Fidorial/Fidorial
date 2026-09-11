package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntityTypes;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.place.BlockTarget;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.Optional;
import java.util.Set;

public final class ColumnTarget implements BlockTarget {

    private static final Set<String> POSITION_KEYS = Set.of("x", "y", "z", "id", "keepPacked");
    private static final BlockState AIR = BlockState.of(Keys.AIR);

    private final ChunkColumn column;
    private final int minX;
    private final int minZ;
    private final int minY;
    private final int maxY;
    private final BitSet placed;
    private int written;

    public ColumnTarget(final ChunkColumn column) {
        this.column = column;
        this.minX = column.chunkX() << 4;
        this.minZ = column.chunkZ() << 4;
        this.minY = column.minY();
        this.maxY = column.minY() + column.height() - 1;
        this.placed = new BitSet(256 * column.height());
    }

    public ChunkColumn column() {
        return column;
    }

    @Override
    public int minX() {
        return minX;
    }

    @Override
    public int minZ() {
        return minZ;
    }

    @Override
    public int maxX() {
        return minX + 15;
    }

    @Override
    public int maxZ() {
        return minZ + 15;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int maxY() {
        return maxY;
    }

    private int index(final int x, final int y, final int z) {
        return ((y - minY) << 8) | ((z - minZ) << 4) | (x - minX);
    }

    @Override
    public BlockState get(final int x, final int y, final int z) {
        return contains(x, y, z) ? column.getBlock(x - minX, y, z - minZ) : AIR;
    }

    @Override
    public void set(final int x, final int y, final int z, final BlockState state, final @Nullable CompoundBinaryTag nbt) {
        if (!contains(x, y, z)) {
            return;
        }
        final int localX = x - minX;
        final int localZ = z - minZ;
        final CompoundBinaryTag data = nbt == null ? null : clean(nbt);
        column.setBlock(localX, y, localZ, state, data);
        if (data != null) {
            final Optional<Key> type = BlockEntityTypes.typeIdentifier(state.name());
            type.ifPresent(key -> column.putBlockEntity(new BlockEntity(localX, y, localZ, key, data)));
        }
        placed.set(index(x, y, z));
        written++;
    }

    private static CompoundBinaryTag clean(final CompoundBinaryTag nbt) {
        boolean needsCleaning = false;
        for (final String key : POSITION_KEYS) {
            if (nbt.contains(key)) {
                needsCleaning = true;
                break;
            }
        }
        if (!needsCleaning) {
            return nbt;
        }
        final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        for (final String key : nbt.keySet()) {
            if (!POSITION_KEYS.contains(key)) {
                builder.put(key, nbt.get(key));
            }
        }
        return builder.build();
    }

    @Override
    public boolean placedByStructure(final int x, final int y, final int z) {
        return contains(x, y, z) && placed.get(index(x, y, z));
    }

    @Override
    public boolean isGround(final BlockState state) {
        return TerrainClassifier.isGround(state);
    }

    @Override
    public int written() {
        return written;
    }
}
