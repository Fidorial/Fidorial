package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Acknowledge_Configuration
public record ServerboundAcknowledgeConfigurationPacket() implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundAcknowledgeConfigurationPacket> DECODER = _ -> new ServerboundAcknowledgeConfigurationPacket();

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleAcknowledgeConfiguration(this);
    }
}
