package fr.fidorial.event.entity;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.projectile.Projectile;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import fr.fidorial.math.BlockPosition;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a projectile hits an entity or a block. Cancelling it lets the projectile fly on as
 * if it had hit nothing.
 *
 * @since 0.1.0
 */
public interface ProjectileHitEvent extends Event, Cancellable {

    /**
     * {@return the projectile}
     *
     * @since 0.1.0
     */
    Projectile projectile();

    /**
     * {@return the entity hit, or {@code null} when a block was hit}
     *
     * @since 0.1.0
     */
    @Nullable Entity hitEntity();

    /**
     * {@return the block hit, or {@code null} when an entity was hit}
     *
     * @since 0.1.0
     */
    @Nullable BlockPosition hitBlock();
}
