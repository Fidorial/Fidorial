package fr.fidorial.plugin;

import com.google.common.base.Preconditions;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The descriptor of a plugin, read from the {@code fidorial.json} file at the root of its jar.
 *
 * <p>Absent lists and maps are read as empty ones; {@code id}, {@code name}, {@code version} and
 * {@code main} are mandatory.</p>
 *
 * @param id           the unique identifier of the plugin, also used as its command namespace
 * @param name         the human-readable name
 * @param version      the version
 * @param main         the fully qualified name of the class implementing {@link Plugin}
 * @param authors      the authors
 * @param depends      the identifiers of the plugins that must be loaded before this one
 * @param permissions  the permissions declared by the plugin, keyed by node
 * @param repositories extra Maven repositories used to resolve the plugin libraries
 * @since 0.1.0
 */
public record PluginMeta(
        String id,
        String name,
        String version,
        String main,
        List<String> authors,
        List<String> depends,
        Map<String, PermissionEntry> permissions,
        List<String> repositories
) {

    /**
     * Validates the mandatory fields and copies the collections, reading absent ones as empty.
     *
     * @throws IllegalArgumentException if {@code id}, {@code name}, {@code version} or {@code main} is missing
     * @since 0.1.0
     */
    public PluginMeta {
        Preconditions.checkArgument(id != null, "The ID of a plugin meta must not be null");
        Preconditions.checkArgument(name != null, "The name of a plugin meta must not be null");
        Preconditions.checkArgument(version != null, "The version of a plugin meta must not be null");
        Preconditions.checkArgument(main != null, "The main of a plugin meta must not be null");
        authors = copy(authors);
        depends = copy(depends);
        permissions = permissions == null ? Map.of() : Map.copyOf(permissions);
        repositories = copy(repositories);
    }

    /**
     * Creates a descriptor declaring no permission and no repository.
     *
     * @param id      the unique identifier of the plugin
     * @param name    the human-readable name
     * @param version the version
     * @param main    the fully qualified name of the class implementing {@link Plugin}
     * @param authors the authors
     * @param depends the identifiers of the plugins that must be loaded before this one
     * @since 0.1.0
     */
    public PluginMeta(final String id, final String name, final String version, final String main, final List<String> authors, final List<String> depends) {
        this(id, name, version, main, authors, depends, Map.of(), List.of());
    }

    /**
     * Creates a descriptor declaring no repository.
     *
     * @param id          the unique identifier of the plugin
     * @param name        the human-readable name
     * @param version     the version
     * @param main        the fully qualified name of the class implementing {@link Plugin}
     * @param authors     the authors
     * @param depends     the identifiers of the plugins that must be loaded before this one
     * @param permissions the permissions declared by the plugin, keyed by node
     * @since 0.1.0
     */
    public PluginMeta(final String id, final String name, final String version, final String main, final List<String> authors, final List<String> depends, final Map<String, PermissionEntry> permissions) {
        this(id, name, version, main, authors, depends, permissions, List.of());
    }

    private static List<String> copy(final @Nullable List<String> list) {
        return list == null ? List.of() : List.copyOf(list);
    }

    /**
     * A permission declared in the descriptor.
     *
     * @param description what the permission allows
     * @param regular     the default value for regular players
     * @param operator    the default value for operators
     * @since 0.1.0
     */
    public record PermissionEntry(String description, TriState regular, TriState operator) {
    }
}
