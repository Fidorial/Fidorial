package fr.fidorial.event.redstone;

import fr.fidorial.event.Cancellable;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;

/**
 * Fired when a block taking part in redstone is told that one of its neighbours changed, before it
 * reacts: the block update that carries redstone from one block to the next.
 *
 * <p>It is fired for every update reaching a block with a {@link RedstoneBehaviour}, the ones that
 * run {@link RedstoneBehaviour#neighborChanged}, which makes it by far the most frequent redstone
 * event: a listener should be quick. Updates reaching a block taking no part in redstone, such as
 * stone, do nothing and fire nothing.</p>
 *
 * <p>Cancelling the event makes the block ignore the update: a lamp does not light up, a repeater
 * does not schedule its switch, dust does not take the new signal into account. That is how a
 * plugin freezes the redstone of an area.</p>
 *
 * @see RedstoneWorld#neighborChanged(BlockPosition, TypedKey)
 * @since 0.1.0
 */
public interface RedstoneUpdateEvent extends RedstoneEvent, Cancellable {

    /**
     * {@return the block type that caused the update, such as redstone dust whose power changed}
     *
     * @since 0.1.0
     */
    TypedKey<BlockType> source();
}