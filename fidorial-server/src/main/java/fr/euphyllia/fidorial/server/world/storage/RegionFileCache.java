package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.world.anvil.RegionFile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps a bounded number of region files open, closing the least recently used ones.
 */
final class RegionFileCache implements AutoCloseable {

    /**
     * Default number of region files kept open per storage.
     */
    static final int DEFAULT_MAX_OPEN = 256;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(RegionFileCache.class);

    /**
     * Value of {@link Entry#users} once the entry is retired: it can no longer be pinned.
     */
    private static final int RETIRED = -1;

    private final int maxOpen;
    private final Map<RegionKey, Entry> entries = new ConcurrentHashMap<>();
    /**
     * Logical clock ordering the uses, cheaper than reading the time.
     */
    private final AtomicLong clock = new AtomicLong();
    private final AtomicBoolean evicting = new AtomicBoolean();
    private volatile boolean closed;

    RegionFileCache(final int maxOpen) {
        if (maxOpen < 1) {
            throw new IllegalArgumentException("maxOpen must be positive, got " + maxOpen);
        }
        this.maxOpen = maxOpen;
    }

    /**
     * Runs an action on a region file, opening it when needed.
     *
     * @param path where the file is, used when it has to be opened
     */
    <R> R with(final Key dimension, final int regionX, final int regionZ, final Path path, final RegionAction<R> action)
            throws IOException {
        final RegionKey key = new RegionKey(dimension, regionX, regionZ);
        final Entry entry = acquire(key, path);
        try {
            return action.apply(entry.file());
        } finally {
            release(entry);
        }
    }

    /**
     * {@return the file if it is open right now, without opening it}
     */
    @Nullable RegionFile peek(final Key dimension, final int regionX, final int regionZ) {
        final Entry entry = entries.get(new RegionKey(dimension, regionX, regionZ));
        return entry == null || entry.users.get() == RETIRED ? null : entry.opened;
    }

    private Entry acquire(final RegionKey key, final Path path) throws IOException {
        while (true) {
            if (closed) {
                throw new IOException("The region storage is closed");
            }
            final Entry entry = entries.computeIfAbsent(key, k -> new Entry(k, path)); // no I/O in there
            if (!entry.tryPin()) {
                entry.gone.join(); // being closed: wait for it, then open it again
                continue;
            }
            entry.lastUse = clock.incrementAndGet();
            try {
                entry.open();
            } catch (final IOException | RuntimeException e) {
                release(entry);
                retire(entry); // let a later call try again
                throw e;
            }
            if (entries.size() > maxOpen) {
                evictOverflow();
            }
            return entry;
        }
    }

    private void release(final Entry entry) {
        entry.users.decrementAndGet();
    }

    /**
     * Closes the least recently used files that nobody is using, down to the limit. Skipped when another thread is
     * already doing it: that thread will bring the count down.
     */
    private void evictOverflow() {
        if (!evicting.compareAndSet(false, true)) {
            return;
        }
        try {
            int excess = entries.size() - maxOpen;
            if (excess <= 0) {
                return;
            }
            final List<Entry> idle = new ArrayList<>();
            for (final Entry entry : entries.values()) {
                if (entry.users.get() == 0) {
                    idle.add(entry);
                }
            }
            idle.sort(Comparator.comparingLong(entry -> entry.lastUse));
            for (final Entry entry : idle) {
                if (excess <= 0) {
                    break;
                }
                if (retire(entry)) {
                    excess--;
                }
            }
        } finally {
            evicting.set(false);
        }
    }

    /**
     * Closes an entry nobody is using and removes it.
     *
     * @return {@code false} when it got pinned meanwhile, or was already retired
     */
    private boolean retire(final Entry entry) {
        if (!entry.users.compareAndSet(0, RETIRED)) {
            return false;
        }
        try {
            entry.closeFile();
        } finally {
            entries.remove(entry.key, entry); // only now may the file be opened again
            entry.gone.complete(null);
        }
        return true;
    }

    @Override
    public void close() {
        closed = true;
        for (final Entry entry : entries.values()) {
            // shutting down: close even what is still in use, as nothing will be saved after this
            entry.users.set(RETIRED);
            try {
                entry.closeFile();
            } finally {
                entries.remove(entry.key, entry);
                entry.gone.complete(null);
            }
        }
    }

    @FunctionalInterface
    interface RegionAction<R> {
        R apply(RegionFile file) throws IOException;
    }

    private record RegionKey(Key dimension, int regionX, int regionZ) {
    }

    private static final class Entry {
        final RegionKey key;
        final Path path;
        /**
         * Threads using the file, or {@link #RETIRED}.
         */
        final AtomicInteger users = new AtomicInteger();
        /**
         * Completed once the entry is closed and out of the map.
         */
        final CompletableFuture<Void> gone = new CompletableFuture<>();
        volatile @Nullable RegionFile opened;
        volatile long lastUse;

        Entry(final RegionKey key, final Path path) {
            this.key = key;
            this.path = path;
        }

        boolean tryPin() {
            while (true) {
                final int current = users.get();
                if (current == RETIRED) {
                    return false;
                }
                if (users.compareAndSet(current, current + 1)) {
                    return true;
                }
            }
        }

        /**
         * Opens the file on first use. Only the threads asking for this very file wait here.
         */
        void open() throws IOException {
            if (opened != null) {
                return;
            }
            synchronized (this) {
                if (opened == null) {
                    opened = new RegionFile(path);
                }
            }
        }

        RegionFile file() {
            final RegionFile file = opened;
            if (file == null) {
                throw new IllegalStateException("Region file " + path + " used before being opened");
            }
            return file;
        }

        void closeFile() {
            final RegionFile file;
            synchronized (this) {
                file = opened;
                opened = null;
            }
            if (file == null) {
                return;
            }
            try {
                file.close();
            } catch (final IOException e) {
                LOGGER.warn("Failed to close the region file {}", path, e);
            }
        }
    }
}
