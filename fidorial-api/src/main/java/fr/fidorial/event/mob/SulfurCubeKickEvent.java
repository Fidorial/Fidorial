package fr.fidorial.event.mob;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.mob.monster.SulfurCube;
import fr.fidorial.event.Cancellable;
import fr.fidorial.math.FinePosition;

/**
 * Fired when a sulfur cube holding a block is about to be sent flying: hit by an entity, or pushed
 * by a player walking into it.
 *
 * <p>Cancelling it leaves the cube as it goes; a hit still lands, harmless, and a hot cube still
 * burns the player pushing it.</p>
 *
 * @since 0.1.0
 */
public interface SulfurCubeKickEvent extends MobEvent, Cancellable {

    /**
     * {@return the cube about to be sent flying}
     *
     * @since 0.1.0
     */
    @Override
    SulfurCube mob();

    /**
     * {@return the entity that hits or pushes the cube}
     *
     * @since 0.1.0
     */
    Entity kicker();

    /**
     * {@return how the cube is sent flying}
     *
     * @since 0.1.0
     */
    Cause cause();

    /**
     * {@return the speed the kick adds to the cube}, in blocks per tick along each axis
     *
     * @since 0.1.0
     */
    FinePosition push();

    /**
     * Changes the speed the kick adds to the cube.
     *
     * @param push the speed, in blocks per tick along each axis
     * @since 0.1.0
     */
    void setPush(FinePosition push);

    /**
     * How a sulfur cube is sent flying.
     *
     * @since 0.1.0
     */
    enum Cause {
        /**
         * Hit by an entity, or by its shot; a sprint or a weapon that knocks back adds to it.
         *
         * @since 0.1.0
         */
        HIT,
        /**
         * Pushed by a player walking into it, the faster the further.
         *
         * @since 0.1.0
         */
        PUSH
    }
}
