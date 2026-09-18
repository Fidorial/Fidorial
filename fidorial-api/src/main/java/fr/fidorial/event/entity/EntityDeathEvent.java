package fr.fidorial.event.entity;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Entity;
import fr.fidorial.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * @since 0.1.0
 */
public class EntityDeathEvent implements Event {

    private final Entity entity;
    private final @Nullable Entity killer;

    public EntityDeathEvent(final Entity entity, final @Nullable Entity killer) {
        this.entity = entity;
        this.killer = killer;
    }

    /**
     * @return the entity credited with the kill, or {@code null} for environmental deaths
     */
    @ThreadContract("get -> any; modify -> owner")
    public @Nullable Entity killer() {
        return killer;
    }

    /**
     * @return the entity that died
     */
    @ThreadContract("get -> any; modify -> owner")
    public Entity entity() {
        return entity;
    }
}
