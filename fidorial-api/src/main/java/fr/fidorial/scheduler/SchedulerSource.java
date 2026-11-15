package fr.fidorial.scheduler;

/// Something bound to a region of the world, on whose thread tasks can be scheduled: an
/// [entity][fr.fidorial.entity.Entity] or a [chunk][fr.fidorial.world.Chunk].
///
/// The tasks of an entity run in the order they were scheduled, on the region holding the entity
/// when they run, following it across regions.
///
/// A task that was scheduled either runs or is retired, never both: it is retired when its source
/// is removed, or its region can no longer run it, before the task's turn. The retired callback
/// may run on any thread.
///
/// @see RegionizedScheduler
/// @since 0.1.0
public interface SchedulerSource {

    /// Schedules a task to run as soon as possible on the region thread owning this source.
    ///
    /// @param task the task to run
    /// @return `false` if the source is removed and the task was not scheduled
    /// @since 0.1.0
    default boolean execute(final Runnable task) {
        return execute(task, () -> {
        });
    }

    /// Schedules a task to run as soon as possible on the region thread owning this source.
    ///
    /// @param task    the task to run
    /// @param retired called instead of the task if it can no longer run
    /// @return `false` if the source is removed: neither `task` nor `retired` will run
    /// @since 0.1.0
    boolean execute(Runnable task, Runnable retired);

    /// Schedules a task to run on the region thread owning this source, after a delay.
    ///
    /// @param task       the task to run
    /// @param delayTicks the minimum number of ticks to wait before running the task
    /// @return `false` if the source is removed or the task could not be scheduled
    /// @since 0.1.0
    default boolean executeDelayed(final Runnable task, final long delayTicks) {
        return executeDelayed(task, () -> {
        }, delayTicks);
    }

    /// Schedules a task to run on the region thread owning this source, after a delay.
    ///
    /// @param task       the task to run
    /// @param retired    called instead of the task if it can no longer run
    /// @param delayTicks the minimum number of ticks to wait before running the task
    /// @return `false` if the source is removed or the task could not be scheduled: neither `task`
    /// nor `retired` will run
    /// @since 0.1.0
    boolean executeDelayed(Runnable task, Runnable retired, long delayTicks);

    /// Checks whether the calling thread may safely touch this source.
    ///
    /// @return `true` if the calling thread is the region thread owning this source
    /// @since 0.1.0
    boolean isOwnedByCurrentThread();
}
