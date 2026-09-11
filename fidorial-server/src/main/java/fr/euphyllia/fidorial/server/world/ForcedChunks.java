package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.util.ConcurrentLongSet;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

public final class ForcedChunks {

    public static final String FILE_NAME = "chunk_tickets.dat";

    private static final String TAG_DATA_VERSION = "DataVersion";
    private static final String TAG_DATA = "data";
    private static final String TAG_TICKETS = "tickets";
    private static final String TAG_CHUNK_POS = "chunk_pos";
    private static final String TAG_TYPE = "type";
    private static final String TAG_LEVEL = "level";

    private static final String FORCED_TYPE = "minecraft:forced";
    private static final int FORCED_LEVEL = 31;

    private final Key worldKey;
    private final ThreadedRegionRegionizer regionizer;
    private final ConcurrentLongSet chunks = new ConcurrentLongSet();
    private final AtomicBoolean dirty = new AtomicBoolean();
    private final AtomicBoolean restored = new AtomicBoolean();

    private final Object lock = new Object();
    private boolean ticketsHeld = true; // guarded by lock

    public ForcedChunks(final Key worldKey, final ThreadedRegionRegionizer regionizer) {
        this.worldKey = worldKey;
        this.regionizer = regionizer;
    }

    public boolean add(final int chunkX, final int chunkZ) {
        if (!track(chunkX, chunkZ)) {
            return false;
        }
        dirty.set(true);
        return true;
    }

    private boolean track(final int chunkX, final int chunkZ) {
        synchronized (lock) {
            if (!chunks.add(ChunkPos.chunkKey(chunkX, chunkZ))) {
                return false;
            }
            if (ticketsHeld) {
                regionizer.addTicket(worldKey, new ChunkPos(chunkX, chunkZ));
            }
        }
        return true;
    }

    public boolean remove(final int chunkX, final int chunkZ) {
        synchronized (lock) {
            if (!chunks.remove(ChunkPos.chunkKey(chunkX, chunkZ))) {
                return false;
            }
            if (ticketsHeld) {
                regionizer.removeTicket(worldKey, new ChunkPos(chunkX, chunkZ));
            }
        }
        dirty.set(true);
        return true;
    }

    public boolean contains(final int chunkX, final int chunkZ) {
        return chunks.contains(ChunkPos.chunkKey(chunkX, chunkZ));
    }

    public boolean contains(final long chunkKey) {
        return chunks.contains(chunkKey);
    }

    public int size() {
        return chunks.size();
    }

    public LongSet snapshot() {
        return chunks.snapshot();
    }

    public void forEach(final LongConsumer action) {
        chunks.forEach(action);
    }

    public void markDirty() {
        dirty.set(true);
    }

    public void releaseTickets() {
        synchronized (lock) {
            if (!ticketsHeld) {
                return;
            }
            ticketsHeld = false;
            chunks.forEach(key -> regionizer.removeTicket(worldKey, new ChunkPos(chunkX(key), chunkZ(key))));
        }
    }

    public int restore(final Path file) throws IOException {
        if (!restored.compareAndSet(false, true) || !Files.isRegularFile(file)) {
            return 0;
        }

        final CompoundBinaryTag root = BinaryTagIO.reader().readNamed(file, BinaryTagIO.Compression.GZIP).getValue();
        final CompoundBinaryTag data = root.getCompound(TAG_DATA);

        int count = 0;
        for (final BinaryTag entry : data.getList(TAG_TICKETS)) {
            if (!(entry instanceof final CompoundBinaryTag ticket)) {
                continue;
            }
            final String type = ticket.getString(TAG_TYPE);
            if (!FORCED_TYPE.equals(type) && !"forced".equals(type)) {
                continue;
            }
            if (!(ticket.get(TAG_CHUNK_POS) instanceof final IntArrayBinaryTag pos) || pos.value().length != 2) {
                continue;
            }
            if (track(pos.value()[0], pos.value()[1])) {
                count++;
            }
        }
        return count;
    }

    public boolean saveIfDirty(final Path file) throws IOException {
        if (!dirty.compareAndSet(true, false)) {
            return false;
        }
        try {
            write(file, chunks.snapshot());
            return true;
        } catch (final IOException | RuntimeException e) {
            dirty.set(true);
            throw e;
        }
    }

    private static void write(final Path file, final LongSet keys) throws IOException {
        final ListBinaryTag.Builder<BinaryTag> tickets = ListBinaryTag.builder();
        for (final long key : keys) {
            tickets.add(CompoundBinaryTag.builder()
                    .putInt(TAG_LEVEL, FORCED_LEVEL)
                    .putString(TAG_TYPE, FORCED_TYPE)
                    .putIntArray(TAG_CHUNK_POS, new int[] {chunkX(key), chunkZ(key)})
                    .build());
        }

        final CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put(TAG_DATA, CompoundBinaryTag.builder().put(TAG_TICKETS, tickets.build()).build())
                .putInt(TAG_DATA_VERSION, VersionConstants.DATA_VERSION)
                .build();

        Files.createDirectories(file.getParent());
        final Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        BinaryTagIO.writer().writeNamed(Map.entry("", root), tmp, BinaryTagIO.Compression.GZIP);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static int chunkX(final long key) {
        return (int) (key >> 32);
    }

    private static int chunkZ(final long key) {
        return (int) key;
    }
}
