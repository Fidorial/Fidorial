package fr.fidorial.event.mob;

import fr.fidorial.entity.mob.monster.CubeMob;
import fr.fidorial.event.Cancellable;

/**
 * Fired when a cube bigger than the smallest (a slime, a magma cube or a sulfur cube) dies and is
 * about to split into smaller ones, half its size.
 *
 * <p>Cancelling it, or setting the count to {@code 0}, leaves nothing behind.</p>
 *
 * @since 0.1.0
 */
public interface CubeSplitEvent extends MobEvent, Cancellable {

    /**
     * {@return the cube that died}
     *
     * @since 0.1.0
     */
    @Override
    CubeMob mob();

    /**
     * {@return how many cubes it splits into}: at first between {@code 2} and {@code 4} for a slime or a
     * magma cube, {@code 2} for a sulfur cube
     *
     * @since 0.1.0
     */
    int count();

    /**
     * Changes how many cubes it splits into.
     *
     * @param count the new count, {@code 0} or more
     * @throws IllegalArgumentException if the count is negative
     * @since 0.1.0
     */
    void setCount(int count);

    /**
     * {@return the size of each cube it splits into}
     *
     * @since 0.1.0
     */
    int childSize();
}
