package fr.euphyllia.fidorial.server.tests.lighting;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.light.ChunkLightData;
import fr.euphyllia.fidorial.server.world.light.FloodFillLightEngine;
import fr.euphyllia.fidorial.server.world.light.LightAccess;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.light.LightType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

final class LightScene implements LightAccess {

    static final BlockState AIR = state(BlockTypeKeys.AIR);
    static final BlockState STONE = state(BlockTypeKeys.STONE);
    static final BlockState GLASS = state(BlockTypeKeys.GLASS);
    static final BlockState WATER = state(BlockTypeKeys.WATER);
    static final BlockState TORCH = state(BlockTypeKeys.TORCH);
    static final BlockState GLOWSTONE = state(BlockTypeKeys.GLOWSTONE);

    private final int minY;
    private final int height;
    private final boolean skylight;
    private final int chunksX;
    private final int chunksZ;
    private final BlockState[] blocks;
    private final ChunkLightData[] light;
    private final LongSet populated = new LongOpenHashSet();
    private final FloodFillLightEngine engine;

    LightScene(final int minY, final int height, final boolean skylight, final int chunksX, final int chunksZ) {
        this.minY = minY;
        this.height = height;
        this.skylight = skylight;
        this.chunksX = chunksX;
        this.chunksZ = chunksZ;
        this.blocks = new BlockState[chunksX * 16 * height * chunksZ * 16];
        Arrays.fill(blocks, AIR);
        this.light = new ChunkLightData[chunksX * chunksZ];
        for (int i = 0; i < light.length; i++) {
            light[i] = new ChunkLightData(minY, height);
        }
        this.engine = new FloodFillLightEngine(minY, height);
    }

    static BlockState state(final TypedKey<BlockType> block) {
        final BlockType type = Blocks.type(block);
        final BlockData data = type == null ? null : type.defaultData();
        if (data == null) {
            throw new IllegalStateException("Unknown block " + block);
        }
        return FidorialServer.getInstance().registries().blockStates().toBlockState(data);
    }

    int width() {
        return chunksX * 16;
    }

    int depth() {
        return chunksZ * 16;
    }

    /// Sets a block without telling the engine.
    LightScene set(final int x, final int y, final int z, final BlockState state) {
        blocks[index(x, y, z)] = state;
        return this;
    }

    /// Sets every block of a box, both corners included, without telling the engine.
    LightScene fill(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1, final BlockState state) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    set(x, y, z, state);
                }
            }
        }
        return this;
    }

    /// Lights every chunk at once.
    LightScene light() {
        final LongSet all = new LongOpenHashSet();
        for (int cx = 0; cx < chunksX; cx++) {
            for (int cz = 0; cz < chunksZ; cz++) {
                all.add(ChunkPos.chunkKey(cx, cz));
            }
        }
        engine.relight(all, this);
        populated.addAll(all);
        return this;
    }

    /// Lights one chunk on its own, as when chunks load one after the other.
    LightScene light(final int chunkX, final int chunkZ) {
        final long key = ChunkPos.chunkKey(chunkX, chunkZ);
        engine.relight(LongOpenHashSet.of(key), this);
        populated.add(key);
        return this;
    }

    /// Checks the seams between every pair of neighboring chunks.
    void checkSeams() {
        for (int cx = 0; cx < chunksX; cx++) {
            for (int cz = 0; cz < chunksZ; cz++) {
                if (cx + 1 < chunksX) {
                    engine.checkChunkEdge(cx, cz, cx + 1, cz, this);
                }
                if (cz + 1 < chunksZ) {
                    engine.checkChunkEdge(cx, cz, cx, cz + 1, this);
                }
            }
        }
    }

    /// Changes a block of a lit scene the way the world does.
    LightScene edit(final int x, final int y, final int z, final BlockState state) {
        set(x, y, z, state);
        engine.checkBlock(x, y, z, this);
        return this;
    }

    int block(final int x, final int y, final int z) {
        return light(LightType.BLOCK, x, y, z);
    }

    int sky(final int x, final int y, final int z) {
        return light(LightType.SKY, x, y, z);
    }

    int light(final LightType type, final int x, final int y, final int z) {
        if (!contains(x, y, z)) {
            throw new IllegalArgumentException(x + "," + y + "," + z + " is outside the scene");
        }
        return light[(x >> 4) * chunksZ + (z >> 4)].get(type, x, y, z);
    }

    /// {@return where the light differs from the same blocks lit from scratch, or {@code null} if it does not}
    @Nullable String differenceFromFullRelight() {
        final LightScene fresh = new LightScene(minY, height, skylight, chunksX, chunksZ);
        System.arraycopy(blocks, 0, fresh.blocks, 0, blocks.length);
        fresh.light();
        for (final LightType type : LightType.values()) {
            for (int x = 0; x < width(); x++) {
                for (int z = 0; z < depth(); z++) {
                    for (int y = minY; y < minY + height; y++) {
                        final int expected = fresh.light(type, x, y, z);
                        final int actual = light(type, x, y, z);
                        if (expected != actual) {
                            return type + " light at " + x + "," + y + "," + z + " is " + actual
                                    + " where lighting it from scratch gives " + expected;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean contains(final int x, final int y, final int z) {
        return x >= 0 && z >= 0 && x < width() && z < depth() && y >= minY && y < minY + height;
    }

    private int index(final int x, final int y, final int z) {
        if (!contains(x, y, z)) {
            throw new IllegalArgumentException(x + "," + y + "," + z + " is outside the scene");
        }
        return ((x * depth()) + z) * height + (y - minY);
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public BlockState blockAt(final int x, final int y, final int z) {
        return contains(x, y, z) ? blocks[index(x, y, z)] : AIR;
    }

    @Override
    public @Nullable ChunkLightData lightAt(final int chunkX, final int chunkZ) {
        if (chunkX < 0 || chunkZ < 0 || chunkX >= chunksX || chunkZ >= chunksZ) {
            return null;
        }
        return light[chunkX * chunksZ + chunkZ];
    }

    @Override
    public int topNonEmptySectionY(final int chunkX, final int chunkZ) {
        for (int y = minY + height - 1; y >= minY; y--) {
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    if (!blockAt((chunkX << 4) | lx, y, (chunkZ << 4) | lz).isAir()) {
                        return y >> 4;
                    }
                }
            }
        }
        return (minY >> 4) - 1;
    }

    @Override
    public @Nullable BlockColumnAccess columnAt(final int chunkX, final int chunkZ) {
        if (lightAt(chunkX, chunkZ) == null) {
            return null;
        }
        return (localX, worldY, localZ) -> blockAt((chunkX << 4) | localX, worldY, (chunkZ << 4) | localZ);
    }

    @Override
    public boolean isLightPopulated(final int chunkX, final int chunkZ) {
        return populated.contains(ChunkPos.chunkKey(chunkX, chunkZ));
    }

    @Override
    public boolean sectionHasEmissiveBlocks(final int chunkX, final int sectionY, final int chunkZ) {
        return true;
    }

    @Override
    public boolean hasSkylight() {
        return skylight;
    }
}
