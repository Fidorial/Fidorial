package fr.euphyllia.fidorial.server.plugin;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

final class ApiPackages {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ApiPackages.class);
    private static final String RESOURCE = "META-INF/fidorial/api-packages.list";

    private static final Set<String> PACKAGES = load();

    private ApiPackages() {
    }

    static Set<String> get() {
        return PACKAGES;
    }

    private static Set<String> load() {
        final Set<String> packages = new TreeSet<>();
        final ClassLoader loader = ApiPackages.class.getClassLoader();
        Preconditions.checkState(loader != null, "The server must not be loaded by the bootstrap class loader");
        try (final InputStream in = loader.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE + " is missing from the server jar. "
                        + "Run :fidorial-server:generateApiPackageIndex.");
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String trimmed = line.trim();
                    if (!trimmed.isEmpty() && trimmed.charAt(0) != '#') {
                        packages.add(trimmed);
                    }
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("cannot read " + RESOURCE, e);
        }
        LOGGER.debug("{} packages are reserved for the server API", packages.size());
        return Set.copyOf(packages);
    }
}
