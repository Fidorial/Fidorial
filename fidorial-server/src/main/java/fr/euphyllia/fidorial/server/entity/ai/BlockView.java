package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerChunk;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.Chunk;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Waterlogged;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BlockView {

    private BlockView() {
    }

    public static @Nullable BlockState blockAt(final ServerWorld world, final int x, final int y, final int z) {
        if (y < world.minY() || y >= world.minY() + world.height()) {
            return BlockState.withoutProperties(BlockTypeKeys.AIR);
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

    /**
     * {@return {@code true} if a mob can be in a block: one with nothing to bump into, such as air,
     * water, grass, a flower or a rail}
     *
     * <p>Lava and powder snow are left out, though nothing stops a mob in them: a mob does not go
     * there of its own accord.</p>
     */
    public static boolean isPassable(final BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.name().equals(BlockTypeKeys.LAVA) || isPowderSnow(state)) {
            return false;
        }

        return BlockCollision.shape(state).isEmpty();
    }

    /**
     * {@return {@code true} if a block is water or holds water}
     */
    public static boolean isWater(final BlockState state) {
        return state.name().equals(BlockTypeKeys.WATER)
                || (state.data() instanceof final Waterlogged waterlogged && waterlogged.isWaterlogged());
    }

    /**
     * {@return the height the water of a block reaches, {@link Double#NaN} if it holds none}
     *
     * <p>As in Vanilla, a source fills 8/9 of its block, flowing water loses 1/9 per level, and water
     * under water fills the whole block.</p>
     */
    public static double waterSurface(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = blockAt(world, x, y, z);
        if (state == null || !isWater(state)) {
            return Double.NaN;
        }
        final BlockState above = blockAt(world, x, y + 1, z);
        if (above != null && isWater(above)) {
            return y + 1.0;
        }
        int level = 0;
        if (state.name().equals(BlockTypeKeys.WATER)) {
            final String property = state.properties().get("level");
            if (property != null) {
                try {
                    level = Integer.parseInt(property);
                } catch (final NumberFormatException ignored) {
                    level = 0;
                }
            }
        }
        return level >= 8 ? y + 1.0 : y + (8 - level) / 9.0;
    }

    public static boolean isPowderSnow(final BlockState state) {
        return isTagged(BlockTypeTagKeys.PATHFINDING_POWDER_SNOW, state);
    }

    public static boolean isTagged(final TagKey<BlockType> tag, final BlockState state) {
        return FidorialServer.getInstance().registries().registry(RegistryKey.BLOCK).isTagged(state.name(), tag);
    }

    /**
     * {@return {@code true} if a mob can stand on a block: one with something to bump into, however
     * low, or powder snow}
     */
    public static boolean isSolidGround(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = blockAt(world, x, y, z);
        return state != null && isSolidGround(state);
    }

    public static boolean isSolidGround(final BlockState state) {
        return !state.isAir() && (!BlockCollision.shape(state).isEmpty() || isPowderSnow(state));
    }

    public static @Nullable TypedKey<BiomeDefinition> biomeAt(final ServerWorld world, final int x, final int y, final int z) {
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
