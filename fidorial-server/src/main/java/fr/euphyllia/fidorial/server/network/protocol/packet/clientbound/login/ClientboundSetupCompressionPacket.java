package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;


public record ClientboundSetupCompressionPacket(int threshold) implements ClientboundPacket {

    @Override
    public Key name() {
        return LoginClientboundPackets.SETUP_COMPRESSION;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(threshold);
    }
}
