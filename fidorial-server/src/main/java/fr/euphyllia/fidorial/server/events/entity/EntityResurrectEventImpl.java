package fr.euphyllia.fidorial.server.events.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.entity.EntityResurrectEvent;
import fr.fidorial.item.ItemStack;

public final class EntityResurrectEventImpl implements EntityResurrectEvent {

    private final LivingEntity entity;
    private final DamageSource source;
    private final ItemStack item;
    private boolean cancelled;

    public EntityResurrectEventImpl(final LivingEntity entity, final DamageSource source, final ItemStack item) {
        Preconditions.checkArgument(entity != null, "The entity being saved must not be null");
        Preconditions.checkArgument(source != null, "The lethal source must not be null");
        Preconditions.checkArgument(item != null, "The item must not be null");
        this.entity = entity;
        this.source = source;
        this.item = item;
    }

    @Override
    public LivingEntity entity() {
        return entity;
    }

    @Override
    public DamageSource source() {
        return source;
    }

    @Override
    public ItemStack item() {
        return item;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
