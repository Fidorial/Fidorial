package fr.fidorial.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

/**
 * A key bound to the registry it belongs to, so lookups are type-safe.
 * <p>
 * It is a {@link Key} itself and can be passed wherever one is expected. Like any key, it equals
 * every other key with the same namespace and value, whatever its registry.
 *
 * @param <T> the type of the entry
 * @since 0.1.0
 */
public sealed interface TypedKey<T> extends Key, Reference<T> permits TypedKeyImpl {

    /**
     * {@return a key of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the entry
     * @param <T>      the type of the entry
     * @since 0.1.0
     */
    static <T> TypedKey<T> withRegistry(final RegistryKey<T> registry, final Key key) {
        return new TypedKeyImpl<>(registry, key);
    }

    /**
     * {@return a key of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the entry, for instance {@code minecraft:stone}
     * @param <T>      the type of the entry
     * @since 0.1.0
     */
    static <T> TypedKey<T> withRegistry(final RegistryKey<T> registry, @KeyPattern final String key) {
        return new TypedKeyImpl<>(registry, Key.key(key));
    }

    /**
     * {@return the registry the key belongs to}
     *
     * @since 0.1.0
     */
    RegistryKey<T> registry();

    /**
     * {@return the plain key, without the registry}
     *
     * @since 0.1.0
     */
    @Override
    Key key();
}
