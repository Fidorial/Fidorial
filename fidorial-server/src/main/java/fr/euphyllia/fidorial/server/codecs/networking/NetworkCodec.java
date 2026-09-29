package fr.euphyllia.fidorial.server.codecs.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Reads and writes values of type {@code T} from and to a buffer of type {@code B}.
 */
public interface NetworkCodec<B, T> {

    T read(B buf);

    void write(B buf, T value);

    static <B, T> NetworkCodec<B, T> of(final Function<B, T> reader, final BiConsumer<B, T> writer) {
        return new NetworkCodec<>() {
            @Override
            public T read(final B buf) {
                return reader.apply(buf);
            }

            @Override
            public void write(final B buf, final T value) {
                writer.accept(buf, value);
            }
        };
    }

    default <U> NetworkCodec<B, U> xmap(final Function<? super T, ? extends U> to, final Function<? super U, ? extends T> from) {
        return of(buf -> to.apply(read(buf)), (buf, value) -> write(buf, from.apply(value)));
    }

    /**
     * A size followed by the elements.
     */
    default NetworkCodec<B, List<T>> listOf(final ToIntFunction<B> readSize, final ObjIntConsumer<B> writeSize, final int maxSize) {
        return of(
                buf -> {
                    final int size = readSize.applyAsInt(buf);
                    if (size < 0 || size > maxSize) {
                        throw new IllegalArgumentException("list size " + size + " out of range [0, " + maxSize + "]");
                    }
                    final List<T> values = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        values.add(read(buf));
                    }
                    return List.copyOf(values);
                },
                (buf, values) -> {
                    writeSize.accept(buf, values.size());
                    for (final T value : values) {
                        write(buf, value);
                    }
                });
    }

    /**
     * A presence flag followed by the value when present.
     */
    default NetworkCodec<B, Optional<T>> optional(final Predicate<B> readPresent, final BiConsumer<B, Boolean> writePresent) {
        return of(
                buf -> readPresent.test(buf) ? Optional.of(read(buf)) : Optional.empty(),
                (buf, value) -> {
                    writePresent.accept(buf, value.isPresent());
                    value.ifPresent(present -> write(buf, present));
                });
    }
}
