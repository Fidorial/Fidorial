package fr.euphyllia.fidorial.server.world.tick;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.redstone.TickPriority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ticks of one kind scheduled in one region, those of blocks or those of fluids.
 *
 * <p>Only the thread ticking the region touches it, but for the copy kept by chunk, which the
 * threads saving chunks read.</p>
 *
 * @param <T> a block type key, or a fluid
 */
final class TickQueue<T> {

    private final PriorityQueue<Scheduled<T>> queue = new PriorityQueue<>(Scheduled.DRAIN_ORDER);
    private final Set<Target<T>> pending = new HashSet<>();
    /**
     * The ticks taken to run in the current game tick that have not run yet.
     */
    private final Set<Target<T>> collected = new HashSet<>();
    private final Map<Long, Map<Target<T>, Scheduled<T>>> byChunk = new ConcurrentHashMap<>();

    /**
     * @return {@code false} if the same tick was already pending
     */
    boolean schedule(final BlockPosition position, final T type, final long trigger, final TickPriority priority,
                     final long order) {
        final Target<T> target = new Target<>(position, type);
        if (!pending.add(target)) {
            return false;
        }
        final Scheduled<T> tick = new Scheduled<>(position, type, trigger, priority, order);
        queue.add(tick);
        byChunk.computeIfAbsent(position.chunk().key(), _ -> new ConcurrentHashMap<>()).put(target, tick);
        return true;
    }

    /**
     * Takes the ticks due by a game time, at most {@code max} of them, in the order they run.
     */
    List<Scheduled<T>> takeDue(final long now, final int max) {
        final List<Scheduled<T>> due = new ArrayList<>();
        Scheduled<T> next;
        while (due.size() < max && (next = queue.peek()) != null && next.trigger() <= now) {
            queue.poll();
            final Target<T> target = next.target();
            pending.remove(target);
            byChunk.computeIfPresent(next.position().chunk().key(), (_, inChunk) -> {
                inChunk.remove(target);
                return inChunk.isEmpty() ? null : inChunk;
            });
            collected.add(target);
            due.add(next);
        }
        return due;
    }

    /**
     * Marks a tick taken by {@link #takeDue} as run, or about to.
     */
    void running(final Scheduled<T> tick) {
        collected.remove(tick.target());
    }

    /**
     * Ends the run of the current game tick.
     */
    void ranAll() {
        collected.clear();
    }

    boolean has(final BlockPosition position, final T type) {
        return pending.contains(new Target<>(position, type));
    }

    boolean willRunThisTick(final BlockPosition position, final T type) {
        return collected.contains(new Target<>(position, type));
    }

    /**
     * {@return the ticks pending in a chunk, in the order they were scheduled, each due as many game
     * ticks later as it is now}. Safe from any thread.
     */
    List<PendingTick<T>> pendingIn(final long chunk, final long now) {
        final Map<Target<T>, Scheduled<T>> inChunk = byChunk.get(chunk);
        if (inChunk == null || inChunk.isEmpty()) {
            return List.of();
        }
        final List<Scheduled<T>> ticks = new ArrayList<>(inChunk.values());
        ticks.sort(Comparator.comparingLong(Scheduled::order));
        final List<PendingTick<T>> pendingTicks = new ArrayList<>(ticks.size());
        for (final Scheduled<T> tick : ticks) {
            final int delay = Math.clamp(tick.trigger() - now, Integer.MIN_VALUE, Integer.MAX_VALUE);
            pendingTicks.add(new PendingTick<>(tick.position(), tick.type(), delay, tick.priority()));
        }
        return pendingTicks;
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }

    void clear() {
        queue.clear();
        pending.clear();
        collected.clear();
        byChunk.clear();
    }

    private record Target<T>(BlockPosition position, T type) {
    }

    /**
     * A tick waiting in the queue.
     *
     * @param trigger the game time it is due at
     * @param order   the order it was scheduled in, among the ticks of its region
     */
    record Scheduled<T>(BlockPosition position, T type, long trigger, TickPriority priority, long order) {

        static final Comparator<Scheduled<?>> DRAIN_ORDER = Comparator
                .<Scheduled<?>>comparingLong(Scheduled::trigger)
                .thenComparing(Scheduled::priority)
                .thenComparingLong(Scheduled::order);

        Target<T> target() {
            return new Target<>(position, type);
        }
    }
}
