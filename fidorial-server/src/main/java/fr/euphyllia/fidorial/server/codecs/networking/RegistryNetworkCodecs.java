package fr.euphyllia.fidorial.server.codecs.networking;

import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

/**
 * Codecs for registry entries, which need a {@link CodecAwarePacketBuffer}.
 */
public final class RegistryNetworkCodecs {

    private RegistryNetworkCodecs() {
        throw new UnsupportedOperationException("RegistryNetworkCodecs cannot be instantiated.");
    }

    /**
     * An entry of {@code registry} sent as its network ID.
     */
    public static <T> NetworkCodec<CodecAwarePacketBuffer, TypedKey<T>> id(final RegistryKey<T> registry) {
        return NetworkCodec.of(
                buf -> {
                    final int id = buf.readVarInt();
                    final TypedKey<T> key = registryOf(buf, registry).byNetworkId(id);
                    if (key == null) {
                        throw new DecoderException("Unknown " + registry.key().asString() + " network ID " + id);
                    }
                    return key;
                },
                (buf, key) -> {
                    final int id = registryOf(buf, registry).networkId(key);
                    if (id < 0) {
                        throw new EncoderException(key.key().asString() + " is not in " + registry.key().asString());
                    }
                    buf.writeVarInt(id);
                });
    }

    private static <T> ServerRegistry<T> registryOf(final CodecAwarePacketBuffer buf, final RegistryKey<T> registry) {
        return buf.registries().registry(registry);
    }
}
