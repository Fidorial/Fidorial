package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.Collection;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Post_Effects
public record ClientboundPostEffectsPacket(Key name, Collection<Key> postEffects) implements ClientboundPacket<ClientboundPostEffectsPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPostEffectsPacket> CODEC =
            NetworkEncoder.field(StringLikeCodecs.KEY_ARRAY, packet -> packet.postEffects().toArray(Key[]::new));

    @Override
    public Key name() {
        return name;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPostEffectsPacket> codec() {
        return CODEC;
    }
}
