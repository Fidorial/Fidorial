package fr.fidorial.event.mob;

import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.Cancellable;

/**
 * Fired when a mob is about to turn into another: a zombie drowning or freezing, a mooshroom
 * sheared into a cow.
 *
 * <p>Cancelling it keeps the mob as it is: a zombie starts counting its time in the water or the
 * snow again, a mooshroom keeps its mushrooms and the shears are not worn.</p>
 *
 * @since 0.1.0
 */
public interface MobConvertEvent extends MobEvent, Cancellable {

    /**
     * {@return the mob it turns into, not yet in the world}: it already took what it keeps of the
     * other, such as its age, and a listener may change it further before it appears
     *
     * @since 0.1.0
     */
    Mob converted();
}
