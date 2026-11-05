package fr.euphyllia.fidorial.server.network.serialization;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import io.netty.buffer.ByteBuf;

/**
 * A {@link PacketBuffer} that reads and writes through {@link NetworkCodec}s and carries the
 * registries, so codecs can send registry entries by network ID.
 */
public final class CodecAwarePacketBuffer extends PacketBuffer {

    private final ServerRegistries registries;

    public CodecAwarePacketBuffer(final ByteBuf buf, final ServerRegistries registries) {
        super(buf);
        this.registries = registries;
    }

    public ServerRegistries registries() {
        return registries;
    }

    /**
     * Accepts both plain {@code PacketBuffer} codecs and codecs that need this buffer.
     */
    public <T> T read(final NetworkCodec<? super CodecAwarePacketBuffer, T> codec) {
        return codec.read(this);
    }

    public <T> CodecAwarePacketBuffer write(final NetworkCodec<? super CodecAwarePacketBuffer, T> codec, final T value) {
        codec.write(this, value);
        return this;
    }
}
