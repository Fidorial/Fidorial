package fr.euphyllia.fidorial.server.codecs.networking;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * Writes values of type {@code T} to a buffer of type {@code B}; the write half of a {@link NetworkCodec}.
 * <p>
 * Records are composed field by field, in wire order:
 * <pre>
 * {@code
 * NetworkEncoder.field(PrimitiveCodecs.VAR_INT, Packet::entityId)
 *         .then(PrimitiveCodecs.BOOL, Packet::onGround);
 * }
 * </pre>
 */
// NullAway loses the nullness of type arguments through wildcards and lambdas
@SuppressWarnings("NullAway")
@FunctionalInterface
public interface NetworkEncoder<B, T extends @Nullable Object> {

    void write(B buf, T value);

    /**
     * One field of {@code R}.
     */
    static <B, R, A extends @Nullable Object> NetworkEncoder<B, R> field(final NetworkEncoder<? super B, A> encoder, final Function<? super R, ? extends A> getter) {
        return (buf, value) -> encoder.write(buf, getter.apply(value));
    }

    /**
     * Writes {@code constant} whatever the value.
     */
    static <B, R, A extends @Nullable Object> NetworkEncoder<B, R> constant(final NetworkEncoder<? super B, A> encoder, final A constant) {
        return (buf, _) -> encoder.write(buf, constant);
    }

    /**
     * Delegates to a write method of the value, for shapes that don't compose field by field.
     */
    static <B, R> NetworkEncoder<B, R> member(final BiConsumer<? super R, ? super B> writer) {
        return (buf, value) -> writer.accept(value, buf);
    }

    /**
     * This, then one more field of {@code R}, which may narrow {@code T}, e.g. after {@link #constant}.
     */
    default <R extends T, S extends B, A extends @Nullable Object> NetworkEncoder<S, R> then(final NetworkEncoder<? super S, A> encoder, final Function<? super R, ? extends A> getter) {
        return (buf, value) -> {
            write(buf, value);
            encoder.write(buf, getter.apply(value));
        };
    }

    /**
     * This, then {@code constant}.
     */
    default <S extends B, A extends @Nullable Object> NetworkEncoder<S, T> thenConstant(final NetworkEncoder<? super S, A> encoder, final A constant) {
        return (buf, value) -> {
            write(buf, value);
            encoder.write(buf, constant);
        };
    }

    default <U extends @Nullable Object> NetworkEncoder<B, U> comap(final Function<? super U, ? extends T> from) {
        return (buf, value) -> write(buf, from.apply(value));
    }

    /**
     * A presence flag followed by the value when it isn't {@code null}.
     */
    default NetworkEncoder<B, @Nullable T> nullable(final BiConsumer<? super B, Boolean> writePresent) {
        return (buf, value) -> {
            writePresent.accept(buf, value != null);
            if (value != null) {
                write(buf, value);
            }
        };
    }

    /**
     * A size followed by the elements.
     */
    default NetworkEncoder<B, List<T>> listOf(final ObjIntConsumer<? super B> writeSize) {
        return (buf, values) -> {
            writeSize.accept(buf, values.size());
            for (final T value : values) {
                write(buf, value);
            }
        };
    }
}
