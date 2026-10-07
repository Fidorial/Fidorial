package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Remove_Entity_Effect
public record ClientboundRemoveMobEffectPacket(int entityId, int effectId) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.REMOVE_MOB_EFFECT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(effectId);
    }
}
