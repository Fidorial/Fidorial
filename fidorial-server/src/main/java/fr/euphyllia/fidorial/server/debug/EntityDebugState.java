package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDebugValuePacket;

import java.util.Map;

/**
 * Per-entity record of the debug values last sent to its viewers.
 */
public final class EntityDebugState {

    private volatile Map<DebugChannel<?>, Sent<?>> sent = Map.of();
    private long epoch = Long.MIN_VALUE;

    public Map<DebugChannel<?>, Sent<?>> sent() {
        return sent;
    }

    void publish(final Map<DebugChannel<?>, Sent<?>> next) {
        sent = Map.copyOf(next);
    }

    boolean advanceEpoch(final long current) {
        if (epoch == current) {
            return false;
        }
        epoch = current;
        return true;
    }

    public record Sent<T>(DebugChannel<T> channel, T value) {
        public ClientboundDebugValuePacket<T> packet(final int entityId) {
            return ClientboundDebugValuePacket.entity(entityId, channel, value);
        }
    }
}
