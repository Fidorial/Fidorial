package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record ClientboundSoundPacket(Sound sound, double x, double y, double z) implements ClientboundPacket<ClientboundSoundPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundPacket> CODEC = NetworkEncoder.member(ClientboundSoundPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.SOUND;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        SoundEventWriter.writeSoundEvent(buf, sound);
        buf.writeVarInt(SoundEventWriter.sourceId(sound.source()));
        buf.writeInt((int) (x * 8.0));
        buf.writeInt((int) (y * 8.0));
        buf.writeInt((int) (z * 8.0));
        buf.writeFloat(sound.volume());
        buf.writeFloat(sound.pitch());
        buf.writeLong(SoundEventWriter.seed(sound));
    }
}
