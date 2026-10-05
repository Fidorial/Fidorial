package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.Collection;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Post_Effects
public record ClientboundPostEffectsPacket(Key name, Collection<Key> postEffects) implements ClientboundPacket {

    @Override
    public Key name() {
        return name;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeKeyArray(postEffects.toArray(Key[]::new));
    }
}
