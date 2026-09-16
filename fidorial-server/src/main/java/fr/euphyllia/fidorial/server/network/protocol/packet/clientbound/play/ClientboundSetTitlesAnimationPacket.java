package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Title_Animation_Times
public record ClientboundSetTitlesAnimationPacket(int fadeIn, int stay, int fadeOut) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_TITLES_ANIMATION;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeInt(fadeIn);
        buf.writeInt(stay);
        buf.writeInt(fadeOut);
    }
}
