package fr.euphyllia.fidorial.bootstrap;

import org.jspecify.annotations.Nullable;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

final class SelfJar {

    private SelfJar() {
    }

    static @Nullable Path locate() {
        final CodeSource source = SelfJar.class.getProtectionDomain().getCodeSource();
        if (source == null) {
            return null;
        }
        final URL location = source.getLocation();
        if (location == null) {
            return null;
        }
        final Path path;
        try {
            path = Paths.get(location.toURI());
        } catch (final URISyntaxException | IllegalArgumentException e) {
            return null;
        }
        return Files.isRegularFile(path) ? path : null;
    }
}
