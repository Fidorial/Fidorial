package fr.euphyllia.fidorial.server.world.tick;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.redstone.TickPriority;

/**
 * A scheduled tick as its chunk keeps it on disk: once the chunk is loaded again, it is scheduled
 * anew, due as many game ticks later as it was still due in when the chunk was saved.
 *
 * @param position where the block or the fluid is
 * @param type     what the tick is for: a block type, or a fluid; it is dropped if something else
 *                 stands there once due
 * @param delay    how many game ticks it was still due in, zero or less when it was already due
 * @param priority the order of the ticks due in the same game tick
 * @param <T>      a block type key, or a fluid
 */
public record PendingTick<T>(BlockPosition position, T type, int delay, TickPriority priority) {
}
