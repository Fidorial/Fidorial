package fr.fidorial.permission;

import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.util.TriState;

import java.util.Map;

/**
 * A set of permission overrides a plugin applies to one holder, released by {@link #close()}.
 *
 * @since 0.1.0
 */
public interface PermissionGrant extends AutoCloseable {

    /**
     * @return the plugin this grant belongs to
     */
    Plugin owner();

    /**
     * @return the holder this grant applies to
     */
    PermissionHolder holder();

    /**
     * Sets the state of a node.
     *
     * @param node  the node
     * @param state the state; {@link TriState#NOT_SET} removes the override
     * @return this grant, for chaining
     * @throws IllegalStateException if the grant has been revoked
     */
    PermissionGrant set(PermissionNode node, TriState state);

    /**
     * Grants a node.
     *
     * @param node node path
     * @return this grant, for chaining
     */
    default PermissionGrant allow(final String node) {
        return set(PermissionNode.of(node), TriState.TRUE);
    }

    /**
     * Refuses a node, overriding any broader grant.
     *
     * @param node node path
     * @return this grant, for chaining
     */
    default PermissionGrant deny(final String node) {
        return set(PermissionNode.of(node), TriState.FALSE);
    }

    /**
     * Drops an override, letting resolution fall through to the next source.
     *
     * @param node node path
     * @return this grant, for chaining
     */
    default PermissionGrant unset(final String node) {
        return set(PermissionNode.of(node), TriState.NOT_SET);
    }

    /**
     * @return an immutable snapshot of the overrides carried by this grant
     */
    Map<PermissionNode, TriState> overrides();

    /**
     * @return {@code true} once {@link #revoke()} has been called
     */
    boolean revoked();

    /**
     * Removes every override of this grant from the holder. Calling it more than once is a no-op.
     */
    void revoke();

    @Override
    default void close() {
        revoke();
    }
}
