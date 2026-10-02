package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.entity.EntityTracker;
import fr.euphyllia.fidorial.server.entity.ai.Navigation;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDebugValuePacket;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class EntityDebugSynchronizer {

    private static final List<Tie<?>> TIES = List.of(
            new Tie<>(DebugChannels.GOAL_SELECTORS, mob -> mob.goalSelector().debugSnapshot()),
            new Tie<>(DebugChannels.ENTITY_PATHS, mob -> mob.navigation() instanceof final Navigation navigation ? navigation.debugSnapshot() : null),
            new Tie<>(DebugChannels.BRAINS, AbstractMovingMob::brainSnapshot));

    private final DebugSubscribers subscribers;
    private final EntityTracker tracker;

    public EntityDebugSynchronizer(final DebugSubscribers subscribers, final EntityTracker tracker) {
        this.subscribers = subscribers;
        this.tracker = tracker;
    }

    public void tick(final AbstractMovingMob mob) {
        tickEntityValues(mob);
        tickBlockIntersections(mob);
    }

    public void sendCurrent(final AbstractMovingMob mob, final ClientConnection viewer) {
        for (final EntityDebugState.Sent<?> sent : mob.debugState().sent().values()) {
            if (DebugSubscribers.wants(viewer, sent.channel())) {
                viewer.send(sent.packet(mob.entityId()));
            }
        }
    }

    private void tickEntityValues(final AbstractMovingMob mob) {
        final EntityDebugState state = mob.debugState();
        final Map<DebugChannel<?>, EntityDebugState.Sent<?>> previous = state.sent();
        if (previous.isEmpty() && !anyProbeActive()) {
            return;
        }
        final boolean resync = state.advanceEpoch(subscribers.epoch());
        final Map<DebugChannel<?>, EntityDebugState.Sent<?>> next = new HashMap<>();
        boolean changed = false;
        for (final Tie<?> tie : TIES) {
            changed |= sync(mob, tie, previous, next, resync);
        }
        if (changed) {
            state.publish(next);
        }
    }

    private <T> boolean sync(final AbstractMovingMob mob, final Tie<T> tie, final Map<DebugChannel<?>, EntityDebugState.Sent<?>> previous, final Map<DebugChannel<?>, EntityDebugState.Sent<?>> next, final boolean resync) {
        final DebugChannel<T> channel = tie.channel();
        final EntityDebugState.Sent<?> before = previous.get(channel);
        if (!subscribers.isActive(channel)) {
            return before != null;
        }
        final T now = tie.snapshot().apply(mob);
        if (now != null) {
            next.put(channel, new EntityDebugState.Sent<>(channel, now));
        }
        final boolean differs = !Objects.equals(before == null ? null : before.value(), now);
        if ((differs || resync) && (before != null || now != null)) {
            tracker.sendToViewers(mob, ClientboundDebugValuePacket.entity(mob.entityId(), channel, now),
                    connection -> DebugSubscribers.wants(connection, channel));
        }
        return differs;
    }

    private void tickBlockIntersections(final AbstractMovingMob mob) {
        final DebugChannel<DebugValues.BlockIntersection> channel = DebugChannels.ENTITY_BLOCK_INTERSECTIONS;
        if (!subscribers.isActive(channel) || tracker.viewerCount(mob) == 0) {
            return;
        }
        mob.forEachIntersectedBlock((pos, intersection) -> tracker.sendToViewers(
                mob,
                ClientboundDebugValuePacket.block(pos, channel, intersection),
                connection -> DebugSubscribers.wants(connection, channel)));
    }

    private boolean anyProbeActive() {
        for (final Tie<?> tie : TIES) {
            if (subscribers.isActive(tie.channel())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ties a channel to the region-thread snapshot that produces its value.
     */
    private record Tie<T>(DebugChannel<T> channel, Function<AbstractMovingMob, @Nullable T> snapshot) {
    }
}
