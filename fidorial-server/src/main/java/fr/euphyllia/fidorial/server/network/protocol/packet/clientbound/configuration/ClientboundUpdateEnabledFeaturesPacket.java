package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundUpdateEnabledFeaturesPacket(Key[] features) implements ClientboundPacket {

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.UPDATE_ENABLED_FEATURES;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeKeyArray(features);
    }
}
