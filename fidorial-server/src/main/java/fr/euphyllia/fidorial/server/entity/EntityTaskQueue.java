package fr.euphyllia.fidorial.server.entity;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import fr.fidorial.math.Location;
import fr.fidorial.scheduler.RegionizedScheduler;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.concurrent.atomic.AtomicBoolean;

/// The tasks waiting for an entity: run in the order they were scheduled, on the region holding the
/// entity when their turn comes, or retired once the entity is removed.
final class EntityTaskQueue {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EntityTaskQueue.class);

    private static final int MAX_TASKS_PER_DRAIN = 256;

    private final AbstractEntity entity;
    private final MultiThreadedQueue<QueuedTask> tasks = new MultiThreadedQueue<>();
    private final AtomicBoolean draining = new AtomicBoolean();

    private record QueuedTask(Runnable run, Runnable retired) {
    }

    EntityTaskQueue(final AbstractEntity entity) {
        this.entity = entity;
    }

    boolean execute(final Runnable task, final Runnable retired) {
        if (entity.isRemoved()) {
            return false;
        }
        tasks.add(new QueuedTask(task, retired));
        if (entity.isRemoved()) {
            retireAll(); // removed while the task was being added
        } else if (draining.compareAndSet(false, true)) {
            chaseDrain();
        }
        return true;
    }

    boolean executeDelayed(final Runnable task, final Runnable retired, final long delayTicks) {
        if (entity.isRemoved()) {
            return false;
        }
        final Location at = entity.location();
        return scheduler().executeDelayed(at.world().key(), at.chunk(), () -> {
            if (!execute(task, retired)) {
                retire(retired);
            }
        }, delayTicks);
    }

    /// Retires every task still waiting. Called once the entity is removed.
    void retireAll() {
        QueuedTask next;
        while ((next = tasks.poll()) != null) {
            retire(next.retired());
        }
    }

    /// Queues [#drain()] on the region holding the entity. Only called by whoever holds `draining`.
    private void chaseDrain() {
        final Location at = entity.location();
        if (!scheduler().execute(at.world().key(), at.chunk(), this::drain)) {
            draining.set(false);
            retireAll();
        }
    }

    /// Runs the queued tasks in order, for as long as the current thread owns the entity.
    private void drain() {
        final RegionizedScheduler scheduler = scheduler();
        for (int ran = 0; ; ran++) {
            if (entity.isRemoved()) {
                draining.set(false);
                retireAll();
                return;
            }
            final Location at = entity.location();
            if (ran >= MAX_TASKS_PER_DRAIN || !scheduler.isOwnedByCurrentThread(at.world().key(), at.chunk())) {
                chaseDrain(); // the entity moved, or others on this region are due their turn
                return;
            }
            final QueuedTask next = tasks.poll();
            if (next == null) {
                draining.set(false);
                if (tasks.isEmpty() || !draining.compareAndSet(false, true)) {
                    return;
                }
                continue;
            }
            try {
                next.run().run();
            } catch (final Throwable throwable) {
                LOGGER.error("Error in a task of {}", entity, throwable);
            }
        }
    }

    private void retire(final Runnable retired) {
        try {
            retired.run();
        } catch (final Throwable throwable) {
            LOGGER.error("Error in a retired callback of {}", entity, throwable);
        }
    }

    private RegionizedScheduler scheduler() {
        return entity.server().scheduler();
    }
}
