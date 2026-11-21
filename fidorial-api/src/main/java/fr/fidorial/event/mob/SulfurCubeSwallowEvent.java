package fr.fidorial.event.mob;

import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.mob.monster.SulfurCube;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a sulfur cube is about to swallow a block: one a player gives it, or one it picks up
 * from the ground.
 *
 * <p>Cancelling it leaves the block where it is: in the hand of the player, or on the ground. It
 * does not fire for a plugin {@linkplain SulfurCube#setSwallowedItem(ItemStack) giving} the cube a
 * block.</p>
 *
 * @since 0.1.0
 */
public interface SulfurCubeSwallowEvent extends MobEvent, Cancellable {

    /**
     * {@return the cube about to swallow the block}
     *
     * @since 0.1.0
     */
    @Override
    SulfurCube mob();

    /**
     * {@return the block about to be swallowed}, one of it
     *
     * @since 0.1.0
     */
    ItemStack item();

    /**
     * {@return the player giving the block, {@code null} if the cube picks it up}
     *
     * @since 0.1.0
     */
    @Nullable Player giver();

    /**
     * {@return the item lying on the ground the cube picks the block from, {@code null} if a player
     * gives it}
     *
     * @since 0.1.0
     */
    @Nullable ItemEntity pickedUp();
}
