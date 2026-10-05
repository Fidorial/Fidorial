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

    private static final int REPATH_COOLDOWN_TICKS = 10;

    private static final double REPATH_TARGET_MOVED_SQ = 2.0 * 2.0;
    private static final int MAX_NODES = 768;

    private static final float MAX_NODE_DISTANCE = (float) Math.sqrt(WAYPOINT_REACHED_SQ);

    private final ServerWorld world;
    private final AtomicReference<@Nullable PathResult> pendingResult = new AtomicReference<>();

    private @Nullable PathPenalty pathPenalty;
    private @Nullable Path path;
    private int waypointIndex;
    private @Nullable Position requestedGoal;

    private long age;
    private long lastRequestTick = -REPATH_COOLDOWN_TICKS;
    private boolean requestInFlight;

    private double lastDistanceSq = Double.MAX_VALUE;
    private int stuckTicks;

    public Navigation(final ServerWorld world) {
        this.world = world;
    }

    private static double distanceSq(final BlockPosition a, final BlockPosition b) {
        final double dx = a.x() - b.x();
        final double dy = a.y() - b.y();
        final double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public void setPathPenalty(@Nullable final PathPenalty pathPenalty) {
        this.pathPenalty = pathPenalty;
    }

    @Override
    public void moveTo(final Location from, final Position goal) {
        if (requestInFlight) {
            return;
        }
        final boolean sameGoal = requestedGoal != null && requestedGoal.distanceSquared(goal) < REPATH_TARGET_MOVED_SQ;
        if (sameGoal && path != null) {
            return;
        }
        if (age - lastRequestTick < REPATH_COOLDOWN_TICKS) {
            return;
        }

        requestedGoal = goal;
        lastRequestTick = age;
        final PathPenalty penalty = this.pathPenalty;
        requestInFlight = FidorialServer.getInstance()
                .aiWorker()
                .submit(() -> pendingResult.set(
                        new PathResult(AStarPathfinder.find(world, from, goal, MAX_NODES, penalty))));
    }

    public void tick(final double x, final double z) {
        age++;
        final PathResult result = pendingResult.getAndSet(null);
        if (result != null) {
            requestInFlight = false;
            path = result.path();
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
        final double distSq = dx * dx + dz * dz;
        if (distSq < WAYPOINT_REACHED_SQ) {
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
    }

    private record PathResult(@Nullable Path path) {
    }
}
