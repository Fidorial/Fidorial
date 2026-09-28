package fr.euphyllia.fidorial.server.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A builder for record codecs, with no field limit.
 * <p>
 * @implNote This builder requires one line per component, in the component order
 */
public final class RecordCodec {

    private static final Object UNSET = new Object();

    private RecordCodec() {
        throw new UnsupportedOperationException("RecordCodec cannot be instantiated.");
    }

    public static <R extends Record> Builder<R> builder(final Class<R> type) {
        return new Builder<>(Objects.requireNonNull(type, "type"));
    }

    /**
     * How keys are derived from component names. Only the part after the last {@code /} of a key is compared, so
     * {@code visual/fog_color} matches {@code fogColor}.
     */
    public enum KeyStyle {
        EXACT,
        SNAKE_CASE,
        KEBAB_CASE,
        UNCHECKED;

        @Nullable String keyFor(final String componentName) {
            return switch (this) {
                case EXACT -> componentName;
                case SNAKE_CASE -> separated(componentName, "_");
                case KEBAB_CASE -> separated(componentName, "-");
                case UNCHECKED -> null;
            };
        }

        private static String separated(final String componentName, final String separator) {
            return componentName.replaceAll("([a-z0-9])([A-Z])", "$1" + separator + "$2").toLowerCase(Locale.ROOT);
        }
    }

    public static final class Builder<R extends Record> {

        private final Class<R> type;
        private final List<Field<R>> fields = new ArrayList<>();
        private KeyStyle keyStyle = KeyStyle.SNAKE_CASE;
        private @Nullable R defaults;
        private boolean writeDefaults;
        private @Nullable Function<R, DataResult<R>> validator;

        private Builder(final Class<R> type) {
            this.type = type;
        }

        /**
         * Defaults to {@link KeyStyle#SNAKE_CASE}.
         */
        public Builder<R> keys(final KeyStyle keyStyle) {
            this.keyStyle = Objects.requireNonNull(keyStyle, "keyStyle");
            return this;
        }

        /**
         * The instance {@link #field(String, Function, Codec)} takes its default values from.
         */
        public Builder<R> defaults(final R defaults) {
            beforeFields("defaults");
            this.defaults = Objects.requireNonNull(defaults, "defaults");
            return this;
        }

        /**
         * Writes values even when they are equal to their default.
         */
        public Builder<R> writeDefaults() {
            beforeFields("writeDefaults");
            this.writeDefaults = true;
            return this;
        }

        public <A> Builder<R> required(final String key, final Function<R, A> getter, final Codec<A> codec) {
            return add(new Keyed<>(key, codec, true,
                    record -> Optional.of(getter.apply(record)),
                    Optional::orElseThrow));
        }

        public <A> Builder<R> field(final String key, final Function<R, A> getter, final Codec<A> codec) {
            final R source = defaults;
            if (source == null) {
                throw new IllegalStateException(type.getName() + "." + key + " has no default; set defaults(...) or pass one");
            }
            final A fallback = Objects.requireNonNull(getter.apply(source),
                    () -> type.getName() + "." + key + " has a null default; use nullable() or optional()");
            return field(key, getter, codec, fallback);
        }

        /**
         * Like {@code optionalFieldOf(key, fallback)}: left out while equal to {@code fallback}, unless
         * {@link #writeDefaults()} is set.
         */
        public <A> Builder<R> field(final String key, final Function<R, A> getter, final Codec<A> codec, final A fallback) {
            return withFallback(key, codec, getter, fallback, value -> value);
        }

        public <A> Builder<R> optional(final String key, final Function<R, Optional<A>> getter, final Codec<A> codec) {
            return add(new Keyed<>(key, codec, false, getter, decoded -> decoded));
        }

        public <A> Builder<R> nullable(final String key, final Function<R, @Nullable A> getter, final Codec<A> codec) {
            return add(new Keyed<>(key, codec, false,
                    record -> Optional.ofNullable(getter.apply(record)),
                    decoded -> decoded.orElse(null)));
        }

        /**
         * A nullable component whose codec stores absence itself, such as an empty string.
         */
        public <A> Builder<R> nullableInline(final String key, final Function<R, @Nullable A> getter, final Codec<Optional<A>> codec) {
            final R source = defaults;
            final Optional<A> fallback = source == null ? Optional.empty() : Optional.ofNullable(getter.apply(source));
            return withFallback(key, codec, record -> Optional.ofNullable(getter.apply(record)), fallback,
                    value -> value.orElse(null));
        }

        /**
         * A component stored directly in this map by another map codec.
         */
        public <A> Builder<R> inline(final Function<R, A> getter, final MapCodec<A> codec) {
            return add(new Inline<>(getter, codec));
        }

        /**
         * A component that isn't stored, and is set to {@code value} when decoding.
         */
        public Builder<R> given(final Object value) {
            return add(new Given<>(Objects.requireNonNull(value, "value")));
        }

        public Builder<R> validate(final Function<R, DataResult<R>> validator) {
            this.validator = Objects.requireNonNull(validator, "validator");
            return this;
        }

        public Codec<R> build() {
            return buildMap().codec();
        }

        public MapCodec<R> buildMap() {
            RecordShapes.checkKeys(type,
                    fields.stream().<@Nullable String>map(field -> field instanceof final Keyed<R, ?> keyed ? keyed.key() : null).toList(),
                    keyStyle);
            return new RecordMapCodec<>(type.getSimpleName(), List.copyOf(fields), RecordShapes.canonicalConstructor(type), validator);
        }

        private <V> Builder<R> withFallback(final String key, final Codec<V> codec, final Function<R, V> read,
                                            final V fallback, final Function<V, @Nullable Object> toArgument) {
            final boolean alwaysWritten = writeDefaults;
            return add(new Keyed<>(key, codec, false,
                    record -> {
                        final V value = read.apply(record);
                        return alwaysWritten || !value.equals(fallback) ? Optional.of(value) : Optional.empty();
                    },
                    decoded -> toArgument.apply(decoded.orElse(fallback))));
        }

        private Builder<R> add(final Field<R> field) {
            fields.add(field);
            return this;
        }

        private void beforeFields(final String option) {
            if (!fields.isEmpty()) {
                throw new IllegalStateException(option + "(...) must be called before the fields of " + type.getName());
            }
        }

        private MethodHandle canonicalConstructor(final RecordComponent[] components) {
            final Class<?>[] parameters = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
            try {
                final Constructor<R> constructor = type.getDeclaredConstructor(parameters);
                constructor.trySetAccessible();
                return MethodHandles.lookup().unreflectConstructor(constructor);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot access the canonical constructor of " + type.getName(), e);
            }
        }

        public Builder<R> validate(final Predicate<R> test, final Function<R, String> message) {
            return validate(CommonCodecs.check(test, message));
        }
    }

    private sealed interface Field<R> {
    }

    /**
     * @param value    what to write for a record, or empty to leave the key out
     * @param argument the constructor argument for a decoded value, or for a missing key when empty
     */
    private record Keyed<R, V>(
            String key,
            Codec<V> codec,
            boolean required,
            Function<R, Optional<V>> value,
            Function<Optional<V>, @Nullable Object> argument
    ) implements Field<R> {
    }

    private record Inline<R, V>(Function<R, V> getter, MapCodec<V> codec) implements Field<R> {
    }

    private record Given<R>(Object value) implements Field<R> {
    }

    private static final class RecordMapCodec<R> extends MapCodec<R> {

        private final String typeName;
        private final List<Field<R>> fields;
        private final MethodHandle constructor;
        private final @Nullable Function<R, DataResult<R>> validator;

        RecordMapCodec(final String typeName, final List<Field<R>> fields, final MethodHandle constructor,
                       final @Nullable Function<R, DataResult<R>> validator) {
            this.typeName = typeName;
            this.fields = fields;
            this.constructor = constructor;
            this.validator = validator;
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return fields.stream().flatMap(field -> switch (field) {
                case final Keyed<R, ?> keyed -> Stream.of(ops.createString(keyed.key()));
                case final Inline<R, ?> inline -> inline.codec().keys(ops);
                case final Given<R> _ -> Stream.empty();
            });
        }

        @Override
        public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            final Object[] arguments = new Object[fields.size()];
            final List<String> errors = new ArrayList<>();
            for (int i = 0; i < fields.size(); i++) {
                final String error = switch (fields.get(i)) {
                    case final Keyed<R, ?> keyed -> decodeKeyed(keyed, ops, input, arguments, i);
                    case final Inline<R, ?> inline -> decodeInline(inline, ops, input, arguments, i);
                    case final Given<R> given -> {
                        arguments[i] = given.value();
                        yield null;
                    }
                };
                if (error != null) {
                    errors.add(error);
                }
            }
            if (errors.isEmpty()) {
                final Function<R, DataResult<R>> check = validator;
                return construct(arguments).flatMap(value -> check == null ? DataResult.success(value) : check.apply(value));
            }
            final Supplier<String> message = () -> String.join("\n", errors);
            if (Arrays.asList(arguments).contains(UNSET)) {
                return DataResult.error(message);
            }
            return construct(arguments).result()
                    .map(partial -> DataResult.error(message, partial))
                    .orElseGet(() -> DataResult.error(message));
        }

        @Override
        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
            RecordBuilder<T> builder = prefix;
            for (final Field<R> field : fields) {
                builder = switch (field) {
                    case final Keyed<R, ?> keyed -> encodeKeyed(keyed, input, ops, builder);
                    case final Inline<R, ?> inline -> encodeInline(inline, input, ops, builder);
                    case final Given<R> _ -> builder;
                };
            }
            return builder;
        }

        @SuppressWarnings("unchecked")
        private DataResult<R> construct(final Object[] arguments) {
            try {
                return DataResult.success((R) constructor.invokeWithArguments(arguments));
            } catch (final Throwable t) {
                return DataResult.error(() -> "Could not create " + typeName + ": " + t);
            }
        }

        private static <R, V, T> @Nullable String decodeKeyed(final Keyed<R, V> field, final DynamicOps<T> ops, final MapLike<T> input, final @Nullable Object[] arguments, final int index) {
            final T raw = input.get(field.key());
            if (raw == null) {
                if (field.required()) {
                    arguments[index] = UNSET;
                    return field.key() + ": missing";
                }
                arguments[index] = field.argument().apply(Optional.empty());
                return null;
            }
            return switch (field.codec().parse(ops, raw)) {
                case final DataResult.Success<V> success -> {
                    arguments[index] = field.argument().apply(Optional.of(success.value()));
                    yield null;
                }
                case final DataResult.Error<V> error -> {
                    arguments[index] = error.partialValue()
                            .map(partial -> field.argument().apply(Optional.of(partial)))
                            .orElse(UNSET);
                    yield prefixed(field.key(), error.message());
                }
            };
        }

        private static <R, V, T> @Nullable String decodeInline(final Inline<R, V> field, final DynamicOps<T> ops, final MapLike<T> input, final Object[] arguments, final int index) {
            return switch (field.codec().decode(ops, input)) {
                case final DataResult.Success<V> success -> {
                    arguments[index] = success.value();
                    yield null;
                }
                case final DataResult.Error<V> error -> {
                    arguments[index] = error.partialValue().map(Object.class::cast).orElse(UNSET);
                    yield error.message();
                }
            };
        }

        private static <R, V, T> RecordBuilder<T> encodeKeyed(final Keyed<R, V> field, final R input, final DynamicOps<T> ops, final RecordBuilder<T> builder) {
            return field.value().apply(input)
                    .map(value -> builder.add(field.key(), field.codec().encodeStart(ops, value)))
                    .orElse(builder);
        }

        private static <R, V, T> RecordBuilder<T> encodeInline(final Inline<R, V> field, final R input, final DynamicOps<T> ops, final RecordBuilder<T> builder) {
            return field.codec().encode(field.getter().apply(input), ops, builder);
        }

        private static final Pattern PATH_LINE = Pattern.compile("[A-Za-z0-9_.:/-]+: ");

        private static String prefixed(final String key, final String message) {
            return message.lines()
                    .map(line -> PATH_LINE.matcher(line).lookingAt() ? key + "." + line : key + ": " + line)
                    .collect(Collectors.joining("\n"));
        }

        @Override
        public String toString() {
            return "RecordCodec[" + typeName + "]";
        }
    }
}
