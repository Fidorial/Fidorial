package fr.fidorial.entity.projectile;

/**
 * A thrown trident ({@code minecraft:trident}).
 *
 * <p>It only damages the first entity it hits. With Loyalty, it then flies back to its thrower.</p>
 *
 * @since 0.1.0
 */
public interface Trident extends ArrowProjectile {

    /**
     * {@return {@code true} once this trident hurt an entity}
     *
     * @since 0.1.0
     */
    boolean hasHit();

    /**
     * {@return the level of Loyalty of this trident, {@code 0} when it does not come back}
     *
     * @since 0.1.0
     */
    int loyalty();
}
