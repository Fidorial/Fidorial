package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundFinishConfigurationPacket() implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundFinishConfigurationPacket> DECODER = _ -> new ServerboundFinishConfigurationPacket();

    @Override
    public void handle(final PacketListener listener) {
        ((ConfigurationPacketListener) listener).handleFinishConfiguration(this);
    }
}
