package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.item.component.SwingAnimation;
import net.kyori.adventure.key.Key;

public record ClientboundSwingAnimationPacket(int entityId, boolean mainHand, SwingAnimation animation) implements ClientboundPacket<ClientboundSwingAnimationPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSwingAnimationPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundSwingAnimationPacket::entityId)
            .then(PrimitiveCodecs.VAR_INT, packet -> packet.mainHand() ? 0 : 1) // 0 = main_hand, 1 = off_hand
            .then(PrimitiveCodecs.VAR_INT, packet -> packet.animation().type() == SwingAnimation.SwingAnimationType.STAB ? 2 : 1)
            .then(PrimitiveCodecs.VAR_INT, packet -> packet.animation().duration());

    @Override
    public Key name() {
        return PlayClientboundPackets.SWING_ANIMATION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSwingAnimationPacket> codec() {
        return CODEC;
    }
}
