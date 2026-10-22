package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundKeyPacket(String serverId, byte[] publicKey, byte[] challenge,
                                   boolean shouldAuthenticate) implements ClientboundPacket {

    @Override
    public Key name() {
        return LoginClientboundPackets.KEY;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeString(serverId)
                .writeByteArray(publicKey)
                .writeByteArray(challenge)
                .writeBoolean(shouldAuthenticate);
    }
}
