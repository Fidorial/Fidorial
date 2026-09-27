package fr.euphyllia.fidorial.server.configuration;

import fr.euphyllia.fidorial.server.ServerConfig;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.euphyllia.fidorial.server.configuration.migration.schemas.LegacyToV1Schema;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.spongepowered.configurate.CommentedConfigurationNode;

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
        final CommentedConfigurationNode imported = LegacyToV1Schema.apply(ServerConfig.read(LEGACY_FILE), target.createNode());
        final ServerConfiguration config = target.load(imported);

        final Path backup = LEGACY_FILE.resolveSibling(LEGACY_FILE.getFileName() + "_old");
        Files.move(LEGACY_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Migrated {} to {} (the old file was kept as {})", LEGACY_FILE, target.path(), backup);
        return config;
    }
}
