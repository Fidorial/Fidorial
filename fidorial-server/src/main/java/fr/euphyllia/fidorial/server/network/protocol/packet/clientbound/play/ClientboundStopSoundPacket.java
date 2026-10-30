package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Nullable;

public record ClientboundStopSoundPacket(
        @Nullable Sound.Source source,
        @Nullable Key sound
) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.STOP_SOUND;
    }

    @Override
    public void write(final PacketBuffer buf) {
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
