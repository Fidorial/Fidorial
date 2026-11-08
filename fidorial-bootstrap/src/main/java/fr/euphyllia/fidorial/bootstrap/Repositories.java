package fr.euphyllia.fidorial.bootstrap;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class Repositories {

    static final String EUPHYLLIA = "https://repo.euphyllia.moe/repository/maven-public/";

    static final List<String> DEFAULTS = List.of(
            EUPHYLLIA,                                          // le miroir d'abord
            "https://repo.papermc.io/repository/maven-public/", // leafpile, adventurex
            "https://nexus.lucko.me/repository/all/",           // spark, bytesocks
            "https://repo.faststats.dev/releases/",             // faststats
            "https://libraries.minecraft.net/");                // brigadier, dfu

    private Repositories() {
    }

    static List<String> resolve(final Path librariesDir, final @Nullable InputStream embedded) throws IOException {
        List<String> repositories = split(System.getProperty("fidorial.libraries.repositories"));
        if (repositories.isEmpty()) {
            repositories = split(System.getenv("FIDORIAL_LIBRARY_REPOSITORIES"));
        }
        if (repositories.isEmpty()) {
            final Path override = librariesDir.resolve("repositories.txt");
            if (Files.isRegularFile(override)) {
                repositories = clean(Files.readAllLines(override, StandardCharsets.UTF_8));
            }
        }
        if (repositories.isEmpty() && embedded != null) {
            repositories = clean(new String(embedded.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
        }
        if (repositories.isEmpty()) {
            repositories = DEFAULTS;
        }
        return repositories;
    }

    private static List<String> split(final @Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return clean(List.of(raw.split(",")));
    }

    private static List<String> clean(final List<String> raw) {
        final Set<String> unique = new LinkedHashSet<>();
        for (final String candidate : raw) {
            final String trimmed = candidate.trim();
            if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
                continue;
            }
            unique.add(trimmed.endsWith("/") ? trimmed : trimmed + "/");
        }
        return new ArrayList<>(unique);
    }
}
