package fr.euphyllia.fidorial.server.world.chunk;

import fr.euphyllia.fidorial.server.util.ConcurrentInt2ObjectMap;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.light.ChunkLightData;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

public final class ChunkColumn {

    public static final Key FULL_STATUS = Key.key("full");

    private final int chunkX;
    private final int chunkZ;
    private final int minY;
    private final int height;
    private final int minSectionY;
    private final int sectionCount;
    private final ChunkSection[] sections;
    private final ConcurrentInt2ObjectMap<BlockEntity> blockEntities = new ConcurrentInt2ObjectMap<>();
    private final EnumMap<Heightmap, long[]> heightmaps = new EnumMap<>(Heightmap.class);
    private volatile boolean heightmapsCached;

    private long inhabitedTime;
    private long lastUpdate;
    private Key status = FULL_STATUS;

    private volatile boolean lightPopulated;
    private @Nullable ChunkLightData lightData;

    public ChunkColumn(final int chunkX, final int chunkZ, final int minY, final int height, final BlockState fillBlock, final @UnknownNullability TypedKey<BiomeDefinition> fillBiome) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.minY = minY;
        this.height = height;
        this.minSectionY = minY >> 4;
        this.sectionCount = height >> 4;
        this.sections = new ChunkSection[sectionCount];
        for (int i = 0; i < sectionCount; i++) {
            sections[i] = new ChunkSection(minSectionY + i, fillBlock, fillBiome);
        }
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public int minY() {
        return minY;
    }

    public int height() {
        return height;
    }

    public int minSectionY() {
        return minSectionY;
    }

    public int sectionCount() {
        return sectionCount;
    }

    public ChunkSection[] sections() {
        return sections;
    }

    public void putSection(final ChunkSection s) {
        final int idx = s.sectionY() - minSectionY;
        if (idx >= 0 && idx < sectionCount) {
            sections[idx] = s;
            synchronized (heightmaps) {
                heightmaps.clear();
                heightmapsCached = false;
            }
        }
    }

    public boolean setBiome(final int localX, final int worldY, final int localZ, final TypedKey<BiomeDefinition> biome) {
        final ChunkSection section = sectionForY(worldY);
        if (section == null) {
            return false;
        }

        final int y = worldY & 15;

        if (biome.equals(section.getBiome(localX, y, localZ))) {
            return false;
        }

        section.setBiome(localX, y, localZ, biome);
        return true;
    }

    public @Nullable TypedKey<BiomeDefinition> getBiome(final int localX, final int worldY, final int localZ) {
        final ChunkSection chunkSection = sectionForY(worldY);
        return chunkSection == null ? null : chunkSection.getBiome(localX, worldY & 15, localZ);
    }

    public long inhabitedTime() {
        return inhabitedTime;
    }

    public void setInhabitedTime(final long t) {
        this.inhabitedTime = t;
    }

    public long lastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final long t) {
        this.lastUpdate = t;
    }

    public Key status() {
        return status;
    }

    public void setStatus(final Key s) {
        this.status = s;
    }

    public synchronized ChunkLightData lightData() {
        ChunkLightData data = lightData;
        if (data == null) {
            data = new ChunkLightData(minY, height);
            lightData = data;
        }
        return data;
    }

    public boolean lightPopulated() {
        return lightPopulated;
    }

    public void setLightPopulated(final boolean populated) {
        this.lightPopulated = populated;
    }

    /**
     * Returns every block entity of this column, in no particular order.
     *
     * @return an unmodifiable snapshot of the block entities
     */
    public Collection<BlockEntity> blockEntities() {
        return Collections.unmodifiableCollection(blockEntities.valuesSnapshot());
    }

    public int blockEntityCount() {
        return blockEntities.size();
    }

    public @Nullable BlockEntity blockEntity(final int localX, final int worldY, final int localZ) {
        return blockEntities.get(BlockEntity.positionKey(localX, worldY, localZ));
    }

    /**
     * Adds or replaces a block entity.
     *
     * @param blockEntity the block entity to store
     */
    public void putBlockEntity(final BlockEntity blockEntity) {
        blockEntities.put(blockEntity.positionKey(), blockEntity);
    }

    public @Nullable BlockEntity removeBlockEntity(final int localX, final int worldY, final int localZ) {
        if (blockEntities.isEmpty()) {
            return null;
        }
        return blockEntities.remove(BlockEntity.positionKey(localX, worldY, localZ));
    }

    public void clearBlockEntities() {
        blockEntities.clear();
    }

    private @Nullable ChunkSection sectionForY(final int worldY) {
        final int idx = (worldY >> 4) - minSectionY;
        if (idx < 0 || idx >= sectionCount) return null;
        return sections[idx];
    }

    public void setBlock(final int localX, final int worldY, final int localZ, final BlockState state) {
        setBlock(localX, worldY, localZ, state, null);
    }

    public void setBlock(final int localX, final int worldY, final int localZ, final BlockState state, final @Nullable CompoundBinaryTag data) {
        final ChunkSection s = sectionForY(worldY);
        if (s == null) {
            return;
        }

        final BlockState previous = s.setBlock(localX, worldY & 15, localZ, state);
        if (previous != state) {
            updateHeightmaps(localX, worldY, localZ, state);
        }

        if (previous.name().equals(state.name())) {
            return;
        }

        removeBlockEntity(localX, worldY, localZ);

        final TypedKey<BlockEntityType> type = state.blockEntityType();
        if (type != null) {
            putBlockEntity(new BlockEntity(localX, worldY, localZ, type, data));
        }
    }

    public BlockState getBlock(final int localX, final int worldY, final int localZ) {
        final ChunkSection s = sectionForY(worldY);
        return s == null ? BlockState.withoutProperties(BlockTypeKeys.AIR) : s.getBlock(localX, worldY & 15, localZ);
    }

    public int heightmapBits() {
        return BitPacking.bitsFor(height + 1, 1);
    }

    /// The packed heightmap. Live types are computed on first use and then kept up to date.
    /// Generation types currently return the copy restored from disk, or a fresh scan.
    public long[] heightmap(final Heightmap type) {
        synchronized (heightmaps) {
            long[] data = heightmaps.get(type);
            if (data == null) {
                if (type.generationOnly()) {
                    return computeHeightmap(type.occupies());
                }
                heightmapsCached = true; // see updateHeightmaps
                data = computeHeightmap(type.occupies());
                heightmaps.put(type, data);
            }
            return data.clone();
        }
    }

    /// Restores a stored heightmap. Returns `false` and ignores the data if its length doesn't fit this column.
    public boolean restoreHeightmap(final Heightmap type, final long[] data) {
        final int perLong = 64 / heightmapBits();
        if (data.length != (256 + perLong - 1) / perLong) {
            return false;
        }
        synchronized (heightmaps) {
            heightmaps.put(type, data.clone());
            if (!type.generationOnly()) {
                heightmapsCached = true;
            }
        }
        return true;
    }

    /// Scans a heightmap for `occupied`, without caching it.
    private long[] computeHeightmap(final Predicate<BlockState> occupied) {
        final int bits = heightmapBits();
        final int perLong = 64 / bits;
        final long[] packed = new long[(256 + perLong - 1) / perLong];
        int remaining = 256;

        for (int i = sections.length - 1; i >= 0 && remaining > 0; i--) {
            final ChunkSection section = sections[i];
            if (section == null || section.isEmpty()) continue;

            final BitSet matching = section.matchingBlocks(occupied);
            if (matching.isEmpty()) continue;

            final int sectionBaseY = (minSectionY + i) << 4;
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    final int col = z * 16 + x;
                    if (readHeight(packed, bits, perLong, col) != 0) continue; // already found from a higher section

                    for (int ly = 15; ly >= 0; ly--) {
                        if (matching.get(ChunkSection.blockIndex(x, ly, z))) {
                            writeHeight(packed, bits, perLong, col, sectionBaseY + ly + 1 - minY);
                            remaining--;
                            break;
                        }
                    }
                }
            }
        }
        return packed;
    }

    private void updateHeightmaps(final int localX, final int worldY, final int localZ, final BlockState state) {
        if (!heightmapsCached) {
            return; // any scan that starts later already sees this block
        }
        updateCachedHeightmaps(localX, worldY, localZ, state);
    }

    private void updateCachedHeightmaps(final int localX, final int worldY, final int localZ, final BlockState state) {
        synchronized (heightmaps) {
            final int bits = heightmapBits();
            final int perLong = 64 / bits;
            final int col = localZ * 16 + localX;
            final int above = worldY + 1 - minY;
            for (final Map.Entry<Heightmap, long[]> entry : heightmaps.entrySet()) {
                if (entry.getKey().generationOnly()) continue; // restored from disk; stays frozen
                final long[] data = entry.getValue();
                final Predicate<BlockState> occupies = entry.getKey().occupies();
                final int top = readHeight(data, bits, perLong, col);
                if (occupies.test(state)) {
                    if (above > top) {
                        writeHeight(data, bits, perLong, col, above);
                    }
                } else if (above == top) {
                    writeHeight(data, bits, perLong, col, scanDown(localX, worldY - 1, localZ, occupies));
                }
            }
        }
    }

    private int scanDown(final int localX, final int fromY, final int localZ, final Predicate<BlockState> occupies) {
        for (int y = fromY; y >= minY; y--) {
            if (occupies.test(getBlock(localX, y, localZ))) {
                return y + 1 - minY;
            }
        }
        return 0;
    }

    private static int readHeight(final long[] data, final int bits, final int perLong, final int index) {
        final int shift = (index % perLong) * bits;
        return (int) ((data[index / perLong] >>> shift) & ((1L << bits) - 1L));
    }

    private static void writeHeight(final long[] data, final int bits, final int perLong, final int index, final int value) {
        final int shift = (index % perLong) * bits;
        final long mask = ((1L << bits) - 1L) << shift;
        data[index / perLong] = (data[index / perLong] & ~mask) | ((long) value << shift);
    }

    public int topNonEmptySectionY() {
        for (int i = sectionCount - 1; i >= 0; i--) {
            final ChunkSection section = sections[i];
            if (section != null && !section.isEmpty()) {
                return minSectionY + i;
            }
        }
        return minSectionY - 1;
    }
}
