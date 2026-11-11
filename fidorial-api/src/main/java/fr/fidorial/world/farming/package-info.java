/**
 * Farming: soil, the plants growing on it, and the engine making them grow.
 *
 * <p>Growing is not a property of every block. Stone never changes, wheat ripens, farmland dries
 * out. So the farming side of a block type is a {@link fr.fidorial.world.farming.FarmingBehaviour},
 * attached to the block types that have one in the {@link fr.fidorial.world.farming.FarmingRegistry},
 * next to and independently from their {@link fr.fidorial.world.block.BlockBehaviour} and their
 * {@link fr.fidorial.world.redstone.RedstoneBehaviour}. A block type with no farming behaviour is
 * inert: it never grows, always stands, drops nothing and ignores fertilizer.</p>
 *
 * <p>The registry also says what items do to the land: which ones plant a block, which ones
 * fertilize, which ones till, and what tilled soil becomes. A plugin adding a plant of its own
 * registers its block type, attaches a behaviour to it, and declares its seed:</p>
 *
 * <pre>{@code
 * FarmingRegistry farming = context.server().registries().farming();
 * farming.attach(RICE, new RiceBehaviour(), plugin);
 * farming.seed(RICE_GRAINS, RICE, plugin);
 * }</pre>
 *
 * <p>The simulation itself, which blocks get a random tick and when, how a change reaches the
 * plants around it, and what the items of players do, is the
 * {@link FarmingEngine} {@linkplain ServiceRegistry
 * service}: a plugin can replace it, or wrap the built-in one, without touching the behaviours.</p>
 *
 * <p>Everything that happens is announced by the events of {@link fr.fidorial.event.farming}.</p>
 *
 * <h2>Threading</h2>
 * <p>Reading a {@link FarmingWorld} is allowed from any thread. Changing
 * blocks, and everything a behaviour is asked to do, happens on the thread of the region owning the
 * block; what reaches a block of another region is handed over to its thread, or skipped when the
 * documentation of the method says so.</p>
 *
 * @since 0.1.0
 */
package fr.fidorial.world.farming;

import fr.fidorial.service.ServiceRegistry;
