package fr.euphyllia.fidorial.server.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.euphyllia.fidorial.bootstrap.Artifact;
import fr.euphyllia.fidorial.bootstrap.BootstrapException;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.fidorial.Server;
import fr.fidorial.permission.PermissionDefinition;
import fr.fidorial.permission.PermissionNode;
import fr.fidorial.permission.PermissionRegistry;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.plugin.PluginManager;
import fr.fidorial.plugin.PluginMeta;
import fr.fidorial.service.ServiceRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public final class JavaPluginManager implements PluginManager, AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(JavaPluginManager.class);
    private static final String DESCRIPTOR = "fidorial.json";
    private static final Gson GSON = new Gson();

    private final Server server;
    private final SimpleEventBus events;
    private final ServiceRegistry services;
    private final Path pluginsFolder;

    private final Map<String, Loaded> plugins = new LinkedHashMap<>();

    private final PermissionRegistry permissions;
    private final Map<String, List<PermissionNode>> declaredByPlugin = new ConcurrentHashMap<>();

    public JavaPluginManager(
            final Server server,
            final SimpleEventBus events,
            final ServiceRegistry services,
            final PermissionRegistry permissions,
            final Path pluginsFolder
    ) {
        this.server = server;
        this.events = events;
        this.services = services;
        this.permissions = permissions;
        this.pluginsFolder = pluginsFolder;
    }

    private static void closeQuietly(@Nullable final URLClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        try {
            classLoader.close();
        } catch (final IOException ignored) {
            // nothing to do: this plugin was being discarded anyway
        }
    }

    public void loadAll() throws IOException {
        Files.createDirectories(pluginsFolder);
        final List<Candidate> candidates = new ArrayList<>();
        try (final Stream<Path> jars = Files.list(pluginsFolder)) {
            for (final Path jar : jars.filter(p -> p.toString().endsWith(".jar")).toList()) {
                readCandidate(jar).ifPresent(candidates::add);
            }
        }
        for (final Candidate candidate : sortByDependencies(candidates)) {
            instantiate(candidate);
        }
        LOGGER.info("{} plugin(s) loaded", plugins.size());
    }

    public void enableAll() {
        final Iterator<Loaded> it = plugins.values().iterator();
        while (it.hasNext()) {
            final Loaded loaded = it.next();
            try {
                events.withOwner(loaded.plugin, loaded.plugin::onEnable);
                loaded.enabled = true;
                LOGGER.info("Plugin enabled: {} v{}", loaded.meta.name(), loaded.meta.version());
            } catch (final Throwable t) {
                LOGGER.error("Could not enable {}, rolling it back", loaded.meta.id(), t);

                teardown(loaded.plugin, loaded.meta.id(), loaded.context);
                it.remove();
                closeQuietly(loaded.classLoader);
            }
        }
    }

    public void disableAll() {
        final List<Loaded> reversed = new ArrayList<>(plugins.values());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            final Loaded loaded = reversed.get(i);
            if (!loaded.enabled) {
                continue;
            }
            try {
                loaded.plugin.onDisable();
            } catch (final Throwable t) {
                LOGGER.error("Error during onDisable of {}", loaded.meta.id(), t);
            } finally {
                loaded.enabled = false;
                events.unsubscribeAll(loaded.plugin);
                services.unregisterAll(loaded.plugin);
                server.blockInteractions().unregisterAll(loaded.plugin);
                removePluginPermissions(loaded.meta.id());
            }
        }
    }

    @Override
    public Collection<PluginMeta> loaded() {
        final List<PluginMeta> list = new ArrayList<>();
        for (final Loaded l : plugins.values()) {
            final PluginMeta meta = l.meta;
            list.add(meta);
        }
        return list;
    }

    @Override
    public Optional<Plugin> plugin(final String id) {
        final Loaded loaded = plugins.get(id);
        return loaded == null ? Optional.empty() : Optional.of(loaded.plugin);
    }

    @Override
    public boolean isEnabled(final String id) {
        final Loaded loaded = plugins.get(id);
        return loaded != null && loaded.enabled;
    }

    @Override
    public void close() {
        disableAll();
        for (final Loaded loaded : plugins.values()) {
            try {
                loaded.context.close();
            } catch (final Exception e) {
                LOGGER.warn("Unable to close the plugin context for {}", loaded.meta.id(), e);
            }
            try {
                loaded.classLoader.close();
            } catch (final IOException e) {
                LOGGER.warn("Unable to close the classloader for {}", loaded.meta.id(), e);
            }
        }
        plugins.clear();
    }

    private void registerDescriptorPermissions(final PluginMeta meta) {
        if (meta.permissions().isEmpty()) {
            return;
        }
        final List<PermissionDefinition> definitions = new ArrayList<>();
        final List<PermissionNode> nodes = new ArrayList<>();
        for (final Map.Entry<String, PluginMeta.PermissionEntry> entry : meta.permissions().entrySet()) {
            try {
                final PermissionNode node = PermissionNode.of(entry.getKey());
                final PluginMeta.PermissionEntry declaration = entry.getValue();
                definitions.add(new PermissionDefinition(
                        node,
                        declaration.description(),
                        declaration.regular(),
                        declaration.operator()));
                nodes.add(node);
            } catch (final IllegalArgumentException e) {
                LOGGER.error("Invalid permission '{}' declared by plugin {}", entry.getKey(), meta.id(), e);
            }
        }
        if (!definitions.isEmpty()) {
            permissions.defineAll(definitions);
            declaredByPlugin.put(meta.id(), List.copyOf(nodes));
        }
    }

    private void removePluginPermissions(final String pluginId) {
        final List<PermissionNode> nodes = declaredByPlugin.remove(pluginId);
        if (nodes != null) {
            nodes.forEach(permissions::undefine);
        }
    }

    private Optional<Candidate> readCandidate(final Path jar) {
        PluginClassLoader classLoader = null;
        try (final JarFile archive = new JarFile(jar.toFile())) {
            final ZipEntry descriptor = archive.getEntry(DESCRIPTOR);
            if (descriptor == null) {
                LOGGER.warn("{} ignored: no {} at the root", jar.getFileName(), DESCRIPTOR);
                return Optional.empty();
            }

            final PluginMeta meta;
            try (final InputStream in = archive.getInputStream(descriptor)) {
                meta = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginMeta.class);
            }
            if (meta == null) {
                throw new JsonSyntaxException(DESCRIPTOR + " is empty");
            }

            final List<Artifact> libraries = PluginLibraries.declared(archive);
            final List<Path> resolved = PluginLibraries.resolve(meta.id(), libraries, meta.repositories());

            final List<URL> urls = new ArrayList<>(resolved.size() + 1);
            urls.add(jar.toUri().toURL());
            for (final Path library : resolved) {
                urls.add(library.toUri().toURL());
            }

            classLoader = new PluginClassLoader(
                    meta.id(),
                    urls.toArray(new URL[0]),
                    getClass().getClassLoader(),
                    ApiPackages.get());
            return Optional.of(new Candidate(meta, jar, classLoader));
        } catch (final JsonSyntaxException | NullPointerException | IllegalArgumentException e) {
            LOGGER.error("{} ignored: invalid {}", jar.getFileName(), DESCRIPTOR, e);
        } catch (final BootstrapException e) {
            LOGGER.error("{} ignored: {}", jar.getFileName(), e.getMessage());
        } catch (final IOException e) {
            LOGGER.error("{} is unreadable", jar.getFileName(), e);
        }
        closeQuietly(classLoader);
        return Optional.empty();
    }

    private void instantiate(final Candidate candidate) {
        final PluginMeta meta = candidate.meta;
        SimplePluginContext context = null;
        Plugin plugin = null;
        try {
            final Class<?> mainClass = Class.forName(meta.main(), true, candidate.classLoader);
            if (!Plugin.class.isAssignableFrom(mainClass)) {
                LOGGER.error("{} ignored: {} does not implement Plugin", meta.id(), meta.main());
                candidate.classLoader.close();
                return;
            }
            plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();
            context = new SimplePluginContext(
                    meta, server, events, services,
                    pluginsFolder.resolve(meta.id()), candidate.jarPath());
            registerDescriptorPermissions(meta);

            final SimplePluginContext finalContext = context;
            final Plugin finalPlugin = plugin;
            events.withOwner(plugin, () -> finalPlugin.onLoad(finalContext));

            plugins.put(meta.id(), new Loaded(meta, plugin, context, candidate.classLoader));
            return;
        } catch (final Throwable t) {
            LOGGER.error("Unable to load {}", meta.id(), t);
        }

        if (plugin != null) {
            teardown(plugin, meta.id(), context);
        } else {
            removePluginPermissions(meta.id());
            if (context != null) {
                context.close();
            }
        }
        closeQuietly(candidate.classLoader);
    }

    private void teardown(final Plugin plugin, final String pluginId, final @Nullable SimplePluginContext context) {
        try {
            plugin.onDisable();
        } catch (final Throwable t) {
            LOGGER.error("Error during rollback onDisable of {}; resources may have leaked", pluginId, t);
        }
        events.unsubscribeAll(plugin);
        services.unregisterAll(plugin);
        server.blockInteractions().unregisterAll(plugin);
        removePluginPermissions(pluginId);
        if (context != null) {
            try {
                context.close();
            } catch (final Exception e) {
                LOGGER.warn("Unable to close the plugin context for {}", pluginId, e);
            }
        }
    }

    private List<Candidate> sortByDependencies(final List<Candidate> candidates) {
        final Map<String, Candidate> byId = new HashMap<>();
        for (final Candidate candidate : candidates) {
            if (byId.putIfAbsent(candidate.meta.id(), candidate) != null) {
                LOGGER.error("Two plugins declare the ID '{}'; the second one is ignored.", candidate.meta.id());
                closeQuietly(candidate.classLoader);
            }
        }

        final List<Candidate> ordered = new ArrayList<>();
        final Set<String> done = new HashSet<>();
        final Set<String> visiting = new HashSet<>();
        for (final Candidate candidate : byId.values()) {
            visit(candidate, byId, done, visiting, ordered);
        }

        final Set<String> kept = new HashSet<>();
        for (final Candidate candidate : ordered) {
            kept.add(candidate.meta().id());
        }
        for (final Candidate candidate : byId.values()) {
            if (!kept.contains(candidate.meta().id())) {
                closeQuietly(candidate.classLoader());
            }
        }

        return ordered;
    }

    private void visit(
            final Candidate candidate,
            final Map<String, Candidate> byId,
            final Set<String> done,
            final Set<String> visiting,
            final List<Candidate> ordered
    ) {
        final String id = candidate.meta.id();
        if (done.contains(id)) {
            return;
        }
        if (!visiting.add(id)) {
            LOGGER.error("Cyclic dependency detected around '{}', plugin ignored", id);
            return;
        }
        for (final String dependency : candidate.meta.depends()) {
            final Candidate resolved = byId.get(dependency);
            if (resolved == null) {
                LOGGER.error("{} ignored: dependency '{}' not found", id, dependency);
                visiting.remove(id);
                return;
            }
            visit(resolved, byId, done, visiting, ordered);
        }
        visiting.remove(id);
        if (done.add(id)) {
            ordered.add(candidate);
        }
    }

    private record Candidate(PluginMeta meta, Path jarPath, URLClassLoader classLoader) {
    }

    private static final class Loaded {
        final PluginMeta meta;
        final Plugin plugin;
        final SimplePluginContext context;
        final URLClassLoader classLoader;
        boolean enabled;

        Loaded(final PluginMeta meta, final Plugin plugin, final SimplePluginContext context, final URLClassLoader classLoader) {
            this.meta = meta;
            this.plugin = plugin;
            this.context = context;
            this.classLoader = classLoader;
        }
    }
}
