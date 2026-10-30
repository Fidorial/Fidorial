package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundStatusResponsePacket(String json) implements ClientboundPacket {

    @Override
    public Key name() {
        return StatusClientboundPackets.STATUS_RESPONSE;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeString(json);
    }
}
