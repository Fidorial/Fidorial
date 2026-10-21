package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.auth.GameProfile;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundLoginFinishedPacket(GameProfile profile, byte options) implements ClientboundPacket {

    @Override
    public Key name() {
        return LoginClientboundPackets.LOGIN_FINISHED;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeUuid(profile.uuid());
        buf.writeString(profile.name());
        buf.writeVarInt(profile.properties().size());
        for (final GameProfile.Property prop : profile.properties()) {
            buf.writeString(prop.name());
            buf.writeString(prop.value());
            buf.writeBoolean(prop.signature() != null);
            if (prop.signature() != null) {
                buf.writeString(prop.signature());
            }
        }
        buf.writeUuid(profile.sessionId());
        buf.writeByte(options);
    }
}
