package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record ClientboundSoundEntityPacket(Sound sound, int entityId) implements ClientboundPacket<ClientboundSoundEntityPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundEntityPacket> CODEC = NetworkEncoder.member(ClientboundSoundEntityPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.SOUND_ENTITY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSoundEntityPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        SoundEventWriter.writeSoundEvent(buf, sound);
        buf.writeVarInt(SoundEventWriter.sourceId(sound.source()));
        buf.writeVarInt(entityId);
        buf.writeFloat(sound.volume());
        buf.writeFloat(sound.pitch());
        buf.writeLong(SoundEventWriter.seed(sound));
    }
}
