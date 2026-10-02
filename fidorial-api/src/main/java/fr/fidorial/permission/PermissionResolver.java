package fr.fidorial.permission;

import net.kyori.adventure.util.TriState;

/**
 * An external source of permission decisions, such as a permission plugin, consulted before the defaults.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface PermissionResolver {

    /**
     * Resolves a node for a holder.
     *
     * @param holder the holder
     * @param node   the node
     * @return the decided state, or {@link TriState#NOT_SET} to defer to the next resolver
     */
    TriState resolve(PermissionHolder holder, PermissionNode node);

    /**
     * Ordering weight; higher weights are consulted first. Defaults to {@code 0}.
     *
     * @return the weight
     */
    default int weight() {
        return 0;
    }

    /**
     * Whether results from this resolver may be cached by the holder until the next invalidation.
     * Return {@code false} for back-ends whose answers can change without going through
     * {@link PermissionHolder#invalidatePermissions()}.
     *
     * @return {@code true} if results are cacheable
     */
    default boolean cacheable() {
        return true;
    }
}
