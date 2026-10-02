package fr.fidorial.event.entity;

import fr.fidorial.entity.Entity;
import fr.fidorial.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * Fired when an entity dies, once its health reached zero and before it is removed from the world.
 *
 * @see fr.fidorial.event.player.PlayerDeathEvent
 * @since 0.1.0
 */
public class EntityDeathEvent implements Event {

    private final Entity entity;
    private final @Nullable Entity killer;

    /**
     * Creates an event.
     *
     * @param entity the entity that died
     * @param killer the entity credited with the kill, or {@code null} for environmental deaths
     * @since 0.1.0
     */
    public EntityDeathEvent(final Entity entity, final @Nullable Entity killer) {
        this.entity = entity;
        this.killer = killer;
    }

    /**
     * @return the entity credited with the kill, or {@code null} for environmental deaths
     */
    public @Nullable Entity killer() {
        return killer;
    }

    /**
     * @return the entity that died
     */
    public Entity entity() {
        return entity;
    }
}
