package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ai.Path;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class AStarPathfinder {

    private static final int[][] CARDINALS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /**
     * The diagonals, each followed by the two of the {@link #CARDINALS} it passes between.
     */
    private static final int[][] DIAGONALS = {{1, 1, 0, 2}, {1, -1, 0, 3}, {-1, 1, 1, 2}, {-1, -1, 1, 3}};

    /**
     * How much the distance left to the goal weighs against the distance walked: the search heads
     * for the goal rather than for the shortest way there.
     */
    private static final double HEURISTIC_WEIGHT = 1.5;

    /**
     * Half the width paths are made for when the mob is not known: that of a zombie.
     */
    private static final double DEFAULT_HALF_WIDTH = 0.3;
    private static final int MAX_DROP = 3;

    /**
     * How much higher a mob gets from one node to the next, jumping, as in Vanilla.
     */
    private static final double MAX_CLIMB = 1.125;

    /**
     * How much higher a mob gets from one node to the next without jumping.
     */
    private static final double MAX_STEP = 0.6;

    /**
     * How high a block in the feet of a mob may be for the mob to stand on it, rather than in front
     * of it.
     */
    private static final double MAX_FLOOR_IN_FEET = 0.5;

    /**
     * Above this floor in its feet, a mob reaches into the second block above its feet.
     */
    private static final double TALL_FLOOR = 0.05;

    private AStarPathfinder() {
    }

    public static @Nullable Path find(final ServerWorld world, final Position start, final Position goal, final int maxNodes) {
        return find(world, start, goal, maxNodes, null);
    }

    public static @Nullable Path find(final ServerWorld world, final Position start, final Position goal, final int maxNodes,
                                      @Nullable final PathPenalty penalty) {
        return find(world, start, goal, maxNodes, penalty, false);
    }

    public static @Nullable Path find(final ServerWorld world, final Position start, final Position goal, final int maxNodes,
                                      @Nullable final PathPenalty penalty, final boolean swims) {
        return find(world, start, goal, maxNodes, penalty, swims, DEFAULT_HALF_WIDTH);
    }

    /**
     * Looks for a path between two blocks: from a block to one of the eight around it, the distance
     * walked counting for what it is, a diagonal for more than a side.
     *
     * @param swims     {@code true} for a mob that swims as well as it walks: its path then also goes
     *              through the water, up, down and across, from one block of water to the next
     * @param halfWidth half the width of the mob; one wider than a block is stricter about diagonals
     */
    public static @Nullable Path find(final ServerWorld world, final Position start, final Position goal, final int maxNodes,
                                      @Nullable final PathPenalty penalty, final boolean swims, final double halfWidth) {
        final Position from = snap(world, start, swims);
        Position snappedGoal = snap(world, goal, swims);
        if (from == null) {
            return null;
        }
        if (snappedGoal == null) {
            snappedGoal = goal;
        }
        // the goal is a block: where a player stands is seldom a whole number, and no node is ever there
        final BlockPosition to = Position.block(snappedGoal.blockX(), snappedGoal.blockY(), snappedGoal.blockZ());
        if (from.blockX() == to.blockX() && from.blockY() == to.blockY() && from.blockZ() == to.blockZ()) {
            return null;
        }
        final boolean wide = halfWidth > 0.5;

        final Long2ObjectOpenHashMap<Node> nodes = new Long2ObjectOpenHashMap<>();
        final PriorityQueue<Node> open = new PriorityQueue<>();

        final Node startNode = new Node(from.blockX(), from.blockY(), from.blockZ(), null, 0.0,
                heuristic(from.blockX(), from.blockY(), from.blockZ(), to));
        nodes.put(pack(from.blockX(), from.blockY(), from.blockZ()), startNode);
        open.add(startNode);
        // where the mob gets to on each side of a node, and what it costs it to be there
        final int[] sideHeights = new int[CARDINALS.length];
        final double[] sideCosts = new double[CARDINALS.length];

        Node best = startNode;
        int visited = 0;

        while (!open.isEmpty() && visited < maxNodes) {
            final Node current = open.remove();
            if (current.closed) {
                continue;
            }
            current.closed = true;
            visited++;

            if (current.h < best.h) {
                best = current;
            }
            if (current.x == to.blockX() && current.y == to.blockY() && current.z == to.blockZ()) {
                return buildPath(current, true);
            }

            for (int side = 0; side < CARDINALS.length; side++) {
                final int nx = current.x + CARDINALS[side][0];
                final int nz = current.z + CARDINALS[side][1];
                final int ny = stepHeight(world, current.x, current.y, current.z, nx, nz);
                sideHeights[side] = ny;
                sideCosts[side] = ny == Integer.MIN_VALUE
                        ? PathPenalty.BLOCKED
                        : relax(world, nodes, open, current, nx, ny, nz, to, penalty);
                if (swims && ny != current.y && isSwimNode(world, nx, current.y, nz)) {
                    relax(world, nodes, open, current, nx, current.y, nz, to, penalty);
                }
            }
            for (final int[] diagonal : DIAGONALS) {
                // a mob only cuts a corner it could walk around on either side without going up: not
                // that of a wall, of a step, nor of what it keeps out of
                if (!isOpenSide(sideHeights[diagonal[2]], sideCosts[diagonal[2]], current.y, wide)
                        || !isOpenSide(sideHeights[diagonal[3]], sideCosts[diagonal[3]], current.y, wide)) {
                    continue;
                }
                final int nx = current.x + diagonal[0];
                final int nz = current.z + diagonal[1];
                final int ny = stepHeight(world, current.x, current.y, current.z, nx, nz);
                if (ny != Integer.MIN_VALUE) {
                    relax(world, nodes, open, current, nx, ny, nz, to, penalty);
                }
            }
            if (swims) {
                if (isSwimNode(world, current.x, current.y + 1, current.z)) {
                    relax(world, nodes, open, current, current.x, current.y + 1, current.z, to, penalty);
                }
                if (isSwimNode(world, current.x, current.y - 1, current.z)) {
                    relax(world, nodes, open, current, current.x, current.y - 1, current.z, to, penalty);
                }
            }
        }

        return best == startNode ? null : buildPath(best, false);
    }

    /**
     * {@return {@code true} if a mob may cut the corner of a node on one of its sides}: it gets there,
     * no higher than where it stands, and the block is none it keeps out of; a mob wider than a block
     * does not cut the corner of a block that costs it anything
     *
     * @param height where the mob gets to on that side, {@link Integer#MIN_VALUE} for nowhere
     * @param cost   what the block there costs it, {@link PathPenalty#BLOCKED} if it keeps out
     */
    private static boolean isOpenSide(final int height, final double cost, final int level, final boolean wide) {
        return height != Integer.MIN_VALUE && height <= level && cost < PathPenalty.BLOCKED && !(wide && cost > 0.0);
    }

    /**
     * Counts the way to a node through another, if it is the shortest known.
     *
     * @return what the node costs the mob beyond the distance, {@link PathPenalty#BLOCKED} if the mob
     *         keeps out of it
     */
    private static double relax(final ServerWorld world, final Long2ObjectOpenHashMap<Node> nodes, final PriorityQueue<Node> open,
                                final Node current, final int nx, final int ny, final int nz, final BlockPosition to,
                                @Nullable final PathPenalty penalty) {
        final double extra = penalty == null ? 0.0 : penalty.cost(world, nx, ny, nz);
        if (extra == PathPenalty.BLOCKED) {
            return extra;
        }
        // a step costs the distance between the two nodes: more across a corner, up or down
        final int dx = nx - current.x;
        final int dy = ny - current.y;
        final int dz = nz - current.z;
        final double g = current.g + Math.sqrt(dx * dx + dy * dy + dz * dz) + extra;

        final long key = pack(nx, ny, nz);
        Node neighbor = nodes.get(key);
        if (neighbor == null) {
            neighbor = new Node(nx, ny, nz, current, g, heuristic(nx, ny, nz, to));
            nodes.put(key, neighbor);
            open.add(neighbor);
        } else if (!neighbor.closed && g < neighbor.g) {
            // taken out before its cost changes: the queue does not notice a node that moves up by itself
            open.remove(neighbor);
            neighbor.g = g;
            neighbor.parent = current;
            open.add(neighbor);
        }
        return extra;
    }

    /**
     * {@return {@code true} if a mob that swims may be in a block}: in water, with room for its head
     * in the block above, water or not
     */
    private static boolean isSwimNode(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && state.isWater() && BlockView.isPassable(world, x, y + 1, z);
    }

    /**
     * {@return the block a path starts from or ends at}: for a mob that swims, the block of water it
     * is in; otherwise the ground under it
     */
    private static @Nullable Position snap(final ServerWorld world, final Position pos, final boolean swims) {
        if (swims && isSwimNode(world, pos.blockX(), pos.blockY(), pos.blockZ())) {
            return Position.block(pos.blockX(), pos.blockY(), pos.blockZ());
        }
        return snapToGround(world, pos);
    }

    /**
     * {@return the height of the node a mob reaches from one block to the next, along a cardinal
     * direction: the same, one up or a few down; {@link Integer#MIN_VALUE} when it cannot go}
     *
     * <p>As in Vanilla, a mob climbs at most {@value #MAX_CLIMB} blocks from one node to the next:
     * from a slab onto a full block, but not from the floor onto a fence or two blocks up. Above
     * {@value #MAX_STEP} it has to jump, and needs room above its head for it.</p>
     */
    private static int stepHeight(final ServerWorld world, final int x, final int y, final int z, final int nx, final int nz) {
        final double current = floorLevel(world, x, y, z);
        final double from = Double.isNaN(current) ? y : current;
        for (int ny = y; ny <= y + 1; ny++) {
            final double to = floorLevel(world, nx, ny, nz);
            if (Double.isNaN(to) || to - from > MAX_CLIMB) {
                continue;
            }
            if (to - from > MAX_STEP && !BlockView.isPassable(world, x, y + 2, z)) {
                continue; // no room to jump
            }
            return ny;
        }

        if (BlockView.isPassable(world, nx, y, nz) && BlockView.isPassable(world, nx, y + 1, nz)) {
            for (int drop = 1; drop <= MAX_DROP; drop++) {
                final int ny = y - drop;
                if (!Double.isNaN(floorLevel(world, nx, ny, nz))) {
                    return ny;
                }
                if (!BlockView.isPassable(world, nx, ny, nz)) {
                    return Integer.MIN_VALUE;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * {@return the height a mob whose feet are in a block stands at, {@link Double#NaN} if it cannot
     * stand there}
     *
     * <p>A mob stands on the block below its feet, if that block is at least half a block high and
     * no higher than a full one; or on a low block in its feet, such as a carpet, a bottom slab or a
     * few layers of snow. It needs room for its head above, two blocks of it when it stands that high.</p>
     */
    static double floorLevel(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState feet = BlockView.blockAt(world, x, y, z);
        if (feet == null || !BlockView.isPassable(world, x, y + 1, z)) {
            return Double.NaN;
        }
        if (BlockView.isPassable(feet)) {
            final BlockState below = BlockView.blockAt(world, x, y - 1, z);
            if (below == null || !BlockView.isSolidGround(below)) {
                return Double.NaN;
            }
            final CollisionShape ground = BlockCollision.shape(below);
            // powder snow carries those that walk on it, and lets the others sink in
            final double top = ground.isEmpty() ? 1.0 : ground.top();
            // lower, the mob stands in the block below; higher, on a fence it should not climb
            return top < MAX_FLOOR_IN_FEET || top > 1.0 ? Double.NaN : y - 1 + top;
        }
        if (!BlockView.isSolidGround(feet)) {
            return Double.NaN; // lava
        }
        final double top = BlockCollision.shape(feet).top();
        if (top > MAX_FLOOR_IN_FEET) {
            return Double.NaN;
        }
        if (top > TALL_FLOOR && !BlockView.isPassable(world, x, y + 2, z)) {
            return Double.NaN;
        }
        return y + top;
    }

    private static boolean isStandable(final ServerWorld world, final int x, final int y, final int z) {
        return !Double.isNaN(floorLevel(world, x, y, z));
    }

    private static @Nullable Position snapToGround(final ServerWorld world, final Position pos) {
        for (int dy = 0; dy >= -MAX_DROP; dy--) {
            if (isStandable(world, pos.blockX(), pos.blockY() + dy, pos.blockZ())) {
                return pos.offsetY(dy);
            }
        }
        for (int dy = 1; dy <= 2; dy++) {
            if (isStandable(world, pos.blockX(), pos.blockY() + dy, pos.blockZ())) {
                return pos.offsetY(dy);
            }
        }
        return null;
    }

    private static double heuristic(final int x, final int y, final int z, final BlockPosition goal) {
        final double dx = x - goal.blockX();
        final double dy = y - goal.blockY();
        final double dz = z - goal.blockZ();
        return HEURISTIC_WEIGHT * Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static @Nullable Path buildPath(final Node end, final boolean reachedGoal) {
        final List<BlockPosition> waypoints = new ArrayList<>();
        for (Node node = end; node.parent != null; node = node.parent) {
            waypoints.add(Position.block(node.x, node.y, node.z));
        }
        if (waypoints.isEmpty()) {
            return null;
        }
        final List<BlockPosition> ordered = waypoints.reversed();
        return new Path(ordered, reachedGoal);
    }

    private static long pack(final int x, final int y, final int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    private static final class Node implements Comparable<Node> {
        final int x;
        final int y;
        final int z;
        final double h;

        @Nullable Node parent;

        double g;
        boolean closed;

        Node(final int x, final int y, final int z, @Nullable final Node parent, final double g, final double h) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        @Override
        public int compareTo(final Node other) {
            return Double.compare(g + h, other.g + other.h);
        }
    }
}
