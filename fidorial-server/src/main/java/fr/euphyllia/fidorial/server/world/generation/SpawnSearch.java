package fr.euphyllia.fidorial.server.world.generation;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.redstone.SupportType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Looks for a place to put new players on a world generated from noise: dry land as close to the origin as
 * possible, on a block a player can stand on, with nothing above it but air and plants to walk through.
 *
 * <p>The final density of the terrain, read every few blocks along the middle column of a chunk, tells which
 * chunks rise above the sea without building them; ice floating on the sea does not count. Only those chunks are
 * then loaded from the world, to see where caves, trees and plants ended up.</p>
 */
public final class SpawnSearch {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(SpawnSearch.class);

    /**
     * How far from the origin, in chunks, land is searched.
     */
    private static final int MAX_RING = 64;
    /**
     * How many chunks may be loaded before giving up.
     */
    private static final int MAX_LOADED = 48;
    /**
     * How far apart, in blocks, the final density is read along a column.
     */
    private static final int SAMPLE_STEP = 4;

    private SpawnSearch() {
    }

    /**
     * {@return {@code true} for a block a player can stand on}: its top holds over its whole face, it holds no
     * fluid, and it is not part of a tree
     */
    private static boolean safe(final BlockState state) {
        if (state.isFluid() || state.isTagged(BlockTypeTagKeys.LEAVES) || state.isTagged(BlockTypeTagKeys.LOGS)) {
            return false;
        }
        final BlockData data = FidorialServer.getInstance().registries().blockStates().resolve(state);
        return data != null && BlockSupport.isFaceSturdy(data, BlockFace.UP, SupportType.FULL);
    }

    /**
     * Searches the spawn of a world.
     *
     * @param world the world, whose chunks are loaded to look at them
     * @param noise the generator of the world
     * @return the block the feet of a player go in, as {x, y, z}, or {@code null} when no land is close enough
     */
    public static int @Nullable [] find(final ServerWorld world, final NoiseChunkGenerator noise) {
        final int seaLevel = noise.settings().seaLevel();
        final Density shape = noise.program().compile(noise.settings().routed("final_density"));
        int loadedChunks = 0;
        for (int ring = 0; ring <= MAX_RING; ring++) {
            for (int chunkX = -ring; chunkX <= ring; chunkX++) {
                for (int chunkZ = -ring; chunkZ <= ring; chunkZ++) {
                    if (Math.max(Math.abs(chunkX), Math.abs(chunkZ)) != ring) {
                        continue; // only the border of the ring: the inside was seen before
                    }
                    if (!risesAboveSea(noise, shape, (chunkX << 4) + 8, (chunkZ << 4) + 8, seaLevel)) {
                        continue;
                    }
                    if (loadedChunks++ >= MAX_LOADED) {
                        LOGGER.warn("No safe spawn found near the origin: the configured spawn is used");
                        return null;
                    }
                    final int[] spot = spotIn(world, chunkX, chunkZ, seaLevel);
                    if (spot != null) {
                        LOGGER.info("Spawn of {} set at {}, {}, {}", world.key().asString(), spot[0], spot[1], spot[2]);
                        return spot;
                    }
                }
            }
        }
        return null;
    }

    /**
     * {@return whether the terrain holds solid ground above the sea at a column}, before caves are carved and
     * fluids poured
     */
    private static boolean risesAboveSea(final NoiseChunkGenerator noise, final Density shape, final int x, final int z,
                                         final int seaLevel) {
        for (int y = seaLevel + 1; y <= noise.heights().maxY(); y += SAMPLE_STEP) {
            if (noise.sample(shape, x, y, z) > 0.0F) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return the safest column of a chunk, the closest to its middle first, or {@code null}}
     */
    private static int @Nullable [] spotIn(final ServerWorld world, final int chunkX, final int chunkZ, final int seaLevel) {
        final ChunkColumn column;
        try {
            column = world.getChunk(chunkX, chunkZ);
        } catch (final IOException failure) {
            LOGGER.warn("Chunk ({}, {}) could not be loaded to search the spawn", chunkX, chunkZ, failure);
            return null;
        }
        final int top = column.minY() + column.height() - 1;
        for (int reach = 0; reach < 8; reach++) {
            for (int lx = 7 - reach; lx <= 8 + reach; lx++) {
                for (int lz = 7 - reach; lz <= 8 + reach; lz++) {
                    if (Math.max(Math.abs(lx * 2 - 15), Math.abs(lz * 2 - 15)) != reach * 2 + 1) {
                        continue; // only the border of this square: the inside was seen before
                    }
                    for (int y = top; y > seaLevel; y--) {
                        final BlockState state = column.getBlock(lx, y, lz);
                        // air, and plants a player walks through
                        if (state.isAir() || !state.isFluid() && BlockCollision.shape(state).isEmpty()) {
                            continue;
                        }
                        if (safe(state)) {
                            return new int[] {(chunkX << 4) + lx, y + 1, (chunkZ << 4) + lz};
                        }
                        break;
                    }
                }
            }
        }
        return null;
    }
}