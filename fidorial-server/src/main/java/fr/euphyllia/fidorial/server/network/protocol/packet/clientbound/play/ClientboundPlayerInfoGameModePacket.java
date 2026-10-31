package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public record ClientboundPlayerInfoGameModePacket(UUID uuid, int gameMode)
        implements ClientboundPacket {

    private static final int ACTIONS = 0x04;

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeByte(ACTIONS);
        buf.writeVarInt(1);
        buf.writeUuid(uuid);
        buf.writeVarInt(gameMode);
    }
}
