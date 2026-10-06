package fr.euphyllia.fidorial.server.world.anvil;

import java.io.DataOutput;

/**
 * Constants of the Region / Anvil format ({@code r.X.Z.mca} files).
 *
 * @see <a href="https://minecraft.wiki/w/Region_file_format">Region file format</a>
 */
public final class RegionConstants {

    /**
     * The size of a sector, in bytes.
     */
    public static final int SECTOR_BYTES = 4096;

    /**
     * The header takes 2 sectors: the location table (4 KiB) and the timestamp table (4 KiB).
     */
    public static final int HEADER_SECTORS = 2;
    public static final int HEADER_BYTES = HEADER_SECTORS * SECTOR_BYTES;

    /**
     * 32×32 chunks per region file.
     */
    public static final int REGION_SIZE = 32;
    public static final int CHUNKS_PER_REGION = REGION_SIZE * REGION_SIZE;

    // Compression bytes (see RegionCompression for the matching algorithms).
    public static final byte COMPRESSION_GZIP = 1;
    public static final byte COMPRESSION_ZLIB = 2;   // Vanilla default
    public static final byte COMPRESSION_NONE = 3;
    public static final byte COMPRESSION_LZ4 = 4;    // Vanilla 24w04a and later

    /**
     * A custom algorithm: followed by a namespaced name (unsigned 16-bit length + UTF-8, like
     * {@link DataOutput#writeUTF(String)}), then by the compressed data. Vanilla recognizes it but provides
     * none; see {@link RegionCompression#isCustom()}.
     */
    public static final byte COMPRESSION_CUSTOM = 127;

    /**
     * The high bit of the compression byte: the chunk data is in an external {@code c.X.Z.mcc} file.
     */
    public static final int EXTERNAL_FLAG = 0x80;

    private RegionConstants() {
    }

    /**
     * The index of a chunk in the header: (x & 31) + (z & 31) * 32.
     */
    public static int headerIndex(final int chunkX, final int chunkZ) {
        return (chunkX & (REGION_SIZE - 1)) + (chunkZ & (REGION_SIZE - 1)) * REGION_SIZE;
    }

    /**
     * The region coordinate of a chunk coordinate (arithmetic division by 32).
     */
    public static int chunkToRegion(final int chunkCoord) {
        return chunkCoord >> 5;
    }

    public static String fileName(final int regionX, final int regionZ) {
        return "r." + regionX + "." + regionZ + ".mca";
    }
}
