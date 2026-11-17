package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Starts the key exchange of an encrypted login.
 *
 * <p>Since 26.4-snapshot-9 the server announces an X25519 public key (X.509 encoded) and a salt
 * instead of an RSA key and a challenge.</p>
 *
 * @param salt               mixed into the derivation of the session keys
 * @param serverId           the name the server gives itself in the exchange, at most 20 characters
 * @param serverKey          the X.509 encoding of the server's X25519 public key
 * @param shouldAuthenticate whether the client must join through Mojang's session server
 */
public record ClientboundKeyPacket(long salt, String serverId, byte[] serverKey,
                                   boolean shouldAuthenticate) implements ClientboundPacket<ClientboundKeyPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundKeyPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.LONG, ClientboundKeyPacket::salt)
            .then(StringLikeCodecs.STRING, ClientboundKeyPacket::serverId)
            .then(PrimitiveCodecs.BYTE_ARRAY, ClientboundKeyPacket::serverKey)
            .then(PrimitiveCodecs.BOOL, ClientboundKeyPacket::shouldAuthenticate);

    @Override
    public Key name() {
        return LoginClientboundPackets.KEY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundKeyPacket> codec() {
        return CODEC;
    }
}
