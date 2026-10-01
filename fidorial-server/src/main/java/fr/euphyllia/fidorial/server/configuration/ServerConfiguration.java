package fr.euphyllia.fidorial.server.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.euphyllia.fidorial.server.configuration.migration.ConfigurationSchemas;
import fr.euphyllia.fidorial.server.moderation.CodeOfConductManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.KEY_CODEC;
import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.commented;
import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.configRecord;

public record ServerConfiguration(
        Network network,
        Status status,
        General general,
        WorldSettings worlds,
        Threading threading,
        @Nullable ResourcePackRequest resourcePack,
        CodeOfConduct codeOfConduct,
        Spark spark
) {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ServerConfiguration.class);
    public static final Path DIRECTORY = Path.of("config");
    private static final Path FILE = DIRECTORY.resolve("fidorial.yaml");

    private static final String HEADER = """
            Fidorial server-wide configuration.
            This file is generated next to the JAR on first startup.""";

    public record Network(
            int port,
            boolean onlineMode,
            boolean encryptOfflineModeConnections,
            int compressionThreshold,
            boolean useIoUring,
            boolean enforceSecureChat,
            ProxyForwarding proxy
    ) {
        static final Network DEFAULTS = new Network(
                25565, true, false, 256, false, true, new ProxyForwarding.None());

        static final Codec<Network> CODEC = configRecord(Network.class, DEFAULTS)
                .field("port", Network::port, commented(Codec.intRange(1, 65535),
                        "The port to which the server should bind"))
                .field("online-mode", Network::onlineMode, commented(Codec.BOOL,
                        "Whether to authenticate players with Mojang"))
                .field("encrypt-offline-mode-connections", Network::encryptOfflineModeConnections, commented(Codec.BOOL,
                        "Whether offline mode connections should be encrypted"))
                .field("compression-threshold", Network::compressionThreshold, commented(Codec.INT,
                        "The minimum size in bytes for a packet to be compressed. Set to -1 to disable"))
                .field("use-io-uring", Network::useIoUring, commented(Codec.BOOL,
                        "Whether the server should attempt to use IO_Uring for network transport when it's available"))
                .field("enforce-secure-chat", Network::enforceSecureChat, commented(Codec.BOOL,
                        "Whether secure chat is enforced on this server"))
                .field("proxy", Network::proxy, commented(ProxyForwarding.CODEC,
                        "Configuration for Velocity proxies"))
                .build();
    }

    public sealed interface ProxyForwarding {

        Codec<ProxyForwarding> CODEC = ProxySection.CODEC.flatXmap(
                ProxySection::toForwarding,
                forwarding -> DataResult.success(ProxySection.of(forwarding)));

        record None() implements ProxyForwarding {
        }

        record Velocity(String secret) implements ProxyForwarding {
            @Override
            public String toString() {
                return "Velocity[secret=<redacted>]";
            }
        }
    }

    private record ProxySection(String mode, String secret) {

        private static final ProxySection NONE = new ProxySection("none", "");

        private static final Codec<ProxySection> CODEC = configRecord(ProxySection.class, NONE)
                .field("mode", ProxySection::mode, commented(Codec.STRING,
                        "How players reach this server: 'none' to connect directly, 'velocity' through a Velocity proxy"))
                .field("secret", ProxySection::secret, commented(Codec.STRING,
                        "The proxy's secret, used with 'velocity'. Must match the proxy's forwarding.secret file"))
                .build();

        private DataResult<ProxyForwarding> toForwarding() {
            return switch (mode) {
                case "none" -> DataResult.success(new ProxyForwarding.None());
                case "velocity" -> secret.isBlank()
                        ? DataResult.error(() -> "secret: Must be set when mode is 'velocity'")
                        : DataResult.success(new ProxyForwarding.Velocity(secret));
                default -> DataResult.error(() -> "mode: Unknown mode '" + mode + "', expected one of: none, velocity");
            };
        }

        private static ProxySection of(final ProxyForwarding forwarding) {
            return switch (forwarding) {
                case final ProxyForwarding.None _ -> NONE;
                case ProxyForwarding.Velocity(final String secret) -> new ProxySection("velocity", secret);
            };
        }
    }

    public record Status(Component motd, int maxPlayers) {
        private static final MiniMessage MOTD_MINI_MESSAGE = MiniMessage.miniMessage(MiniMessage.Preset.FORMATTED_TEXT);

        static final Status DEFAULTS =
                new Status(MOTD_MINI_MESSAGE.deserialize("<red>Fidorial <white>| <blue>Alternative Minecraft Server"), 100);

        static final Codec<Status> CODEC = configRecord(Status.class, DEFAULTS)
                .field("motd", Status::motd, commented(ConfigurationCodecs.miniMessage(MOTD_MINI_MESSAGE),
                        "The message that should be displayed in the server list of the client. Formatted using MiniMessage"))
                .field("max-players", Status::maxPlayers, commented(CommonCodecs.NON_NEGATIVE_INT,
                        "The maximum amount of players able to concurrently play on this server"))
                .build();
    }

    public record General(int viewDistance, int sendDistance, int autoSaveSeconds, Path pluginsPath) {
        static final General DEFAULTS = new General(10, 10, 5, Path.of("plugins"));

        static final Codec<General> CODEC = configRecord(General.class, DEFAULTS)
                .field("view-distance", General::viewDistance, commented(CommonCodecs.POSITIVE_INT,
                        "The distance advertised to the client, in chunks"))
                .field("send-distance", General::sendDistance, commented(CommonCodecs.POSITIVE_INT,
                        "The actual streaming radius. Must not exceed view-distance"))
                .field("auto-save-seconds", General::autoSaveSeconds, commented(CommonCodecs.POSITIVE_INT,
                        "How often should auto-save happen. Declared in seconds"))
                .field("plugins-path", General::pluginsPath, commented(ConfigurationCodecs.PATH,
                        "The path to the directory containing plugins"))
                .validate(general -> general.sendDistance() <= general.viewDistance(),
                        general -> "send-distance (" + general.sendDistance()
                                + ") is greater than view-distance (" + general.viewDistance() + ")")
                .build();
    }

    public record WorldSettings(Path path, Key defaultWorld, @Nullable Long levelSeed, List<Key> initialEnabledPacks) {
        static final WorldSettings DEFAULTS = new WorldSettings(Path.of("world"), Key.key("overworld"), null, List.of(Key.key("vanilla")));

        static final Codec<WorldSettings> CODEC = configRecord(WorldSettings.class, DEFAULTS)
                .field("path", WorldSettings::path, commented(ConfigurationCodecs.PATH,
                        "The path to the directory containing worlds"))
                .field("default-world", WorldSettings::defaultWorld, commented(KEY_CODEC,
                        "The dimension that should serve as the default for various operations, like selecting players' default spawn world"))
                .nullableInline("level-seed", WorldSettings::levelSeed, commented(ConfigurationCodecs.SEED,
                        "The seed to use for the default world generator"))
                .field("initial-enabled-packs", WorldSettings::initialEnabledPacks, commented(KEY_CODEC.listOf(),
                        "The feature packs to be enabled during first world creation. Ignored for existing worlds"))
                .validate(settings -> settings.initialEnabledPacks().contains(Key.key("vanilla")),
                        _ -> "initial-enabled-packs must contain 'minecraft:vanilla'")
                .build();
    }

    public record Threading(int regionWorkers, int chunkWorkers, int lightWorkers, int aiWorkers, int regionSectionShift) {
        private static final int CPUS = Runtime.getRuntime().availableProcessors();

        static final Threading DEFAULTS = new Threading(
                Math.max(2, CPUS / 2), Math.max(2, CPUS / 8), Math.max(2, CPUS / 8), Math.max(2, CPUS / 8), 5);

        static final Codec<Threading> CODEC = configRecord(Threading.class, DEFAULTS)
                .field("region-workers", Threading::regionWorkers, commented(CommonCodecs.POSITIVE_INT,
                        "The amount of threads to be used for managing regions"))
                .field("chunk-workers", Threading::chunkWorkers, commented(CommonCodecs.POSITIVE_INT,
                        "The amount of threads to be used for chunk related work, such as generation"))
                .field("light-workers", Threading::lightWorkers, commented(CommonCodecs.POSITIVE_INT,
                        "The amount of threads to be used for light updates"))
                .field("ai-workers", Threading::aiWorkers, commented(CommonCodecs.POSITIVE_INT,
                        "The amount of threads to be used by entities' pathfinding"))
                .field("region-section-shift", Threading::regionSectionShift, commented(Codec.intRange(0, 10),
                        "Controls the size of regions"))
                .build();
    }

    public record CodeOfConduct(boolean enabled, Path path) {
        static final CodeOfConduct DEFAULTS = new CodeOfConduct(false, Path.of(CodeOfConductManager.DEFAULT_FOLDER));

        static final Codec<CodeOfConduct> CODEC = configRecord(CodeOfConduct.class, DEFAULTS)
                .field("enabled", CodeOfConduct::enabled, commented(Codec.BOOL,
                        "Whether the server should send the Code of Conduct to connecting clients"))
                .field("path", CodeOfConduct::path, commented(ConfigurationCodecs.PATH,
                        "The path to the directory containing Code of Conduct files"))
                .build();
    }

    public record Spark(boolean enabled, Path path) {
        static final Spark DEFAULTS = new Spark(true, Path.of("spark"));

        static final Codec<Spark> CODEC = configRecord(Spark.class, DEFAULTS)
                .field("enabled", Spark::enabled, commented(Codec.BOOL,
                        "Whether the built-in spark module should be enabled"))
                .field("path", Spark::path, commented(ConfigurationCodecs.PATH,
                        "The directory for temporary data generated by spark"))
                .build();
    }

    private record ResourcePackSection(Optional<URI> url, String hash, Optional<UUID> id, boolean required, Component prompt) {

        private static final ResourcePackSection DISABLED = new ResourcePackSection(
                Optional.empty(), "", Optional.empty(), false, Component.empty());

        private static final Codec<ResourcePackSection> SECTION_CODEC = configRecord(ResourcePackSection.class, DISABLED)
                .field("url", ResourcePackSection::url, commented(ConfigurationCodecs.OPTIONAL_URI,
                        "The resource pack's URL"))
                .field("hash", ResourcePackSection::hash, commented(ConfigurationCodecs.SHA1_HEX,
                        "The optional SHA-1 digest of the resource pack. Must be formatted as a lowercase hexadecimal string"))
                .optional("id", ResourcePackSection::id, commented(CommonCodecs.UUID_STRING_CODEC,
                        "The resource pack's UUID. Generated on first startup when missing"))
                .field("required", ResourcePackSection::required, commented(Codec.BOOL,
                        "Whether clients are forced to accept this resource pack in order to continue playing"))
                .field("prompt", ResourcePackSection::prompt, commented(ConfigurationCodecs.miniMessage(MiniMessage.miniMessage()),
                        "The prompt to send to clients. Formatted using MiniMessage"))
                .build();

        static final Codec<Optional<ResourcePackRequest>> CODEC =
                SECTION_CODEC.xmap(ResourcePackSection::toRequest, ResourcePackSection::of);

        private Optional<ResourcePackRequest> toRequest() {
            return url.map(uri -> {
                final ResourcePackRequest.Builder builder = ResourcePackRequest.resourcePackRequest()
                        .packs(ResourcePackInfo.resourcePackInfo(id.orElseGet(UUID::randomUUID), uri, hash))
                        .required(required);
                if (!Component.empty().equals(prompt)) {
                    builder.prompt(prompt);
                }
                return builder.build();
            });
        }

        private static ResourcePackSection of(final Optional<ResourcePackRequest> request) {
            return request.map(r -> {
                final ResourcePackInfo pack = r.packs().getFirst();
                return new ResourcePackSection(
                        Optional.of(pack.uri()),
                        pack.hash(),
                        Optional.of(pack.id()),
                        r.required(),
                        Objects.requireNonNullElse(r.prompt(), Component.empty()));
            }).orElse(DISABLED);
        }
    }

    public static final ServerConfiguration DEFAULTS = new ServerConfiguration(
            Network.DEFAULTS, Status.DEFAULTS, General.DEFAULTS, WorldSettings.DEFAULTS, Threading.DEFAULTS,
            null, CodeOfConduct.DEFAULTS, Spark.DEFAULTS);

    public static final Codec<ServerConfiguration> CODEC = configRecord(ServerConfiguration.class, DEFAULTS)
            .field("network", ServerConfiguration::network, commented(Network.CODEC,
                    "Connection, authentication and proxy settings"))
            .field("status", ServerConfiguration::status, commented(Status.CODEC,
                    "How the server appears in the client's server list"))
            .field("general", ServerConfiguration::general, commented(General.CODEC,
                    "Server-wide settings"))
            .field("worlds", ServerConfiguration::worlds, commented(WorldSettings.CODEC,
                    "World storage and the default world. Per-world settings are in worlds/"))
            .field("threading", ServerConfiguration::threading, commented(Threading.CODEC, """
                    Thread pool sizes. Each value must be at least 1
                    Leave a value blank to let the server choose it based on the number of CPU cores"""))
            .nullableInline("resource-pack", ServerConfiguration::resourcePack, commented(ResourcePackSection.CODEC,
                    "The resource pack sent to players when they join. Leave 'url' empty to disable it"))
            .field("code-of-conduct", ServerConfiguration::codeOfConduct, commented(CodeOfConduct.CODEC, """
                    The Code of Conduct screen shown to players joining for the first time
                    Example files are written to 'path' on first startup"""))
            .field("spark", ServerConfiguration::spark, commented(Spark.CODEC,
                    "The built-in spark profiler"))
            .build();

    public static ServerConfiguration load() throws IOException {
        final ConfigurationCodecs.YamlFile<ServerConfiguration> file =
                new ConfigurationCodecs.YamlFile<>(FILE, CODEC, ConfigurationSchemas.SERVER, HEADER);

        final ServerConfiguration config = file.load();
        LOGGER.debug("Configuration loaded from {}", FILE);
        return config;
    }
}
