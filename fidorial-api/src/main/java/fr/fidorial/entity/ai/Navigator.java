package fr.fidorial.entity.ai;

import fr.fidorial.math.Location;
import fr.fidorial.world.BlockPos;
import org.jspecify.annotations.Nullable;

/**
 * Drives a mob towards a block.
 *
 * @since 0.1.0
 */
public interface Navigator {

    /**
     * A navigator that goes nowhere, used by mobs without pathfinding.
     *
     * @since 0.1.0
     */
    Navigator NONE = new Navigator() {

        @Override
        public void moveTo(final Location from, final BlockPos goal) {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isNavigating() {
            return false;
        }

        @Override
        public @Nullable BlockPos currentWaypoint() {
            return null;
        }
    };

    /**
     * @param from the position the path starts from, usually the mob's own location
     * @param goal the block to walk to
     * @since 0.1.0
     */
    void moveTo(Location from, BlockPos goal);

    /**
     * @param from the position the path starts from
     * @param goal the location to walk to, rounded down to a block
     * @since 0.1.0
     */
    default void moveTo(final Location from, final Location goal) {
        moveTo(from, new BlockPos(
                (int) Math.floor(goal.x()), (int) Math.floor(goal.y()), (int) Math.floor(goal.z())));
    }

    /**
     * Drops the current path; the mob stops where it stands.
     *
     * @since 0.1.0
     */
    void stop();

    /**
     * @return {@code true} while a path is being followed or computed
     * @since 0.1.0
     */
    boolean isNavigating();

    /**
     * @return the block the mob is currently walking to, or {@code null} when idle
     * @since 0.1.0
     */
    @Nullable BlockPos currentWaypoint();
}
