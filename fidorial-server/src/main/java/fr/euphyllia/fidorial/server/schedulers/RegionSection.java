package fr.euphyllia.fidorial.server.schedulers;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import ca.spottedleaf.concurrentutil.executor.queue.PrioritisedTaskQueue;
import ca.spottedleaf.concurrentutil.util.Priority;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

/**
 * One square of the grid a world is cut into, {@code 2^sectionShift} chunks wide, owned by one
 * {@link TickRegion} at a time.
 *
 * <p>A section carries what must follow it when it changes region, its task queues and its tick
 * counter, so a merge or a split only changes the owner. The rest of the per-section state of the
 * server (entities, scheduled block ticks) is keyed by section coordinates and never moves.</p>
 *
 * <p>A section is <em>occupied</em> while it holds loaded chunks or tickets, and <em>alive</em> while
 * it is occupied or within the buffer radius of an occupied section. A section that is not alive is
 * <em>dead</em>: its region drops it once it has no task left.</p>
 */
final class RegionSection {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(RegionSection.class);

    private static final int MAX_IMMEDIATE_PER_TICK = 4096;

    final int x;
    final int z;
    final long key;

    /**
     * Loaded chunks and tickets, plus one for {@link #taskHold}. Only crosses zero under the world lock.
     */
    private final AtomicInteger holds = new AtomicInteger();

    /**
     * Occupied sections within the buffer radius, this one excluded. Guarded by the world lock.
     */
    int occupiedNeighbours;

    /**
     * Whether a hold was taken for tasks sent where no section existed. Guarded by the world lock.
     */
    boolean taskHold;

    /**
     * The owner, written under the world lock.
     */
    volatile TickRegion region;

    private final PrioritisedTaskQueue immediate = new PrioritisedTaskQueue();
    private final MultiThreadedQueue<DelayedTask> incomingDelayed = new MultiThreadedQueue<>();
    private final AtomicInteger pendingDelayed = new AtomicInteger();
    private final AtomicLong sequence = new AtomicLong();

    // confined to the thread ticking the owner
    private final PriorityQueue<ScheduledDelayedTask> delayed = new PriorityQueue<>(ScheduledDelayedTask.ORDER);
    long currentTick;

    private boolean removed; // guarded by this

    RegionSection(final int x, final int z, final TickRegion region) {
        this.x = x;
        this.z = z;
        this.key = ChunkPos.chunkKey(x, z);
        this.region = region;
    }

    int holds() {
        return holds.get();
    }

    /**
     * Adds a hold unless the section is unoccupied: going from zero needs the world lock.
     */
    boolean tryHoldWhileOccupied() {
        for (int current = holds.get(); current > 0; current = holds.get()) {
            if (holds.compareAndSet(current, current + 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a hold unless it is the last one: going to zero needs the world lock.
     */
    boolean tryReleaseUnlessLast() {
        for (int current = holds.get(); current > 1; current = holds.get()) {
            if (holds.compareAndSet(current, current - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return the holds before this one} Under the world lock.
     */
    int hold() {
        return holds.getAndIncrement();
    }

    /**
     * {@return the holds left, or {@code -1} if there was none} Under the world lock.
     */
    int release() {
        return holds.get() == 0 ? -1 : holds.decrementAndGet();
    }

    /**
     * {@return whether the section is occupied or next to an occupied one} Under the world lock.
     */
    boolean isAlive() {
        return holds.get() > 0 || occupiedNeighbours > 0;
    }

    /**
     * Queues a task.
     *
     * @return {@code false} if the section was dropped by its region: look it up again
     */
    boolean offer(final Runnable task, final long delayTicks) {
        synchronized (this) {
            if (removed) {
                return false;
            }
            if (delayTicks <= 0L) {
                immediate.queueTask(task, Priority.NORMAL);
            } else {
                pendingDelayed.incrementAndGet();
                incomingDelayed.add(new DelayedTask(task, delayTicks, sequence.getAndIncrement()));
            }
            return true;
        }
    }

    /**
     * Marks the section as dropped, unless tasks are still queued on it. Called by the thread
     * releasing the owner, under the world lock.
     */
    boolean tryRemove() {
        synchronized (this) {
            if (hasPendingTasks()) {
                return false;
            }
            removed = true;
            return true;
        }
    }

    boolean hasImmediateTasks() {
        return !immediate.hasNoScheduledTasks();
    }

    /**
     * Only exact on the thread ticking the owner.
     */
    boolean hasPendingTasks() {
        return hasImmediateTasks() || pendingDelayed.get() > 0 || !delayed.isEmpty();
    }

    int queuedTasks() {
        final long queued = immediate.getTotalTasksScheduled() - immediate.getTotalTasksExecuted();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, queued) + pendingDelayed.get() + delayed.size());
    }

    /**
     * Counts one more tick and runs the tasks due. On the thread ticking the owner.
     */
    void runDueTasks() {
        currentTick++;
        for (int i = 0; i < MAX_IMMEDIATE_PER_TICK && runImmediate(); i++) {
            // drain
        }

        DelayedTask incoming;
        while ((incoming = incomingDelayed.poll()) != null) {
            pendingDelayed.decrementAndGet();
            delayed.add(new ScheduledDelayedTask(incoming.task(), currentTick + incoming.delayTicks(), incoming.sequence()));
        }
        ScheduledDelayedTask due;
        while ((due = delayed.peek()) != null && due.executeAtTick() <= currentTick) {
            delayed.poll();
            run(due.task());
        }
    }

    /**
     * Runs the queued tasks between two ticks, while {@code canContinue} allows it.
     *
     * @return {@code false} if {@code canContinue} stopped it
     */
    boolean runTasks(final BooleanSupplier canContinue) {
        while (runImmediate()) {
            if (!canContinue.getAsBoolean()) {
                return false;
            }
        }
        return true;
    }

    private boolean runImmediate() {
        try {
            return immediate.executeTask();
        } catch (final Throwable ex) {
            LOGGER.error("Error in a task of section {},{}", x, z, ex);
            return true;
        }
    }

    private void run(final Runnable task) {
        try {
            task.run();
        } catch (final Throwable ex) {
            LOGGER.error("Error in a task of section {},{}", x, z, ex);
        }
    }

    @Override
    public String toString() {
        return "RegionSection{" + x + "," + z + ",holds=" + holds.get() + ",region=" + region.id + "}";
    }

    private record DelayedTask(Runnable task, long delayTicks, long sequence) {
    }

    private record ScheduledDelayedTask(Runnable task, long executeAtTick, long sequence) {
        static final Comparator<ScheduledDelayedTask> ORDER = Comparator
                .comparingLong(ScheduledDelayedTask::executeAtTick)
                .thenComparingLong(ScheduledDelayedTask::sequence);
    }
}
