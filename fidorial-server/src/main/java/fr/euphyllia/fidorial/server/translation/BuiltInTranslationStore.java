package fr.euphyllia.fidorial.server.translation;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.Main;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
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

/**
 * The server's own translations, read from the {@code languages} resources.
 */
public final class BuiltInTranslationStore {

    /**
     * The locale used when a translation is missing in the requested one.
     */
    public static final Locale DEFAULT_LOCALE = Locale.US;

    private static final Gson GSON = new Gson();
    private static final Type LANGUAGE_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final String LANGUAGE_FOLDER = "languages";
    private static final String EXTENSION = ".json";

    private final MiniMessageTranslationStore miniMessageStore =
            MiniMessageTranslationStore.create(Key.key("fidorial", "translations"));

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

    private void loadBuiltin() {
        final ClassLoader loader = Main.class.getClassLoader();
        Preconditions.checkState(loader != null, "The server must not be loaded by the bootstrap class loader");
        final URL url = loader.getResource(LANGUAGE_FOLDER);
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
                    .filter(path -> String.valueOf(path.getFileName()).toLowerCase(Locale.ROOT).endsWith(EXTENSION))
                    .sorted()
                    .forEach(this::loadFile);
        }

        if (!supportedLocales.contains(DEFAULT_LOCALE)) {
            FidorialServer.LOGGER.warn("Default language {} is missing, translations may fall back to raw keys",
                    DEFAULT_LOCALE);
        }
    }

    private void loadFile(final Path path) {
        final String fileName = String.valueOf(path.getFileName());
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

    /**
     * Reads the translations and registers them to the {@link GlobalTranslator}; called once.
     */
    public void load() {
        miniMessageStore.defaultLocale(DEFAULT_LOCALE);
        loadBuiltin();
        GlobalTranslator.translator().addSource(miniMessageStore);
    }

    /**
     * {@return the source holding these translations}
     */
    public Translator translator() {
        return miniMessageStore;
    }
}
