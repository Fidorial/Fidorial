package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record ClientboundSoundPacket(Sound sound, double x, double y, double z) implements ClientboundPacket<ClientboundSoundPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundPacket> CODEC = NetworkEncoder
            .field(SoundEventWriter.SOUND_EVENT, (final ClientboundSoundPacket packet) -> packet.sound.name())
            .then(PrimitiveCodecs.VAR_INT, packet -> SoundEventWriter.sourceId(packet.sound().source()))
            .then(PrimitiveCodecs.INT, packet -> (int) (packet.x() * 8.0))
            .then(PrimitiveCodecs.INT, packet -> (int) (packet.y() * 8.0))
            .then(PrimitiveCodecs.INT, packet -> (int) (packet.z() * 8.0))
            .then(PrimitiveCodecs.FLOAT, packet -> packet.sound().volume())
            .then(PrimitiveCodecs.FLOAT, packet -> packet.sound().pitch())
            .then(PrimitiveCodecs.LONG, packet -> SoundEventWriter.seed(packet.sound()));

    @Override
    public Key name() {
        return PlayClientboundPackets.SOUND;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundPacket> codec() {
        return CODEC;
    }
}
