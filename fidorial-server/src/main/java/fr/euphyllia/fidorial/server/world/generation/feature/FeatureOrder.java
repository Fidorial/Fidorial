package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The order of the features of each decoration step, the same for every chunk of a generator.
 *
 * <p>Each biome lists its features in an order the others must not contradict: a feature listed before another
 * by one biome comes first everywhere. Every feature gets a number, by order of first mention when the biomes
 * are read one after the other; the steps are then laid out by a depth-first walk that respects every biome,
 * starting from the lowest numbers. The position of a feature in its step seeds its stream, so the same feature
 * draws the same values in every chunk.</p>
 */
public final class FeatureOrder {

    private record Node(int step, int number, String feature) {
    }

    private static final Comparator<Node> ORDER = Comparator.comparingInt(Node::step).thenComparingInt(Node::number);

    private final List<List<String>> steps;
    private final List<Map<String, Integer>> positions;

    private FeatureOrder(final List<List<String>> steps) {
        this.steps = steps;
        this.positions = new ArrayList<>();
        for (final List<String> step : steps) {
            final Map<String, Integer> position = new HashMap<>();
            for (int i = 0; i < step.size(); i++) {
                position.put(step.get(i), i);
            }
            positions.add(position);
        }
    }

    /**
     * Lays out the steps.
     *
     * @param biomes the biomes the generator can place, in the order its biome source lists them
     */
    public static FeatureOrder of(final List<BiomeInfo> biomes, final WorldgenData data) {
        final Map<String, Integer> numbers = new HashMap<>();
        final Map<Node, Set<Node>> edges = new TreeMap<>(ORDER);
        int stepCount = 0;
        for (final BiomeInfo biome : biomes) {
            final List<Node> listed = new ArrayList<>();
            stepCount = Math.max(stepCount, biome.features().size());
            for (int step = 0; step < biome.features().size(); step++) {
                for (final String entry : biome.features().get(step)) {
                    for (final String feature : expand(data, entry)) {
                        final int number = numbers.computeIfAbsent(feature, key -> numbers.size());
                        listed.add(new Node(step, number, feature));
                    }
                }
            }
            for (int i = 0; i < listed.size(); i++) {
                final Set<Node> next = edges.computeIfAbsent(listed.get(i), key -> new TreeSet<>(ORDER));
                if (i + 1 < listed.size()) {
                    next.add(listed.get(i + 1));
                }
            }
        }

        final Set<Node> done = new TreeSet<>(ORDER);
        final Set<Node> visiting = new TreeSet<>(ORDER);
        final List<Node> laidOut = new ArrayList<>();
        for (final Node node : edges.keySet()) {
            if (!done.contains(node) && walk(edges, done, visiting, laidOut, node)) {
                throw new IllegalStateException("The biomes order their features in a loop, around " + node.feature());
            }
        }
        Collections.reverse(laidOut);

        final List<List<String>> steps = new ArrayList<>();
        for (int step = 0; step < stepCount; step++) {
            final List<String> features = new ArrayList<>();
            for (final Node node : laidOut) {
                if (node.step() == step) {
                    features.add(node.feature());
                }
            }
            steps.add(List.copyOf(features));
        }
        return new FeatureOrder(List.copyOf(steps));
    }

    /**
     * Visits a feature after everything that must follow it.
     *
     * @return {@code true} if a loop was found
     */
    private static boolean walk(final Map<Node, Set<Node>> edges, final Set<Node> done, final Set<Node> visiting,
                                final List<Node> laidOut, final Node node) {
        if (done.contains(node)) {
            return false;
        }
        if (visiting.contains(node)) {
            return true;
        }
        visiting.add(node);
        for (final Node next : edges.getOrDefault(node, Set.of())) {
            if (walk(edges, done, visiting, laidOut, next)) {
                return true;
            }
        }
        visiting.remove(node);
        done.add(node);
        laidOut.add(node);
        return false;
    }

    /**
     * {@return the placed features an entry of a biome stands for: itself, or every member of a tag}
     */
    static List<String> expand(final WorldgenData data, final String entry) {
        if (entry.startsWith("#")) {
            return data.tag(WorldgenData.Section.PLACED_FEATURE_TAG, entry.substring(1));
        }
        return List.of(WorldgenData.normalize(entry));
    }

    public int stepCount() {
        return steps.size();
    }

    /**
     * {@return the features of a step, in order}
     */
    public List<String> step(final int step) {
        return steps.get(step);
    }

    /**
     * {@return the position of a feature in a step, {@code -1} when it is not part of it}
     */
    public int position(final int step, final String feature) {
        return positions.get(step).getOrDefault(feature, -1);
    }
}
