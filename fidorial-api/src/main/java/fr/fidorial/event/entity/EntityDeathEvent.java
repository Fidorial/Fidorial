package fr.fidorial.event.entity;

import fr.fidorial.entity.Entity;
import fr.fidorial.event.Event;
import fr.fidorial.event.player.PlayerDeathEvent;
import org.jspecify.annotations.Nullable;

/**
 * Fired when an entity dies, once its health reached zero and before it is removed from the world.
 *
 * @see PlayerDeathEvent
 * @since 0.1.0
 */
public interface EntityDeathEvent extends Event {

    /**
     * {@return the entity that died}
     *
     * @since 0.1.0
     */
    Entity entity();

    /**
     * {@return the entity credited with the kill, or {@code null} for environmental deaths}
     *
     * @since 0.1.0
     */
    @Nullable Entity killer();
}
