package fr.euphyllia.fidorial.server.world.generation.climate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Picks a biome from a point of the climate: temperature, humidity, continentalness, erosion, depth and
 * weirdness. Each biome claims boxes of that six-dimensional space; the biome whose box is nearest to the point
 * wins, the offset of a box counting as a seventh, constant distance.
 *
 * <p>The boxes are indexed in a tree of bounding boxes, so a lookup visits a few dozen boxes instead of all of
 * them. Every coordinate is quantized to a ten-thousandth, and a box covers its lower bound but stops one step
 * short of its upper bound.</p>
 *
 * <p>The table is immutable; lookups go through a {@link Cursor}, which remembers the last answer to start the
 * next search from it and belongs to one thread.</p>
 *
 * @param <T> what a box points to, usually a biome key
 */
public final class ClimateTable<T> {

    /**
     * Temperature, humidity, continentalness, erosion, depth, weirdness, and the offset.
     */
    static final int DIMENSIONS = 7;
    private static final int CHILDREN_PER_NODE = 19;
    private static final float QUANTUM = 10000.0F;
    private static final String[] PARAMETER_NAMES = {"temperature", "humidity", "continentalness", "erosion", "depth", "weirdness"};

    private final Node<T> root;

    private ClimateTable(final Node<T> root) {
        this.root = root;
    }

    /**
     * {@return a climate coordinate in the units of the table}
     */
    public static long quantize(final float value) {
        return (long) (value * QUANTUM);
    }

    /**
     * Reads a parameter list as the data generator reports it: a {@code biomes} array of objects holding a
     * {@code biome} and its {@code parameters}.
     *
     * @param list    the report
     * @param mapping turns a biome identifier into what the table points to
     */
    public static <T> ClimateTable<T> fromReport(final JsonElement list, final java.util.function.Function<String, T> mapping) {
        final JsonArray entries = list.getAsJsonObject().getAsJsonArray("biomes");
        final List<Leaf<T>> leaves = new ArrayList<>(entries.size());
        for (final JsonElement entry : entries) {
            final JsonObject object = entry.getAsJsonObject();
            final JsonObject parameters = object.getAsJsonObject("parameters");
            final long[] mins = new long[DIMENSIONS];
            final long[] maxs = new long[DIMENSIONS];
            for (int d = 0; d < PARAMETER_NAMES.length; d++) {
                final JsonElement value = parameters.get(PARAMETER_NAMES[d]);
                if (value.isJsonArray()) {
                    mins[d] = quantize(value.getAsJsonArray().get(0).getAsFloat());
                    maxs[d] = quantize(value.getAsJsonArray().get(1).getAsFloat());
                } else {
                    mins[d] = quantize(value.getAsFloat());
                    maxs[d] = mins[d];
                }
            }
            final long offset = quantize(parameters.get("offset").getAsFloat());
            mins[DIMENSIONS - 1] = offset;
            maxs[DIMENSIONS - 1] = offset;
            leaves.add(new Leaf<>(mins, maxs, mapping.apply(object.get("biome").getAsString())));
        }
        if (leaves.isEmpty()) {
            throw new IllegalArgumentException("A climate table needs at least one biome");
        }
        return new ClimateTable<>(build(leaves));
    }

    /**
     * {@return how far a coordinate is from a range; the upper bound itself is already one step away}
     */
    static long distance(final long min, final long max, final long target) {
        final long above = target - max + 1L;
        final long below = min - target;
        return above > 0L ? above : Math.max(below, 0L);
    }

    private abstract static sealed class Node<T> permits Leaf, Branch {
        final long[] mins;
        final long[] maxs;

        Node(final long[] mins, final long[] maxs) {
            this.mins = mins;
            this.maxs = maxs;
        }

        long distanceTo(final long[] target) {
            long total = 0L;
            for (int d = 0; d < DIMENSIONS; d++) {
                final long gap = distance(mins[d], maxs[d], target[d]);
                total += gap * gap;
            }
            return total;
        }

        long center(final int dimension) {
            return (mins[dimension] + maxs[dimension]) / 2L;
        }

        abstract Leaf<T> nearest(long[] target, Leaf<T> best);
    }

    private static final class Leaf<T> extends Node<T> {
        final T value;

        Leaf(final long[] mins, final long[] maxs, final T value) {
            super(mins, maxs);
            this.value = value;
        }

        @Override
        Leaf<T> nearest(final long[] target, final Leaf<T> best) {
            return this;
        }
    }

    private static final class Branch<T> extends Node<T> {
        final Node<T>[] children;

        @SuppressWarnings("unchecked")
        Branch(final List<? extends Node<T>> children) {
            super(boundsMin(children), boundsMax(children));
            this.children = children.toArray(new Node[0]);
        }

        @Override
        Leaf<T> nearest(final long[] target, final Leaf<T> start) {
            long bestDistance = start == null ? Long.MAX_VALUE : start.distanceTo(target);
            Leaf<T> best = start;
            for (final Node<T> child : children) {
                final long childDistance = child.distanceTo(target);
                if (bestDistance > childDistance) {
                    final Leaf<T> found = child.nearest(target, best);
                    final long foundDistance = child == found ? childDistance : found.distanceTo(target);
                    if (bestDistance > foundDistance) {
                        bestDistance = foundDistance;
                        best = found;
                    }
                }
            }
            return best;
        }
    }

    private static long[] boundsMin(final List<? extends Node<?>> nodes) {
        final long[] mins = new long[DIMENSIONS];
        Arrays.fill(mins, Long.MAX_VALUE);
        for (final Node<?> node : nodes) {
            for (int d = 0; d < DIMENSIONS; d++) {
                mins[d] = Math.min(mins[d], node.mins[d]);
            }
        }
        return mins;
    }

    private static long[] boundsMax(final List<? extends Node<?>> nodes) {
        final long[] maxs = new long[DIMENSIONS];
        Arrays.fill(maxs, Long.MIN_VALUE);
        for (final Node<?> node : nodes) {
            for (int d = 0; d < DIMENSIONS; d++) {
                maxs[d] = Math.max(maxs[d], node.maxs[d]);
            }
        }
        return maxs;
    }

    private static long size(final Node<?> node) {
        long total = 0L;
        for (int d = 0; d < DIMENSIONS; d++) {
            total += Math.abs(node.maxs[d] - node.mins[d]);
        }
        return total;
    }

    /**
     * {@return an ordering by the centres of the boxes along one dimension, then along the following ones}
     */
    private static <T> Comparator<Node<T>> byCenters(final int first, final boolean magnitude) {
        Comparator<Node<T>> order = null;
        for (int i = 0; i < DIMENSIONS; i++) {
            final int dimension = (first + i) % DIMENSIONS;
            final Comparator<Node<T>> next = Comparator.comparingLong(node -> {
                final long center = node.center(dimension);
                return magnitude ? Math.abs(center) : center;
            });
            order = order == null ? next : order.thenComparing(next);
        }
        return order;
    }

    /**
     * Groups the nodes level by level: a level holds groups of up to {@value #CHILDREN_PER_NODE}. Along each
     * dimension the nodes are sorted and cut into groups; the dimension where the groups are the smallest wins.
     */
    private static <T> Node<T> build(final List<? extends Node<T>> nodes) {
        if (nodes.size() == 1) {
            return nodes.getFirst();
        }
        if (nodes.size() <= CHILDREN_PER_NODE) {
            final List<Node<T>> sorted = new ArrayList<>(nodes);
            sorted.sort(Comparator.comparingLong(node -> {
                long magnitude = 0L;
                for (int d = 0; d < DIMENSIONS; d++) {
                    magnitude += Math.abs(node.center(d));
                }
                return magnitude;
            }));
            return new Branch<>(sorted);
        }
        final List<Node<T>> working = new ArrayList<>(nodes);
        long cheapest = Long.MAX_VALUE;
        int bestDimension = -1;
        List<Branch<T>> bestGroups = null;
        for (int d = 0; d < DIMENSIONS; d++) {
            working.sort(byCenters(d, false));
            final List<Branch<T>> groups = group(working);
            long cost = 0L;
            for (final Branch<T> group : groups) {
                cost += size(group);
            }
            if (cheapest > cost) {
                cheapest = cost;
                bestDimension = d;
                bestGroups = groups;
            }
        }
        final List<Node<T>> ordered = new ArrayList<>(bestGroups);
        ordered.sort(byCenters(bestDimension, true));
        final List<Node<T>> built = new ArrayList<>(ordered.size());
        for (final Node<T> group : ordered) {
            built.add(build(Arrays.asList(((Branch<T>) group).children)));
        }
        return new Branch<>(built);
    }

    private static <T> List<Branch<T>> group(final List<Node<T>> sorted) {
        final int perGroup = (int) Math.pow(CHILDREN_PER_NODE,
                Math.floor(Math.log(sorted.size() - 0.01) / Math.log(CHILDREN_PER_NODE)));
        final List<Branch<T>> groups = new ArrayList<>();
        List<Node<T>> current = new ArrayList<>();
        for (final Node<T> node : sorted) {
            current.add(node);
            if (current.size() >= perGroup) {
                groups.add(new Branch<>(current));
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) {
            groups.add(new Branch<>(current));
        }
        return groups;
    }

    /**
     * {@return a new cursor over the table}
     */
    public Cursor<T> cursor() {
        return new Cursor<>(this);
    }

    /**
     * Looks values up in a table, starting each search from the previous answer: neighbouring points usually
     * share their biome, so the search can discard most boxes at once. Belongs to one thread.
     */
    public static final class Cursor<T> {
        private final ClimateTable<T> table;
        private final long[] target = new long[DIMENSIONS];
        private Leaf<T> last;

        private Cursor(final ClimateTable<T> table) {
            this.table = table;
        }

        /**
         * {@return what the box nearest to a climate point points to}; the coordinates are quantized already
         */
        public T find(final long temperature, final long humidity, final long continentalness,
                      final long erosion, final long depth, final long weirdness) {
            target[0] = temperature;
            target[1] = humidity;
            target[2] = continentalness;
            target[3] = erosion;
            target[4] = depth;
            target[5] = weirdness;
            target[6] = 0L;
            final Leaf<T> found = table.root.nearest(target, last);
            last = found;
            return found.value;
        }
    }
}