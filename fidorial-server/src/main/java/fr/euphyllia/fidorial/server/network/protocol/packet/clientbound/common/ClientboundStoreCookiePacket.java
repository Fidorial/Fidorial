package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.key.Key;


/**
 * Stores a cookie on the client, under {@code key}. The client keeps it for the current session only,
 * through transfers to other servers. Exists in the configuration and play phases.
 *
 * @param name    the packet identifier for the current connection phase
 * @param key     the identifier of the cookie
 * @param payload the content of the cookie, at most {@link Player#MAX_COOKIE_SIZE} bytes
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Store_Cookie_(configuration)">Store Cookie</a>
 */
public record ClientboundStoreCookiePacket(Key name, Key key, byte[] payload)
        implements ClientboundPacket<ClientboundStoreCookiePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundStoreCookiePacket> CODEC = NetworkEncoder
            .field(StringLikeCodecs.KEY, ClientboundStoreCookiePacket::key)
            .then(PrimitiveCodecs.byteArray(Player.MAX_COOKIE_SIZE), ClientboundStoreCookiePacket::payload);

    public ClientboundStoreCookiePacket {
        Preconditions.checkArgument(name != null, "The packet identifier of a stored cookie must not be null");
        Preconditions.checkArgument(key != null, "The key of a stored cookie must not be null");
        Preconditions.checkArgument(payload != null, "The payload of a stored cookie must not be null");
        Preconditions.checkArgument(payload.length <= Player.MAX_COOKIE_SIZE,
                "A cookie holds at most %s bytes, got %s", Player.MAX_COOKIE_SIZE, payload.length);
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundStoreCookiePacket> codec() {
        return CODEC;
    }
}
