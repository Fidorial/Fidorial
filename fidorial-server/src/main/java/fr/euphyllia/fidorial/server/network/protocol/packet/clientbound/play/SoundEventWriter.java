package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;

public class SoundEventWriter {

    /// A sound event sent inline by name, without a fixed range.
    static final NetworkEncoder<PacketBuffer, Key> SOUND_EVENT = NetworkEncoder
            .field(CommonNetworkCodecs.PrimitiveCodecs.VAR_INT, (Key _) -> 0)
            .then(CommonNetworkCodecs.StringLikeCodecs.KEY, name -> name)
            .then(CommonNetworkCodecs.PrimitiveCodecs.BOOL, _ -> false);

    private SoundEventWriter() {
    }

    static int sourceId(final Sound.Source source) {
        return source.ordinal();
    }

    static long seed(final Sound sound) {
        return sound.seed().orElseGet(() -> ThreadLocalRandom.current().nextLong());
    }
}
