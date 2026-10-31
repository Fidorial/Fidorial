package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundFinishConfigurationPacket() implements ClientboundPacket {

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.FINISH_CONFIGURATION;
    }

    @Override
    public void write(final PacketBuffer buf) {
        // aucun champ
    }
}
