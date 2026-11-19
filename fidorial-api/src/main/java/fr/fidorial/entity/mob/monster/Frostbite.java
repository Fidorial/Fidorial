package fr.fidorial.entity.mob.monster;

/**
 * A frostbite ({@code minecraft:frostbite}): the zombie of the cold, tougher and slower than the
 * zombie. It chills what it hits, and throws ice balls at what it cannot reach while it holds one.
 * Under water, it turns into a zombie.
 *
 * @since 0.1.0
 */
public interface Frostbite extends Zombie {

    /**
     * {@return {@code true} if this frostbite holds an ice ball}: it then throws it, again and again,
     * at a target it cannot hit with its fists
     *
     * @since 0.1.0
     */
    boolean holdsIceBall();

    /**
     * Gives this frostbite an ice ball to throw, or takes it away.
     *
     * @param holdsIceBall {@code true} to give it one
     * @since 0.1.0
     */
    void setHoldsIceBall(boolean holdsIceBall);

    /**
     * {@return {@code true} while this frostbite throws ice balls rather than using its fists}
     *
     * @since 0.1.0
     */
    boolean isRanged();

    /**
     * Sets how much the throws of this frostbite stray, in place of what the difficulty gives.
     *
     * @param uncertainty {@code 0} for throws that go straight at the target, a negative value to
     *                    follow the difficulty again
     * @since 0.1.0
     */
    void setRangedAttackUncertainty(float uncertainty);
}
