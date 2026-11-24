package fr.fidorial.entity.projectile;

import fr.fidorial.item.ItemStack;

/**
 * A projectile that flies like an arrow: it deals damage scaling with its speed, sticks in the
 * block it hits and can be picked up again.
 *
 * @since 0.1.0
 */
public interface ArrowProjectile extends Projectile {

    /**
     * {@return the damage of a hit at a speed of one block per tick, in half-hearts}
     *
     * @since 0.1.0
     */
    double baseDamage();

    /**
     * Changes the damage of a hit at a speed of one block per tick.
     *
     * @param damage the damage, in half-hearts
     * @since 0.1.0
     */
    void setBaseDamage(double damage);

    /**
     * {@return {@code true} if this projectile was shot at full power, which adds a random bonus to
     * its damage}
     *
     * @since 0.1.0
     */
    boolean isCritical();

    /**
     * Marks this projectile as shot at full power, or not.
     *
     * @param critical {@code true} for a critical projectile
     * @since 0.1.0
     */
    void setCritical(boolean critical);

    /**
     * {@return how many entities this projectile goes through before it stops}
     *
     * @since 0.1.0
     */
    int piercing();

    /**
     * {@return {@code true} once this projectile is stuck in a block}
     *
     * @since 0.1.0
     */
    boolean isStuck();

    /**
     * {@return {@code true} if a player in survival mode may pick this projectile up}
     *
     * @since 0.1.0
     */
    boolean canBePickedUp();

    /**
     * {@return the item a player gets back when picking this projectile up}
     *
     * @since 0.1.0
     */
    ItemStack pickupItem();

    /**
     * {@return the bow, crossbow or trident this projectile was shot with, whose enchantments
     * apply to its hits; {@link ItemStack#EMPTY} if unknown}
     *
     * @since 0.1.0
     */
    ItemStack weapon();
}
