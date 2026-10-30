package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundPongResponsePacket(long payload) implements ClientboundPacket {

    @Override
    public Key name() {
        return StatusClientboundPackets.PONG_RESPONSE;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeLong(payload);
    }
}
