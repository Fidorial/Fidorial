package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundBrandPacket(String brand) implements ClientboundPacket {

    private static final Key BRAND_CHANNEL = Key.key("brand");

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.CUSTOM_PAYLOAD;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeKey(BRAND_CHANNEL).writeString(brand);
    }
}
