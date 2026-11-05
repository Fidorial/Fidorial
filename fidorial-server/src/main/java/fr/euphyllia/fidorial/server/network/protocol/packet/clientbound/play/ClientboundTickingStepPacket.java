package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Step_Tick
public record ClientboundTickingStepPacket(int tickSteps) implements ClientboundPacket<ClientboundTickingStepPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundTickingStepPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.VAR_INT, ClientboundTickingStepPacket::tickSteps);

    @Override
    public Key name() {
        return PlayClientboundPackets.TICKING_STEP;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundTickingStepPacket> codec() {
        return CODEC;
    }
}
