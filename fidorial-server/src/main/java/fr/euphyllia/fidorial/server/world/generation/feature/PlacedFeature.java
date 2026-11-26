package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

import java.util.Arrays;
import java.util.List;

/**
 * A feature with the steps that choose where it goes.
 *
 * <p>The steps run depth first: every position a step gives goes through all the following steps before the next
 * position of that step is looked at, which fixes the order in which the stream is drawn.</p>
 *
 * @param id         the identifier, used to check the biomes; empty for a placement written inline
 * @param feature    what is built at the final positions
 * @param placements the steps, in order
 */
public record PlacedFeature(String id, Feature feature, List<Placement> placements) {

    public PlacedFeature {
        placements = List.copyOf(placements);
    }

    /**
     * Places the feature from an origin.
     *
     * @param biomeCheck whether the biome of the positions must list this feature
     * @return {@code true} if anything was built
     */
    public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                         final boolean biomeCheck) {
        if (placements.isEmpty()) {
            return feature.place(region, random, x, y, z);
        }
        final PlacementRun run = new PlacementRun(region, biomeCheck ? this : null);
        final Stack stack = new Stack();
        final Stack produced = new Stack();
        stack.push(x, y, z, 0);
        boolean placedAny = false;
        while (stack.size > 0) {
            stack.size--;
            final int index = stack.size;
            final int px = stack.xs[index];
            final int py = stack.ys[index];
            final int pz = stack.zs[index];
            final int step = stack.steps[index];
            produced.size = 0;
            placements.get(step).apply(run, random, px, py, pz, (ox, oy, oz) -> produced.push(ox, oy, oz, 0));
            final int next = step + 1;
            if (next < placements.size()) {
                for (int i = produced.size - 1; i >= 0; i--) {
                    stack.push(produced.xs[i], produced.ys[i], produced.zs[i], next);
                }
            } else {
                for (int i = 0; i < produced.size; i++) {
                    placedAny |= feature.place(region, random, produced.xs[i], produced.ys[i], produced.zs[i]);
                }
            }
        }
        return placedAny;
    }

    /**
     * Positions waiting for a step, with the index of that step.
     */
    private static final class Stack {
        int[] xs = new int[16];
        int[] ys = new int[16];
        int[] zs = new int[16];
        int[] steps = new int[16];
        int size;

        void push(final int x, final int y, final int z, final int step) {
            if (size == xs.length) {
                final int grown = size * 2;
                xs = Arrays.copyOf(xs, grown);
                ys = Arrays.copyOf(ys, grown);
                zs = Arrays.copyOf(zs, grown);
                steps = Arrays.copyOf(steps, grown);
            }
            xs[size] = x;
            ys[size] = y;
            zs[size] = z;
            steps[size] = step;
            size++;
        }
    }
}
