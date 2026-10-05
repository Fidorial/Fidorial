package fr.euphyllia.fidorial.server.configuration;

import fr.euphyllia.fidorial.server.ServerConfig;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.euphyllia.fidorial.server.configuration.migration.schemas.LegacyToV1Schema;
import fr.fidorial.world.Location;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class LegacyConfigurationMigration {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(LegacyConfigurationMigration.class);
    private static final Path LEGACY_FILE = Path.of("fidorial.properties");

    private LegacyConfigurationMigration() {
        throw new UnsupportedOperationException("LegacyConfigMigration cannot be instantiated.");
    }

    static boolean isNeeded(final Path target) {
        return Files.notExists(target) && Files.isRegularFile(LEGACY_FILE);
    }

    static ServerConfiguration migrate(final ConfigurationCodecs.YamlFile<ServerConfiguration> target) throws IOException {
        final ServerConfig legacy = ServerConfig.read(LEGACY_FILE);
        final ServerConfiguration config = target.load(LegacyToV1Schema.apply(legacy, target.createNode()));
        WorldConfigurationContainer.defaultsFile(ServerConfiguration.DIRECTORY).save(new WorldConfiguration(
                new WorldConfiguration.Gameplay(
                        legacy.pvp(), legacy.defaultGameMode(), legacy.generateStructures(),
                        new Location(legacy.spawnX(), legacy.spawnY(), legacy.spawnZ(), 0f, 0f)),
                WorldConfiguration.Storage.DEFAULTS));

        final Path backup = LEGACY_FILE.resolveSibling(LEGACY_FILE.getFileName() + "_old");
        Files.move(LEGACY_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Migrated {} to {} (the old file was kept as {})", LEGACY_FILE, target.path(), backup);
        return config;
    }
}
