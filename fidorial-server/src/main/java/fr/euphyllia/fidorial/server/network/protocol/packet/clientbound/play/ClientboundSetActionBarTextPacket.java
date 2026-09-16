package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Action_Bar_Text
public record ClientboundSetActionBarTextPacket(Component message) implements ClientboundPacket {
    @Override
    public Key name() {
        return PlayClientboundPackets.SET_ACTION_BAR_TEXT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeComponent(message);
    }
}
