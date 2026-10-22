package fr.euphyllia.fidorial.server.world.chunk;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class PalettedContainer<T> {

    private static final int LINEAR_LOOKUP_LIMIT = 8;
    private @Nullable Object2IntOpenHashMap<T> lookup;

    private final int size;
    private long @Nullable [] data; // null while every entry is palette[0]
    private final int minBits;

    private int bits;
    private int valuesPerLong;

    private @Nullable T lastValue = null;
    private int lastIndex = -1;

    private final StampedLock lock = new StampedLock();

    private volatile Object[] paletteArray = new Object[0];

    public PalettedContainer(final int size, final int minBits, final T fill) {
        this.size = size;
        this.minBits = minBits;
        indexOf(fill);
    }

    public static <T> PalettedContainer<T> fromNbt(final int size, final int minBits, final List<T> palette, final long @Nullable [] data) {
        final PalettedContainer<T> c = new PalettedContainer<>(size, minBits, palette.getFirst());
        for (int i = 1; i < palette.size(); i++) {
            c.indexOf(palette.get(i));
        }
        if (data != null && data.length > 0 && palette.size() > 1) {
            final int bits = BitPacking.bitsFor(palette.size(), minBits);
            if (bits == c.bits && data.length == longCount(size, c.valuesPerLong)) {
                c.data = data;
            } else {
                // duplicate palette entries or a mismatching length, go through the unpacked form
                c.data = BitPacking.pack(BitPacking.unpack(data, bits, size), c.bits);
            }
        }
        return c;
    }

    private static int longCount(final int size, final int valuesPerLong) {
        return (size + valuesPerLong - 1) / valuesPerLong;
    }

    private static int read(final long[] data, final int bits, final int valuesPerLong, final int index) {
        final int longIndex = index / valuesPerLong;
        final int shift = (index - longIndex * valuesPerLong) * bits;
        return (int) ((data[longIndex] >>> shift) & ((1L << bits) - 1L));
    }

    private static void write(final long[] data, final int bits, final int valuesPerLong, final int index, final int value) {
        final int longIndex = index / valuesPerLong;
        final int shift = (index - longIndex * valuesPerLong) * bits;
        final long mask = ((1L << bits) - 1L) << shift;
        data[longIndex] = (data[longIndex] & ~mask) | ((long) value << shift);
    }

    private int indexOf(final T value) {
        if (value == lastValue) {
            return lastIndex;
        }
        final Object[] pal = paletteArray;
        final int found = find(pal, value);
        if (found != -1) {
            lastValue = value;
            lastIndex = found;
            return found;
        }

        final int next = pal.length;
        final Object[] grown = Arrays.copyOf(pal, next + 1);
        grown[next] = value;
        if (lookup != null) {
            lookup.put(value, next);
        } else if (grown.length > LINEAR_LOOKUP_LIMIT) {
            final Object2IntOpenHashMap<T> map = new Object2IntOpenHashMap<>(grown.length * 2);
            map.defaultReturnValue(-1);
            for (int i = 0; i < grown.length; i++) {
                @SuppressWarnings("unchecked")
                final T entry = (T) grown[i];
                map.put(entry, i);
            }
            lookup = map;
        }
        resize(BitPacking.bitsFor(grown.length, minBits));
        paletteArray = grown;
        lastValue = value;
        lastIndex = next;
        return next;
    }

    private int find(final Object[] pal, final T value) {
        final Object2IntOpenHashMap<T> map = lookup;
        if (map != null) {
            return map.getInt(value);
        }
        for (int i = 0; i < pal.length; i++) {
            if (value.equals(pal[i])) {
                return i;
            }
        }
        return -1;
    }

    private void resize(final int newBits) {
        if (newBits == bits) {
            return;
        }
        final int newValuesPerLong = 64 / newBits;
        final long[] d = data;
        if (d != null) {
            final long[] repacked = new long[longCount(size, newValuesPerLong)];
            for (int i = 0; i < size; i++) {
                write(repacked, newBits, newValuesPerLong, i, read(d, bits, valuesPerLong, i));
            }
            data = repacked;
        }
        bits = newBits;
        valuesPerLong = newValuesPerLong;
    }

    public void set(final int index, final T value) {
        final long stamp = lock.writeLock();
        try {
            final int idx = indexOf(value);
            long[] d = data;
            if (d == null) {
                if (idx == 0) {
                    return;
                }
                d = data = new long[longCount(size, valuesPerLong)];
            }
            write(d, bits, valuesPerLong, index, idx);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public T get(final int index) {
        final long stamp = lock.tryOptimisticRead();
        final Object[] pal = paletteArray;
        final long[] d = data;
        final int b = bits;
        final int perLong = valuesPerLong;
        // the fields may not match each other until validated
        int di = 0;
        if (d != null) {
            final int longIndex = perLong == 0 ? d.length : index / perLong;
            di = longIndex < d.length
                    ? (int) ((d[longIndex] >>> ((index - longIndex * perLong) * b)) & ((1L << b) - 1L))
                    : -1;
        }
        if (stamp != 0L && lock.validate(stamp) && di >= 0 && di < pal.length) {
            @SuppressWarnings("unchecked")
            final T v = (T) pal[di];
            return v;
        }

        final long rs = lock.readLock();
        try {
            final long[] locked = data;
            @SuppressWarnings("unchecked")
            final T v = (T) paletteArray[locked == null ? 0 : read(locked, bits, valuesPerLong, index)];
            return v;
        } finally {
            lock.unlockRead(rs);
        }
    }

    public List<T> palette() {
        final long stamp = lock.readLock();
        try {
            return List.copyOf(paletteList());
        } finally {
            lock.unlockRead(stamp);
        }
    }

    private List<T> paletteList() {
        @SuppressWarnings("unchecked")
        final T[] pal = (T[]) paletteArray;
        return Arrays.asList(pal);
    }

    public boolean isSingleValue() {
        final long stamp = lock.readLock();
        try {
            return paletteArray.length == 1;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public int bitsPerEntry() {
        final long stamp = lock.readLock();
        try {
            return bits;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public long[] packedGlobal(final int bits, final ToIntFunction<T> mapper) {
        final long stamp = lock.readLock();
        try {
            final List<T> pal = paletteList();
            final int[] global = new int[size];
            final long[] d = data;
            if (d == null) {
                Arrays.fill(global, mapper.applyAsInt(pal.getFirst()));
            } else {
                for (int i = 0; i < size; i++) {
                    global[i] = mapper.applyAsInt(pal.get(read(d, this.bits, valuesPerLong, i)));
                }
            }
            return BitPacking.pack(global, bits);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public long @Nullable [] packedData() {
        final long stamp = lock.readLock();
        try {
            if (paletteArray.length == 1) return null;
            final long[] d = data;
            return d != null ? d.clone() : new long[longCount(size, valuesPerLong)];
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public boolean contains(final Predicate<T> test) {
        final long stamp = lock.readLock();
        try {
            for (final T value : paletteList()) {
                if (test.test(value)) return true;
            }
            return false;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public int count(final Predicate<T> test) {
        final long stamp = lock.readLock();
        try {
            final List<T> pal = paletteList();
            final long[] d = data;
            if (d == null) {
                return test.test(pal.getFirst()) ? size : 0;
            }
            final boolean[] matches = new boolean[pal.size()];
            boolean any = false;
            for (int i = 0; i < matches.length; i++) {
                matches[i] = test.test(pal.get(i));
                any |= matches[i];
            }
            if (!any) return 0;

            final long mask = (1L << bits) - 1L;
            int n = 0;
            int index = 0;
            for (final long word : d) {
                long w = word;
                for (int i = 0; i < valuesPerLong && index < size; i++, index++) {
                    if (matches[(int) (w & mask)]) n++;
                    w >>>= bits;
                }
            }
            return n;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public PalettedContainerSnapshot<T> snapshot() {
        final long stamp = lock.readLock();
        try {
            final long[] d = data;
            return new PalettedContainerSnapshot<>(paletteList(), d != null ? BitPacking.unpack(d, bits, size) : null, minBits);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public record PalettedContainerSnapshot<T>(List<T> palette, int @Nullable [] data, int minBits) {
        public T get(final int index) {
            return palette.get(data == null ? 0 : data[index]);
        }
    }

    public record PackedSnapshot<T>(List<T> palette, long @Nullable [] data, int bits) {
    }

    public PackedSnapshot<T> packedSnapshot() {
        final long stamp = lock.readLock();
        try {
            final long[] d = data;
            return new PackedSnapshot<>(paletteList(), d != null ? d.clone() : null, bits);
        } finally {
            lock.unlockRead(stamp);
        }
    }
}
