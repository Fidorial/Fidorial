package fr.fidorial.scheduler;

/**
 * Something bound to a region of the world, on whose thread tasks can be scheduled: an
 * {@linkplain fr.fidorial.entity.Entity entity} or a {@linkplain fr.fidorial.world.Chunk chunk}.
 *
 * <p>The target region is resolved when the task is scheduled: a task scheduled for an entity runs
 * on the region that held the entity at that moment.</p>
 *
 * @see RegionizedScheduler
 * @since 0.1.0
 */
public interface SchedulerSource {

    /**
     * Schedules a task to run as soon as possible on the region thread owning this source.
     *
     * @param task the task to run
     * @return {@code false} if the source is removed and the task was not scheduled
     * @since 0.1.0
     */
    boolean execute(Runnable task);

    /**
     * Schedules a task to run on the region thread owning this source, after a delay.
     *
     * @param task       the task to run
     * @param delayTicks the minimum number of ticks to wait before running the task
     * @return {@code false} if the source is removed and the task was not scheduled
     * @since 0.1.0
     */
    boolean executeDelayed(Runnable task, long delayTicks);

    /**
     * Checks whether the calling thread may safely touch this source.
     *
     * @return {@code true} if the calling thread is the region thread owning this source
     * @since 0.1.0
     */
    boolean isOwnedByCurrentThread();
}
