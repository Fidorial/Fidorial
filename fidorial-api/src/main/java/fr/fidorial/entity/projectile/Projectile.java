package fr.fidorial.entity.projectile;

import fr.fidorial.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * An entity flying through the air after being shot or thrown.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Projectile extends Entity {

    /**
     * {@return the entity that shot or threw this projectile, credited with what it hits, or
     * {@code null} if there is none}
     *
     * @since 0.1.0
     */
    @Nullable Entity owner();

    /**
     * {@return the velocity of this projectile along the x axis, in blocks per tick}
     *
     * @since 0.1.0
     */
    double velocityX();

    /**
     * {@return the velocity of this projectile along the y axis, in blocks per tick}
     *
     * @since 0.1.0
     */
    double velocityY();

    /**
     * {@return the velocity of this projectile along the z axis, in blocks per tick}
     *
     * @since 0.1.0
     */
    double velocityZ();

    /**
     * Changes the velocity of this projectile.
     *
     * @param x the velocity along the x axis, in blocks per tick
     * @param y the velocity along the y axis, in blocks per tick
     * @param z the velocity along the z axis, in blocks per tick
     * @since 0.1.0
     */
    void setVelocity(double x, double y, double z);
}
