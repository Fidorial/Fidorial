package fr.euphyllia.fidorial.server.codecs.api.status;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.fidorial.status.Favicon;
import fr.fidorial.status.ServerStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs.COMPONENT_CODEC;

/// Codecs related to the [ServerStatus] API
public final class ServerStatusCodecs {

    private static final String FAVICON_PREFIX = "data:image/png;base64,";

    private static final Codec<ServerStatus.Version> VERSION_CODEC = RecordCodec.builder(ServerStatus.Version.class)
            .keys(RecordCodec.KeyStyle.UNCHECKED)
            .required("name", ServerStatus.Version::name, Codec.STRING)
            .required("protocol", ServerStatus.Version::protocolVersion, Codec.INT)
            .build();

    private static final Codec<ServerStatus.SamplePlayer> SAMPLE_PLAYER_CODEC = RecordCodec.builder(ServerStatus.SamplePlayer.class)
            .required("name", ServerStatus.SamplePlayer::name, Codec.STRING)
            .required("id", ServerStatus.SamplePlayer::id, CommonCodecs.UUID_STRING_CODEC)
            .build();

    private static final Codec<Players> PLAYERS_CODEC = RecordCodec.builder(Players.class)
            .required("max", Players::max, Codec.INT)
            .required("online", Players::online, Codec.INT)
            .field("sample", Players::sample, SAMPLE_PLAYER_CODEC.listOf(), List.of())
            .build();

    private static final Codec<Favicon> FAVICON_CODEC = Codec.STRING.comapFlatMap(
            data -> {
                if (!data.startsWith(FAVICON_PREFIX)) {
                    return DataResult.error(() -> "Favicon is not a PNG data URI");
                }
                try {
                    final byte[] bytes = Base64.getDecoder().decode(data.substring(FAVICON_PREFIX.length()));
                    return DataResult.success(Favicon.read(new ByteArrayInputStream(bytes)));
                } catch (final IllegalArgumentException | IOException e) {
                    return DataResult.error(() -> "Invalid favicon: " + e.getMessage());
                }
            },
            favicon -> FAVICON_PREFIX + Base64.getEncoder().encodeToString(favicon.data()));

    public static final Codec<ServerStatus> SERVER_STATUS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VERSION_CODEC.fieldOf("version").forGetter(ServerStatus::version),
            PLAYERS_CODEC.fieldOf("players").forGetter(status -> new Players(status.maxPlayers(), status.players(), status.samplePlayers())),
            COMPONENT_CODEC.fieldOf("description").forGetter(ServerStatus::description),
            FAVICON_CODEC.optionalFieldOf("favicon").forGetter(ServerStatus::favicon),
            Codec.BOOL.optionalFieldOf("enforcesSecureChat", false).forGetter(ServerStatus::enforceSecureChat)
    ).apply(instance, (version, players, description, favicon, enforceSecureChat) -> ServerStatus.builder()
            .version(version)
            .maxPlayers(players.max())
            .players(players.online())
            .samplePlayers(players.sample())
            .description(description)
            .favicon(favicon.orElse(null))
            .enforceSecureChat(enforceSecureChat)
            .build()));

    private ServerStatusCodecs() {
        throw new UnsupportedOperationException("ServerStatusCodecs cannot be instantiated.");
    }

    private record Players(int max, int online, List<ServerStatus.SamplePlayer> sample) {
    }
}
