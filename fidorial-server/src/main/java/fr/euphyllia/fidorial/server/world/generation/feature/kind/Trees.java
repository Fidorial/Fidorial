package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.data.Orientable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

/**
 * Trees: a trunk, a crown of leaves on each point the trunk leaves for it, sometimes roots, then decorations such
 * as vines or a bee nest. Once built, the leaves learn how far they are from a log.
 */
final class Trees {

    private Trees() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("tree", Trees::parse);
    }

    /**
     * Where a crown grows, and how wide its base is.
     *
     * @param x            where the crown is centred
     * @param radiusOffset how much wider the crown is than usual
     * @param heightOffset how much taller the crown is than usual
     * @param sizeX        how many trunk columns the crown spans along x
     * @param sizeZ        the same along z
     */
    record Attachment(int x, int y, int z, int radiusOffset, int heightOffset, int sizeX, int sizeZ) {

        static Attachment of(final int x, final int y, final int z, final int radiusOffset, final boolean doubleTrunk) {
            final int size = doubleTrunk ? 2 : 1;
            return new Attachment(x, y, z, radiusOffset, 0, size, size);
        }

        boolean doubleTrunk() {
            return sizeX == 2 && sizeZ == 2;
        }
    }

    /**
     * How wide the trunk needs free room at each height, and how short the tree may be cut when it lacks room.
     */
    private record RoomNeeded(int limit, int upperLimit, int lowerSize, int middleSize, int upperSize, int minClippedHeight,
                              boolean threeLayers) {

        static RoomNeeded read(final JsonObject data) {
            final boolean three = Values.type(data).equals("three_layers_feature_size");
            return new RoomNeeded(
                    integer(data, "limit", 1), integer(data, "upper_limit", 1), integer(data, "lower_size", 0),
                    integer(data, "middle_size", 1), integer(data, "upper_size", 1),
                    data.has("min_clipped_height") ? data.get("min_clipped_height").getAsInt() : -1, three);
        }

        int sizeAt(final int treeHeight, final int y) {
            if (!threeLayers) {
                return y < limit ? lowerSize : upperSize;
            }
            if (y < limit) {
                return lowerSize;
            }
            return y >= treeHeight - upperLimit ? upperSize : middleSize;
        }
    }

    static int integer(final JsonObject data, final String key, final int fallback) {
        return data.has(key) ? data.get(key).getAsInt() : fallback;
    }

    private static Feature parse(final FeatureLibrary library, final JsonObject data) {
        final List<TreeDecorations.Decoration> decorations = new ArrayList<>();
        if (data.has("decorators")) {
            for (final JsonElement decorator : data.getAsJsonArray("decorators")) {
                decorations.add(TreeDecorations.parse(library, decorator.getAsJsonObject()));
            }
        }
        return new Tree(
                library.source(data.get("trunk_provider")),
                TrunkShapes.parse(library, data.getAsJsonObject("trunk_placer")),
                library.source(data.get("foliage_provider")),
                CrownShapes.parse(data.getAsJsonObject("foliage_placer")),
                data.has("root_placer") ? MangroveRoots.parse(library, data.getAsJsonObject("root_placer")) : null,
                RoomNeeded.read(data.getAsJsonObject("minimum_size")),
                List.copyOf(decorations),
                data.has("ignore_vines") && data.get("ignore_vines").getAsBoolean(),
                data.has("below_trunk_provider") ? library.source(data.get("below_trunk_provider")) : null);
    }

    /**
     * A tree, as the data describes it.
     */
    record Tree(StateSource trunk, TrunkShapes.Shape trunkShape, StateSource foliage, CrownShapes.Shape crown,
                @Nullable MangroveRoots roots, RoomNeeded room, List<TreeDecorations.Decoration> decorations,
                boolean ignoreVines, @Nullable StateSource belowTrunk) implements Feature {

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            final Build build = new Build(region, random, this);
            if (!grow(build, x, y, z) || build.logs.isEmpty() && build.leaves.isEmpty()) {
                return false;
            }
            if (!decorations.isEmpty()) {
                final TreeDecorations.Context context = new TreeDecorations.Context(build);
                for (final TreeDecorations.Decoration decoration : decorations) {
                    decoration.place(context);
                }
            }
            return build.settleLeaves();
        }

        private boolean grow(final Build build, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            final int treeHeight = trunkShape.height(random);
            final int foliageHeight = crown.foliageHeight(random, treeHeight);
            final int trunkHeight = treeHeight - foliageHeight;
            final int leafRadius = crown.foliageRadius(random, trunkHeight);
            final int trunkY = roots == null ? y : y + roots.trunkOffset(random);
            final int minY = Math.min(y, trunkY);
            final int maxY = Math.max(y, trunkY) + treeHeight + 1;
            if (minY < build.region.minY() + 1 || maxY > build.region.maxY() + 1) {
                return false;
            }
            final int clipped = freeHeight(build, treeHeight, x, trunkY, z);
            if (clipped < treeHeight && (room.minClippedHeight() < 0 || clipped < room.minClippedHeight())) {
                return false;
            }
            if (roots != null && !roots.place(build, x, y, z, trunkY)) {
                return false;
            }
            for (final Attachment attachment : trunkShape.place(build, clipped, x, trunkY, z)) {
                crown.create(build, clipped, attachment, foliageHeight, leafRadius);
            }
            return true;
        }

        /**
         * {@return how tall the tree can grow before it meets something, two less than the first blocked height}
         */
        private int freeHeight(final Build build, final int treeHeight, final int x, final int y, final int z) {
            for (int dy = 0; dy <= treeHeight + 1; dy++) {
                final int r = room.sizeAt(treeHeight, dy);
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (!trunkShape.isFree(build, x + dx, y + dy, z + dz)
                                || !ignoreVines && build.states.is(build.region.id(x + dx, y + dy, z + dz), BlockTypeKeys.VINE)) {
                            return dy - 2;
                        }
                    }
                }
            }
            return treeHeight;
        }
    }

    /**
     * One tree being built: the blocks set so far, by kind, and the helpers the parts share.
     */
    static final class Build {

        final DecorationRegion region;
        final StateFlags states;
        final BitRandom random;
        final Tree tree;
        final Set<PlantKinds.Spot> roots = new HashSet<>();
        final Set<PlantKinds.Spot> logs = new HashSet<>();
        final Set<PlantKinds.Spot> leaves = new HashSet<>();
        final Set<PlantKinds.Spot> decorations = new HashSet<>();
        private final StateFlags.Membership replaceableByTrees;
        private final StateFlags.Membership logTag;
        private final StateFlags.Membership keepsLeaves;

        Build(final DecorationRegion region, final BitRandom random, final Tree tree) {
            this.region = region;
            this.states = region.states();
            this.random = random;
            this.tree = tree;
            this.replaceableByTrees = states.membership(BlockTypeTagKeys.REPLACEABLE_BY_TREES);
            this.logTag = states.membership(BlockTypeTagKeys.LOGS);
            this.keepsLeaves = states.membership(BlockTypeTagKeys.PREVENTS_NEARBY_LEAF_DECAY);
        }

        boolean validTreePos(final int x, final int y, final int z) {
            final char state = region.id(x, y, z);
            return states.isAir(state) || replaceableByTrees.test(state);
        }

        boolean isLog(final int x, final int y, final int z) {
            return logTag.test(region.id(x, y, z));
        }

        boolean airOrLeaves(final int x, final int y, final int z) {
            final char state = region.id(x, y, z);
            return states.isAir(state) || states.has(state, StateFlags.LEAVES);
        }

        void setRoot(final int x, final int y, final int z, final char state) {
            roots.add(new PlantKinds.Spot(x, y, z));
            region.set(x, y, z, state);
        }

        void setLog(final int x, final int y, final int z, final char state) {
            logs.add(new PlantKinds.Spot(x, y, z));
            region.set(x, y, z, state);
        }

        void setLeaf(final int x, final int y, final int z, final char state) {
            leaves.add(new PlantKinds.Spot(x, y, z));
            region.set(x, y, z, state);
        }

        boolean hasLeaf(final int x, final int y, final int z) {
            return leaves.contains(new PlantKinds.Spot(x, y, z));
        }

        void setDecoration(final int x, final int y, final int z, final char state) {
            decorations.add(new PlantKinds.Spot(x, y, z));
            region.set(x, y, z, state);
        }

        /**
         * Sets the block under the trunk, when the tree has one for it.
         */
        void placeBelowTrunk(final int x, final int y, final int z) {
            if (tree.belowTrunk() == null) {
                return;
            }
            final char state = tree.belowTrunk().pick(region, random, x, y, z);
            if (state != StateSource.NONE) {
                setLog(x, y, z, state);
            }
        }

        /**
         * Sets a log where the tree may grow.
         *
         * @return {@code true} if it was set
         */
        boolean placeLog(final int x, final int y, final int z, final IntUnaryOperator change) {
            if (!tree.trunkShape().validPos(this, x, y, z)) {
                return false;
            }
            setLog(x, y, z, (char) change.applyAsInt(tree.trunk().pickOrKeep(region, random, x, y, z)));
            return true;
        }

        boolean placeLog(final int x, final int y, final int z) {
            return placeLog(x, y, z, IntUnaryOperator.identity());
        }

        IntUnaryOperator axis(final Orientable.Axis axis) {
            return state -> states.with((char) state, BlockProperties.AXIS, axis);
        }

        /**
         * Sets a leaf block where the tree may grow and no lasting leaf is already there.
         *
         * @return {@code true} if it was set
         */
        boolean tryLeaf(final int x, final int y, final int z) {
            final char current = region.id(x, y, z);
            if (Boolean.TRUE.equals(states.get(current, BlockProperties.PERSISTENT)) || !validTreePos(x, y, z)) {
                return false;
            }
            char state = tree.foliage().pickOrKeep(region, random, x, y, z);
            state = states.with(state, BlockProperties.WATERLOGGED, states.has(current, StateFlags.WATER_SOURCE));
            setLeaf(x, y, z, state);
            return true;
        }

        /**
         * Gives each leaf its distance to the nearest log, walking out from the logs one step at a time.
         *
         * @return {@code false} if the tree set no block at all
         */
        boolean settleLeaves() {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            final List<Set<PlantKinds.Spot>> all = List.of(roots, logs, leaves, decorations);
            for (final Set<PlantKinds.Spot> set : all) {
                for (final PlantKinds.Spot spot : set) {
                    minX = Math.min(minX, spot.x());
                    minY = Math.min(minY, spot.y());
                    minZ = Math.min(minZ, spot.z());
                    maxX = Math.max(maxX, spot.x());
                    maxY = Math.max(maxY, spot.y());
                    maxZ = Math.max(maxZ, spot.z());
                }
            }
            if (minX == Integer.MAX_VALUE) {
                return false;
            }
            final int spanX = maxX - minX + 1;
            final int spanY = maxY - minY + 1;
            final int spanZ = maxZ - minZ + 1;
            final BitSet done = new BitSet(spanX * spanY * spanZ);
            final int fMinX = minX;
            final int fMinY = minY;
            final int fMinZ = minZ;
            final ToIntFunction<int[]> index = p -> ((p[0] - fMinX) * spanY + (p[1] - fMinY)) * spanZ + (p[2] - fMinZ);
            for (final Set<PlantKinds.Spot> set : List.of(logs, decorations, roots)) {
                for (final PlantKinds.Spot spot : set) {
                    done.set(index.applyAsInt(new int[] {spot.x(), spot.y(), spot.z()}));
                }
            }
            List<int[]> current = new ArrayList<>();
            for (final PlantKinds.Spot log : logs) {
                current.add(new int[] {log.x(), log.y(), log.z()});
            }
            List<int[]> next = new ArrayList<>();
            int distance = 0;
            while (!current.isEmpty() || !next.isEmpty()) {
                if (current.isEmpty()) {
                    current = next;
                    next = new ArrayList<>();
                    distance++;
                }
                final int[] at = current.removeLast();
                final int neighbourDistance = distance + 1;
                for (final int direction : Facing.ALL) {
                    final int nx = at[0] + Facing.DX[direction];
                    final int ny = at[1] + Facing.DY[direction];
                    final int nz = at[2] + Facing.DZ[direction];
                    if (nx < minX || nx > maxX || ny < minY || ny > maxY || nz < minZ || nz > maxZ) {
                        continue;
                    }
                    final int slot = index.applyAsInt(new int[] {nx, ny, nz});
                    if (done.get(slot)) {
                        continue;
                    }
                    final char state = region.id(nx, ny, nz);
                    final int existing = keepsLeaves.test(state) ? 0 : distanceOf(state);
                    if (existing >= 0 && existing >= neighbourDistance) {
                        region.set(nx, ny, nz, states.with(state, BlockProperties.DISTANCE_1_7, neighbourDistance));
                        done.set(slot);
                        if (neighbourDistance + 1 < 7) {
                            next.add(new int[] {nx, ny, nz});
                        }
                    }
                }
            }
            return true;
        }

        private int distanceOf(final char state) {
            final Integer value = states.get(state, BlockProperties.DISTANCE_1_7);
            return value == null ? -1 : value;
        }
    }

    /**
     * The roots of a mangrove: the trunk starts above the ground, held by roots that spread and bend down from
     * its foot, turning into muddy roots in mud.
     */
    record MangroveRoots(IntSource trunkOffset, StateSource rootState, @Nullable StateSource aboveRoot, float aboveRootChance,
                         StateFlags.Membership growThrough, StateFlags.Membership muddyIn, StateSource muddyRoots,
                         int maxWidth, int maxLength, float skewChance) {

        static MangroveRoots parse(final FeatureLibrary library, final JsonObject data) {
            final JsonObject placement = data.getAsJsonObject("mangrove_root_placement");
            final JsonObject above = data.has("above_root_placement") ? data.getAsJsonObject("above_root_placement") : null;
            return new MangroveRoots(IntSource.parse(data.get("trunk_offset_y")), library.source(data.get("root_provider")),
                    above == null ? null : library.source(above.get("above_root_provider")),
                    above == null ? 0.0F : above.get("above_root_placement_chance").getAsFloat(),
                    library.blockSet(placement.get("can_grow_through")), library.blockSet(placement.get("muddy_roots_in")),
                    library.source(placement.get("muddy_roots_provider")), placement.get("max_root_width").getAsInt(),
                    placement.get("max_root_length").getAsInt(), placement.get("random_skew_chance").getAsFloat());
        }

        int trunkOffset(final BitRandom random) {
            return trunkOffset.sample(random);
        }

        private boolean canPlace(final Build build, final int x, final int y, final int z) {
            return build.validTreePos(x, y, z) || growThrough.test(build.region.id(x, y, z));
        }

        boolean place(final Build build, final int x, final int y, final int z, final int trunkY) {
            for (int py = y; py < trunkY; py++) {
                if (!canPlace(build, x, py, z)) {
                    return false;
                }
            }
            final List<int[]> positions = new ArrayList<>();
            positions.add(new int[] {x, trunkY - 1, z});
            for (final int direction : Facing.HORIZONTAL) {
                final int px = x + Facing.DX[direction];
                final int pz = z + Facing.DZ[direction];
                final List<int[]> branch = new ArrayList<>();
                if (!simulate(build, px, trunkY, pz, direction, x, trunkY, z, branch, 0)) {
                    return false;
                }
                positions.addAll(branch);
                positions.add(new int[] {px, trunkY, pz});
            }
            for (final int[] position : positions) {
                placeRoot(build, position[0], position[1], position[2]);
            }
            return true;
        }

        private boolean simulate(final Build build, final int x, final int y, final int z, final int direction,
                                 final int ox, final int oy, final int oz, final List<int[]> found, final int layer) {
            if (layer == maxLength || found.size() > maxLength) {
                return false;
            }
            for (final int[] next : nextPositions(build.random, x, y, z, direction, ox, oy, oz)) {
                if (canPlace(build, next[0], next[1], next[2])) {
                    found.add(next);
                    if (!simulate(build, next[0], next[1], next[2], direction, ox, oy, oz, found, layer + 1)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private List<int[]> nextPositions(final BitRandom random, final int x, final int y, final int z, final int direction,
                                          final int ox, final int oy, final int oz) {
            final int[] below = {x, y - 1, z};
            final int[] beside = {x + Facing.DX[direction], y, z + Facing.DZ[direction]};
            final int width = Math.abs(x - ox) + Math.abs(y - oy) + Math.abs(z - oz);
            if (width > maxWidth - 3 && width <= maxWidth) {
                return random.nextFloat() < skewChance ? List.of(below, new int[] {beside[0], beside[1] - 1, beside[2]}) : List.of(below);
            }
            if (width > maxWidth) {
                return List.of(below);
            }
            if (random.nextFloat() < skewChance) {
                return List.of(below);
            }
            return random.nextBoolean() ? List.of(beside) : List.of(below);
        }

        private void placeRoot(final Build build, final int x, final int y, final int z) {
            final DecorationRegion region = build.region;
            if (muddyIn.test(region.id(x, y, z))) {
                build.setRoot(x, y, z, waterlogged(build, muddyRoots.pickOrKeep(region, build.random, x, y, z), x, y, z));
                return;
            }
            if (!canPlace(build, x, y, z)) {
                return;
            }
            build.setRoot(x, y, z, waterlogged(build, rootState.pickOrKeep(region, build.random, x, y, z), x, y, z));
            if (aboveRoot != null && build.random.nextFloat() < aboveRootChance && region.isAir(x, y + 1, z)) {
                build.setRoot(x, y + 1, z, waterlogged(build, aboveRoot.pickOrKeep(region, build.random, x, y + 1, z), x, y + 1, z));
            }
        }

        private static char waterlogged(final Build build, final char state, final int x, final int y, final int z) {
            return build.states.with(state, BlockProperties.WATERLOGGED, build.region.has(x, y, z, StateFlags.WATER));
        }
    }

    /**
     * {@return a list sorted by height, ties kept in the order the set gives them}
     */
    static List<PlantKinds.Spot> byHeight(final Set<PlantKinds.Spot> set) {
        final List<PlantKinds.Spot> list = new ArrayList<>(set);
        list.sort(Comparator.comparingInt(PlantKinds.Spot::y));
        return list;
    }
}
