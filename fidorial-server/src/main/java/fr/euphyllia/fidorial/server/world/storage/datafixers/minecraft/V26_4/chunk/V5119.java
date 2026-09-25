package fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.chunk;

import ca.spottedleaf.converter.DataConverter;
import ca.spottedleaf.converter.types.ListType;
import ca.spottedleaf.converter.types.MapType;
import fr.euphyllia.fidorial.server.world.chunk.BitPacking;
import org.jspecify.annotations.Nullable;

/**
 * <p>Vanilla moves the old 4x4x4 section biomes to {@code noise_biomes} and schedules a retrogen of the
 * {@code minecraft:biomes} status to rebuild per-block biomes. The chunk-level renames
 * ({@code Status} to {@code status}, {@code below_zero_retrogen} to {@code retrogen}) follow vanilla.</p>
 */
public final class V5119 extends DataConverter<MapType, MapType> {

    public static final int TO_VERSION = 5119;

    private static final int OLD_BIOME_COUNT = 64;
    private static final int NEW_BIOME_COUNT = 4096;

    public V5119() {
        super(TO_VERSION);
    }

    @Override
    @Nullable public MapType convert(final @Nullable MapType chunk, final long sourceVersion, final long toVersion) {
        if (chunk == null) {
            return null;
        }

        chunk.rename("Status", "status");
        chunk.rename("below_zero_retrogen", "retrogen");

        final ListType sections = chunk.getListUnchecked("sections", null);
        if (sections == null) {
            return chunk;
        }

        for (int i = 0, len = sections.size(); i < len; ++i) {
            final MapType section = sections.getMap(i, null);
            if (section == null) {
                continue;
            }
            final MapType biomes = section.getMap("biomes", null);
            if (biomes == null) {
                continue;
            }
            section.setMap("biomes", expandNoiseBiomes(biomes));
            sections.setMap(i, section);
        }

        chunk.setList("sections", sections);
        return chunk;
    }

    /**
     * Turns a 4x4x4 biome container (vanilla {@code noise_biomes} layout) into a per-block one.
     * The palette is kept; only {@code data} is re-packed.
     */
    public static MapType expandNoiseBiomes(final MapType biomes) {
        final ListType palette = biomes.getListUnchecked("palette", null);
        final long[] data = biomes.getLongs("data", null);
        if (palette == null || palette.size() <= 1 || data == null || data.length == 0) {
            return biomes; // single-valued: no data array, same meaning at any resolution
        }

        final int bits = BitPacking.bitsFor(palette.size(), 1);
        final int[] cells = BitPacking.unpack(data, bits, OLD_BIOME_COUNT);
        final int[] perBlock = new int[NEW_BIOME_COUNT];
        for (int index = 0; index < NEW_BIOME_COUNT; index++) {
            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = index >> 8;
            perBlock[index] = cells[((y >> 2) << 4) | ((z >> 2) << 2) | (x >> 2)];
        }

        final MapType expanded = biomes.copy();
        expanded.setLongs("data", BitPacking.pack(perBlock, bits));
        return expanded;
    }
}
