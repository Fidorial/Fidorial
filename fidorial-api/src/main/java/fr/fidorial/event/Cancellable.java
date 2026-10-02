package fr.fidorial.event;

/**
 * An {@link Event} whose outcome listeners can veto.
 *
 * <p>Listeners keep being called once an event is cancelled, so a later listener may restore it;
 * the server only checks the flag after every listener has run.</p>
 *
 * @since 0.1.0
 */
public interface Cancellable {

    /**
     * {@return {@code true} if the action this event describes will not happen}
     *
     * @since 0.1.0
     */
    boolean isCancelled();

    /**
     * Prevents or re-allows the action this event describes.
     *
     * @param cancelled {@code true} to prevent it
     * @since 0.1.0
     */
    void setCancelled(boolean cancelled);
}
