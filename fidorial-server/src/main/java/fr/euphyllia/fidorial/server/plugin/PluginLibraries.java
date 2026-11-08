package fr.euphyllia.fidorial.server.plugin;

import fr.euphyllia.fidorial.bootstrap.Artifact;
import fr.euphyllia.fidorial.bootstrap.LibraryStore;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


final class PluginLibraries {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PluginLibraries.class);
    static final String LOCK_ENTRY = "META-INF/fidorial/libraries.list";

    private static @Nullable LibraryStore store;

    private PluginLibraries() {
    }

    static List<Artifact> declared(final JarFile jar) throws IOException {
        final ZipEntry entry = jar.getEntry(LOCK_ENTRY);
        if (entry == null) {
            return List.of();
        }
        final InputStream in = jar.getInputStream(entry);
        if (in == null) {
            throw new IOException("Cannot read " + LOCK_ENTRY + " in " + jar.getName());
        }
        try (in) {
            return Artifact.read(in, true);
        }
    }

    static List<Path> resolve(
            final String pluginId,
            final List<Artifact> artifacts,
            final List<String> repositories
    ) throws IOException {
        if (artifacts.isEmpty()) {
            return List.of();
        }
        final List<Path> paths = store().resolve(
                artifacts,
                "librar" + (artifacts.size() == 1 ? "y" : "ies") + " for plugin " + pluginId,
                repositories);
        LOGGER.info("{} resolved {} librar{}", pluginId, artifacts.size(), artifacts.size() == 1 ? "y" : "ies");
        warnAboutShadowedLibraries(pluginId, paths);
        return paths;
    }

    private static void warnAboutShadowedLibraries(final String pluginId, final List<Path> libraries) {
        for (final Path library : libraries) {
            final Set<String> shadowed = new TreeSet<>();
            try (final JarFile jar = new JarFile(library.toFile())) {
                jar.stream()
                        .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class"))
                        .forEach(entry -> {
                            final int separator = entry.getName().lastIndexOf('/');
                            if (separator > 0) {
                                final String candidate = entry.getName().substring(0, separator).replace('/', '.');
                                if (ApiPackages.get().contains(candidate)) {
                                    shadowed.add(candidate);
                                }
                            }
                        });
            } catch (final IOException e) {
                LOGGER.debug("Could not inspect {} for shadowed packages", library, e);
                continue;
            }
            if (!shadowed.isEmpty()) {
                LOGGER.warn("{} bundles {} which provides {} package(s) the server already owns "
                                + "({}); the server's version will be used",
                        pluginId, library.getFileName(), shadowed.size(), String.join(", ", shadowed));
            }
        }
    }

    private static synchronized LibraryStore store() {
        if (store == null) {
            final Path root = Paths.get(System.getProperty("fidorial.libraries.dir", "libraries"))
                    .toAbsolutePath()
                    .normalize();
            final String configured = System.getProperty("fidorial.libraries.repositories");
            final List<String> repositories = configured == null || configured.isBlank()
                    ? List.of("https://repo.euphyllia.moe/repository/maven-public/")
                    : new ArrayList<>(Arrays.asList(configured.split(",")));
            store = new LibraryStore(root, repositories);
        }
        return store;
    }
}
