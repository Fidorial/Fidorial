package fr.fidorial.event.mob;

import fr.fidorial.entity.mob.Shearable;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a player is about to shear a mob ready for it: a sheep, a mooshroom, a sulfur cube
 * holding a block.
 *
 * <p>Cancelling it leaves the mob as it is, and the shears are not worn. It does not fire for a
 * plugin {@linkplain Shearable#shear() shearing} the mob.</p>
 *
 * @since 0.1.0
 */
public interface MobShearEvent extends MobEvent, Cancellable {

    /**
     * {@return the mob about to be sheared}
     *
     * @since 0.1.0
     */
    @Override
    Shearable mob();

    /**
     * {@return the player shearing it}
     *
     * @since 0.1.0
     */
    Player player();

    /**
     * {@return the shears the player uses}
     *
     * @since 0.1.0
     */
    ItemStack shears();
}
