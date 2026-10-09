package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerChunk;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.Chunk;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class BlockView {

    private static final Set<Key> PASSABLE = Set.of(Key.key("water"));
    private static final Key BLOCKS = RegistryKey.BLOCK.key();
    private static final Key POWDER_SNOW = Key.key("pathfinding/powder_snow");

    private BlockView() {
    }

    public static @Nullable BlockState blockAt(final ServerWorld world, final int x, final int y, final int z) {
        if (y < world.minY() || y >= world.minY() + world.height()) {
            return BlockState.of(BlockTypeKeys.AIR.key());
        }
        final Optional<Chunk> optionalChunk = world.chunkIfLoaded(x >> 4, z >> 4);
        if (optionalChunk.isEmpty()) return null;
        final Chunk chunk = optionalChunk.get();
        return chunk instanceof final ServerChunk serverChunk ? serverChunk.column().getBlock(x & 15, y, z & 15) : null;
    }

    public static boolean isPassable(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = blockAt(world, x, y, z);
        return state != null && isPassable(state);
    }

    public static boolean isPassable(final BlockState state) {
        if (state.isAir()) {
            return true;
        }
        final Key name = state.name();
        return PASSABLE.contains(name);
    }

    public static boolean isPowderSnow(final BlockState state) {
        return isTagged(POWDER_SNOW, state);
    }

    public static boolean isTagged(final Key tag, final BlockState state) {
        return FidorialServer.getInstance().registries().frozen().isTagged(BLOCKS, tag, state.name());
    }

    public static boolean isSolidGround(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = blockAt(world, x, y, z);
        return state != null && !isPassable(state);
    }

    public static @Nullable Key biomeAt(final ServerWorld world, final int x, final int y, final int z) {
        final Optional<Chunk> optionalChunk = world.chunkIfLoaded(x >> 4, z >> 4);
        if (optionalChunk.isEmpty()) return null;
        final Chunk chunk = optionalChunk.get();
        return chunk instanceof final ServerChunk serverChunk
                ? serverChunk.column().getBiome(x & 15, y, z & 15)
                : null;
    }

    public static boolean hasLineOfSight(
            final ServerWorld world,
            final double fromX,
            final double fromY,
            final double fromZ,
            final double toX,
            final double toY,
            final double toZ
    ) {
        final double dx = toX - fromX;
        final double dy = toY - fromY;
        final double dz = toZ - fromZ;
        final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 1.0E-6 || distance > 32.0) {
            return distance <= 32.0;
        }
        final int steps = (int) Math.ceil(distance * 2.0);
        final double stepX = dx / steps;
        final double stepY = dy / steps;
        final double stepZ = dz / steps;
        double x = fromX;
        double y = fromY;
        double z = fromZ;
        int lastBx = Integer.MIN_VALUE;
        int lastBy = Integer.MIN_VALUE;
        int lastBz = Integer.MIN_VALUE;

        for (int i = 0; i < steps; i++) {
            x += stepX;
            y += stepY;
            z += stepZ;
            final int bx = (int) Math.floor(x);
            final int by = (int) Math.floor(y);
            final int bz = (int) Math.floor(z);
            if (bx == lastBx && by == lastBy && bz == lastBz) {
                continue;
            }
            lastBx = bx;
            lastBy = by;
            lastBz = bz;
            final BlockState state = blockAt(world, bx, by, bz);
            if (state != null && !isPassable(state)) {
                return false;
            }
        }
        return true;
    }
}
