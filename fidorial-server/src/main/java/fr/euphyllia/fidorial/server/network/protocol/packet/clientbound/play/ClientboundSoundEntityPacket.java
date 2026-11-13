package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record ClientboundSoundEntityPacket(Sound sound, int entityId) implements ClientboundPacket<ClientboundSoundEntityPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundEntityPacket> CODEC = NetworkEncoder
            .field(SoundEventWriter.SOUND_EVENT, (final ClientboundSoundEntityPacket packet) -> packet.sound.name())
            .then(PrimitiveCodecs.VAR_INT, packet -> SoundEventWriter.sourceId(packet.sound().source()))
            .then(PrimitiveCodecs.VAR_INT, ClientboundSoundEntityPacket::entityId)
            .then(PrimitiveCodecs.FLOAT, packet -> packet.sound().volume())
            .then(PrimitiveCodecs.FLOAT, packet -> packet.sound().pitch())
            .then(PrimitiveCodecs.LONG, packet -> SoundEventWriter.seed(packet.sound()));

    @Override
    public Key name() {
        return PlayClientboundPackets.SOUND_ENTITY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundEntityPacket> codec() {
        return CODEC;
    }
}
