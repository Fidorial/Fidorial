package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DebugSubscription;
import org.jspecify.annotations.Nullable;

public record DebugChannel<T>(TypedKey<DebugSubscription> key, int networkId, Scope scope, @Nullable NetworkCodec<PacketBuffer, T> codec) {

    public enum Scope {
        ENTITY,
        CHUNK,
        BLOCK,
        EVENT,
        SAMPLE
    }

    public DebugChannel {
        if (networkId < 0 || networkId >= Integer.SIZE) {
            throw new IllegalArgumentException(key.key().asString() + " has id " + networkId + ", which does not fit a subscription mask");
        }
        if ((codec == null) != (scope == Scope.SAMPLE)) {
            throw new IllegalArgumentException(key.key().asString() + ": only sample channels carry no value codec");
        }
    }

    public int bit() {
        return 1 << networkId;
    }

    public NetworkCodec<PacketBuffer, T> valueCodec() {
        if (codec == null) {
            throw new IllegalStateException(key.key().asString() + " carries no value");
        }
        return codec;
    }
}
