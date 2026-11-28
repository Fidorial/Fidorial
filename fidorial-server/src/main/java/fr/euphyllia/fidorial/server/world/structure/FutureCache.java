package fr.euphyllia.fidorial.server.world.structure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public final class FutureCache<K, V> {

    private final int capacity;
    private final LinkedHashMap<K, CompletableFuture<V>> entries;

    public FutureCache(final int capacity) {
        this.capacity = capacity;
        this.entries = new LinkedHashMap<>(capacity, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<K, CompletableFuture<V>> eldest) {
                return size() > FutureCache.this.capacity;
            }
        };
    }

    public V get(final K key, final Function<K, V> loader) {
        final CompletableFuture<V> future;
        final boolean owner;
        synchronized (entries) {
            final CompletableFuture<V> existing = entries.get(key);
            if (existing != null) {
                future = existing;
                owner = false;
            } else {
                future = new CompletableFuture<>();
                entries.put(key, future);
                owner = true;
            }
        }
        if (owner) {
            try {
                future.complete(loader.apply(key));
            } catch (final Throwable failure) {
                synchronized (entries) {
                    entries.remove(key, future);
                }
                future.completeExceptionally(failure);
            }
        }
        try {
            return future.join();
        } catch (final CompletionException failure) {
            if (failure.getCause() instanceof final RuntimeException runtime) {
                throw runtime;
            }
            throw failure;
        }
    }

    /**
     * Computes the value of a key on the calling thread when no thread has started it yet, and returns at once
     * when one has, without waiting for it.
     *
     * <p>Threads that will soon all {@link #get} the same keys call this first: each one takes the keys nobody is
     * on, instead of every thread queueing behind the one computing the first key.</p>
     *
     * @return whether this thread computed the value
     */
    public boolean prefetch(final K key, final Function<K, V> loader) {
        final CompletableFuture<V> future;
        synchronized (entries) {
            if (entries.containsKey(key)) {
                return false;
            }
            future = new CompletableFuture<>();
            entries.put(key, future);
        }
        try {
            future.complete(loader.apply(key));
        } catch (final Throwable failure) {
            synchronized (entries) {
                entries.remove(key, future);
            }
            future.completeExceptionally(failure);
            if (failure instanceof final Error error) {
                throw error;
            }
            // the next get computes it again and reports the failure to its caller
        }
        return true;
    }

    public void put(final K key, final V value) {
        synchronized (entries) {
            entries.putIfAbsent(key, CompletableFuture.completedFuture(value));
        }
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
    }
}
