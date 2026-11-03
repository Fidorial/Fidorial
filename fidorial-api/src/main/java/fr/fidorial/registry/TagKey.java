package fr.fidorial.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

/**
 * A tag bound to the registry its members belong to.
 * <p>
 * It is a {@link Key} itself, without the leading {@code #}, and can be passed wherever one is
 * expected. Like any key, it equals every other key with the same namespace and value.
 *
 * @param <T> the type of the members
 * @since 0.1.0
 */
public sealed interface TagKey<T> extends Key permits TagKeyImpl {

    /**
     * {@return a tag of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the tag
     * @param <T>      the type of the members
     * @since 0.1.0
     */
    static <T> TagKey<T> withRegistry(final RegistryKey<T> registry, final Key key) {
        return new TagKeyImpl<>(registry, key);
    }

    /**
     * {@return a tag of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the tag, for instance {@code minecraft:logs}
     * @param <T>      the type of the members
     * @since 0.1.0
     */
    static <T> TagKey<T> withRegistry(final RegistryKey<T> registry, @KeyPattern final String key) {
        return new TagKeyImpl<>(registry, Key.key(key));
    }

    /**
     * {@return the registry of the tag}
     *
     * @since 0.1.0
     */
    RegistryKey<T> registry();

    /**
     * {@return the plain key of the tag, without the registry}
     *
     * @since 0.1.0
     */
    @Override
    Key key();
}
