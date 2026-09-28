package fr.euphyllia.fidorial.server.configuration.migration.schemas;

import fr.euphyllia.fidorial.server.ServerConfig;
import fr.euphyllia.fidorial.server.configuration.migration.ConfigurationSchemas;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Objects;

/**
 * Migrates the legacy {@code fidorial.properties} to the V1 YAML layout.
 */
public final class LegacyToV1Schema {

    public static final int VERSION = 1;

    private LegacyToV1Schema() {
        throw new UnsupportedOperationException("LegacyToV1Schema cannot be instantiated.");
    }

    public static CommentedConfigurationNode apply(final ServerConfig legacy, final CommentedConfigurationNode root) {
        final CommentedConfigurationNode network = root.node("network");
        network.node("port").raw(legacy.port());
        network.node("online-mode").raw(legacy.onlineMode());
        network.node("encrypt-offline-mode-connections").raw(legacy.encryptOfflineModeConnections());
        network.node("compression-threshold").raw(legacy.compressionThreshold());
        network.node("use-io-uring").raw(legacy.useIoUring());
        network.node("enforce-secure-chat").raw(legacy.enforcesSecureChat());
        final CommentedConfigurationNode proxy = network.node("proxy");
        switch (legacy.proxyMode()) {
            case NONE -> proxy.node("mode").raw("none");
            case VELOCITY -> {
                proxy.node("mode").raw("velocity");
                proxy.node("secret").raw(Objects.requireNonNullElse(legacy.velocitySecret(), ""));
            }
        }

        final CommentedConfigurationNode status = root.node("status");
        status.node("motd").raw(legacy.motd());
        status.node("max-players").raw(legacy.maxPlayers());

        final CommentedConfigurationNode general = root.node("general");
        general.node("view-distance").raw(legacy.viewDistance());
        general.node("send-distance").raw(legacy.sendDistance());
        general.node("auto-save-seconds").raw(legacy.autoSaveSeconds());
        general.node("plugins-path").raw(legacy.pluginsPath().toString());

        final CommentedConfigurationNode worlds = root.node("worlds");
        worlds.node("path").raw(legacy.worldPath().toString());
        worlds.node("default-world").raw(legacy.defaultWorld().asString());
        worlds.node("level-seed").raw(legacy.levelSeed() == null ? "" : legacy.levelSeed());

        final CommentedConfigurationNode threading = root.node("threading");
        threading.node("region-workers").raw(legacy.regionWorkers());
        threading.node("chunk-workers").raw(legacy.chunkWorkers());
        threading.node("light-workers").raw(legacy.lightWorkers());
        threading.node("ai-workers").raw(legacy.aiWorkers());
        threading.node("region-section-shift").raw(legacy.regionShift());

        final CommentedConfigurationNode resourcePack = root.node("resource-pack");
        resourcePack.node("url").raw(Objects.requireNonNullElse(legacy.resourcePackUrl(), ""));
        resourcePack.node("hash").raw(Objects.requireNonNullElse(legacy.resourcePackHash(), ""));
        if (legacy.resourcePackId() != null) {
            resourcePack.node("id").raw(legacy.resourcePackId().toString());
        }
        resourcePack.node("required").raw(legacy.resourcePackForced());
        resourcePack.node("prompt").raw(legacy.resourcePackPrompt() == null
                ? ""
                : MiniMessage.miniMessage().serialize(legacy.resourcePackPrompt()));

        final CommentedConfigurationNode codeOfConduct = root.node("code-of-conduct");
        codeOfConduct.node("enabled").raw(legacy.enableCodeOfConduct());
        codeOfConduct.node("path").raw(legacy.codeOfConductPath().toString());

        final CommentedConfigurationNode spark = root.node("spark");
        spark.node("enabled").raw(legacy.sparkEnabled());
        spark.node("path").raw(legacy.sparkPath().toString());

        root.node(ConfigurationSchemas.SERVER.versionKey()).raw(VERSION);
        return root;
    }
}
