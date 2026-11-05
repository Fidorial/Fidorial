package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Nullable;

public record ClientboundStopSoundPacket(
        @Nullable Sound.Source source,
        @Nullable Key sound
) implements ClientboundPacket<ClientboundStopSoundPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundStopSoundPacket> CODEC = NetworkEncoder.member(ClientboundStopSoundPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.STOP_SOUND;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundStopSoundPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        int flags = 0;
        if (source != null) {
            flags |= 0x01;
        }
        if (sound != null) {
            flags |= 0x02;
        }
        buf.writeByte(flags);
        if (source != null) {
            buf.writeVarInt(source.ordinal());
        }
        if (sound != null) {
            buf.writeKey(sound);
        }
    }
}
