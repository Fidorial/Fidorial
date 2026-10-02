package fr.fidorial.permission;

import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.util.TriState;

import java.util.Map;

/**
 * A {@link PermissionHolder} backed by a {@link PermissionState}.
 *
 * @since 0.1.0
 */
public interface PermissionStateHolder extends PermissionHolder {

    /**
     * @return the engine backing this holder
     */
    PermissionState permissions();

    @Override
    default TriState permissionState(final PermissionNode node) {
        return permissions().resolve(node);
    }

    @Override
    default PermissionGrant newGrant(final Plugin owner) {
        return permissions().newGrant(owner);
    }

    @Override
    default Map<PermissionNode, TriState> activeOverrides() {
        return permissions().activeOverrides();
    }

    @Override
    default void invalidatePermissions() {
        permissions().invalidate();
    }
}
