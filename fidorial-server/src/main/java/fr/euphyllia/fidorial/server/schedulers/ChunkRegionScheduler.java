package fr.euphyllia.fidorial.server.schedulers;

import ca.spottedleaf.concurrentutil.executor.PrioritisedExecutor;
import ca.spottedleaf.concurrentutil.util.Priority;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ChunkRegionScheduler {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChunkRegionScheduler.class);
    private final LongOpenHashSet locked = new LongOpenHashSet(2048);
    private final Long2ObjectOpenHashMap<@Nullable List<PendingTask>> waitingOn = new Long2ObjectOpenHashMap<>(512);
    private final PrioritisedExecutor executor;

    public ChunkRegionScheduler(final PrioritisedExecutor executor) {
        this.executor = executor;
    }

    public void submit(final LongSet keys, final Runnable task, final Priority priority) {
        final PendingTask pending = new PendingTask(new LongOpenHashSet(keys), task, priority);
        final boolean ready;
        synchronized (this) {
            ready = lockOrWait(pending);
        }
        if (ready) {
            dispatch(pending);
        }
    }

    private boolean lockOrWait(final PendingTask pending) {
        int blockers = 0;
        final LongIterator it = pending.keys.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            if (locked.contains(key)) {
                blockers++;
                waitingOn.computeIfAbsent(key, _ -> new ArrayList<>(2)).add(pending);
            }
        }
        if (blockers == 0) {
            locked.addAll(pending.keys);
            return true;
        }
        pending.remainingBlockers = blockers;
        return false;
    }

    private void release(final PendingTask finished) {
        List<PendingTask> started = null;
        synchronized (this) {
            locked.removeAll(finished.keys);

            List<PendingTask> unblocked = null;
            final LongIterator it = finished.keys.iterator();
            while (it.hasNext()) {
                final List<PendingTask> waiters = waitingOn.remove(it.nextLong());
                if (waiters == null) {
                    continue;
                }
                for (final PendingTask waiter : waiters) {
                    if (--waiter.remainingBlockers == 0) {
                        if (unblocked == null) unblocked = new ArrayList<>();
                        unblocked.add(waiter);
                    }
                }
            }
            if (unblocked == null) return;

            for (final PendingTask waiter : unblocked) {
                if (lockOrWait(waiter)) {
                    if (started == null) started = new ArrayList<>();
                    started.add(waiter);
                }
            }
        }
        if (started == null) return;

        for (final PendingTask task : started) {
            try {
                dispatch(task);
            } catch (final RuntimeException e) {
                LOGGER.error("Failed to start a pending region task during release", e);
            }
        }
    }

    private void dispatch(final PendingTask pending) {
        final Runnable run = () -> {
            try {
                pending.task.run();
            } finally {
                release(pending);
            }
        };
        try {
            executor.queueTask(run, pending.priority);
        } catch (final IllegalStateException e) {
            // executor shut down
            run.run();
        }
    }

    private static final class PendingTask {
        final LongOpenHashSet keys;
        final Runnable task;
        final Priority priority;
        int remainingBlockers;

        PendingTask(final LongOpenHashSet keys, final Runnable task, final Priority priority) {
            this.keys = keys;
            this.task = task;
            this.priority = priority;
        }
    }
}
