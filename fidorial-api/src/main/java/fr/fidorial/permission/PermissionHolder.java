package fr.fidorial.permission;

import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.util.TriState;

import java.util.Map;

/**
 * Something permissions are checked against: a player or the console.
 *
 * @since 0.1.0
 */
public interface PermissionHolder {

    /**
     * @return {@code true} if this holder has operator status
     */
    boolean isOperator();

    /**
     * Sets the operator status of this holder.
     *
     * @param operator new status
     */
    void setOperator(boolean operator);

    /**
     * Resolves a node against grants, declared defaults and any registered
     * {@link PermissionResolver}.
     *
     * @param node the node
     * @return the resolved state
     */
    TriState permissionState(PermissionNode node);

    /**
     * Resolves a node from its textual path.
     *
     * @param node node path
     * @return the resolved state
     */
    default TriState permissionState(final String node) {
        return permissionState(PermissionNode.of(node));
    }

    /**
     * Convenience check. Only {@link TriState#TRUE} counts as held, so an unresolved node is
     * treated as refused at the call site while still being distinguishable through
     * {@link #permissionState(PermissionNode)}.
     *
     * @param node the node
     * @return {@code true} if the node resolves to {@link TriState#TRUE}
     */
    default boolean hasPermission(final PermissionNode node) {
        return permissionState(node) == TriState.TRUE;
    }

    /**
     * Convenience check from a textual path.
     *
     * @param node node path
     * @return {@code true} if the node resolves to {@link TriState#TRUE}
     */
    default boolean hasPermission(final String node) {
        return hasPermission(PermissionNode.of(node));
    }

    /**
     * Tests whether a node has been decided by a grant or a declaration, as opposed to falling
     * through unresolved.
     *
     * @param node node path
     * @return {@code true} if the state is not {@link TriState#NOT_SET}
     */
    default boolean isPermissionDecided(final String node) {
        return permissionState(node) != TriState.NOT_SET;
    }

    /**
     * Opens a new revocable set of overrides for this holder.
     *
     * @param owner the plugin requesting the grant
     * @return the grant
     */
    PermissionGrant newGrant(Plugin owner);

    /**
     * Returns every override currently applied by active grants, most specific first. Declared
     * defaults are not included; use {@link PermissionRegistry#definitions()} for those.
     *
     * @return an immutable snapshot of the active overrides
     */
    Map<PermissionNode, TriState> activeOverrides();

    /**
     * Drops every cached resolution. Called by the server when operator status changes or when the
     * registry is mutated; plugins rarely need it.
     */
    void invalidatePermissions();
}
