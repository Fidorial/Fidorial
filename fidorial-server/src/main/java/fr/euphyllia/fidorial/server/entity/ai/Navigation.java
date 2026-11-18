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
import java.util.function.DoubleSupplier;

public class Navigation implements Navigator {

    /**
     * How close to the middle of a waypoint a mob that walks gets, along each axis, before it heads
     * for the next one: half its width, or for a mob narrower than this, what is left of this width
     * once half its own is taken. 0.45 blocks for a zombie as for a cow.
     */
    private static final double WAYPOINT_REACH_WIDTH = 0.75;

    /**
     * A mob gives up a waypoint it walked past, the next one being behind it too or closer, when it
     * is within two blocks of it.
     */
    private static final double SKIPPED_WAYPOINT_WITHIN_SQ = 2.0 * 2.0;
    private static final double CLOSE_TO_WAYPOINT_SQ = 0.5;

    /**
     * A mob that swims drifts: it reaches a waypoint from a little further than one that walks.
     */
    private static final double SWIM_WAYPOINT_REACHED_SQ = 0.45 * 0.45;

    /**
     * How long a mob waits before it looks again for a path it did not find.
     */
    private static final int REPATH_COOLDOWN_TICKS = 10;

    private static final int MAX_NODES = 768;

    /**
     * Half the width paths are made for when the mob does not tell its own: that of a zombie.
     */
    private static final double DEFAULT_HALF_WIDTH = 0.3;

    private final ServerWorld world;
    private final DoubleSupplier halfWidth;
    private final AtomicReference<@Nullable PathResult> pendingResult = new AtomicReference<>();

    private @Nullable PathPenalty pathPenalty;
    private boolean swims;
    private @Nullable Path path;
    private int waypointIndex;
    private @Nullable Position requestedGoal;

    private long age;
    private long lastRequestTick = -REPATH_COOLDOWN_TICKS;
    private boolean requestInFlight;

    /**
     * The number of the last path asked for, and of the only one still wanted: {@link #stop()} moves
     * on to the next one, so that a path that was on its way when the mob was stopped is dropped
     * when it comes in, instead of being walked by whichever goal runs by then.
     */
    private int requestId;

    private double lastDistanceSq = Double.MAX_VALUE;
    private int stuckTicks;
    private double speed;
    private boolean pathFailed;

    /**
     * For a mob that climbs walls, where it was last sent, the speed asked, and how close to it it
     * stops walking straight there once its path ends.
     */
    private double climberWidth;
    private @Nullable BlockPosition climbGoal;
    private double climbSpeed;

    public Navigation(final ServerWorld world) {
        this(world, () -> DEFAULT_HALF_WIDTH);
    }

    /**
     * @param halfWidth half the width of the mob, asked for with each path: a baby grows up
     */
    public Navigation(final ServerWorld world, final DoubleSupplier halfWidth) {
        this.world = world;
        this.halfWidth = halfWidth;
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
     * Makes this the navigation of a mob that climbs walls: once its path ends, or while there is
     * none, the mob walks straight to where it was last sent, up the walls in its way, until it is
     * within its width of it.
     *
     * @param width the width of the mob
     */
    public void setClimbsWalls(final double width) {
        this.climberWidth = width;
    }

    /**
     * Asks for a path to a goal, to be followed at a speed, as vanilla's navigation: the mob keeps
     * that speed until the path ends, whichever goal asked for it.
     *
     * @param speed the movement speed of the mob times the speed modifier of the goal
     * @return {@code false} if the request was dropped, another one being on its way or too recent
     */
    public boolean moveTo(final Location from, final Position goal, final double speed) {
        aimClimb(goal, speed);
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
        aimClimb(goal, 0.0);
        if (request(from, goal)) {
            this.speed = 0.0;
        }
    }

    private void aimClimb(final Position goal, final double speed) {
        if (climberWidth > 0.0) {
            climbGoal = Position.block((int) Math.floor(goal.x()), (int) Math.floor(goal.y()), (int) Math.floor(goal.z()));
            climbSpeed = speed;
        }
    }

    /**
     * {@return where the mob heads for now}: the next point of its path, or, for a mob that
     * {@linkplain #setClimbsWalls climbs walls} and has no path to follow, where it was last sent;
     * {@code null} if it heads nowhere
     */
    public @Nullable Position heading() {
        final Position waypoint = currentWaypoint();
        return waypoint != null || path != null ? waypoint : climbGoal;
    }

    /**
     * {@return the speed the mob heads for its {@linkplain #heading() heading} at}
     */
    public double headingSpeed() {
        return path == null && climbGoal != null ? climbSpeed : speed;
    }

    /**
     * @return {@code true} if the mob is on its way to the goal: a path was asked for, or the one it
     *         follows already leads there
     */
    private boolean request(final Location from, final Position goal) {
        // the path to a block is kept for as long as it is followed, and another block gets its own
        // path at once: how often is up to the goal that asks
        final Position requested = requestedGoal;
        final boolean sameGoal = requested != null
                && requested.blockX() == goal.blockX()
                && requested.blockY() == goal.blockY()
                && requested.blockZ() == goal.blockZ();
        if (sameGoal && (path != null || requestInFlight)) {
            return true;
        }
        // one search at a time: a goal that sends the mob elsewhere meanwhile stops it first, or asks again
        if (requestInFlight) {
            return false;
        }
        if (pathFailed && age - lastRequestTick < REPATH_COOLDOWN_TICKS) {
            return false;
        }

        requestedGoal = goal;
        lastRequestTick = age;
        final PathPenalty penalty = this.pathPenalty;
        final boolean swimming = this.swims;
        final double width = halfWidth.getAsDouble();
        final int id = ++requestId;
        requestInFlight = FidorialServer.getInstance()
                .workers().aiWorker()
                .submit(() -> deliver(new PathResult(id,
                        AStarPathfinder.find(world, from, goal, MAX_NODES, penalty, swimming, width))));
        return requestInFlight;
    }

    /**
     * Hands over a path from the worker that found it. Two may be on their way at once, when the mob
     * was stopped and sent elsewhere meanwhile: the older one never takes the place of the newer one.
     */
    private void deliver(final PathResult result) {
        pendingResult.accumulateAndGet(result,
                (waiting, incoming) -> waiting == null || incoming.id() > waiting.id() ? incoming : waiting);
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

    /**
     * Follows the path from where the mob is.
     *
     * @param vertical {@code true} for a mob that swims along its path: a waypoint is then only
     *                 reached within a block of its height, and not from under or above it
     */
    public void tick(final double x, final double y, final double z, final boolean vertical) {
        age++;
        final PathResult result = pendingResult.getAndSet(null);
        // a path asked for before the mob was stopped is no longer wanted
        if (result != null && result.id() == requestId) {
            requestInFlight = false;
            path = result.path();
            pathFailed = path == null;
            waypointIndex = 0;
            lastDistanceSq = Double.MAX_VALUE;
            stuckTicks = 0;
        }
        if (path == null) {
            dropReachedClimbGoal(x, y, z);
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
        final boolean reached;
        if (vertical) {
            reached = dx * dx + dz * dz < SWIM_WAYPOINT_REACHED_SQ && Math.abs(dy) < 1.0;
        } else {
            final double reach = waypointReach();
            reached = (Math.abs(dx) < reach && Math.abs(dz) < reach) || isPastWaypoint(x, y, z);
        }
        if (reached) {
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

    /**
     * {@return how close to the middle of a waypoint this mob gets, along each axis, before it heads
     * for the next one}
     */
    private double waypointReach() {
        final double width = halfWidth.getAsDouble() * 2.0;
        return width > WAYPOINT_REACH_WIDTH ? width / 2.0 : WAYPOINT_REACH_WIDTH - width / 2.0;
    }

    /**
     * {@return {@code true} if the mob walked past the waypoint it heads for}: close to it, the
     * waypoint after it being closer, or the mob all but on this one, and the two lying on opposite
     * sides of the mob. It then heads for the next one instead of turning back; never past the last
     * waypoint, which it has to reach.
     */
    private boolean isPastWaypoint(final double x, final double y, final double z) {
        final Path current = path;
        if (current == null || waypointIndex + 1 >= current.waypoints().size()) {
            return false;
        }
        final double feet = Math.floor(y + 0.5);
        final BlockPosition next = current.waypoints().get(waypointIndex);
        final double nextX = next.blockX() + 0.5 - x;
        final double nextY = next.blockY() - feet;
        final double nextZ = next.blockZ() + 0.5 - z;
        final double nextSq = nextX * nextX + nextY * nextY + nextZ * nextZ;
        if (nextSq >= SKIPPED_WAYPOINT_WITHIN_SQ) {
            return false;
        }
        final BlockPosition after = current.waypoints().get(waypointIndex + 1);
        final double afterX = after.blockX() + 0.5 - x;
        final double afterY = after.blockY() - feet;
        final double afterZ = after.blockZ() + 0.5 - z;
        final double afterSq = afterX * afterX + afterY * afterY + afterZ * afterZ;
        if (afterSq >= nextSq && nextSq >= CLOSE_TO_WAYPOINT_SQ) {
            return false;
        }
        return afterX * nextX + afterY * nextY + afterZ * nextZ < 0.0;
    }

    /**
     * A mob that climbs walls stops walking straight to where it was sent once it is within its width
     * of the middle of that block, or of the block at its own height above it.
     */
    private void dropReachedClimbGoal(final double x, final double y, final double z) {
        final BlockPosition goal = climbGoal;
        if (goal == null) {
            return;
        }
        if (isWithin(goal.blockX(), goal.blockY(), goal.blockZ(), x, y, z)
                || (y > goal.blockY() && isWithin(goal.blockX(), (int) Math.floor(y), goal.blockZ(), x, y, z))) {
            climbGoal = null;
        }
    }

    private boolean isWithin(final int blockX, final int blockY, final int blockZ,
                             final double x, final double y, final double z) {
        final double dx = blockX + 0.5 - x;
        final double dy = blockY + 0.5 - y;
        final double dz = blockZ + 0.5 - z;
        return dx * dx + dy * dy + dz * dz < climberWidth * climberWidth;
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
                (float) waypointReach());
    }

    /**
     * Stops the mob where it is and forgets where it was going: the path it follows, and the one
     * that may be on its way. Whoever sends the mob somewhere next is served at once, even right
     * after a path that was not found.
     */
    @Override
    public void stop() {
        path = null;
        waypointIndex = 0;
        requestedGoal = null;
        stuckTicks = 0;
        lastDistanceSq = Double.MAX_VALUE;
        speed = 0.0;
        requestId++;
        requestInFlight = false;
        lastRequestTick = age - REPATH_COOLDOWN_TICKS;
    }

    private record PathResult(int id, @Nullable Path path) {
    }
}
