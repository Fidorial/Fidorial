package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.permission.DefaultPermissions;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Server-wide view of who is subscribed to what.
 */
public final class DebugSubscribers {

    private final Supplier<? extends Collection<ServerPlayer>> players;
    private final AtomicLong epoch = new AtomicLong();
    private volatile int active;

    public DebugSubscribers(final Supplier<? extends Collection<ServerPlayer>> players) {
        this.players = players;
    }

    public int update(final ServerPlayer player, final int requested) {
        final boolean permitted = isPermitted(player);
        return apply(player.updateDebugSubscriptions(state -> state.withRequested(requested, permitted)));
    }

    public int refresh(final ServerPlayer player) {
        final boolean permitted = isPermitted(player);
        return apply(player.updateDebugSubscriptions(state -> state.withPermission(permitted)));
    }

    public void clear(final ServerPlayer player) {
        player.updateDebugSubscriptions(_ -> DebugSubscriptionState.EMPTY);
        recompute();
    }

    public synchronized void recompute() {
        int union = 0;
        for (final ServerPlayer player : players.get()) {
            union |= player.debugSubscriptions().effective();
        }
        active = union;
    }

    public boolean isActive(final DebugChannel<?> channel) {
        return (active & channel.bit()) != 0;
    }

    public long epoch() {
        return epoch.get();
    }

    public static boolean wants(final ClientConnection connection, final DebugChannel<?> channel) {
        final ServerPlayer player = connection.player();
        return player != null && connection.isInPlayState() && player.debugSubscriptions().wants(channel);
    }

    public static boolean wants(final ServerPlayer player, final DebugChannel<?> channel) {
        return player.connection().isInPlayState() && player.debugSubscriptions().wants(channel);
    }

    private static boolean isPermitted(final ServerPlayer player) {
        return player.hasPermission(DefaultPermissions.DEBUG_SUBSCRIPTIONS);
    }

    private int apply(final DebugSubscriptionState.Transition transition) {
        recompute();
        final int gained = transition.gained();
        if (gained != 0) {
            epoch.incrementAndGet();
        }
        return gained;
    }
}
