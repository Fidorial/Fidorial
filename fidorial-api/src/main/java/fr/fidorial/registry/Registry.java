package fr.fidorial.registry;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A read-only collection of values addressed by {@link TypedKey}, such as the entity types.
 *
 * @since 0.1.0
 */
public interface Registry<T> {

    /**
     * {@return the key identifying this registry}
     *
     * @since 0.1.0
     */
    RegistryKey<T> registryKey();

    /**
     * Gets the value registered under a key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if nothing is registered under that key
     * @since 0.1.0
     */
    T get(TypedKey<T> key);

    /**
     * {@return the value registered under a key, or empty if there is none}
     *
     * @param key the key
     * @since 0.1.0
     */
    Optional<T> find(TypedKey<T> key);

    /**
     * Gets the key a value is registered under.
     *
     * @param value the value
     * @return its key
     * @throws UnsupportedOperationException if this registry cannot map values back to keys
     * @since 0.1.0
     */
    TypedKey<T> key(T value);

    /**
     * {@return every registered value, in registration order}
     *
     * @since 0.1.0
     */
    Collection<T> values();

    /**
     * {@return a stream over every registered value, in registration order}
     *
     * @since 0.1.0
     */
    Stream<T> stream();
}
