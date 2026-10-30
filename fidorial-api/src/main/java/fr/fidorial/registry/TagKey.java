package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

/**
 * A tag bound to the registry its members belong to.
 *
 * @param registry the registry of the tag
 * @param key      the key of the tag, without the leading {@code #}
 * @param <T>      the type of the members
 * @since 0.1.0
 */
public record TagKey<T>(RegistryKey<T> registry, Key key) {

    public TagKey {
        Preconditions.checkArgument(registry != null, "The registry of a tag key must not be null");
        Preconditions.checkArgument(key != null, "The key of a tag key must not be null");
    }

    /**
     * {@return a tag of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the tag
     * @param <T>      the type of the members
     * @since 0.1.0
     */
    public static <T> TagKey<T> create(final RegistryKey<T> registry, final Key key) {
        return new TagKey<>(registry, key);
    }

    /**
     * {@return a tag of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the tag, for instance {@code minecraft:logs}
     * @param <T>      the type of the members
     * @since 0.1.0
     */
    public static <T> TagKey<T> create(final RegistryKey<T> registry, @KeyPattern final String key) {
        return new TagKey<>(registry, Key.key(key));
    }

    @Override
    public String toString() {
        return "TagKey[" + registry.key() + " / #" + key + "]";
    }
}
