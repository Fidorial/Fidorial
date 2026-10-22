package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.world.ServerWorld;
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
            final Node current = open.poll();
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

    private static int stepHeight(final ServerWorld world, final int x, final int y, final int z, final int nx, final int nz) {
        if (isStandable(world, nx, y, nz)) {
            return y;
        }

        if (BlockView.isPassable(world, x, y + 2, z) && isStandable(world, nx, y + 1, nz)) {
            return y + 1;
        }

        if (BlockView.isPassable(world, nx, y, nz) && BlockView.isPassable(world, nx, y + 1, nz)) {
            for (int drop = 1; drop <= MAX_DROP; drop++) {
                final int ny = y - drop;
                if (!BlockView.isPassable(world, nx, ny, nz)) {
                    return Integer.MIN_VALUE;
                }
                if (isStandable(world, nx, ny, nz)) {
                    return ny;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isStandable(final ServerWorld world, final int x, final int y, final int z) {
        return BlockView.isPassable(world, x, y, z)
                && BlockView.isPassable(world, x, y + 1, z)
                && BlockView.isSolidGround(world, x, y - 1, z);
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
