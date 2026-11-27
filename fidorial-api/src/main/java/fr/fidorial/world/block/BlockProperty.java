package fr.fidorial.world.block;

import com.google.common.base.Preconditions;
import net.kyori.adventure.util.Index;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/// A property of a block type and the values its states take, in state order.
///
/// @param <T> the type of the values
/// @since 0.1.0
public sealed interface BlockProperty<T> permits BlockProperty.Flag, BlockProperty.Range, BlockProperty.Choice {

    /// The pattern property names and value names must match.
    ///
    /// @since 0.1.0
    Pattern NAME = Pattern.compile("[a-z0-9_]+");

    /// {@return the property name, for instance `facing`}
    ///
    /// @since 0.1.0
    String name();

    /// {@return the accepted values, at least two, in state order}
    ///
    /// @since 0.1.0
    List<T> values();

    /// {@return the name a value is written as}
    ///
    /// @param value an accepted value
    /// @throws IllegalArgumentException if the value is not accepted
    /// @since 0.1.0
    String nameOf(T value);

    /// {@return the value written as a name, `null` if none is}
    ///
    /// @param name the written value
    /// @since 0.1.0
    @Nullable T parse(String name);

    /// {@return the position of a value in [#values()], `-1` if it is not accepted}
    ///
    /// @param value the value
    /// @since 0.1.0
    default int indexOf(final T value) {
        return values().indexOf(value);
    }

    /// A property that is either `true` or `false`.
    ///
    /// @param name the property name
    /// @since 0.1.0
    record Flag(String name) implements BlockProperty<Boolean> {

        private static final List<Boolean> VALUES = List.of(true, false);

        public Flag {
            checkName(name);
        }

        @Override
        public List<Boolean> values() {
            return VALUES;
        }

        @Override
        public String nameOf(final Boolean value) {
            return value.toString();
        }

        @Override
        public @Nullable Boolean parse(final String name) {
            return switch (name) {
                case "true" -> true;
                case "false" -> false;
                default -> null;
            };
        }
    }

    /// A property taking every integer from `min` to `max`.
    ///
    /// @param name the property name
    /// @param min  the lowest value, `0` or more
    /// @param max  the highest value, above `min`
    /// @since 0.1.0
    record Range(String name, int min, int max) implements BlockProperty<Integer> {

        public Range {
            checkName(name);
            Preconditions.checkArgument(min >= 0, "The property '%s' must not go below 0, got %s", name, min);
            Preconditions.checkArgument(max > min, "The property '%s' must end above %s, got %s", name, min, max);
        }

        @Override
        public List<Integer> values() {
            return IntStream.rangeClosed(min, max).boxed().toList();
        }

        @Override
        public int indexOf(final Integer value) {
            return value >= min && value <= max ? value - min : -1;
        }

        @Override
        public String nameOf(final Integer value) {
            Preconditions.checkArgument(indexOf(value) >= 0, "%s is not a value of the property '%s'", value, name);
            return value.toString();
        }

        @Override
        public @Nullable Integer parse(final String name) {
            if (name.isEmpty() || name.length() > 10 || !name.chars().allMatch(c -> c >= '0' && c <= '9')) {
                return null;
            }
            final long value = Long.parseLong(name);
            return value >= min && value <= max ? (int) value : null;
        }
    }

    /// A property taking some constants of an enum, each written as its lowercased name.
    ///
    /// @param name   the property name
    /// @param type   the enum
    /// @param values the accepted constants, in state order
    /// @param <E>    the enum
    /// @since 0.1.0
    record Choice<E extends Enum<E>>(String name, Class<E> type, List<E> values) implements BlockProperty<E> {

        private static final ClassValue<Index<String, Object>> NAMES = new ClassValue<>() {
            @Override
            protected Index<String, Object> computeValue(final Class<?> type) {
                final Object[] constants = type.getEnumConstants();
                Preconditions.checkState(constants != null, "%s is not an enum", type.getName());
                return Index.create(constant -> ((Enum<?>) constant).name().toLowerCase(Locale.ROOT), constants);
            }
        };

        public Choice {
            checkName(name);
            Preconditions.checkArgument(type != null, "The enum of the property '%s' must not be null", name);
            values = List.copyOf(values);
            Preconditions.checkArgument(values.size() > 1, "The property '%s' needs at least two values", name);
            Preconditions.checkArgument(new HashSet<>(values).size() == values.size(),
                    "The property '%s' lists a value twice", name);
            for (final E value : values) {
                Preconditions.checkArgument(NAME.matcher(value.name().toLowerCase(Locale.ROOT)).matches(),
                        "The value %s of the property '%s' may only hold a-z, 0-9 and _ once lowercased", value, name);
            }
        }

        /// {@return a property accepting every constant of an enum}
        ///
        /// @param name the property name
        /// @param type the enum
        /// @param <E>  the enum
        /// @since 0.1.0
        public static <E extends Enum<E>> Choice<E> of(final String name, final Class<E> type) {
            return new Choice<>(name, type, List.copyOf(EnumSet.allOf(type)));
        }

        /// {@return a property accepting some constants of an enum}
        ///
        /// @param name   the property name
        /// @param type   the enum
        /// @param values the accepted constants, in state order
        /// @param <E>    the enum
        /// @since 0.1.0
        @SafeVarargs
        public static <E extends Enum<E>> Choice<E> of(final String name, final Class<E> type, final E... values) {
            return new Choice<>(name, type, List.of(values));
        }

        @Override
        public String nameOf(final E value) {
            Preconditions.checkArgument(values.contains(value), "%s is not a value of the property '%s'", value, name);
            return value.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public @Nullable E parse(final String name) {
            final Object constant = NAMES.get(type).value(name);
            return constant != null && values.contains(constant) ? type.cast(constant) : null;
        }
    }

    private static void checkName(final String name) {
        Preconditions.checkArgument(name != null, "The name of a block property must not be null");
        Preconditions.checkArgument(NAME.matcher(name).matches(),
                "The block property name '%s' may only hold a-z, 0-9 and _", name);
    }
}
