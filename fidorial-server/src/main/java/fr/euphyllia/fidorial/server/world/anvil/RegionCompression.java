package fr.euphyllia.fidorial.server.world.anvil;

import com.github.luben.zstd.ZstdInputStreamNoFinalizer;
import com.github.luben.zstd.ZstdOutputStreamNoFinalizer;
import com.github.luben.zstd.util.Native;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * The compression algorithms of chunks in region files ({@code .mca}).
 * <p>
 * {@link #id()} is the byte written right before the compressed data of each chunk. The first four algorithms are
 * Vanilla's, so a world using them stays readable by Vanilla (24w04a or later for LZ4). Custom algorithms
 * ({@link #isCustom()}) are written as {@link RegionConstants#COMPRESSION_CUSTOM} followed by their namespaced name;
 * Vanilla cannot read them.
 * <p>
 * Every algorithm can always be read, whichever one is configured, so a single file may mix several of them, for
 * instance while it is being converted after the configuration changed.
 * <p>
 * <b>Never remove a constant</b>: the worlds using it would become unreadable.
 */
public enum RegionCompression {

    /**
     * GZip (RFC 1952). Readable by Vanilla, which never writes it; slightly heavier than zlib.
     */
    GZIP(RegionConstants.COMPRESSION_GZIP, null, "gzip") {
        @Override
        public InputStream decompress(final InputStream in) throws IOException {
            return new GZIPInputStream(in, BUFFER_SIZE);
        }

        @Override
        public OutputStream compress(final OutputStream out) throws IOException {
            return new GZIPOutputStream(out, BUFFER_SIZE);
        }
    },

    /**
     * Zlib / Deflate (RFC 1950). Vanilla's default: a good size / CPU trade-off.
     */
    ZLIB(RegionConstants.COMPRESSION_ZLIB, null, "zlib", "deflate") {
        @Override
        public InputStream decompress(final InputStream in) {
            final Inflater inflater = INFLATERS.get();
            inflater.reset();
            return new InflaterInputStream(in, inflater, BUFFER_SIZE);
        }

        @Override
        public OutputStream compress(final OutputStream out) {
            final Deflater deflater = DEFLATERS.get();
            deflater.reset();
            return new DeflaterOutputStream(out, deflater, BUFFER_SIZE);
        }
    },

    /**
     * No compression: the fastest, but files are much larger.
     */
    NONE(RegionConstants.COMPRESSION_NONE, null, "none") {
        @Override
        public InputStream decompress(final InputStream in) {
            return in;
        }

        @Override
        public OutputStream compress(final OutputStream out) {
            return out;
        }
    },

    /**
     * LZ4, in lz4-java's block stream format like Vanilla. Much faster than zlib to compress and decompress, at the
     * cost of larger files.
     */
    LZ4(RegionConstants.COMPRESSION_LZ4, null, "lz4") {
        @Override
        public InputStream decompress(final InputStream in) {
            // The safe decompressor: region files are not trusted input.
            return LZ4BlockInputStream.newBuilder().withDecompressor(Lz4.FACTORY.safeDecompressor()).build(in);
        }

        @Override
        public OutputStream compress(final OutputStream out) {
            return new LZ4BlockOutputStream(out, Lz4.BLOCK_SIZE, Lz4.FACTORY.fastCompressor());
        }
    },

    /**
     * Zstandard, at its default level (3). Smaller files than zlib for a similar or lower CPU cost.
     * A custom algorithm: <b>Vanilla cannot read it</b>.
     */
    ZSTD(RegionConstants.COMPRESSION_CUSTOM, "fidorial:zstd", "zstd") {
        @Override
        public InputStream decompress(final InputStream in) throws IOException {
            return new ZstdInputStreamNoFinalizer(in);
        }

        @Override
        public OutputStream compress(final OutputStream out) throws IOException {
            return new ZstdOutputStreamNoFinalizer(out);
        }

        @Override
        public void checkAvailable() {
            try {
                Native.load();
            } catch (final UnsatisfiedLinkError | RuntimeException e) {
                throw new IllegalStateException("zstd compression is not available on this platform ("
                        + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "): " + e.getMessage(), e);
            }
        }
    };

    /**
     * The algorithm used by Vanilla, the configuration default, and the only one Fidorial wrote before
     * {@code storage.region-compression} existed.
     */
    public static final RegionCompression DEFAULT = ZLIB;
    private static final int BUFFER_SIZE = 8192;

    /*
     * Creating an Inflater or a Deflater allocates native zlib state (about 256 KiB for a Deflater): reuse one per
     * thread instead of creating one per chunk. Streams given an Inflater or a Deflater never end it when closed.
     */
    private static final ThreadLocal<Inflater> INFLATERS = ThreadLocal.withInitial(Inflater::new);
    private static final ThreadLocal<Deflater> DEFLATERS = ThreadLocal.withInitial(Deflater::new);

    private static final class Lz4 {

        /**
         * The block size of {@link LZ4BlockOutputStream#LZ4BlockOutputStream(OutputStream)}, used by Vanilla.
         */
        static final int BLOCK_SIZE = 1 << 16;

        /**
         * {@link LZ4Factory#fastestInstance()} only uses the native library when lz4-java is loaded by the system class
         * loader, which is never the case behind the bootstrap: ask for it explicitly, falling back to Java.
         */
        static final LZ4Factory FACTORY = factory();

        private static LZ4Factory factory() {
            try {
                return LZ4Factory.nativeInstance();
            } catch (final Throwable unavailable) {
                return LZ4Factory.fastestJavaInstance();
            }
        }
    }

    private static final RegionCompression[] BY_ID = new RegionCompression[128];

    static {
        for (final RegionCompression compression : values()) {
            if (!compression.isCustom()) {
                BY_ID[compression.id] = compression;
            }
        }
    }

    private final byte id;
    private final @Nullable String customName;
    private final String configName;
    private final List<String> aliases;

    RegionCompression(final byte id, final @Nullable String customName, final String configName, final String... aliases) {
        this.id = id;
        this.customName = customName;
        this.configName = configName;
        this.aliases = List.of(aliases);
    }

    /**
     * {@return the compression byte stored before each chunk}
     */
    public byte id() {
        return id;
    }

    /**
     * {@return whether this algorithm does not exist in Vanilla, and is stored as byte 127 followed by its name}
     */
    public boolean isCustom() {
        return customName != null;
    }

    /**
     * {@return the namespaced name written after byte 127, or {@code null} for a Vanilla algorithm}
     */
    public @Nullable String customName() {
        return customName;
    }

    /**
     * {@return the name used in configuration files}
     */
    public String configName() {
        return configName;
    }

    /**
     * Wraps {@code in} to read data compressed with this algorithm.
     */
    public abstract InputStream decompress(InputStream in) throws IOException;

    /**
     * Wraps {@code out} to write data compressed with this algorithm. Closing the returned stream finishes the
     * compression (and closes {@code out}).
     */
    public abstract OutputStream compress(OutputStream out) throws IOException;

    /**
     * Checks that this algorithm can be used on this machine (native library loaded, etc.).
     *
     * @throws IllegalStateException if it cannot
     */
    public void checkAvailable() {
    }

    /**
     * {@return the Vanilla algorithm of a compression byte, or {@code null} if it is unknown}
     * The high bit (external {@code .mcc} chunk) must have been cleared beforehand; byte 127 is resolved with
     * {@link #byCustomName(String)}.
     */
    public static @Nullable RegionCompression byId(final int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : null;
    }

    /**
     * {@return the custom algorithm with this namespaced name, or {@code null}}
     */
    public static @Nullable RegionCompression byCustomName(final String customName) {
        for (final RegionCompression compression : values()) {
            if (customName.equals(compression.customName)) {
                return compression;
            }
        }
        return null;
    }

    /**
     * {@return the algorithm with this configuration name (or one of its aliases), or {@code null}}
     */
    public static @Nullable RegionCompression byName(final String name) {
        final String normalized = name.strip().toLowerCase(Locale.ROOT);
        for (final RegionCompression compression : values()) {
            if (compression.configName.equals(normalized) || compression.aliases.contains(normalized)) {
                return compression;
            }
        }
        return null;
    }

    /**
     * {@return the accepted names, for error messages and configuration comments}
     */
    public static String names() {
        return Arrays.stream(values()).map(RegionCompression::configName).collect(Collectors.joining(", "));
    }
}
