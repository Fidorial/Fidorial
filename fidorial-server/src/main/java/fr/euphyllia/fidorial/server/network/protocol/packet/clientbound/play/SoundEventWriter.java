package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;

public class SoundEventWriter {

    private SoundEventWriter() {
    }

    static void writeSoundEvent(final PacketBuffer buf, final Sound sound) {
        buf.writeVarInt(0);
        buf.writeKey(sound.name());
        buf.writeBoolean(false);
    }

    static int sourceId(final Sound.Source source) {
        return source.ordinal();
    }

    static long seed(final Sound sound) {
        return sound.seed().orElseGet(() -> ThreadLocalRandom.current().nextLong());
    }
}
