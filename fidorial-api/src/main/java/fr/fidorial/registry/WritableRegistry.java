package fr.fidorial.registry;

import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.key.Keyed;

import java.util.Optional;

/**
 * A registry plugins can add entries to, on top of the vanilla ones.
 *
 * <p>Vanilla entries always stay registered: they can be replaced, and come back once the
 * replacement is {@linkplain #unregister(TypedKey) unregistered}. Registries sent to clients
 * should be changed while plugins load: a change once players are connected sends them back to
 * the configuration phase.</p>
 *
 * @param <T> the type of the values
 * @see GameRegistries#writable(RegistryKey)
 * @since 0.1.0
 */
public interface WritableRegistry<T> extends Registry<T> {

    /**
     * Registers a new entry.
     *
     * @param key   the key to register it under
     * @param value the value
     * @param owner the plugin registering it
     * @return {@code value}
     * @throws IllegalStateException    if something is already registered under {@code key}; use
     *                                  {@link #overwrite(TypedKey, Object, Plugin)} to replace it
     * @throws IllegalArgumentException if the value is invalid, or carries a key other than {@code key}
     * @since 0.1.0
     */
    T register(TypedKey<T> key, T value, Plugin owner);

    /**
     * Registers a new entry under the key the value carries.
     *
     * @param value the value, carrying its key
     * @param owner the plugin registering it
     * @return {@code value}
     * @throws IllegalArgumentException if the value does not carry its key, or is invalid
     * @throws IllegalStateException    if something is already registered under its key
     * @since 0.1.0
     */
    default T register(final T value, final Plugin owner) {
        if (!(value instanceof final Keyed keyed)) {
            throw new IllegalArgumentException("The values of " + registryKey() + " do not carry their key");
        }
        return register(TypedKey.create(registryKey(), keyed.key()), value, owner);
    }

    /**
     * Reads an entry from its data pack JSON representation and registers it.
     *
     * @param key   the key to register it under
     * @param json  the JSON, as found in a data pack
     * @param owner the plugin registering it
     * @return the registered value
     * @throws IllegalArgumentException if the JSON is malformed or invalid
     * @throws IllegalStateException    if something is already registered under {@code key}
     * @since 0.1.0
     */
    T registerFromJson(TypedKey<T> key, String json, Plugin owner);

    /**
     * Registers an entry, replacing any existing one under the same key, vanilla ones included.
     *
     * @param key   the key to register it under
     * @param value the value
     * @param owner the plugin registering it
     * @return the value previously registered under that key, if any
     * @throws IllegalArgumentException if the value is invalid, or carries a key other than {@code key}
     * @since 0.1.0
     */
    Optional<T> overwrite(TypedKey<T> key, T value, Plugin owner);

    /**
     * Removes an entry added by a plugin, or restores the vanilla value of a replaced one.
     *
     * @param key the key
     * @return {@code true} if an entry was removed or restored, {@code false} if nothing was
     * registered by a plugin under {@code key}
     * @since 0.1.0
     */
    boolean unregister(TypedKey<T> key);

    /**
     * Removes every entry a plugin added, and restores every vanilla value it replaced.
     *
     * @param owner the plugin
     * @since 0.1.0
     */
    void unregisterAll(Plugin owner);

    /**
     * Adds an entry to a tag, creating the tag if needed.
     *
     * @param tag the tag
     * @param key the key of the entry
     * @return {@code true} if the entry was added, {@code false} if it already belonged to the tag
     * @throws IllegalArgumentException if nothing is registered under {@code key}
     * @since 0.1.0
     */
    boolean addToTag(TagKey<T> tag, TypedKey<T> key);

    /**
     * Removes an entry from a tag.
     *
     * @param tag the tag
     * @param key the key of the entry
     * @return {@code true} if the entry was removed, {@code false} if it did not belong to the tag
     * @since 0.1.0
     */
    boolean removeFromTag(TagKey<T> tag, TypedKey<T> key);
}
