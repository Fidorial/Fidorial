package fr.fidorial.event.entity;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * Fired before an entity takes damage, once invulnerability frames have been cleared but before
 * armor, absorption and knockback are applied.
 *
 * @since 0.1.0
 */
public class EntityDamageEvent implements Event, Cancellable {

    private final LivingEntity entity;
    private final DamageSource source;
    private float damage;
    private double knockback;
    private boolean cancelled;

    public EntityDamageEvent(
            final LivingEntity entity, final DamageSource source, final float damage, final double knockback) {
        this.entity = entity;
        this.source = source;
        this.damage = damage;
        this.knockback = knockback;
    }

    @ThreadContract("get -> any; modify -> owner")
    public LivingEntity entity() {
        return entity;
    }

    /**
     * @return the origin of the hit, carrying the damage type and the entities involved
     */
    public DamageSource source() {
        return source;
    }

    /**
     * @return the entity that caused the damage, or {@code null} for environmental damage
     */
    @ThreadContract("get -> any; modify -> owner")
    public @Nullable Entity damager() {
        return source.causingEntity();
    }

    /**
     * @return the damage about to be dealt, in half-hearts, before armor and absorption
     */
    public float damage() {
        return damage;
    }

    /**
     * @param damage the damage to deal instead; {@code <= 0} makes the hit harmless
     */
    public void setDamage(final float damage) {
        this.damage = damage;
    }

    /**
     * @return the horizontal knockback strength about to be applied
     */
    public double knockback() {
        return knockback;
    }

    /**
     * @param knockback the knockback strength to apply instead; {@code 0} disables it
     */
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
