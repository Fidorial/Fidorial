package fr.fidorial.plugin;

import fr.fidorial.Server;
import fr.fidorial.permission.PermissionRegistry;

import java.util.Collection;
import java.util.Optional;

/**
 * Loads plugins and exposes what is currently running.
 *
 * <p>Permissions are deliberately not part of this interface: they live in
 * {@link PermissionRegistry}, reachable through
 * {@link Server#permissions()}. Keeping the two apart means a plugin that only wants to
 * declare permissions does not need a handle on the plugin loader, and the registry has no
 * knowledge of plugin lifecycles.
 *
 * @since 0.1.0
 */
public interface PluginManager {

    /**
     * {@return the metadata of every loaded plugin}
     *
     * @since 0.1.0
     */
    Collection<PluginMeta> loaded();

    /**
     * Looks up a loaded plugin by identifier.
     *
     * @param id the plugin identifier
     * @return the plugin, if loaded
     * @since 0.1.0
     */
    Optional<Plugin> plugin(String id);

    /**
     * Tests whether a plugin is currently enabled.
     *
     * @param id the plugin identifier
     * @return {@code true} if enabled
     * @since 0.1.0
     */
    boolean isEnabled(String id);
}
