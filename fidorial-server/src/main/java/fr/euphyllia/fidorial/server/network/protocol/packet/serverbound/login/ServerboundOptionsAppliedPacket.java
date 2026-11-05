package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * The client's answer to {@code apply_options}: it has switched the requested options on its side.
 */
public record ServerboundOptionsAppliedPacket() implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundOptionsAppliedPacket> DECODER = _ -> new ServerboundOptionsAppliedPacket();

    @Override
    public void handle(final PacketListener listener) {
        ((LoginPacketListener) listener).handleOptionsApplied(this);
    }
}
