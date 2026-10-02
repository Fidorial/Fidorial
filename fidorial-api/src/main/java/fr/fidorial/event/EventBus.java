package fr.fidorial.event;

import fr.fidorial.plugin.PluginContext;

import java.util.function.Consumer;

/**
 * Dispatches {@linkplain Event events} to the listeners subscribed to them.
 *
 * <p>Events are delivered synchronously, on the thread that {@linkplain #post(Event) posts} them,
 * usually the region thread owning the block or entity involved. A listener that throws is logged
 * and does not prevent the other listeners from running.</p>
 *
 * <p>Every subscription has an owner, released all at once by {@link #unsubscribeAll(Object)}. The
 * bus returned by {@link PluginContext#events()} is bound to its plugin: what is
 * subscribed through it is released when the plugin is disabled, whatever the thread it was
 * subscribed from.</p>
 *
 * @since 0.1.0
 */
public interface EventBus {

    /**
     * Subscribes a listener at {@link EventPriority#NORMAL} priority.
     *
     * @param type     the event type to observe; subtypes are delivered too
     * @param listener the listener to call
     * @param <E>      the event type
     * @return the handle of the subscription
     * @since 0.1.0
     */
    default <E extends Event> Subscription subscribe(final Class<E> type, final Consumer<E> listener) {
        return subscribe(type, EventPriority.NORMAL, listener);
    }

    /**
     * Subscribes a listener.
     *
     * @param type     the event type to observe; subtypes are delivered too
     * @param priority when the listener is called relative to the others
     * @param listener the listener to call
     * @param <E>      the event type
     * @return the handle of the subscription
     * @since 0.1.0
     */
    <E extends Event> Subscription subscribe(Class<E> type, EventPriority priority, Consumer<E> listener);

    /**
     * Delivers an event to every matching listener, in priority order, on the calling thread.
     *
     * @param event the event to deliver
     * @param <E>   the event type
     * @return the same event, as left by the listeners
     * @since 0.1.0
     */
    <E extends Event> E post(E event);

    /**
     * Unsubscribes every listener owned by {@code owner}.
     *
     * @param owner the owner of the subscriptions, usually a {@link fr.fidorial.plugin.Plugin}
     * @since 0.1.0
     */
    void unsubscribeAll(Object owner);
}
