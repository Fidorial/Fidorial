package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Asks the client to switch on the given connection options (see {@link ConnectionOptions}).
 * The client answers with {@code options_applied}; the server only switches them on once it receives that answer.
 */
public record ClientboundApplyOptionsPacket(byte options) implements ClientboundPacket<ClientboundApplyOptionsPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundApplyOptionsPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.BYTE, ClientboundApplyOptionsPacket::options);

    @Override
    public Key name() {
        return LoginClientboundPackets.APPLY_OPTIONS;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundApplyOptionsPacket> codec() {
        return CODEC;
    }
}
