package fr.euphyllia.fidorial.server.moderation;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class CodeOfConductManager {

    public static final String DEFAULT_FOLDER = "codeofconduct";
    public static final String DEFAULT_LANGUAGE = "en_us";

    private static final int MAX_LENGTH = 32767;

    private static final String EXTENSION = ".txt";

    private static final String BUNDLED_RESOURCE_FOLDER = "/" + DEFAULT_FOLDER + "/";
    private static final String[] BUNDLED_LANGUAGES = {DEFAULT_LANGUAGE, "fr_fr"};

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CodeOfConductManager.class);

    private final boolean enabled;
    private final Path folder;

    private volatile Map<String, String> byLanguage = Map.of();

    public CodeOfConductManager(final boolean enabled, final Path folder) {
        this.enabled = enabled;
        this.folder = folder;
        reload();
    }

    public void reload() {
        if (!enabled) {
            byLanguage = Map.of();
            return;
        }
        if (!Files.isDirectory(folder)) {
            writeBundledExamples();
        }
        if (!Files.isDirectory(folder)) {
            LOGGER.warn(
                    "enable-code-of-conduct is true but the folder {} does not exist; the Code of Conduct is disabled.",
                    folder.toAbsolutePath());
            byLanguage = Map.of();
            return;
        }

        final Map<String, String> loaded = new HashMap<>();
        try (final Stream<Path> files = Files.list(folder)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(EXTENSION))
                    .forEach(path -> read(path, loaded));
        } catch (final IOException | UncheckedIOException e) {
            LOGGER.error("Unable to list the Code of Conduct folder {}", folder.toAbsolutePath(), e);
            byLanguage = Map.of();
            return;
        }

        if (loaded.isEmpty()) {
            LOGGER.warn(
                    "enable-code-of-conduct is true but no <lang>{} file was found in {}; the Code of Conduct is disabled.",
                    EXTENSION,
                    folder.toAbsolutePath());
        } else {
            LOGGER.info("Code of Conduct loaded for {} language(s): {}", loaded.size(), loaded.keySet());
        }
        byLanguage = Map.copyOf(loaded);
    }

    private void writeBundledExamples() {
        try {
            Files.createDirectories(folder);
        } catch (final IOException e) {
            LOGGER.error("Unable to create the Code of Conduct folder {}", folder.toAbsolutePath(), e);
            return;
        }

        for (final String language : BUNDLED_LANGUAGES) {
            final String fileName = language + EXTENSION;
            final Path target = folder.resolve(fileName);
            if (Files.exists(target)) {
                continue;
            }
            try (final InputStream in =
                         CodeOfConductManager.class.getResourceAsStream(BUNDLED_RESOURCE_FOLDER + fileName)) {
                if (in == null) {
                    LOGGER.warn("Missing bundled Code of Conduct example {}", BUNDLED_RESOURCE_FOLDER + fileName);
                    continue;
                }
                Files.copy(in, target);
                LOGGER.debug("Example Code of Conduct written to {}", target.toAbsolutePath());
            } catch (final IOException e) {
                LOGGER.error("Unable to write the example Code of Conduct {}", target.toAbsolutePath(), e);
            }
        }
    }

    private void read(final Path path, final Map<String, String> target) {
        final String fileName = path.getFileName().toString();
        final String language = fileName.substring(0, fileName.length() - EXTENSION.length())
                .toLowerCase(Locale.ROOT);
        if (language.isBlank()) {
            return;
        }
        final String content;
        try {
            content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            LOGGER.error("Unable to read the Code of Conduct file {}", path.toAbsolutePath(), e);
            return;
        }
        if (content.isBlank()) {
            LOGGER.warn("The Code of Conduct file {} is empty and was ignored.", path.toAbsolutePath());
            return;
        }
        if (content.length() > MAX_LENGTH) {
            LOGGER.warn(
                    "The Code of Conduct file {} is longer than {} characters and was truncated.",
                    path.toAbsolutePath(),
                    MAX_LENGTH);
            target.put(language, content.substring(0, MAX_LENGTH));
            return;
        }
        target.put(language, content);
    }

    public boolean enabled() {
        return enabled && !byLanguage.isEmpty();
    }

    public @Nullable String contentFor(final Locale locale) {
        final Map<String, String> snapshot = byLanguage;
        if (snapshot.isEmpty()) {
            return null;
        }

        final String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        final String country = locale.getCountry().toLowerCase(Locale.ROOT);

        if (!language.isEmpty() && !country.isEmpty()) {
            final String exact = snapshot.get(language + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        if (!language.isEmpty()) {
            final String bare = snapshot.get(language);
            if (bare != null) {
                return bare;
            }
            final String prefix = language + "_";
            for (final Map.Entry<String, String> entry : snapshot.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    return entry.getValue();
                }
            }
        }

        final String fallback = snapshot.get(DEFAULT_LANGUAGE);
        return fallback != null ? fallback : snapshot.values().iterator().next();
    }

    public Path folder() {
        return folder;
    }
}
