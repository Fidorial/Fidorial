package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public record ClientboundSoundEntityPacket(Sound sound, int entityId) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.SOUND_ENTITY;
    }

    @Override
    public void write(final PacketBuffer buf) {
        SoundEventWriter.writeSoundEvent(buf, sound);
        buf.writeVarInt(SoundEventWriter.sourceId(sound.source()));
        buf.writeVarInt(entityId);
        buf.writeFloat(sound.volume());
        buf.writeFloat(sound.pitch());
        buf.writeLong(SoundEventWriter.seed(sound));
    }
}
