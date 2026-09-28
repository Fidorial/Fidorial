package fr.euphyllia.fidorial.server.codecs.networking;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.euphyllia.fidorial.server.codecs.RecordCodec.KeyStyle;
import fr.euphyllia.fidorial.server.codecs.RecordShapes;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A builder for record {@link NetworkCodec}s, with no field limit. Fields may also carry a DFU {@link Codec}, in which
 * case the same declaration yields a {@link RecordCodec} through {@link Builder#buildCodec()}.
 *
 * @implNote One line per component, in the component order, which is also the wire order
 */
public final class NetworkRecordCodec {

    private NetworkRecordCodec() {
        throw new UnsupportedOperationException("NetworkRecordCodec cannot be instantiated.");
    }

    /**
     * @param bufferType only used to fix {@code B} for the method references that follow
     */
    public static <B, R extends Record> Builder<B, R> builder(final Class<R> type, final Class<B> bufferType) {
        Objects.requireNonNull(bufferType, "bufferType");
        return new Builder<>(Objects.requireNonNull(type, "type"));
    }

    public static final class Builder<B, R extends Record> {

        private final Class<R> type;
        private final RecordCodec.Builder<R> codec;
        private final List<Field<B, R>> fields = new ArrayList<>();
        private KeyStyle keyStyle = KeyStyle.SNAKE_CASE;
        private boolean codecComplete = true;
        private @Nullable Predicate<R> test;
        private @Nullable Function<R, String> message;

        private Builder(final Class<R> type) {
            this.type = type;
            this.codec = RecordCodec.builder(type);
        }

        /**
         * Defaults to {@link KeyStyle#SNAKE_CASE}, like {@link RecordCodec}.
         */
        public Builder<B, R> keys(final KeyStyle keyStyle) {
            this.keyStyle = Objects.requireNonNull(keyStyle, "keyStyle");
            codec.keys(keyStyle);
            return this;
        }

        public <A> Builder<B, R> required(final String key, final Function<R, A> getter, final Function<B, A> reader, final BiConsumer<B, A> writer) {
            return required(key, getter, NetworkCodec.of(reader, writer));
        }

        public <A> Builder<B, R> required(final String key, final Function<R, A> getter, final NetworkCodec<B, A> network) {
            codecComplete = false;
            return add(new Networked<>(key, getter, network, value -> value));
        }

        public <A> Builder<B, R> optional(final String key, final Function<R, Optional<A>> getter, final NetworkCodec<B, Optional<A>> network) {
            codecComplete = false;
            return add(new Networked<>(key, getter, network, value -> value));
        }

        public <A> Builder<B, R> nullable(final String key, final Function<R, @Nullable A> getter, final NetworkCodec<B, Optional<A>> network) {
            codecComplete = false;
            return add(nullableField(key, getter, network));
        }

        public <A> Builder<B, R> required(final String key, final Function<R, A> getter, final Codec<A> codec, final Function<B, A> reader, final BiConsumer<B, A> writer) {
            return required(key, getter, codec, NetworkCodec.of(reader, writer));
        }

        public <A> Builder<B, R> required(final String key, final Function<R, A> getter, final Codec<A> codec, final NetworkCodec<B, A> network) {
            this.codec.required(key, getter, codec);
            return add(new Networked<>(key, getter, network, value -> value));
        }

        public <A> Builder<B, R> optional(final String key, final Function<R, Optional<A>> getter, final Codec<A> codec, final NetworkCodec<B, Optional<A>> network) {
            this.codec.optional(key, getter, codec);
            return add(new Networked<>(key, getter, network, value -> value));
        }

        public <A> Builder<B, R> nullable(final String key, final Function<R, @Nullable A> getter, final Codec<A> codec, final NetworkCodec<B, Optional<A>> network) {
            this.codec.nullable(key, getter, codec);
            return add(nullableField(key, getter, network));
        }

        /**
         * A component that is neither stored nor sent, and is set to {@code value} when decoding.
         */
        public Builder<B, R> given(final Object value) {
            codec.given(value);
            return add(new Given<>(Objects.requireNonNull(value, "value")));
        }

        /**
         * Checked after reading from the network, and after DFU decoding when {@link #buildCodec()} is used.
         */
        public Builder<B, R> validate(final Predicate<R> test, final Function<R, String> message) {
            codec.validate(test, message);
            this.test = Objects.requireNonNull(test, "test");
            this.message = Objects.requireNonNull(message, "message");
            return this;
        }

        public NetworkCodec<B, R> build() {
            RecordShapes.checkKeys(type,
                    fields.stream().<@Nullable String>map(field -> field instanceof final Networked<B, R, ?> networked ? networked.key() : null).toList(),
                    keyStyle);
            return new Impl<>(type.getSimpleName(), List.copyOf(fields), RecordShapes.canonicalConstructor(type), test, message);
        }

        /**
         * The DFU codec for the same shape; only available when every field was declared with a {@link Codec}.
         */
        public Codec<R> buildCodec() {
            if (!codecComplete) {
                throw new IllegalStateException(type.getName() + " declares network-only fields; it has no DFU codec");
            }
            return codec.build();
        }

        private static <B, R, A> Networked<B, R, Optional<A>> nullableField(final String key, final Function<R, @Nullable A> getter,
                                                                            final NetworkCodec<B, Optional<A>> network) {
            return new Networked<>(key, record -> Optional.ofNullable(getter.apply(record)), network,
                    value -> value.orElse(null));
        }

        private Builder<B, R> add(final Field<B, R> field) {
            fields.add(Objects.requireNonNull(field, "field"));
            return this;
        }
    }

    private sealed interface Field<B, R> {
    }

    /**
     * @param value    what to write for a record
     * @param argument the constructor argument for a read value
     */
    private record Networked<B, R, V>(
            String key,
            Function<R, V> value,
            NetworkCodec<B, V> codec,
            Function<V, @Nullable Object> argument
    ) implements Field<B, R> {
    }

    private record Given<B, R>(Object value) implements Field<B, R> {
    }

    private record Impl<B, R>(
            String typeName,
            List<Field<B, R>> fields,
            MethodHandle constructor,
            @Nullable Predicate<R> test,
            @Nullable Function<R, String> message
    ) implements NetworkCodec<B, R> {

        @Override
        public R read(final B buf) {
            final @Nullable Object[] arguments = new Object[fields.size()];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = switch (fields.get(i)) {
                    case final Networked<B, R, ?> networked -> readField(networked, buf);
                    case final Given<B, R> given -> given.value();
                };
            }
            final R value = construct(arguments);
            if (test != null && !test.test(value)) {
                throw new DecoderException(typeName + ": " + Objects.requireNonNull(message).apply(value));
            }
            return value;
        }

        @Override
        public void write(final B buf, final R value) {
            for (final Field<B, R> field : fields) {
                switch (field) {
                    case final Networked<B, R, ?> networked -> writeField(networked, buf, value);
                    case final Given<B, R> _ -> {
                    }
                }
            }
        }

        private <V> @Nullable Object readField(final Networked<B, R, V> field, final B buf) {
            try {
                return field.argument().apply(field.codec().read(buf));
            } catch (final RuntimeException e) {
                throw new DecoderException(typeName + "." + field.key() + ": " + e.getMessage(), e);
            }
        }

        private <V> void writeField(final Networked<B, R, V> field, final B buf, final R record) {
            try {
                field.codec().write(buf, field.value().apply(record));
            } catch (final RuntimeException e) {
                throw new EncoderException(typeName + "." + field.key() + ": " + e.getMessage(), e);
            }
        }

        @SuppressWarnings("unchecked")
        private R construct(final @Nullable Object[] arguments) {
            try {
                return (R) constructor.invokeWithArguments(arguments);
            } catch (final Error e) {
                throw e;
            } catch (final Throwable t) {
                throw new DecoderException("Could not create " + typeName + ": " + t.getMessage(), t);
            }
        }
    }
}
