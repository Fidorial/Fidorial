package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record ClientboundSoundPacket(Sound sound, double x, double y, double z) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.SOUND;
    }

    @Override
    public void write(final PacketBuffer buf) {
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
