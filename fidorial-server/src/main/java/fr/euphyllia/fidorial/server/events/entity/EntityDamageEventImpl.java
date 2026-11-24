package fr.euphyllia.fidorial.server.events.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

public final class EntityDamageEventImpl implements EntityDamageEvent {

    private final LivingEntity entity;
    private final DamageSource source;
    private float damage;
    private double knockback;
    private boolean cancelled;

    public EntityDamageEventImpl(final LivingEntity entity, final DamageSource source, final float damage, final double knockback) {
        Preconditions.checkArgument(entity != null, "The entity being hurt must not be null");
        Preconditions.checkArgument(source != null, "The source of the damage must not be null");
        this.entity = entity;
        this.source = source;
        this.damage = damage;
        this.knockback = knockback;
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
    public @Nullable Entity damager() {
        return source.causingEntity();
    }

    @Override
    public float damage() {
        return damage;
    }

    @Override
    public void setDamage(final float damage) {
        this.damage = damage;
    }

    @Override
    public double knockback() {
        return knockback;
    }

    @Override
    public void setKnockback(final double knockback) {
        this.knockback = knockback;
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
