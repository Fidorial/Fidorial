package fr.fidorial.event;

/**
 * The order in which listeners of the same event are called, from {@link #LOWEST} to
 * {@link #MONITOR}.
 *
 * <p>Listeners called later see the changes made by earlier ones, so the higher the priority,
 * the more say a listener has over the final outcome.</p>
 *
 * @since 0.1.0
 */
public enum EventPriority {
    /**
     * Called first; for listeners whose changes others may override.
     */
    LOWEST,
    /**
     * Called after {@link #LOWEST}.
     */
    LOW,
    /**
     * The default priority.
     */
    NORMAL,
    /**
     * Called after {@link #NORMAL}.
     */
    HIGH,
    /**
     * Called last among the listeners allowed to change the event.
     */
    HIGHEST,
    /**
     * Called at the very end, to observe the final outcome; such listeners must not change the event.
     */
    MONITOR
}
