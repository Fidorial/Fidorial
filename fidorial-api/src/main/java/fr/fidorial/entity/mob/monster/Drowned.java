package fr.fidorial.entity.mob.monster;

/**
 * A drowned ({@code minecraft:drowned}): the zombie of the waters. It swims after its targets, and
 * heads back to the water by day.
 *
 * @since 0.1.0
 */
public interface Drowned extends Zombie {

    /**
     * {@return {@code true} while this drowned swims towards the shore}
     *
     * @since 0.1.0
     */
    boolean isSearchingForLand();
}
