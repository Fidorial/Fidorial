package fr.fidorial.event.mob;

import fr.fidorial.event.Cancellable;

/**
 * Fired when a mob is about to explode, as a creeper does at the end of its fuse.
 *
 * <p>Cancelling it keeps the mob alive: a creeper shrinks back, and whatever lit it goes
 * out.</p>
 *
 * @since 0.1.0
 */
public interface MobExplodeEvent extends MobEvent, Cancellable {

    /**
     * {@return the power of the explosion}: {@code 3} for a creeper, twice that for a charged one
     *
     * @since 0.1.0
     */
    float power();

    /**
     * Changes the power of the explosion.
     *
     * @param power the new power; at {@code 0} or less the mob vanishes without hurting or
     *              breaking anything
     * @since 0.1.0
     */
    void setPower(float power);

    /**
     * {@return {@code true} if the explosion breaks blocks}, as it does when the
     * {@code mob_griefing} game rule allows it
     *
     * @since 0.1.0
     */
    boolean breaksBlocks();

    /**
     * Lets the explosion break blocks, or keeps it from doing so.
     *
     * @param breaksBlocks {@code true} to let it
     * @since 0.1.0
     */
    void setBreaksBlocks(boolean breaksBlocks);
}
