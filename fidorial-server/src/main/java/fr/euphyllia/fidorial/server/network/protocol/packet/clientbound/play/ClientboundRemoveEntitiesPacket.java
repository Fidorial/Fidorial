package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundRemoveEntitiesPacket(int... entityIds) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.REMOVE_ENTITIES;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(entityIds.length);
        for (final int entityId : entityIds) {
            buf.writeVarInt(entityId);
        }
    }
}
