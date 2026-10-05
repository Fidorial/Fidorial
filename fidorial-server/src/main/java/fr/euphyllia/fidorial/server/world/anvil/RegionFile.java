package fr.euphyllia.fidorial.server.world.anvil;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class RegionFile implements Closeable {

    private final RandomAccessFile raf;
    private final int[] offsets = new int[RegionConstants.CHUNKS_PER_REGION];
    private final int[] sectorCounts = new int[RegionConstants.CHUNKS_PER_REGION];
    private final int[] timestamps = new int[RegionConstants.CHUNKS_PER_REGION];
    private boolean[] usedSectors = new boolean[0];

    public RegionFile(final Path path) throws IOException {
        Files.createDirectories(path.getParent());
        this.raf = new RandomAccessFile(path.toFile(), "rw");

        if (raf.length() < RegionConstants.HEADER_BYTES) {
            raf.setLength(RegionConstants.HEADER_BYTES);
        }

        if (raf.length() % RegionConstants.SECTOR_BYTES != 0) {
            final long padded = (raf.length() / RegionConstants.SECTOR_BYTES + 1) * RegionConstants.SECTOR_BYTES;
            raf.setLength(padded);
        }
        readHeader();
    }

    private void readHeader() throws IOException {
        raf.seek(0);
        for (int i = 0; i < RegionConstants.CHUNKS_PER_REGION; i++) {
            final int packed = raf.readInt();
            offsets[i] = packed >>> 8;
            sectorCounts[i] = packed & 0xFF;
        }
        for (int i = 0; i < RegionConstants.CHUNKS_PER_REGION; i++) {
            timestamps[i] = raf.readInt();
        }
        rebuildSectorMap();
    }

    private void rebuildSectorMap() throws IOException {
        final int totalSectors = (int) (raf.length() / RegionConstants.SECTOR_BYTES);
        usedSectors = new boolean[Math.max(totalSectors, RegionConstants.HEADER_SECTORS)];
        usedSectors[0] = true;
        usedSectors[1] = true;
        for (int i = 0; i < RegionConstants.CHUNKS_PER_REGION; i++) {
            if (offsets[i] == 0 || sectorCounts[i] == 0) continue;
            for (int s = 0; s < sectorCounts[i]; s++) {
                final int sector = offsets[i] + s;
                if (sector < usedSectors.length) usedSectors[sector] = true;
            }
        }
    }

    public boolean hasChunk(final int chunkX, final int chunkZ) {
        final int i = RegionConstants.headerIndex(chunkX, chunkZ);
        return offsets[i] != 0 && sectorCounts[i] != 0;
    }

    /**
     * A chunk that was read, with the algorithm it was stored with.
     */
    public record ChunkRead(CompoundBinaryTag tag, RegionCompression compression) {
    }

    /**
     * The raw bytes of a chunk: {@code payload} holds the compressed data from {@code dataOffset} on (before it is the
     * name of a custom algorithm).
     */
    private record Stored(RegionCompression compression, byte[] payload, int dataOffset) {

        InputStream decompressed() throws IOException {
            return compression.decompress(new ByteArrayInputStream(payload, dataOffset, payload.length - dataOffset));
        }
    }

    public @Nullable CompoundBinaryTag readChunk(final int chunkX, final int chunkZ) throws IOException {
        final ChunkRead read = read(chunkX, chunkZ);
        return read == null ? null : read.tag();
    }

    /**
     * Reads a chunk, and tells which algorithm it was stored with so that it can be rewritten with another one.
     */
    public @Nullable ChunkRead read(final int chunkX, final int chunkZ) throws IOException {
        final Stored stored = readStored(chunkX, chunkZ);
        if (stored == null) return null;
        try (final DataInputStream in = new DataInputStream(stored.decompressed())) {
            return new ChunkRead(BinaryTagIO.reader().readNamed((DataInput) in).getValue(), stored.compression());
        }
    }

    /**
     * {@return the algorithm the chunk is stored with, without decompressing it, or {@code null} if there is no chunk}
     */
    public @Nullable RegionCompression compression(final int chunkX, final int chunkZ) throws IOException {
        final int i = RegionConstants.headerIndex(chunkX, chunkZ);
        if (offsets[i] == 0 || sectorCounts[i] == 0) return null;
        raf.seek((long) offsets[i] * RegionConstants.SECTOR_BYTES);
        final int length = raf.readInt();
        if (length <= 0) return null;
        final int compressionByte = raf.readUnsignedByte();
        final RegionCompression vanilla = vanillaCompression(compressionByte, chunkX, chunkZ);
        return vanilla != null ? vanilla : customCompression(raf.readUTF(), chunkX, chunkZ);
    }

    /**
     * Rewrites a chunk with {@code target} if it is stored with another algorithm. The NBT data is copied as is
     * (decompressed, then compressed again) without being parsed.
     *
     * @return whether the chunk was rewritten
     */
    public boolean recompress(final int chunkX, final int chunkZ, final RegionCompression target) throws IOException {
        final Stored stored = readStored(chunkX, chunkZ);
        if (stored == null || stored.compression() == target) return false;
        final byte[] nbt;
        try (final InputStream in = stored.decompressed()) {
            nbt = in.readAllBytes();
        }
        writeFrame(chunkX, chunkZ, buildFrame(nbt, target));
        return true;
    }

    private @Nullable Stored readStored(final int chunkX, final int chunkZ) throws IOException {
        final int i = RegionConstants.headerIndex(chunkX, chunkZ);
        if (offsets[i] == 0 || sectorCounts[i] == 0) return null;

        raf.seek((long) offsets[i] * RegionConstants.SECTOR_BYTES);
        final int length = raf.readInt();
        if (length <= 0) return null;
        if (length > sectorCounts[i] * RegionConstants.SECTOR_BYTES - 4) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " is corrupted: its length " + length
                    + " exceeds its " + sectorCounts[i] + " allocated sector(s)");
        }
        final int compressionByte = raf.readUnsignedByte();

        final byte[] payload = new byte[length - 1];
        raf.readFully(payload);

        final RegionCompression vanilla = vanillaCompression(compressionByte, chunkX, chunkZ);
        if (vanilla != null) {
            return new Stored(vanilla, payload, 0);
        }
        final ByteArrayInputStream bytes = new ByteArrayInputStream(payload);
        final String name = new DataInputStream(bytes).readUTF();
        return new Stored(customCompression(name, chunkX, chunkZ), payload, payload.length - bytes.available());
    }

    /**
     * {@return the Vanilla algorithm of this byte, or {@code null} for a custom algorithm (127)}
     */
    private static @Nullable RegionCompression vanillaCompression(final int compressionByte, final int chunkX, final int chunkZ)
            throws IOException {
        if ((compressionByte & RegionConstants.EXTERNAL_FLAG) != 0) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " is stored in an external .mcc file, which is not supported");
        }
        if (compressionByte == RegionConstants.COMPRESSION_CUSTOM) {
            return null;
        }
        final RegionCompression compression = RegionCompression.byId(compressionByte);
        if (compression == null) {
            throw new IOException("Unknown compression " + compressionByte + " for chunk " + chunkX + "," + chunkZ
                    + " (supported: " + RegionCompression.names() + ")");
        }
        return compression;
    }

    private static RegionCompression customCompression(final String name, final int chunkX, final int chunkZ) throws IOException {
        final RegionCompression compression = RegionCompression.byCustomName(name);
        if (compression == null) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " uses an unknown custom compression: " + name);
        }
        return compression;
    }

    public int timestamp(final int chunkX, final int chunkZ) {
        return timestamps[RegionConstants.headerIndex(chunkX, chunkZ)];
    }

    /**
     * Writes a chunk with the default compression ({@link RegionCompression#DEFAULT}).
     */
    public void writeChunk(final int chunkX, final int chunkZ, final CompoundBinaryTag chunk) throws IOException {
        writeChunk(chunkX, chunkZ, chunk, RegionCompression.DEFAULT);
    }

    public void writeChunk(final int chunkX, final int chunkZ, final CompoundBinaryTag chunk,
                           final RegionCompression compression) throws IOException {
        final ByteArrayOutputStream nbt = new ByteArrayOutputStream(16384);
        try (final DataOutputStream out = new DataOutputStream(nbt)) {
            BinaryTagIO.writer().writeNamed(Map.entry("", chunk), (DataOutput) out);
        }
        writeFrame(chunkX, chunkZ, buildFrame(nbt.toByteArray(), compression));
    }

    /**
     * Writes a frame to new sectors, updates the header, and only then frees the old sectors, so that the previous
     * version of the chunk stays intact if the server stops in the middle of the write.
     */
    private void writeFrame(final int chunkX, final int chunkZ, final byte[] frame) throws IOException {
        final int neededSectors = (frame.length + RegionConstants.SECTOR_BYTES - 1) / RegionConstants.SECTOR_BYTES;
        if (neededSectors >= 256) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " is too large (" + neededSectors
                    + " sectors): it would need an external .mcc file");
        }

        final int i = RegionConstants.headerIndex(chunkX, chunkZ);

        final int oldOffset = offsets[i];
        final int oldCount = sectorCounts[i];

        final int start = allocateSectors(neededSectors);

        raf.seek((long) start * RegionConstants.SECTOR_BYTES);
        raf.write(frame);

        final int pad = neededSectors * RegionConstants.SECTOR_BYTES - frame.length;
        if (pad > 0) raf.write(new byte[pad]);

        offsets[i] = start;
        sectorCounts[i] = neededSectors;
        timestamps[i] = (int) (System.currentTimeMillis() / 1000L);
        writeHeaderEntry(i);

        freeSectors(oldOffset, oldCount);
    }

    private static byte[] buildFrame(final byte[] nbt, final RegionCompression compression) throws IOException {
        final ByteArrayOutputStream compressed = new ByteArrayOutputStream(Math.max(1024, nbt.length / 4));
        try (final OutputStream out = compression.compress(compressed)) {
            out.write(nbt);
        }

        // [length][compression byte][name, if custom][data]
        final ByteArrayOutputStream body = new ByteArrayOutputStream(compressed.size() + 32);
        final DataOutputStream bodyOut = new DataOutputStream(body);
        bodyOut.writeByte(compression.id());
        final String customName = compression.customName();
        if (customName != null) {
            bodyOut.writeUTF(customName);
        }
        compressed.writeTo(bodyOut);

        final ByteArrayOutputStream frame = new ByteArrayOutputStream(body.size() + 4);
        final DataOutputStream frameOut = new DataOutputStream(frame);
        frameOut.writeInt(body.size());
        body.writeTo(frameOut);

        return frame.toByteArray();
    }

    private void freeSectors(final int start, final int count) {
        for (int s = 0; s < count; s++) {
            final int sector = start + s;
            if (sector >= RegionConstants.HEADER_SECTORS && sector < usedSectors.length) {
                usedSectors[sector] = false;
            }
        }
    }

    private int allocateSectors(final int count) throws IOException {
        int run = 0;
        int start = RegionConstants.HEADER_SECTORS;
        for (int s = RegionConstants.HEADER_SECTORS; s < usedSectors.length; s++) {
            if (!usedSectors[s]) {
                if (run == 0) start = s;
                if (++run == count) {
                    for (int k = 0; k < count; k++) usedSectors[start + k] = true;
                    return start;
                }
            } else {
                run = 0;
            }
        }
        // Not enough free space: append sectors at the end.
        final int newStart = usedSectors.length;
        final int newLength = newStart + count;
        usedSectors = Arrays.copyOf(usedSectors, newLength);
        for (int k = 0; k < count; k++) usedSectors[newStart + k] = true;
        raf.setLength((long) newLength * RegionConstants.SECTOR_BYTES);
        return newStart;
    }

    private void writeHeaderEntry(final int i) throws IOException {
        raf.seek((long) i * 4);
        raf.writeInt((offsets[i] << 8) | (sectorCounts[i] & 0xFF));
        raf.seek(RegionConstants.SECTOR_BYTES + (long) i * 4);
        raf.writeInt(timestamps[i]);
    }

    @Override
    public void close() throws IOException {
        raf.getFD().sync();
        raf.close();
    }
}
