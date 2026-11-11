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
        final Position from = snapToGround(world, start);
        Position to = snapToGround(world, goal);
        if (from == null) {
            return null;
        }
        if (to == null) {
            to = goal;
        }
        if (from.equals(to)) {
            return null;
        }

        final Long2ObjectOpenHashMap<Node> nodes = new Long2ObjectOpenHashMap<>();
        final PriorityQueue<Node> open = new PriorityQueue<>();

        final Node startNode = new Node(from.blockX(), from.blockY(), from.blockZ(), null, 0.0, heuristic(from, to));
        nodes.put(pack(from.blockX(), from.blockY(), from.blockZ()), startNode);
        open.add(startNode);

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
            if (current.x == to.x() && current.y == to.y() && current.z == to.z()) {
                return buildPath(current, true);
            }

            for (final int[] dir : CARDINALS) {
                final int nx = current.x + dir[0];
                final int nz = current.z + dir[1];
                final int ny = stepHeight(world, current.x, current.y, current.z, nx, nz);
                if (ny == Integer.MIN_VALUE) {
                    continue;
                }
                final double extra = penalty == null ? 0.0 : penalty.cost(world, nx, ny, nz);
                if (extra == PathPenalty.BLOCKED) {
                    continue;
                }
                final double moveCost = 1.0 + verticalCost(ny - current.y) + extra;
                final double g = current.g + moveCost;
                final long key = pack(nx, ny, nz);
                Node neighbor = nodes.get(key);
                if (neighbor == null) {
                    neighbor = new Node(nx, ny, nz, current, g, heuristic(Position.block(nx, ny, nz), to));
                    nodes.put(key, neighbor);
                    open.add(neighbor);
                } else if (!neighbor.closed && g < neighbor.g) {
                    neighbor.g = g;
                    neighbor.parent = current;
                    open.add(neighbor); // re-insertion, l'ancienne entree sera ignoree via closed/g
                }
            }
        }

        return best == startNode ? null : buildPath(best, false);
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

    private static double verticalCost(final int dy) {
        if (dy > 0) {
            return 0.5;
        }
        return dy < 0 ? -dy * 0.1 : 0.0;
    }

    private static double heuristic(final Position a, final Position b) {
        final double dx = a.x() - b.x();
        final double dy = a.y() - b.y();
        final double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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
