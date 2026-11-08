/**
 * Redstone: the signals blocks emit and read, and the engine that carries them.
 *
 * <p>Redstone is not a property of every block. Stone emits nothing, a lever, a torch or a trapped
 * chest do, and a lamp only listens. So the redstone side of a block type is a
 * {@link RedstoneBehaviour}, attached to the block types that have one
 * in the {@link RedstoneRegistry}, next to and independently from their
 * {@link BlockBehaviour}. A block type with no redstone behaviour is inert,
 * except that it may conduct power: conduction comes from the shape of the block, not from a
 * behaviour.</p>
 *
 * <p>The simulation itself, the order updates spread in, scheduled ticks and how signals are
 * resolved, is the {@link RedstoneEngine}
 * {@linkplain ServiceRegistry service}: a plugin can replace it, or wrap the
 * built-in one, without touching the behaviours.</p>
 *
 * <h2>Threading</h2>
 * <p>Reading signals is allowed from any thread. Changing blocks, and everything a behaviour is
 * asked to do, happens on the thread of the region owning the block; updates reaching a block of
 * another region are handed over to its thread.</p>
 *
 * @since 0.1.0
 */
package fr.fidorial.world.redstone;

import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.block.BlockBehaviour;
