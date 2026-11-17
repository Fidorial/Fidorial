package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login;

import fr.euphyllia.fidorial.auth.SessionKeyExchange;
import fr.euphyllia.fidorial.auth.SessionKeys;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * The client's half of the key exchange, since 26.4-snapshot-9.
 *
 * @param clientKey the X.509 encoding of the client's X25519 public key
 * @param signature the integrity digest of the session computed by the client, 32 bytes, not
 *                  length-prefixed
 */
public record ServerboundKeyPacket(byte[] clientKey, byte[] signature) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundKeyPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.byteArray(SessionKeyExchange.MAX_ENCODED_KEY_LENGTH),
            PrimitiveCodecs.fixedByteArray(SessionKeys.INTEGRITY_DIGEST_LENGTH),
            ServerboundKeyPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((LoginPacketListener) listener).handleKey(this);
    }
}
