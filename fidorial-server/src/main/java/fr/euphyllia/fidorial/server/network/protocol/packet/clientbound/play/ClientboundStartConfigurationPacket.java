package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Start_Configuration
// used when registries are updated to resync their contents to clients
public record ClientboundStartConfigurationPacket() implements ClientboundPacket<ClientboundStartConfigurationPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundStartConfigurationPacket> CODEC = (_, _) -> {};

    @Override
    public Key name() {
        return PlayClientboundPackets.START_CONFIGURATION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundStartConfigurationPacket> codec() {
        return CODEC;
    }
}
