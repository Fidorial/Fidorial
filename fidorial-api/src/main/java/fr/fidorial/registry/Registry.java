package fr.fidorial.registry;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The values of a registry, addressed by {@link TypedKey}, in network order.
 *
 * <p>Every registry holds the vanilla entries; {@linkplain WritableRegistry writable} ones may also
 * hold entries added or replaced by plugins.</p>
 *
 * @param <T> the type of the values
 * @see GameRegistries#registry(RegistryKey)
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
     * @throws IllegalArgumentException if the value is not registered
     * @since 0.1.0
     */
    TypedKey<T> key(T value);

    /**
     * {@return whether something is registered under {@code key}}
     *
     * @param key the key
     * @since 0.1.0
     */
    boolean contains(TypedKey<T> key);

    /**
     * {@return whether {@code key} was added or replaced by a plugin rather than coming from vanilla}
     *
     * @param key the key
     * @since 0.1.0
     */
    boolean isCustom(TypedKey<T> key);

    /**
     * {@return every registered key, in network order}
     *
     * @since 0.1.0
     */
    Collection<TypedKey<T>> keys();

    /**
     * {@return every registered value, in network order}
     *
     * @since 0.1.0
     */
    Collection<T> values();

    /**
     * {@return a stream over every registered value, in network order}
     *
     * @since 0.1.0
     */
    Stream<T> stream();

    /**
     * {@return the number of registered values}
     *
     * @since 0.1.0
     */
    int size();

    /**
     * {@return every tag of this registry}
     *
     * @since 0.1.0
     */
    Collection<TagKey<T>> tags();

    /**
     * {@return the members of {@code tag}, in order, or an empty collection if the tag does not exist}
     *
     * @param tag the tag
     * @since 0.1.0
     */
    Collection<TypedKey<T>> tag(TagKey<T> tag);

    /**
     * {@return whether {@code key} belongs to {@code tag}}
     *
     * @param key the key
     * @param tag the tag
     * @since 0.1.0
     */
    boolean isTagged(TypedKey<T> key, TagKey<T> tag);
}
