package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.entity.player.Player;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the client to a cookie request, in the login, configuration or play phase.
 *
 * @param key     the identifier of the requested cookie
 * @param payload the content of the cookie, or {@code null} if the client has no such cookie
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Cookie_Response_(login)">Cookie Response</a>
 */
public record ServerboundCookieResponsePacket(Key key, byte @Nullable [] payload) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundCookieResponsePacket> DECODER =
            ServerboundCookieResponsePacket::decode;

    private static ServerboundCookieResponsePacket decode(final PacketBuffer buf) {
        final Key key = buf.readKey();
        final byte[] payload = buf.readOptionalByteArray(Player.MAX_COOKIE_SIZE);
        return new ServerboundCookieResponsePacket(key, payload);
    }

    @Override
    public void handle(final PacketListener listener) {
        switch (listener) {
            case final PlayPacketListener play -> play.handleCookieResponse(this);
            case final ConfigurationPacketListener config -> config.handleCookieResponse(this);
            case final LoginPacketListener login -> login.handleCookieResponse(this);
            default -> {
            }
        }
    }
}
