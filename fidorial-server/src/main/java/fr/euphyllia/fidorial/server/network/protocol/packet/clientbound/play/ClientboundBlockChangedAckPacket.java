package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Acknowledge_Block_Change
public record ClientboundBlockChangedAckPacket(int sequence) implements ClientboundPacket<ClientboundBlockChangedAckPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundBlockChangedAckPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.VAR_INT, ClientboundBlockChangedAckPacket::sequence);

    @Override
    public Key name() {
        return PlayClientboundPackets.BLOCK_CHANGED_ACK;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundBlockChangedAckPacket> codec() {
        return CODEC;
    }
}
