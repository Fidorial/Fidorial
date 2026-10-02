package fr.fidorial.entity.ai;

import fr.fidorial.world.BlockPos;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * A path computed by the pathfinder.
 *
 * @param waypoints   the blocks to walk through, in order
 * @param reachesGoal {@code true} if the path ends at the goal, {@code false} if it only gets as close as possible
 * @since 0.1.0
 */
public record Path(List<BlockPos> waypoints, boolean reachesGoal) {

    /**
     * Copies the waypoints.
     */
    public Path {
        waypoints = List.copyOf(waypoints);
    }

    /**
     * {@return the last waypoint of the path}
     *
     * @throws NoSuchElementException if the path has no waypoint
     * @since 0.1.0
     */
    public BlockPos target() {
        return waypoints.getLast();
    }
}
