package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;


/**
 * A key bound to the registry it belongs to, so lookups are type-safe.
 *
 * @param registry the registry the key belongs to
 * @param key      the key of the entry
 * @param <T>      the type of the entry
 * @since 0.1.0
 */
public record TypedKey<T>(RegistryKey<T> registry, Key key) {

    /**
     * Validates the components.
     */
    public TypedKey {
        Preconditions.checkArgument(registry != null, "The registry of a typed key must not be null");
        Preconditions.checkArgument(key != null, "The key of a typed key must not be null");
    }

    /**
     * {@return a key of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the entry
     * @param <T>      the type of the entry
     * @since 0.1.0
     */
    public static <T> TypedKey<T> create(final RegistryKey<T> registry, final Key key) {
        return new TypedKey<>(registry, key);
    }

    /**
     * {@return a key of the given registry}
     *
     * @param registry the registry
     * @param key      the key of the entry, for instance {@code minecraft:stone}
     * @param <T>      the type of the entry
     * @since 0.1.0
     */
    public static <T> TypedKey<T> create(final RegistryKey<T> registry, @KeyPattern final String key) {
        return new TypedKey<>(registry, Key.key(key));
    }

    @Override
    public String toString() {
        return "TypedKey[" + registry.key() + " / " + key + "]";
    }
}
