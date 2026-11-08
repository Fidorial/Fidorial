package fr.fidorial.world.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;

/**
 * The order scheduled ticks due on the same game tick run in: every {@link #EXTREMELY_HIGH} tick
 * first, then every {@link #VERY_HIGH} one, and so on. Ticks of the same priority run in the order
 * they were scheduled.
 *
 * <p>Repeaters rely on it: one turning on runs before one turning off, so that a line of them never
 * flickers.</p>
 *
 * @see RedstoneWorld#scheduleTick(BlockPosition, TypedKey, int, TickPriority)
 * @since 0.1.0
 */
public enum TickPriority {
    /**
     * Runs before every other tick.
     */
    EXTREMELY_HIGH,
    /**
     * Runs right after {@link #EXTREMELY_HIGH}.
     */
    VERY_HIGH,
    /**
     * Runs right after {@link #VERY_HIGH}.
     */
    HIGH,
    /**
     * The default priority.
     */
    NORMAL,
    /**
     * Runs right after {@link #NORMAL}.
     */
    LOW,
    /**
     * Runs right after {@link #LOW}.
     */
    VERY_LOW,
    /**
     * Runs after every other tick.
     */
    EXTREMELY_LOW
}
