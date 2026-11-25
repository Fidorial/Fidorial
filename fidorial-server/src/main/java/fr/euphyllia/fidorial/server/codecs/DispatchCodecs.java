package fr.euphyllia.fidorial.server.codecs;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class DispatchCodecs {
    private DispatchCodecs() {
    }

    public record Variant<T>(
            String id,
            @Nullable String key,
            boolean explicit,
            Predicate<? super T> test,
            MapCodec<T> codec
    ) {
        @SuppressWarnings("unchecked")
        public static <T, C extends T> Variant<T> of(final String id, final @Nullable String key, final boolean explicit, final Class<C> type, final MapCodec<C> codec) {
            return new Variant<>(id, key, explicit, type::isInstance, (MapCodec<T>) codec);
        }

        @SuppressWarnings("unchecked")
        public static <T, C extends T> Variant<T> of(final String id, final @Nullable String key, final boolean explicit, final Predicate<? super T> test, final MapCodec<C> codec) {
            return new Variant<>(id, key, explicit, test, (MapCodec<T>) codec);
        }
    }

    public static <T> MapCodec<T> matcher(final String typeField, final List<Variant<T>> variants) {
        return new MapCodec<>() {
            @Override
            public <O> Stream<O> keys(final DynamicOps<O> ops) {
                return Stream.concat(
                        Stream.of(ops.createString(typeField)),
                        variants.stream().flatMap(v -> v.codec().keys(ops)));
            }

            @Override
            public <O> DataResult<T> decode(final DynamicOps<O> ops, final MapLike<O> input) {
                final O type = input.get(typeField);
                if (type != null) {
                    final Optional<String> id = ops.getStringValue(type).result();
                    if (id.isPresent()) {
                        final Key key = Key.parseable(id.get()) ? Key.key(id.get()) : null;
                        final String name = key != null && key.namespace().equals(Key.MINECRAFT_NAMESPACE) ? key.value() : id.get();
                        for (final Variant<T> v : variants) {
                            if (v.id().equals(id.get()) || v.id().equals(name)) {
                                return v.codec().decode(ops, input);
                            }
                        }
                        return DataResult.error(() -> "Unknown '" + typeField + "': " + id.get());
                    }
                }
                for (final Variant<T> v : variants) {
                    if (v.key() != null && input.get(v.key()) != null) {
                        return v.codec().decode(ops, input);
                    }
                }
                return DataResult.error(() -> "Could not infer type from fields");
            }

            @Override
            public <O> RecordBuilder<O> encode(final T input, final DynamicOps<O> ops, final RecordBuilder<O> prefix) {
                for (final Variant<T> v : variants) {
                    if (v.test().test(input)) {
                        final RecordBuilder<O> builder = v.codec().encode(input, ops, prefix);
                        return v.explicit()
                                ? builder.add(typeField, ops.createString(v.id()))
                                : builder;
                    }
                }
                return prefix.withErrorsFrom(DataResult.error(() -> "Unsupported value: " + input));
            }
        };
    }
}
