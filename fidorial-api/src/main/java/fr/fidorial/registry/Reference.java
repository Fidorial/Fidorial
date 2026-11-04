package fr.fidorial.registry;

import com.google.common.base.Preconditions;

import java.util.NoSuchElementException;

/**
 * A value of a registry, given either by the key it is registered under or inline.
 * <p>
 * A {@link TypedKey} is a reference, so the generated keys can be passed as they are.
 *
 * @param <T> the type of the value
 * @since 0.1.0
 */
public sealed interface Reference<T> permits TypedKey, Reference.Inline {

    /**
     * {@return a reference to a value that is not registered}
     *
     * @param value the value
     * @param <T>   the type of the value
     * @since 0.1.0
     */
    static <T> Reference<T> inline(final T value) {
        return new Inline<>(value);
    }

    /**
     * Gets the value, looking it up in {@code registry} if this is a key.
     *
     * @param registry the registry the value belongs to
     * @return the value
     * @throws NoSuchElementException if nothing is registered under the key
     * @since 0.1.0
     */
    T resolve(Registry<T> registry);

    /**
     * A value given inline, not registered.
     *
     * @param value the value
     * @param <T>   the type of the value
     * @since 0.1.0
     */
    record Inline<T>(T value) implements Reference<T> {

        public Inline {
            Preconditions.checkArgument(value != null, "The value of an inline reference must not be null");
        }

        @Override
        public T resolve(final Registry<T> registry) {
            return value;
        }
    }
}
