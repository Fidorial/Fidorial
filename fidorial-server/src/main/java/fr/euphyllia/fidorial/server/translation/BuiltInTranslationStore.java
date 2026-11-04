package fr.euphyllia.fidorial.server.translation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.Main;
import fr.fidorial.translation.TranslationStore;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class BuiltInTranslationStore implements TranslationStore {

    private static final Gson GSON = new Gson();
    private static final Type LANGUAGE_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final Locale DEFAULT_LOCALE = Locale.US;
    private static final String LANGUAGE_FOLDER = "languages";
    private static final String EXTENSION = ".json";

    private final MiniMessageTranslationStore miniMessageStore =
            MiniMessageTranslationStore.create(Key.key("translations"));

    private final Set<Locale> supportedLocales = ConcurrentHashMap.newKeySet();

    private static FileSystem openJarFileSystem(final URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Map.of());
        } catch (final FileSystemAlreadyExistsException ex) {
            return FileSystems.getFileSystem(uri);
        }
    }

    private static @Nullable Locale parseLocale(final String fileName) {
        final String name = fileName.substring(0, fileName.length() - EXTENSION.length());
        if (name.isBlank()) {
            return null;
        }
        final Locale locale = Locale.forLanguageTag(name.replace('_', '-'));
        return locale.getLanguage().isEmpty() ? null : locale;
    }

    private Locale resolveLocale(@Nullable final Locale locale) {
        if (locale == null || supportedLocales.isEmpty()) {
            return DEFAULT_LOCALE;
        }

        if (supportedLocales.contains(locale)) {
            return locale;
        }

        for (final Locale supported : supportedLocales) {
            if (supported.getLanguage().equals(locale.getLanguage())) {
                return supported;
            }
        }
        return DEFAULT_LOCALE;
    }

    private void loadBuiltin() {
        final URL url = Main.class.getClassLoader().getResource(LANGUAGE_FOLDER);
        if (url == null) {
            FidorialServer.LOGGER.warn("Missing builtin language folder: {}", LANGUAGE_FOLDER);
            return;
        }

        try {
            final URI uri = url.toURI();

            if ("jar".equals(uri.getScheme())) {
                try (final FileSystem fileSystem = openJarFileSystem(uri)) {
                    loadDirectory(fileSystem.getPath(LANGUAGE_FOLDER));
                }
            } else {
                loadDirectory(Paths.get(uri));
            }
        } catch (final URISyntaxException | IOException ex) {
            FidorialServer.LOGGER.error("Couldn't scan language folder {}", LANGUAGE_FOLDER, ex);
        }
    }

    private void loadDirectory(final Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            FidorialServer.LOGGER.warn("Language path is not a directory: {}", directory);
            return;
        }

        try (final Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(EXTENSION))
                    .sorted()
                    .forEach(this::loadFile);
        }

        if (!supportedLocales.contains(DEFAULT_LOCALE)) {
            FidorialServer.LOGGER.warn("Default language {} is missing, translations may fall back to raw keys",
                    DEFAULT_LOCALE);
        }
    }

    private void loadFile(final Path path) {
        final String fileName = path.getFileName().toString();
        final Locale locale = parseLocale(fileName);
        if (locale == null) {
            FidorialServer.LOGGER.warn("Ignoring language file with invalid name: {}", fileName);
            return;
        }

        try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            load(locale, reader);
        } catch (final IOException | RuntimeException ex) {
            FidorialServer.LOGGER.error("Couldn't load language {}", fileName, ex);
        }
    }

    private void load(final Locale locale, final Reader reader) throws IOException {
        final Map<String, String> entries = GSON.fromJson(reader, LANGUAGE_TYPE);
        if (entries == null || entries.isEmpty()) {
            return;
        }
        miniMessageStore.registerAll(locale, entries);
        supportedLocales.add(locale);

        FidorialServer.LOGGER.debug("Loaded {} translations for {}", entries.size(), locale);
    }

    @Override
    public void load() {
        unload();

        loadBuiltin();
        GlobalTranslator.translator().addSource(miniMessageStore);
    }

    @Override
    public void unload() {
        GlobalTranslator.translator().removeSource(miniMessageStore);
    }

    @Override
    public Component renderComponent(final Component component, final Locale locale) {
        return GlobalTranslator.render(component, resolveLocale(locale));
    }

    @Override
    public Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    public Set<Locale> getSupportedLocales() {
        return Set.copyOf(supportedLocales);
    }
}
