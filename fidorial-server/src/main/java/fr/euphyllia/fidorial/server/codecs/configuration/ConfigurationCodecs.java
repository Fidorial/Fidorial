package fr.euphyllia.fidorial.server.codecs.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.euphyllia.fidorial.server.configuration.exception.InvalidConfigurationException;
import fr.euphyllia.fidorial.server.configuration.migration.ConfigurationSchemas;
import fr.fidorial.entity.GameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.extra.dfu.v7.ConfigurateOps;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ConfigurationCodecs {

    private static final Pattern SHA1 = Pattern.compile("[0-9a-f]{40}");

    public static final Codec<Path> PATH = Codec.STRING.comapFlatMap(ConfigurationCodecs::parsePath, Path::toString);

    private static final String GAME_MODES = Arrays.stream(GameMode.values())
            .map(mode -> mode.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.joining(", "));

    public static final Codec<GameMode> GAME_MODE = Codec.STRING.comapFlatMap(
            raw -> {
                final GameMode mode = GameMode.byName(raw.strip());
                return mode != null
                        ? DataResult.success(mode)
                        : DataResult.error(() -> "Unknown game mode '" + raw + "', expected one of: " + GAME_MODES);
            },
            mode -> mode.name().toLowerCase(Locale.ROOT));

    public static final Codec<Optional<Long>> SEED = Codec.either(Codec.LONG, Codec.STRING).xmap(
            either -> either.map(Optional::of, ConfigurationCodecs::seedFromText),
            seed -> seed.<Either<Long, String>>map(Either::left).orElseGet(() -> Either.right("")));

    public static final Codec<Optional<URI>> OPTIONAL_URI = Codec.STRING.comapFlatMap(
            raw -> {
                final String value = raw.strip();
                if (value.isEmpty()) {
                    return DataResult.success(Optional.empty());
                }
                try {
                    return DataResult.success(Optional.of(new URI(value)));
                } catch (final URISyntaxException e) {
                    return DataResult.error(() -> "Not a valid URI: '" + raw + "' (" + e.getReason() + ")");
                }
            },
            uri -> uri.map(URI::toString).orElse(""));

    public static final Codec<String> SHA1_HEX = Codec.STRING.validate(CommonCodecs.check(
            hash -> hash.isEmpty() || SHA1.matcher(hash).matches(),
            hash -> "Not a 40-character lowercase SHA-1 hex string: '" + hash + "'"));

    private ConfigurationCodecs() {
        throw new UnsupportedOperationException("ConfigurationCodecs cannot be instantiated.");
    }

    /**
     * A {@link RecordCodec} builder for a configuration record, writing every setting.
     */
    public static <R extends Record> RecordCodec.Builder<R> configRecord(final Class<R> type, final R defaults) {
        return configRecord(type, defaults, true);
    }

    /**
     * A {@link RecordCodec} builder for a configuration record.
     *
     * @param writeDefaults whether settings equal to their default are written;
     * when {@code false}, only the settings that differ from {@code defaults} are encoded
     */
    public static <R extends Record> RecordCodec.Builder<R> configRecord(final Class<R> type, final R defaults, final boolean writeDefaults) {
        final RecordCodec.Builder<R> builder = RecordCodec.builder(type)
                .keys(RecordCodec.KeyStyle.KEBAB_CASE)
                .defaults(defaults);
        return writeDefaults ? builder.writeDefaults() : builder;
    }

    /**
     * Attaches {@code comment} to the node {@code codec} encodes to, when encoding to Configurate nodes.
     */
    public static <A> Codec<A> commented(final Codec<A> codec, final String comment) {
        if (comment.isBlank()) {
            return codec;
        }
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return codec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return codec.encode(input, ops, prefix).map(node -> {
                    if (node instanceof final CommentedConfigurationNode commentedNode) {
                        commentedNode.comment(comment);
                    }
                    return node;
                });
            }

            @Override
            public String toString() {
                return codec + "[commented]";
            }
        };
    }

    public static Codec<Component> miniMessage(final MiniMessage miniMessage) {
        return Codec.STRING.comapFlatMap(
                raw -> {
                    try {
                        return DataResult.success(miniMessage.deserialize(raw));
                    } catch (final RuntimeException e) {
                        return DataResult.error(() -> "Invalid MiniMessage '" + raw + "': " + e.getMessage());
                    }
                },
                miniMessage::serialize);
    }

    public static YamlConfigurationLoader.Builder yamlLoader() {
        return YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .commentsEnabled(true);
    }

    /**
     * A versioned YAML file whose contents are fully described by {@code codec}.
     * <p>
     * {@link #load()} reads the file, upgrades it through {@code schemas}, decodes it, then encodes the value back out and saves it
     */
    public record YamlFile<T>(
            Path path,
            Codec<T> codec,
            ConfigurationSchemas schemas,
            @Nullable String header
    ) {

        public CommentedConfigurationNode createNode() {
            return loader().createNode();
        }

        private static void removeBlankValues(final ConfigurationNode node) {
            for (final ConfigurationNode child : List.copyOf(node.childrenMap().values())) {
                if (child.isMap()) {
                    removeBlankValues(child);
                } else if (!child.isList() && child.rawScalar() == null) {
                    node.removeChild(Objects.requireNonNull(child.key()));
                }
            }
        }

        public T load() throws ConfigurateException, InvalidConfigurationException {
            return load(loader().load());
        }

        /**
         * Upgrades, decodes and saves {@code root}.
         */
        public T load(final CommentedConfigurationNode root) throws ConfigurateException, InvalidConfigurationException {
            schemas.upgrade(path, root);
            removeBlankValues(root);
            final T value = decode(root);
            save(value);
            return value;
        }

        /**
         * Loads the file without rewriting it, so it keeps only the settings written in it. A missing file is
         * created with just the header and the version.
         */
        public T loadSparse() throws IOException {
            final boolean existed = Files.exists(path);
            final YamlConfigurationLoader loader = loader();
            final CommentedConfigurationNode root = loader.load();
            removeBlankValues(root);
            final int version = root.node(schemas.versionKey()).getInt(-1);
            schemas.upgrade(path, root);
            if (!existed || version != schemas.latestVersion()) {
                schemas.stamp(root);
                Files.createDirectories(path.toAbsolutePath().getParent());
                loader.save(root);
            }
            return decode(root);
        }

        private T decode(final CommentedConfigurationNode root) {
            return switch (codec.parse(ops(root), root)) {
                case final DataResult.Success<T> success -> success.value();
                case final DataResult.Error<T> error ->
                        throw new InvalidConfigurationException(path, error.message().lines().toList());
            };
        }

        public void save(final T value) throws ConfigurateException {
            final YamlConfigurationLoader loader = loader();
            final CommentedConfigurationNode root = loader.createNode();
            schemas.stamp(root);

            final ConfigurationNode encoded = codec.encodeStart(ops(root), value)
                    .getOrThrow(message -> new ConfigurateException(root, "Could not encode " + path + ": " + message));
            root.mergeFrom(encoded);
            loader.save(root);
        }

        private YamlConfigurationLoader loader() {
            return yamlLoader()
                    .path(path)
                    .headerMode(HeaderMode.PRESET)
                    .defaultOptions(options -> options.header(header))
                    .build();
        }
    }

    private static DynamicOps<ConfigurationNode> ops(final ConfigurationNode node) {
        return ConfigurateOps.builder().factoryFromNode(node).build();
    }

    private static DataResult<Path> parsePath(final String raw) {
        try {
            return DataResult.success(Path.of(raw));
        } catch (final InvalidPathException e) {
            return DataResult.error(() -> "Invalid path '" + raw + "': " + e.getReason());
        }
    }

    private static Optional<Long> seedFromText(final String text) {
        final String value = text.strip();
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value));
        } catch (final NumberFormatException notANumber) {
            return Optional.of((long) value.hashCode());
        }
    }
}
