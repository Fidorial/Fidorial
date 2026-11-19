package fr.fidorial.entity.mob;

/**
 * A mob that shears cut something off: a sheep its wool, a mooshroom its mushrooms.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Shearable extends Mob {

    /**
     * {@return {@code true} if shears would cut something off this mob now}
     *
     * @since 0.1.0
     */
    boolean readyForShearing();

    /**
     * Shears this mob, as a player with shears does, whether it is ready or not: what is cut off
     * falls around it.
     *
     * @since 0.1.0
     */
    void shear();
}
