package fr.euphyllia.fidorial.server.events.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.event.entity.EntityDeathEvent;
import org.jspecify.annotations.Nullable;

public final class EntityDeathEventImpl implements EntityDeathEvent {

    private final Entity entity;
    private final @Nullable Entity killer;

    public EntityDeathEventImpl(final Entity entity, final @Nullable Entity killer) {
        Preconditions.checkArgument(entity != null, "The entity that died must not be null");
        this.entity = entity;
        this.killer = killer;
    }

    @Override
    public Entity entity() {
        return entity;
    }

    @Override
    public @Nullable Entity killer() {
        return killer;
    }
}
