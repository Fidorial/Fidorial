package fr.euphyllia.fidorial.server.plugin;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class loader of one plugin.
 */
final class PluginClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    private final ClassLoader serverLoader;
    private final Set<String> parentFirstPackages;
    private final String pluginId;
    private final List<PluginClassLoader> dependencies = new CopyOnWriteArrayList<>();

    PluginClassLoader(
            final String pluginId,
            final URL[] urls,
            final ClassLoader serverLoader,
            final Set<String> parentFirstPackages
    ) {
        super("fidorial-plugin:" + pluginId, urls, serverLoader);
        this.pluginId = pluginId;
        this.serverLoader = serverLoader;
        this.parentFirstPackages = parentFirstPackages;
    }

    String pluginId() {
        return pluginId;
    }

    /**
     * Makes the classes of {@code dependency} visible to this plugin.
     *
     * @param dependency the class loader of a plugin listed in this plugin's {@code depends}
     */
    void addDependency(final PluginClassLoader dependency) {
        if (dependency != this && !dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> found = findLoadedClass(name);

            if (found == null) {
                final boolean parentFirst = isParentFirst(name);

                if (parentFirst) {
                    found = tryParent(name);
                }
                if (found == null) {
                    found = tryHere(name);
                }
                if (found == null && !parentFirst) {
                    found = tryParent(name);
                }
                if (found == null && !parentFirst) {
                    found = tryDependencies(name, new HashSet<>());
                }
                if (found == null) {
                    throw new ClassNotFoundException(name + " (from plugin " + pluginId + ")");
                }
            }

            if (resolve) {
                resolveClass(found);
            }
            return found;
        }
    }

    private boolean isParentFirst(final String className) {
        if (className.startsWith("java.")
                || className.startsWith("javax.")
                || className.startsWith("jdk.")
                || className.startsWith("sun.")) {
            return true;
        }
        final int separator = className.lastIndexOf('.');
        if (separator < 0) {
            return true;
        }
        return parentFirstPackages.contains(className.substring(0, separator));
    }

    private @Nullable Class<?> tryParent(final String name) {
        try {
            return serverLoader.loadClass(name);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    private @Nullable Class<?> tryHere(final String name) {
        try {
            return findClass(name);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Looks {@code name} up in the plugins this one depends on, then in theirs.
     *
     * <p>Locks are always taken from a plugin towards its dependencies, never the other way, and
     * cyclic dependencies are rejected before any plugin is linked to its dependencies, so this cannot
     * deadlock.</p>
     */
    private @Nullable Class<?> tryDependencies(final String name, final Set<PluginClassLoader> visited) {
        for (final PluginClassLoader dependency : dependencies) {
            if (!visited.add(dependency)) {
                continue;
            }
            final Class<?> found = dependency.findOwnOrDependencies(name, visited);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Class<?> findOwnOrDependencies(final String name, final Set<PluginClassLoader> visited) {
        synchronized (getClassLoadingLock(name)) {
            final Class<?> loaded = findLoadedClass(name);
            if (loaded != null) {
                return loaded;
            }
            final Class<?> own = tryHere(name);
            if (own != null) {
                return own;
            }
        }
        return tryDependencies(name, visited);
    }

    @Override
    public @Nullable URL getResource(final String name) {
        final URL own = findResource(name);
        return own != null ? own : serverLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        final List<URL> all = new ArrayList<>();
        all.addAll(Collections.list(findResources(name)));
        all.addAll(Collections.list(serverLoader.getResources(name)));
        return Collections.enumeration(all);
    }
}
