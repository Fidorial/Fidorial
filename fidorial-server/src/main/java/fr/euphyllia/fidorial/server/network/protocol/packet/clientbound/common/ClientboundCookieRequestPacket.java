package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;


/**
 * Asks the client for the cookie stored under {@code key}; it answers with a cookie response, empty when
 * it has no such cookie. Exists in the login, configuration and play phases.
 *
 * @param name the packet identifier for the current connection phase
 * @param key  the identifier of the cookie
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Cookie_Request_(login)">Cookie Request</a>
 */
public record ClientboundCookieRequestPacket(Key name, Key key) implements ClientboundPacket<ClientboundCookieRequestPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCookieRequestPacket> CODEC = NetworkEncoder
            .field(StringLikeCodecs.KEY, ClientboundCookieRequestPacket::key);

    public ClientboundCookieRequestPacket {
        Preconditions.checkArgument(name != null, "The packet identifier of a cookie request must not be null");
        Preconditions.checkArgument(key != null, "The key of a requested cookie must not be null");
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCookieRequestPacket> codec() {
        return CODEC;
    }
}
