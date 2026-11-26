package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.BlockTest;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.PlacedFeature;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * The features of the nether: basalt deltas and columns, pillars, blobs of another stone in the netherrack,
 * glowstone and wart growing from their neighbours, patches projected on the ground, and huge fungi.
 */
final class NetherKinds {

    /**
     * The blocks a basalt delta does not overwrite.
     */
    static final TagKey<BlockType> DELTA_CANNOT_REPLACE =
            TagKey.withRegistry(RegistryKey.BLOCK, Key.key("fidorial", "delta_cannot_replace"));

    private static final int[][] NEIGHBOURS = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    private static final int NONE = Integer.MIN_VALUE;
    private static final double DELTA_RIM_CHANCE = 0.9;
    private static final float HUGE_FUNGUS_CHANCE = 0.06F;
    private static final float HUGE_STEM_CORNER_CHANCE = 0.1F;
    private static final double HAT_DROP_CHANCE = 0.15;
    private static final int MIN_VINE_AGE = 23;
    private static final int MAX_VINE_AGE = 25;

    private NetherKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("single_block_pillar", NetherKinds::singleBlockPillar);
        parsers.put("random_neighbor_spread", NetherKinds::randomNeighborSpread);
        parsers.put("projected_random_patchy_square", NetherKinds::projectedPatchySquare);
        parsers.put("stepped_column_cluster", SteppedColumns::parse);
        parsers.put("delta_feature", NetherKinds::delta);
        parsers.put("netherrack_replace_blobs", NetherKinds::replaceBlobs);
        parsers.put("huge_fungus", HugeFungus::parse);
    }

    /**
     * Visits a position, and tells whether something was set there.
     */
    @FunctionalInterface
    private interface Visitor {
        boolean visit(int dx, int dy, int dz);
    }

    /**
     * Visits the offsets of a box by growing Manhattan distance, up to a distance: within one distance, by growing
     * x, then growing y, the positive z before its opposite.
     *
     * @return {@code true} if any visit set something
     */
    private static boolean byDistance(final int reachX, final int reachY, final int reachZ, final int maxDistance,
                                      final Visitor visitor) {
        boolean any = false;
        for (int distance = 0; distance <= maxDistance; distance++) {
            final int spanX = Math.min(reachX, distance);
            for (int dx = -spanX; dx <= spanX; dx++) {
                final int spanY = Math.min(reachY, distance - Math.abs(dx));
                for (int dy = -spanY; dy <= spanY; dy++) {
                    final int dz = distance - Math.abs(dx) - Math.abs(dy);
                    if (dz <= reachZ) {
                        any |= visitor.visit(dx, dy, dz);
                        if (dz != 0) {
                            any |= visitor.visit(dx, dy, -dz);
                        }
                    }
                }
            }
        }
        return any;
    }

    private static TypedKey<BlockType> blockOf(final FeatureLibrary library, final char state) {
        return library.context().states().palette().state(state).name();
    }

    /**
     * A pillar of one block, growing up or down through the blocks it may replace, each block a draw away from
     * stopping; a feature may cap it.
     */
    private static Feature singleBlockPillar(final FeatureLibrary library, final JsonObject data) {
        final StateSource block = library.source(data.get("block"));
        final BlockTest canReplace = library.test(data.get("can_replace"));
        final int step = BlockFace.valueOf(data.get("direction").getAsString().toUpperCase(Locale.ROOT)).dy();
        final float chanceToContinue = data.has("chance_to_continue") ? data.get("chance_to_continue").getAsFloat() : 1.0F;
        final @Nullable PlacedFeature cap = data.has("cap_feature") ? library.placedFeature(data.get("cap_feature")) : null;
        return (region, random, x, y, z) -> {
            int py = y;
            while (canReplace.test(region, x, py, z) && random.nextFloat() < chanceToContinue && !region.isOutsideHeight(py)) {
                region.set(x, py, z, block.pickOrKeep(region, random, x, py, z));
                py += step;
            }
            py -= step;
            if (cap != null) {
                cap.place(region, random, x, py, z, false);
            }
            return true;
        };
    }

    /**
     * Sets a block, then tries to grow more of it around: a spot is kept when it may be replaced and exactly one
     * of its six neighbours is an accepted block.
     */
    private static Feature randomNeighborSpread(final FeatureLibrary library, final JsonObject data) {
        final StateSource block = library.source(data.get("block"));
        final StateFlags.Membership accepted = library.blockSet(data.get("accepted_neighbors"));
        final BlockTest canReplace = library.test(data.get("can_replace"));
        final IntSource attempts = IntSource.parse(data.get("attempts"));
        final IntSource xzOffset = IntSource.parse(data.get("xz_offset"));
        final IntSource yOffset = IntSource.parse(data.get("y_offset"));
        return (region, random, x, y, z) -> {
            region.set(x, y, z, block.pickOrKeep(region, random, x, y, z));
            final int tries = attempts.sample(random);
            for (int i = 0; i < tries; i++) {
                final int px = x + xzOffset.sample(random);
                final int py = y + yOffset.sample(random);
                final int pz = z + xzOffset.sample(random);
                if (canReplace.test(region, px, py, pz) && acceptedNeighbours(region, accepted, px, py, pz) == 1) {
                    region.set(px, py, pz, block.pickOrKeep(region, random, px, py, pz));
                }
            }
            return true;
        };
    }

    /**
     * {@return how many of the six neighbours of a block are accepted, counting no further than two}
     */
    private static int acceptedNeighbours(final DecorationRegion region, final StateFlags.Membership accepted,
                                          final int x, final int y, final int z) {
        int count = 0;
        for (final int[] side : NEIGHBOURS) {
            if (accepted.test(region.id(x + side[0], y + side[1], z + side[2])) && ++count > 1) {
                break;
            }
        }
        return count;
    }

    /**
     * A square of blocks, sparser towards its corners, each one dropped through the blocks below it may fall
     * through, a few blocks at most.
     */
    private static Feature projectedPatchySquare(final FeatureLibrary library, final JsonObject data) {
        final StateSource block = library.source(data.get("block"));
        final BlockTest projectThrough = library.test(data.get("project_through"));
        final IntSource size = IntSource.parse(data.get("size"));
        final int maxDrop = data.get("max_projection_height").getAsInt();
        return (region, random, x, y, z) -> {
            final int half = size.sample(random);
            final int bound = half * half + 1;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    if (random.nextInt(bound) >= bound - Math.abs(dx) * Math.abs(dz)) {
                        continue;
                    }
                    final int px = x + dx;
                    final int pz = z + dz;
                    int py = y;
                    int drop = maxDrop;
                    while (projectThrough.test(region, px, py - 1, pz)) {
                        py--;
                        if (--drop <= 0) {
                            break;
                        }
                    }
                    final char state = block.pick(region, random, px, py, pz);
                    if (state != StateSource.NONE) {
                        region.set(px, py, pz, state);
                    }
                }
            }
            return true;
        };
    }

    /**
     * A pool of one block in a floor, sometimes with a rim of another block: each spot of an ellipse whose
     * neighbours are all solid but the one above, which must be air, takes the rim, and the spot moved by the
     * rim size takes the contents.
     */
    private static Feature delta(final FeatureLibrary library, final JsonObject data) {
        final char contents = library.state(data.get("contents"));
        final char rim = library.state(data.get("rim"));
        final IntSource size = IntSource.parse(data.get("size"));
        final IntSource rimSize = IntSource.parse(data.get("rim_size"));
        final TypedKey<BlockType> contentsBlock = blockOf(library, contents);
        final StateFlags.Membership kept = library.context().states().membership(DELTA_CANNOT_REPLACE);
        return (region, random, x, y, z) -> {
            final boolean withRim = random.nextDouble() < DELTA_RIM_CHANCE;
            final int rimX = withRim ? rimSize.sample(random) : 0;
            final int rimZ = withRim ? rimSize.sample(random) : 0;
            final boolean hasRim = withRim && rimX != 0 && rimZ != 0;
            final int radiusX = size.sample(random);
            final int radiusZ = size.sample(random);
            final int limit = Math.min(Math.max(radiusX, radiusZ), radiusX + radiusZ);
            return byDistance(radiusX, 0, radiusZ, limit, (dx, dy, dz) -> {
                boolean placed = false;
                if (deltaCanTake(region, contentsBlock, kept, x + dx, y, z + dz)) {
                    if (hasRim) {
                        region.set(x + dx, y, z + dz, rim);
                        placed = true;
                    }
                    if (deltaCanTake(region, contentsBlock, kept, x + dx + rimX, y, z + dz + rimZ)) {
                        region.set(x + dx + rimX, y, z + dz + rimZ, contents);
                        placed = true;
                    }
                }
                return placed;
            });
        };
    }

    private static boolean deltaCanTake(final DecorationRegion region, final TypedKey<BlockType> contents,
                                        final StateFlags.Membership kept, final int x, final int y, final int z) {
        final StateFlags states = region.states();
        final char current = region.id(x, y, z);
        if (states.is(current, contents) || kept.test(current)) {
            return false;
        }
        for (final int[] side : NEIGHBOURS) {
            final boolean up = side[1] == 1;
            if (region.isAir(x + side[0], y + side[1], z + side[2]) != up) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replaces the blocks of a target within a lumpy ball, centred on the first target block found going down from
     * the origin.
     */
    private static Feature replaceBlobs(final FeatureLibrary library, final JsonObject data) {
        final TypedKey<BlockType> target = blockOf(library, library.state(data.get("target")));
        final char replacement = library.state(data.get("state"));
        final IntSource radius = IntSource.parse(data.get("radius"));
        return (region, random, x, y, z) -> {
            final StateFlags states = region.states();
            int cy = Math.clamp(y, region.minY() + 1, region.maxY());
            while (cy > region.minY() + 1 && !states.is(region.id(x, cy, z), target)) {
                cy--;
            }
            if (cy <= region.minY() + 1) {
                return false;
            }
            final int centerY = cy;
            final int radiusX = radius.sample(random);
            final int radiusY = radius.sample(random);
            final int radiusZ = radius.sample(random);
            final int limit = Math.min(Math.max(radiusX, Math.max(radiusY, radiusZ)), radiusX + radiusY + radiusZ);
            return byDistance(radiusX, radiusY, radiusZ, limit, (dx, dy, dz) -> {
                if (states.is(region.id(x + dx, centerY + dy, z + dz), target)) {
                    region.set(x + dx, centerY + dy, z + dz, replacement);
                    return true;
                }
                return false;
            });
        };
    }

    /**
     * Clusters of columns, such as basalt in the deltas: columns drawn around an origin grow shorter away from it,
     * each one a small square of stacks that rise from the floor, or start from the air above when the spot is
     * taken.
     */
    private record SteppedColumns(StateSource block, BlockTest continueThrough, BlockTest canReplace,
                                  StateFlags.Membership cannotPlaceOn, IntSource clusterReach, IntSource columnCount,
                                  IntSource columnReach, IntSource height) implements Feature {

        static Feature parse(final FeatureLibrary library, final JsonObject data) {
            return new SteppedColumns(library.source(data.get("block")), library.test(data.get("continue_through")),
                    library.test(data.get("can_replace")), library.blockSet(data.get("cannot_place_on")),
                    IntSource.parse(data.get("cluster_reach")), IntSource.parse(data.get("column_count")),
                    IntSource.parse(data.get("column_reach")), IntSource.parse(data.get("height")));
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            if (!canStandAt(region, x, y, z)) {
                return false;
            }
            final int columnHeight = height.sample(random);
            final int reach = Math.min(columnHeight, clusterReach.sample(random));
            final int count = columnCount.sample(random);
            final int width = 2 * reach + 1;
            boolean placed = false;
            for (int i = 0; i < count; i++) {
                final int px = x - reach + random.nextInt(width);
                final int py = y + random.nextInt(1);
                final int pz = z - reach + random.nextInt(width);
                final int blocks = columnHeight - (Math.abs(px - x) + Math.abs(py - y) + Math.abs(pz - z));
                if (blocks >= 0) {
                    placed |= placeColumn(region, random, px, py, pz, blocks, columnReach.sample(random));
                }
            }
            return placed;
        }

        private boolean placeColumn(final DecorationRegion region, final BitRandom random, final int x, final int y,
                                    final int z, final int columnHeight, final int reach) {
            boolean placed = false;
            for (int pz = z - reach; pz <= z + reach; pz++) {
                for (int px = x - reach; px <= x + reach; px++) {
                    final int distance = Math.abs(px - x) + Math.abs(pz - z);
                    final int start = canReplace.test(region, px, y, pz)
                            ? findFloor(region, px, y, pz, distance)
                            : findAir(region, px, y, pz, distance);
                    if (start == NONE) {
                        continue;
                    }
                    int py = start;
                    for (int blocks = columnHeight - distance / 2; blocks >= 0; blocks--) {
                        if (canReplace.test(region, px, py, pz)) {
                            region.set(px, py, pz, block.pickOrKeep(region, random, px, py, pz));
                            placed = true;
                        } else if (!continueThrough.test(region, px, py, pz)) {
                            break;
                        }
                        py++;
                    }
                }
            }
            return placed;
        }

        /**
         * {@return the first height going down where a column can stand, {@link #NONE} after {@code limit} steps}
         */
        private int findFloor(final DecorationRegion region, final int x, final int y, final int z, final int limit) {
            int py = y;
            for (int left = limit; py > region.minY() + 1 && left > 0; left--) {
                if (canStandAt(region, x, py, z)) {
                    return py;
                }
                py--;
            }
            return NONE;
        }

        private boolean canStandAt(final DecorationRegion region, final int x, final int y, final int z) {
            if (!canReplace.test(region, x, y, z)) {
                return false;
            }
            final char below = region.id(x, y - 1, z);
            return !region.states().isAir(below) && !cannotPlaceOn.test(below);
        }

        /**
         * {@return the first air going up, {@link #NONE} after {@code limit} steps or on a block columns cannot stand
         * on}
         */
        private int findAir(final DecorationRegion region, final int x, final int y, final int z, final int limit) {
            int py = y;
            for (int left = limit; py <= region.maxY() && left > 0; left--) {
                final char current = region.id(x, py, z);
                if (cannotPlaceOn.test(current)) {
                    return NONE;
                }
                if (region.states().isAir(current)) {
                    return py;
                }
                py++;
            }
            return NONE;
        }
    }

    /**
     * A huge fungus: a stem, sometimes three blocks wide, under a hat of wart blocks dotted with light blocks; from
     * the hats of the crimson ones hang weeping vines.
     */
    private record HugeFungus(TypedKey<BlockType> base, char stem, char hat, char decor, BlockTest replaceable,
                              boolean planted, boolean vines, int depth) implements Feature {

        static Feature parse(final FeatureLibrary library, final JsonObject data) {
            final char hat = library.state(data.get("hat_state"));
            return new HugeFungus(blockOf(library, library.state(data.get("valid_base_block"))),
                    library.state(data.get("stem_state")), hat, library.state(data.get("decor_state")),
                    library.test(data.get("replaceable_blocks")),
                    data.has("planted") && data.get("planted").getAsBoolean(),
                    library.context().states().is(hat, BlockTypeKeys.NETHER_WART_BLOCK),
                    library.context().generator().settings().height());
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            if (!region.states().is(region.id(x, y - 1, z), base)) {
                return false;
            }
            int totalHeight = random.nextIntInRange(4, 13);
            if (random.nextInt(12) == 0) {
                totalHeight *= 2;
            }
            if (!planted && y + totalHeight + 1 >= depth) {
                return false;
            }
            final boolean huge = !planted && random.nextFloat() < HUGE_FUNGUS_CHANCE;
            region.set(x, y, z, region.id(region.blocks().air));
            placeStem(region, random, x, y, z, totalHeight, huge);
            placeHat(region, random, x, y, z, totalHeight, huge);
            return true;
        }

        private boolean replaceable(final DecorationRegion region, final int x, final int y, final int z, final boolean plants) {
            return region.has(x, y, z, StateFlags.REPLACEABLE) || (plants && replaceable.test(region, x, y, z));
        }

        private void clearForPlanted(final DecorationRegion region, final int x, final int y, final int z) {
            if (planted && !region.isAir(x, y - 1, z)) {
                region.set(x, y, z, region.id(region.blocks().air));
            }
        }

        private void placeStem(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                               final int totalHeight, final boolean huge) {
            final int radius = huge ? 1 : 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    final boolean corner = huge && Math.abs(dx) == radius && Math.abs(dz) == radius;
                    for (int dy = 0; dy < totalHeight; dy++) {
                        final int px = x + dx;
                        final int py = y + dy;
                        final int pz = z + dz;
                        if (!replaceable(region, px, py, pz, true)) {
                            continue;
                        }
                        if (planted) {
                            clearForPlanted(region, px, py, pz);
                            region.set(px, py, pz, stem);
                        } else if (!corner || random.nextFloat() < HUGE_STEM_CORNER_CHANCE) {
                            region.set(px, py, pz, stem);
                        }
                    }
                }
            }
        }

        private void placeHat(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                              final int totalHeight, final boolean huge) {
            final int hatHeight = Math.min(random.nextInt(1 + totalHeight / 3) + 5, totalHeight);
            final int hatStart = totalHeight - hatHeight;
            for (int dy = hatStart; dy <= totalHeight; dy++) {
                int radius = dy < totalHeight - random.nextInt(3) ? 2 : 1;
                if (hatHeight > 8 && dy < hatStart + 4) {
                    radius = 3;
                }
                if (huge) {
                    radius++;
                }
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        final boolean edgeX = dx == -radius || dx == radius;
                        final boolean edgeZ = dz == -radius || dz == radius;
                        final boolean inside = !edgeX && !edgeZ && dy != totalHeight;
                        final boolean corner = edgeX && edgeZ;
                        final int px = x + dx;
                        final int py = y + dy;
                        final int pz = z + dz;
                        if (!replaceable(region, px, py, pz, false)) {
                            continue;
                        }
                        clearForPlanted(region, px, py, pz);
                        if (dy < hatStart + 3) {
                            if (!inside) {
                                placeHatDrop(region, random, px, py, pz);
                            }
                        } else if (inside) {
                            placeHatBlock(region, random, px, py, pz, 0.1F, 0.2F, vines ? 0.1F : 0.0F);
                        } else if (corner) {
                            placeHatBlock(region, random, px, py, pz, 0.01F, 0.7F, vines ? 0.083F : 0.0F);
                        } else {
                            placeHatBlock(region, random, px, py, pz, 5.0E-4F, 0.98F, vines ? 0.07F : 0.0F);
                        }
                    }
                }
            }
        }

        private void placeHatBlock(final DecorationRegion region, final BitRandom random, final int x, final int y,
                                   final int z, final float decorChance, final float hatChance, final float vineChance) {
            if (random.nextFloat() < decorChance) {
                region.set(x, y, z, decor);
            } else if (random.nextFloat() < hatChance) {
                region.set(x, y, z, hat);
                if (random.nextFloat() < vineChance) {
                    hangVines(region, random, x, y, z);
                }
            }
        }

        /**
         * Sets a block of the hat bottom: always under another hat block, otherwise by chance.
         */
        private void placeHatDrop(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            final StateFlags states = region.states();
            if (states.palette().state(region.id(x, y - 1, z)).name().equals(states.palette().state(hat).name())) {
                region.set(x, y, z, hat);
            } else if (random.nextFloat() < HAT_DROP_CHANCE) {
                region.set(x, y, z, hat);
                if (vines && random.nextInt(11) == 0) {
                    hangVines(region, random, x, y, z);
                }
            }
        }

        /**
         * Hangs weeping vines under a hat block, ending with a tip of a random age.
         */
        private static void hangVines(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            if (!region.isAir(x, y - 1, z)) {
                return;
            }
            int length = random.nextIntInRange(1, 5);
            if (random.nextInt(7) == 0) {
                length *= 2;
            }
            final StateFlags states = region.states();
            final char body = region.id(region.blocks().state(BlockTypeKeys.WEEPING_VINES_PLANT));
            final char tip = region.id(region.blocks().state(BlockTypeKeys.WEEPING_VINES));
            int py = y - 1;
            for (int placed = 0; placed <= length; placed++) {
                if (region.isAir(x, py, z)) {
                    if (placed == length || !region.isAir(x, py - 1, z)) {
                        region.set(x, py, z, states.with(tip, "age", Integer.toString(random.nextIntInRange(MIN_VINE_AGE, MAX_VINE_AGE))));
                        break;
                    }
                    region.set(x, py, z, body);
                }
                py--;
            }
        }
    }
}
