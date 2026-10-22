package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * The client's answer to {@code apply_options}: it has switched the requested options on its side.
 */
public record ServerboundOptionsAppliedPacket() implements ServerboundPacket {

    public static ServerboundOptionsAppliedPacket read(final PacketBuffer buf) {
        return new ServerboundOptionsAppliedPacket();
    }

    @Override
    public void handle(final PacketListener listener) {
        ((LoginPacketListener) listener).handleOptionsApplied(this);
    }
}
