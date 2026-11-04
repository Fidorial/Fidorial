package fr.fidorial.item.enchantment;

import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.Set;

/**
 * A set of registry entries, either a tag or an explicit list of keys, as data packs declare it.
 *
 * <p>Resolving a tag requires the server registries: use {@link Enchantments#isSupportedItem}
 * or {@link Enchantments#areCompatible} to test membership.</p>
 *
 * @param <T> the type of the entries
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface KeySet<T> {

    /**
     * {@return the tag this set refers to, such as {@code minecraft:enchantable/weapon}, or empty for an
     * explicit list}
     *
     * @since 0.1.0
     */
    Optional<TagKey<T>> tag();

    /**
     * {@return the explicitly listed keys, empty for a tag}
     *
     * @since 0.1.0
     */
    Set<TypedKey<T>> keys();

    /**
     * {@return {@code true} if this set refers to a tag}
     *
     * @since 0.1.0
     */
    default boolean isTag() {
        return tag().isPresent();
    }

    /**
     * {@return {@code true} if this set neither refers to a tag nor lists any key}
     *
     * @since 0.1.0
     */
    default boolean isEmpty() {
        return tag().isEmpty() && keys().isEmpty();
    }
}
