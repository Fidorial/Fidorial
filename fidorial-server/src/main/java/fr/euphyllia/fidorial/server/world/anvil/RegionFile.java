package fr.euphyllia.fidorial.server.world.anvil;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;

/**
 * Thread-safe: only the file accesses and the sector bookkeeping hold the lock of this object. Compressing,
 * decompressing and (de)serializing NBT happen outside of it, so several threads can work on chunks of the same
 * region at once.
 */
public final class RegionFile implements Closeable {

    /**
     * The buffer between the NBT codec and the (de)compressor. Without it, every byte of NBT is a call into the
     * compression library.
     */
    private static final int STREAM_BUFFER = 8192;

    private final FileChannel channel;
    private final int[] offsets = new int[RegionConstants.CHUNKS_PER_REGION];
    private final int[] sectorCounts = new int[RegionConstants.CHUNKS_PER_REGION];
    private final int[] timestamps = new int[RegionConstants.CHUNKS_PER_REGION];
    /**
     * Incremented on every write of a chunk, so that {@link #recompress} never overwrites a newer version.
     */
    private final int[] generations = new int[RegionConstants.CHUNKS_PER_REGION];
    private boolean[] usedSectors = new boolean[0];

    public RegionFile(final Path path) throws IOException {
        Files.createDirectories(path.getParent());
        this.channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);

        final long sectors = Math.max(RegionConstants.HEADER_SECTORS,
                (channel.size() + RegionConstants.SECTOR_BYTES - 1) / RegionConstants.SECTOR_BYTES);
        if (channel.size() != sectors * RegionConstants.SECTOR_BYTES) {
            setLength(sectors * RegionConstants.SECTOR_BYTES);
        }
        readHeader();
    }

    private void readHeader() throws IOException {
        final ByteBuffer header = ByteBuffer.allocate(RegionConstants.HEADER_BYTES);
        readFully(header, 0);
        header.flip();
        for (int i = 0; i < RegionConstants.CHUNKS_PER_REGION; i++) {
            final int packed = header.getInt();
            offsets[i] = packed >>> 8;
            sectorCounts[i] = packed & 0xFF;
        }
        for (int i = 0; i < RegionConstants.CHUNKS_PER_REGION; i++) {
            timestamps[i] = header.getInt();
        }
        rebuildSectorMap();
    }

    private void rebuildSectorMap() throws IOException {
        final int totalSectors = (int) (channel.size() / RegionConstants.SECTOR_BYTES);
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

    public synchronized boolean hasChunk(final int chunkX, final int chunkZ) {
        final int i = RegionConstants.headerIndex(chunkX, chunkZ);
        return offsets[i] != 0 && sectorCounts[i] != 0;
    }

    public synchronized int timestamp(final int chunkX, final int chunkZ) {
        return timestamps[RegionConstants.headerIndex(chunkX, chunkZ)];
    }

    /**
     * A chunk that was read, with the algorithm it was stored with.
     */
    public record ChunkRead(CompoundBinaryTag tag, RegionCompression compression) {
    }

    /**
     * The compressed data of a stored chunk, read from the file in a single access.
     *
     * @param generation the value of {@link #generations} when it was read
     */
    private record StoredChunk(RegionCompression compression, byte[] bytes, int dataOffset, int dataLength, int generation) {

        InputStream decompressed() throws IOException {
            return new BufferedInputStream(compression.decompress(new ByteArrayInputStream(bytes, dataOffset, dataLength)), STREAM_BUFFER);
        }
    }

    /**
     * Reads a chunk, and tells which algorithm it was stored with so that it can be rewritten with another one.
     */
    public @Nullable ChunkRead read(final int chunkX, final int chunkZ) throws IOException {
        final StoredChunk stored = readStored(chunkX, chunkZ);
        if (stored == null) return null;
        try (final DataInputStream in = new DataInputStream(stored.decompressed())) {
            return new ChunkRead(BinaryTagIO.reader().readNamed((DataInput) in).getValue(), stored.compression());
        }
    }

    public void writeChunk(final int chunkX, final int chunkZ, final CompoundBinaryTag chunk,
                           final RegionCompression compression) throws IOException {
        final byte[] frame = buildFrame(compression,
                out -> BinaryTagIO.writer().writeNamed(Map.entry("", chunk), (DataOutput) new DataOutputStream(out)));
        synchronized (this) {
            writeFrame(chunkX, chunkZ, frame);
        }
    }

    /**
     * Rewrites a chunk with {@code target} if it is stored with another algorithm. The NBT data is streamed from the
     * old compression to the new one without being parsed. Does nothing if the chunk is written again meanwhile.
     *
     * @return whether the chunk was rewritten
     */
    public boolean recompress(final int chunkX, final int chunkZ, final RegionCompression target) throws IOException {
        final StoredChunk stored = readStored(chunkX, chunkZ);
        if (stored == null || stored.compression() == target) return false;
        final byte[] frame;
        try (final InputStream in = stored.decompressed()) {
            frame = buildFrame(target, in::transferTo);
        }
        synchronized (this) {
            if (generations[RegionConstants.headerIndex(chunkX, chunkZ)] != stored.generation()) return false;
            writeFrame(chunkX, chunkZ, frame);
        }
        return true;
    }

    /**
     * Reads the frame of a chunk ({@code [length][compression byte][name, if custom][data]}) with a single access to
     * the file, covering its allocated sectors.
     *
     * @return the frame, or {@code null} if there is no chunk
     * @see <a href="https://minecraft.wiki/w/Region_file_format#Chunk_data">Region file format: chunk data</a>
     */
    private @Nullable StoredChunk readStored(final int chunkX, final int chunkZ) throws IOException {
        final int i = RegionConstants.headerIndex(chunkX, chunkZ);
        final byte[] bytes;
        final int generation;
        synchronized (this) {
            if (offsets[i] == 0 || sectorCounts[i] == 0) return null;
            bytes = new byte[sectorCounts[i] * RegionConstants.SECTOR_BYTES];
            readFully(ByteBuffer.wrap(bytes), (long) offsets[i] * RegionConstants.SECTOR_BYTES);
            generation = generations[i];
        }

        final ByteBuffer frame = ByteBuffer.wrap(bytes);
        final int length = frame.getInt();
        if (length <= 0) return null;
        if (length > bytes.length - Integer.BYTES) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " is corrupted: its length " + length
                    + " exceeds its " + bytes.length / RegionConstants.SECTOR_BYTES + " allocated sector(s)");
        }
        final int compressionByte = Byte.toUnsignedInt(frame.get());
        if ((compressionByte & RegionConstants.EXTERNAL_FLAG) != 0) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " is stored in an external .mcc file, which is not supported");
        }
        final RegionCompression compression;
        if (compressionByte == RegionConstants.COMPRESSION_CUSTOM) {
            final int nameLength = Short.toUnsignedInt(frame.getShort());
            final String name = new String(bytes, frame.position(), nameLength, StandardCharsets.UTF_8);
            frame.position(frame.position() + nameLength);
            compression = RegionCompression.byCustomName(name);
            if (compression == null) {
                throw new IOException("Chunk " + chunkX + "," + chunkZ + " uses an unknown custom compression: " + name);
            }
        } else {
            compression = RegionCompression.byId(compressionByte);
            if (compression == null) {
                throw new IOException("Unknown compression " + compressionByte + " for chunk " + chunkX + "," + chunkZ
                        + " (supported: " + RegionCompression.names() + ")");
            }
        }
        return new StoredChunk(compression, bytes, frame.position(), Integer.BYTES + length - frame.position(), generation);
    }

    @FunctionalInterface
    private interface ChunkContent {
        void writeTo(OutputStream out) throws IOException;
    }

    /**
     * Builds the frame of a chunk: {@code [length][compression byte][name, if custom][compressed data]}.
     *
     * @see <a href="https://minecraft.wiki/w/Region_file_format#Chunk_data">Region file format: chunk data</a>
     */
    private static byte[] buildFrame(final RegionCompression compression, final ChunkContent content) throws IOException {
        final ByteArrayOutputStream frame = new ByteArrayOutputStream(STREAM_BUFFER);
        final DataOutputStream header = new DataOutputStream(frame);
        header.writeInt(0); // the length, known once the data is compressed
        header.writeByte(compression.id());
        final String customName = compression.customName();
        if (customName != null) {
            header.writeUTF(customName);
        }
        try (final OutputStream out = new BufferedOutputStream(compression.compress(frame), STREAM_BUFFER)) {
            content.writeTo(out);
        }

        final byte[] bytes = frame.toByteArray();
        ByteBuffer.wrap(bytes).putInt(0, bytes.length - Integer.BYTES);
        return bytes;
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

        // The sectors already exist (allocateSectors grows the file), so there is no padding to write.
        final int start = allocateSectors(neededSectors);
        writeFully(ByteBuffer.wrap(frame), (long) start * RegionConstants.SECTOR_BYTES);

        offsets[i] = start;
        sectorCounts[i] = neededSectors;
        timestamps[i] = (int) (System.currentTimeMillis() / 1000L);
        generations[i]++;
        writeHeaderEntry(i);

        freeSectors(oldOffset, oldCount);
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
        setLength((long) newLength * RegionConstants.SECTOR_BYTES);
        return newStart;
    }

    private void writeHeaderEntry(final int i) throws IOException {
        final ByteBuffer entry = ByteBuffer.allocate(Integer.BYTES);
        entry.putInt(0, (offsets[i] << 8) | (sectorCounts[i] & 0xFF));
        writeFully(entry, (long) i * Integer.BYTES);
        entry.clear().putInt(0, timestamps[i]);
        writeFully(entry, RegionConstants.SECTOR_BYTES + (long) i * Integer.BYTES);
    }

    /**
     * Resizes the file to {@code length} bytes; new bytes read as zeros.
     */
    private void setLength(final long length) throws IOException {
        final long size = channel.size();
        if (length < size) {
            channel.truncate(length);
        } else if (length > size) {
            writeFully(ByteBuffer.allocate(1), length - 1);
        }
    }

    private void readFully(final ByteBuffer buffer, final long position) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer, position + buffer.position()) < 0) {
                throw new EOFException("Unexpected end of region file at " + (position + buffer.position()));
            }
        }
    }

    private void writeFully(final ByteBuffer buffer, final long position) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer, position + buffer.position());
        }
    }

    @Override
    public synchronized void close() throws IOException {
        channel.force(true);
        channel.close();
    }
}
