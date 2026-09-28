package fr.euphyllia.fidorial.server.configuration;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.euphyllia.fidorial.server.configuration.migration.ConfigurationSchemas;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldConfigurationContainer {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldConfigurationContainer.class);

    private static final String DEFAULTS_HEADER = """
            Fidorial world configuration.
            These settings apply to every world, unless overridden in the fidorial-world.yml file
            inside that world's dimension folder, e.g. world/dimensions/minecraft/overworld/data/fidorial/fidorial-world.yaml.""";

    private final WorldConfiguration defaults;
    private final Codec<WorldConfiguration> overrideCodec;
    private final Map<Key, WorldConfiguration> loaded = new ConcurrentHashMap<>();

    private WorldConfigurationContainer(final WorldConfiguration defaults) {
        this.defaults = defaults;
        this.overrideCodec = WorldConfiguration.codec(defaults);
    }

    public static WorldConfigurationContainer load(final Path configDirectory) throws IOException {
        final ConfigurationCodecs.YamlFile<WorldConfiguration> file = defaultsFile(configDirectory);
        final WorldConfigurationContainer container = new WorldConfigurationContainer(file.load());
        LOGGER.info("Default world configuration loaded from {}", file.path());
        return container;
    }

    static ConfigurationCodecs.YamlFile<WorldConfiguration> defaultsFile(final Path configDirectory) {
        return new ConfigurationCodecs.YamlFile<>(
                configDirectory.resolve("fidorial-world-default.yaml"), WorldConfiguration.CODEC, ConfigurationSchemas.WORLD, DEFAULTS_HEADER);
    }

    public WorldConfiguration defaults() {
        return defaults;
    }

    /**
     * The settings of a loaded world, or the defaults for a world that isn't loaded.
     */
    public WorldConfiguration resolve(final Key world) {
        return loaded.getOrDefault(world, defaults);
    }

    /**
     * Reads the overrides of {@code world} from {@code file}, creating it if missing. Done once per load of the world.
     */
    public WorldConfiguration loadWorld(final Key world, final Path file) {
        return loaded.computeIfAbsent(world, _ -> read(world, file));
    }

    /**
     * Forgets an unloaded world, so its file is read again the next time it is loaded.
     */
    public void unloadWorld(final Key world) {
        loaded.remove(world);
    }

    private WorldConfiguration read(final Key world, final Path file) {
        final String header = """
                Configuration overrides for %s.
                Use this to override specific settings from config/fidorial-world-default.yaml.""".formatted(world.asString());
        try {
            final WorldConfiguration configuration =
                    new ConfigurationCodecs.YamlFile<>(file, overrideCodec, ConfigurationSchemas.WORLD, header).loadSparse();
            LOGGER.info("Configuration overrides for {} loaded from {}", world.asString(), file);
            return configuration;
        } catch (final IOException e) {
            throw new UncheckedIOException("Could not read the configuration of " + world.asString(), e);
        }
    }
}
