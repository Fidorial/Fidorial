package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.NoiseChunkGenerator;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.random.XoroshiroSource;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

/**
 * The world as one decoration sees it: the chunk being decorated and the eight around it. Blocks can be read and
 * set anywhere in those nine chunks; outside, reads give air and writes are refused.
 *
 * <p>Heights outside the dimension read as air too. Not thread-safe: one decoration at a time, holding the locks
 * of the nine chunks.</p>
 */
public final class DecorationRegion {

    private final DecorationContext context;
    private final StateFlags states;
    private final int centerX;
    private final int centerZ;
    private final int minY;
    private final int maxY;
    private final DecorationChunk[] chunks;
    private final char air;
    private @Nullable XoroshiroSource ownRandom;

    /**
     * @param chunks the nine chunks, the one at offset {@code (dx, dz)} at index {@code (dx + 1) * 3 + dz + 1}
     */
    DecorationRegion(final DecorationContext context, final int centerX, final int centerZ, final DecorationChunk[] chunks) {
        this.context = context;
        this.states = context.states();
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.minY = context.heights().minY();
        this.maxY = context.heights().maxY();
        this.chunks = chunks;
        this.air = states.palette().id(context.blocks().air);
    }

    public DecorationContext context() {
        return context;
    }

    public StateFlags states() {
        return states;
    }

    public BlockLookup blocks() {
        return context.blocks();
    }

    public NoiseChunkGenerator generator() {
        return context.generator();
    }

    public HeightContext heights() {
        return context.heights();
    }

    public int centerX() {
        return centerX;
    }

    public int centerZ() {
        return centerZ;
    }

    public int minY() {
        return minY;
    }

    /**
     * {@return the highest height of the dimension, included}
     */
    public int maxY() {
        return maxY;
    }

    public long seed() {
        return context.generator().seed();
    }

    /**
     * {@return the stream of the region itself, the same for every decoration of the chunk}
     */
    public XoroshiroSource ownRandom() {
        if (ownRandom == null) {
            ownRandom = context.regionRandoms().at(centerX << 4, 0, centerZ << 4);
        }
        return ownRandom;
    }

    /**
     * {@return one of the nine chunks, by its offset from the chunk being decorated}
     */
    DecorationChunk chunk(final int dx, final int dz) {
        return chunks[(dx + 1) * 3 + dz + 1];
    }

    private @Nullable DecorationChunk chunkAt(final int x, final int z) {
        final int dx = (x >> 4) - centerX;
        final int dz = (z >> 4) - centerZ;
        if (dx < -1 || dx > 1 || dz < -1 || dz > 1) {
            return null;
        }
        return chunks[(dx + 1) * 3 + dz + 1];
    }

    public boolean isOutsideHeight(final int y) {
        return y < minY || y > maxY;
    }

    /**
     * {@return {@code true} if a block can be set: inside the nine chunks and the dimension}
     */
    public boolean canWrite(final int x, final int y, final int z) {
        if (isOutsideHeight(y)) {
            return false;
        }
        final DecorationChunk chunk = chunkAt(x, z);
        return chunk != null && !chunk.handedOver();
    }

    /**
     * {@return the number of the state at a block}
     */
    public char id(final int x, final int y, final int z) {
        if (isOutsideHeight(y)) {
            return air;
        }
        final DecorationChunk chunk = chunkAt(x, z);
        return chunk == null ? air : chunk.blocks().id(x & 15, y, z & 15);
    }

    public BlockState state(final int x, final int y, final int z) {
        return states.palette().state(id(x, y, z));
    }

    public boolean isAir(final int x, final int y, final int z) {
        return states.isAir(id(x, y, z));
    }

    public boolean has(final int x, final int y, final int z, final long flag) {
        return states.has(id(x, y, z), flag);
    }

    /**
     * Sets a block.
     *
     * @return {@code false} when the block is outside the region
     */
    public boolean set(final int x, final int y, final int z, final char id) {
        if (isOutsideHeight(y)) {
            return false;
        }
        final DecorationChunk chunk = chunkAt(x, z);
        if (chunk == null || chunk.handedOver()) {
            return false;
        }
        chunk.set(x & 15, y, z & 15, id);
        return true;
    }

    /**
     * Sets a block without following it in the column heights, as ore veins do.
     *
     * @return {@code false} when the block is outside the region
     */
    public boolean setRaw(final int x, final int y, final int z, final char id) {
        if (isOutsideHeight(y)) {
            return false;
        }
        final DecorationChunk chunk = chunkAt(x, z);
        if (chunk == null || chunk.handedOver()) {
            return false;
        }
        chunk.setRaw(x & 15, y, z & 15, id);
        return true;
    }

    public boolean set(final int x, final int y, final int z, final BlockState state) {
        return set(x, y, z, states.palette().id(state));
    }

    public char id(final BlockState state) {
        return states.palette().id(state);
    }

    public BlockState state(final char id) {
        return states.palette().state(id);
    }

    /**
     * {@return the first free height of a column}
     */
    public int height(final HeightmapKind kind, final int x, final int z) {
        final DecorationChunk chunk = chunkAt(x, z);
        return chunk == null ? minY : chunk.height(kind, x & 15, z & 15);
    }

    /**
     * {@return {@code true} if a state can stay at a block}
     */
    public boolean canSurvive(final char id, final int x, final int y, final int z) {
        return context.survival().canSurvive(this, id, x, y, z);
    }

    /**
     * Keeps the extra data of the block entity at a block, which must already be set.
     */
    public void putBlockEntity(final int x, final int y, final int z, final CompoundBinaryTag data) {
        final DecorationChunk chunk = chunkAt(x, z);
        if (chunk != null && !isOutsideHeight(y)) {
            chunk.putBlockEntity(x & 15, y, z & 15, data);
        }
    }

    /**
     * Lets a fluid set at a block start flowing once its chunk is loaded.
     */
    public void scheduleFluid(final int x, final int y, final int z) {
        final DecorationChunk chunk = chunkAt(x, z);
        if (chunk == null || isOutsideHeight(y)) {
            return;
        }
        final char id = id(x, y, z);
        if (states.hasFluid(id)) {
            chunk.scheduleFluid(x, y, z, states.has(id, StateFlags.LAVA));
        }
    }

    /**
     * {@return the biome of a block}
     */
    public BiomeInfo biome(final int x, final int y, final int z) {
        final DecorationChunk chunk = chunkAt(x, z);
        if (chunk == null) {
            return context.generator().biomeAt(x, y, z);
        }
        return context.biomes().get(chunk.blocks().biomes().biome(x, y, z));
    }
}
