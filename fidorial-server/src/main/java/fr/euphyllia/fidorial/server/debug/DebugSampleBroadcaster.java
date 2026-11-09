package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDebugSamplePacket;
import fr.euphyllia.fidorial.server.schedulers.RegionTickProfiler;
import fr.euphyllia.fidorial.server.schedulers.TickRegion;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class DebugSampleBroadcaster implements RegionTickProfiler {

    private static final long TICK_INTERVAL_NS = TimeUnit.MILLISECONDS.toNanos(50L);

    private final DebugSubscribers subscribers;
    private final Supplier<? extends Collection<ServerPlayer>> players;

    public DebugSampleBroadcaster(final DebugSubscribers subscribers, final Supplier<? extends Collection<ServerPlayer>> players) {
        this.subscribers = subscribers;
        this.players = players;
    }

    @Override
    public void heartbeat() {
    }

    @Override
    public void reportRegionTick(final double durationMillis) {
    }

    @Override
    public void reportRegionTick(final TickRegion region, final long tickNanos, final long taskNanos) {
        final DebugChannel<Void> channel = DebugChannels.DEDICATED_SERVER_TICK_TIME;
        if (!subscribers.isActive(channel)) {
            return;
        }
        ClientboundDebugSamplePacket packet = null;
        for (final ServerPlayer player : players.get()) {
            if (!DebugSubscribers.wants(player, channel)
                    || !player.world().key().equals(region.world())
                    || !region.owns(player.chunk())) {
                continue;
            }
            if (packet == null) {
                final long idle = Math.max(0L, TICK_INTERVAL_NS - tickNanos - taskNanos);
                packet = new ClientboundDebugSamplePacket(tickNanos + taskNanos + idle, tickNanos, taskNanos, idle);
            }
            player.connection().send(packet);
        }
    }
}
