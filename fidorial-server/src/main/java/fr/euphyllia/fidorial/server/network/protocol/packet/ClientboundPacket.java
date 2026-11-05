package fr.euphyllia.fidorial.server.network.protocol.packet;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * @param <P> the packet type itself
 */
public interface ClientboundPacket<P extends ClientboundPacket<P>> {

    Key name();

    NetworkEncoder<? super CodecAwarePacketBuffer, P> codec();

    @SuppressWarnings("unchecked")
    default void write(final CodecAwarePacketBuffer buf) {
        codec().write(buf, (P) this);
    }
}
