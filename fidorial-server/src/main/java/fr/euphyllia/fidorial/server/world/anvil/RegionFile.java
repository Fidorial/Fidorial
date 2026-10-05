package fr.euphyllia.fidorial.server.world.anvil;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

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
     * The header of a stored chunk: its compression, and the length of the compressed data that follows it.
     */
    private record ChunkHeader(RegionCompression compression, int dataLength) {
    }

    /**
     * Reads a chunk, and tells which algorithm it was stored with so that it can be rewritten with another one.
     */
    public @Nullable ChunkRead read(final int chunkX, final int chunkZ) throws IOException {
        final ChunkHeader header = readChunkHeader(chunkX, chunkZ);
        if (header == null) return null;
        try (final DataInputStream in = new DataInputStream(header.compression().decompress(chunkData(header.dataLength())))) {
            return new ChunkRead(BinaryTagIO.reader().readNamed((DataInput) in).getValue(), header.compression());
        }
    }

    /**
     * Rewrites a chunk with {@code target} if it is stored with another algorithm. The NBT data is streamed from the
     * old compression to the new one without being parsed.
     *
     * @return whether the chunk was rewritten
     */
    public boolean recompress(final int chunkX, final int chunkZ, final RegionCompression target) throws IOException {
        final ChunkHeader header = readChunkHeader(chunkX, chunkZ);
        if (header == null || header.compression() == target) return false;
        final byte[] frame;
        try (final InputStream in = header.compression().decompress(chunkData(header.dataLength()))) {
            frame = buildFrame(target, in::transferTo);
        }
        writeFrame(chunkX, chunkZ, frame);
        return true;
    }

    /**
     * Reads the header of a chunk ({@code [length][compression byte][name, if custom]}) and leaves the file
     * positioned at the start of its compressed data.
     *
     * @return the header, or {@code null} if there is no chunk
     * @see <a href="https://minecraft.wiki/w/Region_file_format#Chunk_data">Region file format: chunk data</a>
     */
    private @Nullable ChunkHeader readChunkHeader(final int chunkX, final int chunkZ) throws IOException {
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
        if ((compressionByte & RegionConstants.EXTERNAL_FLAG) != 0) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " is stored in an external .mcc file, which is not supported");
        }
        if (compressionByte != RegionConstants.COMPRESSION_CUSTOM) {
            final RegionCompression compression = RegionCompression.byId(compressionByte);
            if (compression == null) {
                throw new IOException("Unknown compression " + compressionByte + " for chunk " + chunkX + "," + chunkZ
                        + " (supported: " + RegionCompression.names() + ")");
            }
            return new ChunkHeader(compression, length - 1);
        }

        final long nameStart = raf.getFilePointer();
        final String name = raf.readUTF();
        final RegionCompression compression = RegionCompression.byCustomName(name);
        if (compression == null) {
            throw new IOException("Chunk " + chunkX + "," + chunkZ + " uses an unknown custom compression: " + name);
        }

        return new ChunkHeader(compression, length - 1 - (int) (raf.getFilePointer() - nameStart));
    }

    /**
     * {@return the next {@code length} bytes of the file, read directly from it as they are consumed}
     * Closing the stream leaves the file open.
     */
    private InputStream chunkData(final int length) {
        return new BufferedInputStream(new InputStream() {
            private int remaining = length;

            @Override
            public int read() throws IOException {
                if (remaining <= 0) return -1;
                final int b = raf.read();
                if (b >= 0) remaining--;
                return b;
            }

            @Override
            public int read(final byte[] buffer, final int offset, final int count) throws IOException {
                if (remaining <= 0) return -1;
                final int read = raf.read(buffer, offset, Math.min(count, remaining));
                if (read > 0) remaining -= read;
                return read;
            }
        }, Math.clamp(length, 1, 8192));
    }

    public int timestamp(final int chunkX, final int chunkZ) {
        return timestamps[RegionConstants.headerIndex(chunkX, chunkZ)];
    }

    public void writeChunk(final int chunkX, final int chunkZ, final CompoundBinaryTag chunk,
                           final RegionCompression compression) throws IOException {
        writeFrame(chunkX, chunkZ, buildFrame(compression,
                out -> BinaryTagIO.writer().writeNamed(Map.entry("", chunk), (DataOutput) new DataOutputStream(out))));
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
        final ByteArrayOutputStream frame = new ByteArrayOutputStream(8192);
        final DataOutputStream header = new DataOutputStream(frame);
        header.writeInt(0); // the length, known once the data is compressed
        header.writeByte(compression.id());
        final String customName = compression.customName();
        if (customName != null) {
            header.writeUTF(customName);
        }

        try (final OutputStream out = compression.compress(frame)) {
            content.writeTo(out);
        }
        final byte[] bytes = frame.toByteArray();
        ByteBuffer.wrap(bytes).putInt(0, bytes.length - Integer.BYTES);
        return bytes;
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
