package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Ageable;

/**
 * A cube that hops around: a slime, a magma cube or a sulfur cube. Its size sets how big it is,
 * its health, its speed and how hard it hits; a cube bigger than 1 splits into cubes of half its
 * size once dead.
 *
 * <p>A cube has an age like the animals: a baby is half as big as its size says. Only sulfur cubes
 * are born babies and grow up.</p>
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface CubeMob extends Ageable {

    /**
     * The smallest size of a cube.
     *
     * @since 0.1.0
     */
    int MIN_SIZE = 1;

    /**
     * The biggest size of a cube.
     *
     * @since 0.1.0
     */
    int MAX_SIZE = 127;

    /**
     * {@return the size of this cube, from {@value #MIN_SIZE} to {@value #MAX_SIZE}}
     *
     * @since 0.1.0
     */
    int size();

    /**
     * Gives this cube a size, kept between {@value #MIN_SIZE} and {@value #MAX_SIZE}: its health,
     * speed and what depends on its size follow.
     *
     * @param size   the size
     * @param healed {@code true} to give it its full new health
     * @since 0.1.0
     */
    void setSize(int size, boolean healed);

    /**
     * {@return {@code true} for a cube of size {@value #MIN_SIZE}}
     *
     * @since 0.1.0
     */
    default boolean isTiny() {
        return size() <= MIN_SIZE;
    }
}
