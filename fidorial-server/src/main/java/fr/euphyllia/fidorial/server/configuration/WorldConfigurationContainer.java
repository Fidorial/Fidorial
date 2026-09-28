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
            These settings apply to every world, unless overridden in worlds/<namespace>/<world>.yml.""";

    private final Path worldsDirectory;
    private final WorldConfiguration defaults;
    private final Codec<WorldConfiguration> overrideCodec;
    private final Map<Key, WorldConfiguration> resolved = new ConcurrentHashMap<>();

    private WorldConfigurationContainer(final Path worldsDirectory, final WorldConfiguration defaults) {
        this.worldsDirectory = worldsDirectory;
        this.defaults = defaults;
        this.overrideCodec = WorldConfiguration.codec(defaults);
    }

    public static WorldConfigurationContainer load(final Path configDirectory) throws IOException {
        final var configFile = defaultsFile(configDirectory);
        LOGGER.info("Default world configuration loaded from {}", configFile.path());
        return new WorldConfigurationContainer(configDirectory.resolve("worlds"), configFile.load());
    }

    static ConfigurationCodecs.YamlFile<WorldConfiguration> defaultsFile(final Path configDirectory) {
        return new ConfigurationCodecs.YamlFile<>(
                configDirectory.resolve("worlds").resolve("default.yml"), WorldConfiguration.CODEC, ConfigurationSchemas.WORLD, DEFAULTS_HEADER);
    }

    public WorldConfiguration defaults() {
        return defaults;
    }

    public WorldConfiguration resolve(final Key world) {
        return resolved.computeIfAbsent(world, this::read);
    }

    private WorldConfiguration read(final Key world) {
        final String header = """
                Configuration overrides for %s.
                Use this to override specific configurations from worlds/default.yml.""".formatted(world.asString());
        try {
            final Path override = overridePath(world);
            LOGGER.info("Configuration overrides for {} loaded from {}", world, override);
            return new ConfigurationCodecs.YamlFile<>(override, overrideCodec, ConfigurationSchemas.WORLD, header).loadSparse();
        } catch (final IOException e) {
            throw new UncheckedIOException("Could not read the configuration of " + world.asString(), e);
        }
    }

    private Path overridePath(final Key world) {
        return worldsDirectory.resolve(world.namespace()).resolve(world.value() + ".yml");
    }
}
