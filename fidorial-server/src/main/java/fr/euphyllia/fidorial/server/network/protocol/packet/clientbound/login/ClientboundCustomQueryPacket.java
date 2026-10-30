package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundCustomQueryPacket(int transactionId, Key channel, byte[] payload)
        implements ClientboundPacket {

    @Override
    public Key name() {
        return LoginClientboundPackets.CUSTOM_QUERY;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(transactionId)
                .writeKey(channel)
                .writeRawBytes(payload);
    }
}
