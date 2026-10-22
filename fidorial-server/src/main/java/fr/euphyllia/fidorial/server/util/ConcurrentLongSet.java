package fr.euphyllia.fidorial.server.util;

import ca.spottedleaf.concurrentutil.collection.iterator.BaseLongIterator;
import ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2ReferenceHashTable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;
import java.util.function.LongConsumer;

/**
 * A concurrent set of primitive {@code long} keys.
 */
public final class ConcurrentLongSet {

    private static final Object PRESENT = new Object();

    private final ConcurrentChainedLong2ReferenceHashTable<Object> table;

    public ConcurrentLongSet() {
        this.table = new ConcurrentChainedLong2ReferenceHashTable<>();
    }

    public ConcurrentLongSet(final int expected) {
        this.table = ConcurrentChainedLong2ReferenceHashTable.createWithExpected(expected);
    }

    public boolean add(final long value) {
        return table.putIfAbsent(value, PRESENT) == null;
    }

    public boolean remove(final long value) {
        return table.remove(value) != null;
    }

    public boolean contains(final long value) {
        return table.containsKey(value);
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public int size() {
        return table.size();
    }

    public void clear() {
        table.clear();
    }

    public void addAll(final Collection<Long> values) {
        for (final long value : values) {
            add(value);
        }
    }

    public void addAll(final LongSet values) {
        final LongIterator it = values.iterator();
        while (it.hasNext()) {
            add(it.nextLong());
        }
    }

    public void removeAll(final LongSet values) {
        final LongIterator it = values.iterator();
        while (it.hasNext()) {
            remove(it.nextLong());
        }
    }

    public void forEach(final LongConsumer action) {
        final BaseLongIterator it = table.keyIterator();
        while (it.hasNext()) {
            action.accept(it.nextLong());
        }
    }

    public LongSet snapshot() {
        final LongOpenHashSet copy = new LongOpenHashSet(Math.max(8, size()));
        forEach(copy::add);
        return copy;
    }

    @Override
    public String toString() {
        return "ConcurrentLongSet(size=" + size() + ")";
    }
}
