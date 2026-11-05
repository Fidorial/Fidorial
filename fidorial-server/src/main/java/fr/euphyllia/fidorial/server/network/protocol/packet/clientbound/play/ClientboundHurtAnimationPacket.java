package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Hurt_Animation
public record ClientboundHurtAnimationPacket(int entityId, float yaw) implements ClientboundPacket<ClientboundHurtAnimationPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundHurtAnimationPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundHurtAnimationPacket::entityId)
            .then(PrimitiveCodecs.FLOAT, ClientboundHurtAnimationPacket::yaw);

    @Override
    public Key name() {
        return PlayClientboundPackets.HURT_ANIMATION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundHurtAnimationPacket> codec() {
        return CODEC;
    }
}
