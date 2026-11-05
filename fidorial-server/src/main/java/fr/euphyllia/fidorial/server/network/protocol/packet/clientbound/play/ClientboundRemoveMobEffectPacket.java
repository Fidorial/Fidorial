package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Remove_Entity_Effect
public record ClientboundRemoveMobEffectPacket(int entityId, int effectId) implements ClientboundPacket<ClientboundRemoveMobEffectPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundRemoveMobEffectPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundRemoveMobEffectPacket::entityId)
            .then(PrimitiveCodecs.VAR_INT, ClientboundRemoveMobEffectPacket::effectId);

    @Override
    public Key name() {
        return PlayClientboundPackets.REMOVE_MOB_EFFECT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundRemoveMobEffectPacket> codec() {
        return CODEC;
    }
}
