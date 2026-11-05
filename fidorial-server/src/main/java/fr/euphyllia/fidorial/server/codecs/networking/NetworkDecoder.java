package fr.euphyllia.fidorial.server.codecs.networking;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Reads values of type {@code T} from a buffer of type {@code B}; the read half of a {@link NetworkCodec}.
 * <p>
 * Records are composed from one decoder per component, in wire order, then the constructor:
 * <pre>
 * {@code
 * NetworkDecoder.composite(
 *         PrimitiveCodecs.VAR_INT,
 *         PrimitiveCodecs.BOOL,
 *         Packet::new);
 * }
 * </pre>
 */
@FunctionalInterface
public interface NetworkDecoder<B, T extends @Nullable Object> {

    T read(B buf);

    default <U extends @Nullable Object> NetworkDecoder<B, U> map(final Function<? super T, ? extends U> to) {
        return buf -> to.apply(read(buf));
    }

    /**
     * A size followed by the elements.
     */
    default NetworkDecoder<B, List<T>> listOf(final ToIntFunction<? super B> readSize, final int maxSize) {
        return buf -> {
            final int size = readSize.applyAsInt(buf);
            if (size < 0 || size > maxSize) {
                throw new IllegalArgumentException("list size " + size + " out of range [0, " + maxSize + "]");
            }
            final List<T> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(read(buf));
            }
            return List.copyOf(values);
        };
    }

    static <B, T1 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final Function<? super T1, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            return constructor.apply(v1);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final BiFunction<? super T1, ? super T2, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            return constructor.apply(v1, v2);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final Function3<? super T1, ? super T2, ? super T3, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            return constructor.apply(v1, v2, v3);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            return constructor.apply(v1, v2, v3, v4);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final Function9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final Function10<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, T11 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final NetworkDecoder<? super B, ? extends T11> d11,
            final Function11<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            final T11 v11 = d11.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, T11 extends @Nullable Object, T12 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final NetworkDecoder<? super B, ? extends T11> d11,
            final NetworkDecoder<? super B, ? extends T12> d12,
            final Function12<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            final T11 v11 = d11.read(buf);
            final T12 v12 = d12.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, T11 extends @Nullable Object, T12 extends @Nullable Object, T13 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final NetworkDecoder<? super B, ? extends T11> d11,
            final NetworkDecoder<? super B, ? extends T12> d12,
            final NetworkDecoder<? super B, ? extends T13> d13,
            final Function13<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            final T11 v11 = d11.read(buf);
            final T12 v12 = d12.read(buf);
            final T13 v13 = d13.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, T11 extends @Nullable Object, T12 extends @Nullable Object, T13 extends @Nullable Object, T14 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final NetworkDecoder<? super B, ? extends T11> d11,
            final NetworkDecoder<? super B, ? extends T12> d12,
            final NetworkDecoder<? super B, ? extends T13> d13,
            final NetworkDecoder<? super B, ? extends T14> d14,
            final Function14<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, ? super T14, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            final T11 v11 = d11.read(buf);
            final T12 v12 = d12.read(buf);
            final T13 v13 = d13.read(buf);
            final T14 v14 = d14.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, T11 extends @Nullable Object, T12 extends @Nullable Object, T13 extends @Nullable Object, T14 extends @Nullable Object, T15 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final NetworkDecoder<? super B, ? extends T11> d11,
            final NetworkDecoder<? super B, ? extends T12> d12,
            final NetworkDecoder<? super B, ? extends T13> d13,
            final NetworkDecoder<? super B, ? extends T14> d14,
            final NetworkDecoder<? super B, ? extends T15> d15,
            final Function15<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, ? super T14, ? super T15, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            final T11 v11 = d11.read(buf);
            final T12 v12 = d12.read(buf);
            final T13 v13 = d13.read(buf);
            final T14 v14 = d14.read(buf);
            final T15 v15 = d15.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
        };
    }

    static <B, T1 extends @Nullable Object, T2 extends @Nullable Object, T3 extends @Nullable Object, T4 extends @Nullable Object, T5 extends @Nullable Object, T6 extends @Nullable Object, T7 extends @Nullable Object, T8 extends @Nullable Object, T9 extends @Nullable Object, T10 extends @Nullable Object, T11 extends @Nullable Object, T12 extends @Nullable Object, T13 extends @Nullable Object, T14 extends @Nullable Object, T15 extends @Nullable Object, T16 extends @Nullable Object, R> NetworkDecoder<B, R> composite(
            final NetworkDecoder<? super B, ? extends T1> d1,
            final NetworkDecoder<? super B, ? extends T2> d2,
            final NetworkDecoder<? super B, ? extends T3> d3,
            final NetworkDecoder<? super B, ? extends T4> d4,
            final NetworkDecoder<? super B, ? extends T5> d5,
            final NetworkDecoder<? super B, ? extends T6> d6,
            final NetworkDecoder<? super B, ? extends T7> d7,
            final NetworkDecoder<? super B, ? extends T8> d8,
            final NetworkDecoder<? super B, ? extends T9> d9,
            final NetworkDecoder<? super B, ? extends T10> d10,
            final NetworkDecoder<? super B, ? extends T11> d11,
            final NetworkDecoder<? super B, ? extends T12> d12,
            final NetworkDecoder<? super B, ? extends T13> d13,
            final NetworkDecoder<? super B, ? extends T14> d14,
            final NetworkDecoder<? super B, ? extends T15> d15,
            final NetworkDecoder<? super B, ? extends T16> d16,
            final Function16<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, ? super T14, ? super T15, ? super T16, ? extends R> constructor) {
        return buf -> {
            final T1 v1 = d1.read(buf);
            final T2 v2 = d2.read(buf);
            final T3 v3 = d3.read(buf);
            final T4 v4 = d4.read(buf);
            final T5 v5 = d5.read(buf);
            final T6 v6 = d6.read(buf);
            final T7 v7 = d7.read(buf);
            final T8 v8 = d8.read(buf);
            final T9 v9 = d9.read(buf);
            final T10 v10 = d10.read(buf);
            final T11 v11 = d11.read(buf);
            final T12 v12 = d12.read(buf);
            final T13 v13 = d13.read(buf);
            final T14 v14 = d14.read(buf);
            final T15 v15 = d15.read(buf);
            final T16 v16 = d16.read(buf);
            return constructor.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
        };
    }
}
