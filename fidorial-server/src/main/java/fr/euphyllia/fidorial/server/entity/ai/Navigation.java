package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.debug.DebugValues;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.ai.Navigator;
import fr.fidorial.entity.ai.Path;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Navigation implements Navigator {

    private static final double WAYPOINT_REACHED_SQ = 0.35 * 0.35;

    /**
     * A mob that swims drifts: it reaches a waypoint from a little further than one that walks.
     */
    private static final double SWIM_WAYPOINT_REACHED_SQ = 0.45 * 0.45;

    private static final int REPATH_COOLDOWN_TICKS = 10;

    private static final double REPATH_TARGET_MOVED_SQ = 2.0 * 2.0;
    private static final int MAX_NODES = 768;

    private static final float MAX_NODE_DISTANCE = (float) Math.sqrt(WAYPOINT_REACHED_SQ);

    private final ServerWorld world;
    private final AtomicReference<@Nullable PathResult> pendingResult = new AtomicReference<>();

    private @Nullable PathPenalty pathPenalty;
    private boolean swims;
    private @Nullable Path path;
    private int waypointIndex;
    private @Nullable Position requestedGoal;

    private long age;
    private long lastRequestTick = -REPATH_COOLDOWN_TICKS;
    private boolean requestInFlight;

    private double lastDistanceSq = Double.MAX_VALUE;
    private int stuckTicks;
    private double speed;
    private boolean pathFailed;

    public Navigation(final ServerWorld world) {
        this.world = world;
    }

    /**
     * Tells whether the paths go through the water, for a mob that swims as well as it walks.
     */
    public void setSwims(final boolean swims) {
        this.swims = swims;
    }

    public void setPathPenalty(@Nullable final PathPenalty pathPenalty) {
        this.pathPenalty = pathPenalty;
    }

    /**
     * Asks for a path to a goal, to be followed at a speed, as vanilla's navigation: the mob keeps
     * that speed until the path ends, whichever goal asked for it.
     *
     * @param speed the movement speed of the mob times the speed modifier of the goal
     * @return {@code false} if the request was dropped, another one being on its way or too recent
     */
    public boolean moveTo(final Location from, final Position goal, final double speed) {
        if (!request(from, goal)) {
            return false;
        }
        this.speed = speed;
        return true;
    }

    /**
     * {@return the speed asked along the path being followed, {@code 0} if a goal sets the speed of
     * the mob itself every tick}
     */
    public double speed() {
        return speed;
    }

    /**
     * Changes the speed along the path being followed, as a mob that starts to run.
     */
    public void setSpeed(final double speed) {
        this.speed = speed;
    }

    @Override
    public void moveTo(final Location from, final Position goal) {
        if (request(from, goal)) {
            this.speed = 0.0;
        }
    }

    /**
     * @return {@code true} if the mob is on its way to the goal: a path was asked for, or the one it
     *         follows already leads there
     */
    private boolean request(final Location from, final Position goal) {
        if (requestInFlight) {
            return false;
        }
        final boolean sameGoal = requestedGoal != null && requestedGoal.distanceSquared(goal) < REPATH_TARGET_MOVED_SQ;
        if (sameGoal && path != null) {
            return true;
        }
        if (age - lastRequestTick < REPATH_COOLDOWN_TICKS) {
            return false;
        }

        requestedGoal = goal;
        lastRequestTick = age;
        final PathPenalty penalty = this.pathPenalty;
        final boolean swimming = this.swims;
        requestInFlight = FidorialServer.getInstance()
                .workers().aiWorker()
                .submit(() -> pendingResult.set(
                        new PathResult(AStarPathfinder.find(world, from, goal, MAX_NODES, penalty, swimming))
                        )
                );
        return requestInFlight;
    }

    /**
     * {@return {@code true} if the last path asked for was not found}: there is no way to the goal
     */
    public boolean lastPathFailed() {
        return pathFailed;
    }

    public @Nullable PathPenalty pathPenalty() {
        return pathPenalty;
    }

    public void tick(final double x, final double z) {
        tick(x, 0.0, z, false);
    }

    /**
     * Follows the path from where the mob is.
     *
     * @param vertical {@code true} for a mob that swims along its path: a waypoint is then only
     *                 reached within a block of its height, and not from under or above it
     */
    public void tick(final double x, final double y, final double z, final boolean vertical) {
        age++;
        final PathResult result = pendingResult.getAndSet(null);
        if (result != null) {
            requestInFlight = false;
            path = result.path();
            pathFailed = path == null;
            waypointIndex = 0;
            lastDistanceSq = Double.MAX_VALUE;
            stuckTicks = 0;
        }
        if (path == null) {
            return;
        }

        final Position waypoint = currentWaypoint();
        if (waypoint == null) {
            return;
        }
        final double dx = waypoint.x() + 0.5 - x;
        final double dz = waypoint.z() + 0.5 - z;
        final double dy = vertical ? waypoint.y() - y : 0.0;
        final double distSq = dx * dx + dz * dz + (vertical ? dy * dy : 0.0);
        if (dx * dx + dz * dz < (vertical ? SWIM_WAYPOINT_REACHED_SQ : WAYPOINT_REACHED_SQ) && Math.abs(dy) < 1.0) {
            waypointIndex++;
            lastDistanceSq = Double.MAX_VALUE;
            stuckTicks = 0;
            if (waypointIndex >= path.waypoints().size()) {
                stop();
            }
            return;
        }
        if (distSq >= lastDistanceSq - 1.0E-4) {
            if (++stuckTicks > 40) {
                stop();
            }
        } else {
            lastDistanceSq = distSq;
            stuckTicks = 0;
        }
    }

    @Override
    public @Nullable Position currentWaypoint() {
        if (path == null || waypointIndex >= path.waypoints().size()) {
            return null;
        }
        return path.waypoints().get(waypointIndex);
    }

    /**
     * {@return {@code true} if the path being followed stops short of where the mob was sent}: there
     * is no way to the goal itself
     */
    public boolean isGoalOutOfReach() {
        final Path current = path;
        return current != null && !current.reachesGoal();
    }

    /**
     * {@return where the path being followed leads, {@code null} when no path is followed}
     */
    public @Nullable Position destination() {
        final Path current = path;
        return current == null || current.waypoints().isEmpty() ? null : current.waypoints().getLast();
    }

    @Override
    public boolean isNavigating() {
        return currentWaypoint() != null || requestInFlight;
    }

    public DebugValues.@Nullable PathInfo debugSnapshot() {
        final Path current = path;
        if (current == null || current.waypoints().isEmpty()) {
            return null;
        }
        final List<BlockPosition> waypoints = current.waypoints();
        final Position target = requestedGoal != null ? requestedGoal : waypoints.getLast();
        final List<DebugValues.PathInfo.Node> nodes = new ArrayList<>(waypoints.size());
        for (final Position waypoint : waypoints) {
            nodes.add(DebugValues.PathInfo.Node.walkable(waypoint));
        }
        return new DebugValues.PathInfo(
                current.reachesGoal(), waypointIndex, target, nodes,
                List.of(DebugValues.PathInfo.Node.walkable(target)),
                List.of(), List.of(),
                MAX_NODE_DISTANCE);
    }

    @Override
    public void stop() {
        path = null;
        waypointIndex = 0;
        requestedGoal = null;
        stuckTicks = 0;
        lastDistanceSq = Double.MAX_VALUE;
        speed = 0.0;
    }

    private record PathResult(@Nullable Path path) {
    }
}
