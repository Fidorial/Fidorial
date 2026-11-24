package fr.fidorial.event.entity;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.projectile.Projectile;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import fr.fidorial.item.ItemStack;

/**
 * Fired when an entity shoots or throws a projectile with an item, such as an arrow with a bow or
 * a crossbow, or a trident, before the projectile enters the world. Cancelling it keeps the
 * projectile out of the world, and the item and its ammunition untouched.
 *
 * @since 0.1.0
 */
public interface EntityShootProjectileEvent extends Event, Cancellable {

    /**
     * {@return the entity shooting}
     *
     * @since 0.1.0
     */
    LivingEntity shooter();

    /**
     * {@return the item shooting the projectile: the bow, the crossbow or the trident}
     *
     * @since 0.1.0
     */
    ItemStack weapon();

    /**
     * {@return the projectile about to enter the world}
     *
     * @since 0.1.0
     */
    Projectile projectile();

    /**
     * {@return how hard the projectile is shot, between {@code 0} and {@code 1}}
     *
     * @since 0.1.0
     */
    float force();
}
