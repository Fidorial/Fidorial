package fr.fidorial.event;

/**
 * The handle of a listener subscribed to an {@link EventBus}.
 *
 * <p>Closing it, for instance through try-with-resources, unsubscribes the listener.</p>
 *
 * @since 0.1.0
 */
public interface Subscription extends AutoCloseable {

    /**
     * {@return {@code true} until the listener is unsubscribed}
     *
     * @since 0.1.0
     */
    boolean isActive();

    /**
     * Unsubscribes the listener. Calling it again does nothing.
     *
     * @since 0.1.0
     */
    void unsubscribe();

    /**
     * Unsubscribes the listener, exactly like {@link #unsubscribe()}.
     *
     * @since 0.1.0
     */
    @Override
    default void close() {
        unsubscribe();
    }
}
