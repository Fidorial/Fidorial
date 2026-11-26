package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.BlockTest;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.HeightmapKind;
import fr.euphyllia.fidorial.server.world.generation.feature.PlacedFeature;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The features that grow plants over an area: patches of moss or clay with plants on them, rooted azaleas,
 * bamboo, vines, and the lichens and veins that cover cave walls.
 */
final class PlantKinds {

    private PlantKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("vegetation_patch", (library, data) -> new Patch(library, data, false));
        parsers.put("waterlogged_vegetation_patch", (library, data) -> new Patch(library, data, true));
        parsers.put("root_system", PlantKinds::rootSystem);
        parsers.put("bamboo", PlantKinds::bamboo);
        parsers.put("vines", (library, data) -> PlantKinds::vines);
        parsers.put("multiface_growth", PlantKinds::multifaceGrowth);
    }

    /**
     * A block position whose hash spreads positions the same way across a hash set as the rest of the
     * generation expects, so that walking a set of them draws the stream in the same order.
     */
    record Spot(int x, int y, int z) {

        @Override
        public int hashCode() {
            return (y + z * 31) * 31 + x;
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof final Spot spot && spot.x == x && spot.y == y && spot.z == z;
        }
    }

    /**
     * A patch of ground blocks laid over the floor or the ceiling, with a feature planted on some of them; the
     * waterlogged kind fills the enclosed ground blocks with water instead.
     */
    private static final class Patch implements Feature {

        private final StateFlags.Membership replaceable;
        private final StateSource ground;
        private final PlacedFeature vegetation;
        private final boolean ceiling;
        private final IntSource depth;
        private final float extraBottomChance;
        private final int verticalRange;
        private final float vegetationChance;
        private final IntSource radius;
        private final float extraEdgeChance;
        private final boolean waterlogged;

        Patch(final FeatureLibrary library, final JsonObject data, final boolean waterlogged) {
            this.replaceable = library.blockSet(data.get("replaceable"));
            this.ground = library.source(data.get("ground_state"));
            this.vegetation = library.placedFeature(data.get("vegetation_feature"));
            this.ceiling = "ceiling".equals(data.get("surface").getAsString());
            this.depth = IntSource.parse(data.get("depth"));
            this.extraBottomChance = data.get("extra_bottom_block_chance").getAsFloat();
            this.verticalRange = data.get("vertical_range").getAsInt();
            this.vegetationChance = data.get("vegetation_chance").getAsFloat();
            this.radius = IntSource.parse(data.get("xz_radius"));
            this.extraEdgeChance = data.get("extra_edge_column_chance").getAsFloat();
            this.waterlogged = waterlogged;
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            final int xRadius = radius.sample(random) + 1;
            final int zRadius = radius.sample(random) + 1;
            Set<Spot> surface = groundPatch(region, random, x, y, z, xRadius, zRadius);
            if (waterlogged) {
                final Set<Spot> enclosed = new HashSet<>();
                for (final Spot spot : surface) {
                    if (!exposed(region, spot)) {
                        enclosed.add(spot);
                    }
                }
                for (final Spot spot : enclosed) {
                    region.set(spot.x(), spot.y(), spot.z(), region.blocks().water);
                }
                surface = enclosed;
            }
            for (final Spot spot : surface) {
                if (vegetationChance > 0.0F && random.nextFloat() < vegetationChance) {
                    plant(region, random, spot);
                }
            }
            return !surface.isEmpty();
        }

        private void plant(final DecorationRegion region, final BitRandom random, final Spot spot) {
            final int outwards = ceiling ? -1 : 1;
            if (!waterlogged) {
                vegetation.place(region, random, spot.x(), spot.y() + outwards, spot.z(), false);
                return;
            }
            // the plant goes into the water, one block lower than on dry ground
            if (vegetation.place(region, random, spot.x(), spot.y() + outwards - 1, spot.z(), false)) {
                final StateFlags states = region.states();
                final char placed = region.id(spot.x(), spot.y(), spot.z());
                if ("false".equals(states.property(placed, "waterlogged"))) {
                    region.set(spot.x(), spot.y(), spot.z(), states.with(placed, "waterlogged", "true"));
                }
            }
        }

        private static boolean exposed(final DecorationRegion region, final Spot spot) {
            final StateFlags states = region.states();
            final int[] sides = {Facing.NORTH, Facing.EAST, Facing.SOUTH, Facing.WEST, Facing.DOWN};
            for (final int side : sides) {
                final char neighbour = region.id(spot.x() + Facing.DX[side], spot.y() + Facing.DY[side], spot.z() + Facing.DZ[side]);
                if (!states.sturdy(neighbour, face(Facing.OPPOSITE[side]))) {
                    return true;
                }
            }
            return false;
        }

        private Set<Spot> groundPatch(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                                      final int xRadius, final int zRadius) {
            final int inwards = ceiling ? 1 : -1;
            final BlockFace towardsPatch = ceiling ? BlockFace.DOWN : BlockFace.UP;
            final Set<Spot> surface = new HashSet<>();
            for (int dx = -xRadius; dx <= xRadius; dx++) {
                final boolean xEdge = dx == -xRadius || dx == xRadius;
                for (int dz = -zRadius; dz <= zRadius; dz++) {
                    final boolean zEdge = dz == -zRadius || dz == zRadius;
                    final boolean corner = xEdge && zEdge;
                    final boolean edgeOnly = (xEdge || zEdge) && !corner;
                    if (corner || edgeOnly && (extraEdgeChance == 0.0F || random.nextFloat() > extraEdgeChance)) {
                        continue;
                    }
                    final int px = x + dx;
                    final int pz = z + dz;
                    int py = y;
                    for (int step = 0; region.isAir(px, py, pz) && step < verticalRange; step++) {
                        py += inwards;
                    }
                    for (int step = 0; !region.isAir(px, py, pz) && step < verticalRange; step++) {
                        py -= inwards;
                    }
                    final int groundY = py + inwards;
                    if (region.isAir(px, py, pz) && region.states().sturdy(region.id(px, groundY, pz), towardsPatch)) {
                        final int layers = depth.sample(random)
                                + (extraBottomChance > 0.0F && random.nextFloat() < extraBottomChance ? 1 : 0);
                        if (layGround(region, random, px, groundY, pz, layers, inwards)) {
                            surface.add(new Spot(px, groundY, pz));
                        }
                    }
                }
            }
            return surface;
        }

        private boolean layGround(final DecorationRegion region, final BitRandom random, final int x, final int startY, final int z,
                                  final int layers, final int inwards) {
            final StateFlags states = region.states();
            int y = startY;
            for (int i = 0; i < layers; i++) {
                final char state = ground.pickOrKeep(region, random, x, y, z);
                final char current = region.id(x, y, z);
                if (!sameBlock(states, state, current)) {
                    if (!replaceable.test(current)) {
                        return i != 0;
                    }
                    region.set(x, y, z, state);
                    y += inwards;
                }
            }
            return true;
        }
    }

    static boolean sameBlock(final StateFlags states, final char a, final char b) {
        return states.palette().state(a).name().equals(states.palette().state(b).name());
    }

    static BlockFace face(final int direction) {
        return switch (direction) {
            case Facing.DOWN -> BlockFace.DOWN;
            case Facing.UP -> BlockFace.UP;
            case Facing.NORTH -> BlockFace.NORTH;
            case Facing.SOUTH -> BlockFace.SOUTH;
            case Facing.WEST -> BlockFace.WEST;
            default -> BlockFace.EAST;
        };
    }

    private static Feature rootSystem(final FeatureLibrary library, final JsonObject data) {
        final PlacedFeature tree = library.placedFeature(data.get("feature"));
        final int space = data.get("required_vertical_space_for_tree").getAsInt();
        final int levelDistance = data.get("level_test_distance").getAsInt();
        final int levelDeviation = data.get("max_level_deviation").getAsInt();
        final int rootRadius = data.get("root_radius").getAsInt();
        final StateFlags.Membership rootReplaceable = library.blockSet(data.get("root_replaceable"));
        final StateSource rootState = library.source(data.get("root_state_provider"));
        final int rootAttempts = data.get("root_placement_attempts").getAsInt();
        final int columnMax = data.get("root_column_max_height").getAsInt();
        final int hangingRadius = data.get("hanging_root_radius").getAsInt();
        final int hangingSpan = data.get("hanging_roots_vertical_span").getAsInt();
        final StateSource hangingState = library.source(data.get("hanging_root_state_provider"));
        final int hangingAttempts = data.get("hanging_root_placement_attempts").getAsInt();
        final int waterAllowed = data.get("allowed_vertical_water_for_tree").getAsInt();
        final BlockTest treeAllowed = library.test(data.get("allowed_tree_position"));
        return (region, random, x, y, z) -> {
            if (!region.isAir(x, y, z)) {
                return false;
            }
            final StateFlags states = region.states();
            boolean grown = false;
            for (int dy = 0; dy < columnMax; dy++) {
                final int ty = y + dy + 1;
                if (region.height(HeightmapKind.WORLD_SURFACE, x, z) < ty) {
                    break;
                }
                if (treeAllowed.test(region, x, ty, z) && roomForTree(region, x, ty, z, space, waterAllowed, levelDistance, levelDeviation)) {
                    final char below = region.id(x, ty - 1, z);
                    if (states.has(below, StateFlags.LAVA) || !states.has(below, StateFlags.SOLID)) {
                        break;
                    }
                    if (tree.place(region, random, x, ty, z, false)) {
                        for (int py = y; py < y + dy; py++) {
                            for (int i = 0; i < rootAttempts; i++) {
                                final int px = x + random.nextInt(rootRadius) - random.nextInt(rootRadius);
                                final int pz = z + random.nextInt(rootRadius) - random.nextInt(rootRadius);
                                if (rootReplaceable.test(region.id(px, py, pz))) {
                                    region.set(px, py, pz, rootState.pickOrKeep(region, random, px, py, pz));
                                }
                            }
                        }
                        grown = true;
                        break;
                    }
                }
            }
            if (grown) {
                for (int i = 0; i < hangingAttempts; i++) {
                    final int px = x + random.nextInt(hangingRadius) - random.nextInt(hangingRadius);
                    final int py = y + random.nextInt(hangingSpan) - random.nextInt(hangingSpan);
                    final int pz = z + random.nextInt(hangingRadius) - random.nextInt(hangingRadius);
                    if (region.isAir(px, py, pz)) {
                        final char state = hangingState.pickOrKeep(region, random, px, py, pz);
                        if (region.canSurvive(state, px, py, pz) && states.sturdy(region.id(px, py + 1, pz), BlockFace.DOWN)) {
                            region.set(px, py, pz, state);
                        }
                    }
                }
            }
            return true;
        };
    }

    private static boolean roomForTree(final DecorationRegion region, final int x, final int y, final int z, final int space,
                                       final int waterAllowed, final int levelDistance, final int levelDeviation) {
        final StateFlags states = region.states();
        for (int i = 1; i <= space; i++) {
            final char state = region.id(x, y + i, z);
            if (!states.isAir(state) && !(i + 1 <= waterAllowed && states.has(state, StateFlags.WATER))) {
                return false;
            }
        }
        if (levelDistance > 0) {
            // south, west, north, east: the order of the horizontal directions by their 2D index
            final int[][] corners = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
            for (final int[] corner : corners) {
                final int cx = x + corner[0] * levelDistance;
                final int cz = z + corner[1] * levelDistance;
                if (region.isAir(cx, y - levelDeviation, cz) || !region.isAir(cx, y + levelDeviation, cz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Feature bamboo(final FeatureLibrary library, final JsonObject data) {
        final float probability = data.get("probability").getAsFloat();
        return (region, random, x, y, z) -> {
            final StateFlags states = region.states();
            if (!region.isAir(x, y, z)) {
                return false;
            }
            final char plain = region.id(region.blocks().state(BlockTypeKeys.BAMBOO));
            if (region.canSurvive(plain, x, y, z)) {
                final int height = random.nextInt(12) + 5;
                if (random.nextFloat() < probability) {
                    final int r = random.nextInt(4) + 1;
                    final StateFlags.Membership podzolGround = states.membership(BlockTypeTagKeys.BENEATH_BAMBOO_PODZOL_REPLACEABLE);
                    final char podzol = region.id(region.blocks().state(BlockTypeKeys.PODZOL));
                    for (int px = x - r; px <= x + r; px++) {
                        for (int pz = z - r; pz <= z + r; pz++) {
                            final int dx = px - x;
                            final int dz = pz - z;
                            if (dx * dx + dz * dz <= r * r) {
                                final int py = region.height(HeightmapKind.WORLD_SURFACE, px, pz) - 1;
                                if (podzolGround.test(region.id(px, py, pz))) {
                                    region.set(px, py, pz, podzol);
                                }
                            }
                        }
                    }
                }
                final char trunk = states.with(states.with(states.with(plain, "age", "1"), "leaves", "none"), "stage", "0");
                int py = y;
                for (int i = 0; i < height && region.isAir(x, py, z); i++) {
                    region.set(x, py, z, trunk);
                    py++;
                }
                if (py - y >= 3) {
                    region.set(x, py, z, states.with(states.with(trunk, "leaves", "large"), "stage", "1"));
                    region.set(x, py - 1, z, states.with(trunk, "leaves", "large"));
                    region.set(x, py - 2, z, states.with(trunk, "leaves", "small"));
                }
            }
            return true;
        };
    }

    private static boolean vines(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
        if (!region.isAir(x, y, z)) {
            return false;
        }
        final StateFlags states = region.states();
        for (final int direction : Facing.ALL) {
            if (direction == Facing.DOWN) {
                continue;
            }
            final char neighbour = region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]);
            if (states.sturdy(neighbour, face(Facing.OPPOSITE[direction]))) {
                final char vine = region.id(region.blocks().state(BlockTypeKeys.VINE));
                region.set(x, y, z, states.with(vine, Facing.NAMES[direction], "true"));
                return true;
            }
        }
        return false;
    }

    private static Feature multifaceGrowth(final FeatureLibrary library, final JsonObject data) {
        final char block = library.state(data.get("block"));
        final int searchRange = data.has("search_range") ? data.get("search_range").getAsInt() : 10;
        final boolean floor = data.has("can_place_on_floor") && data.get("can_place_on_floor").getAsBoolean();
        final boolean ceiling = data.has("can_place_on_ceiling") && data.get("can_place_on_ceiling").getAsBoolean();
        final boolean wall = data.has("can_place_on_wall") && data.get("can_place_on_wall").getAsBoolean();
        final float spreadChance = data.has("chance_of_spreading") ? data.get("chance_of_spreading").getAsFloat() : 0.5F;
        final StateFlags.Membership placedOn = library.blockSet(data.get("can_be_placed_on"));
        final List<Integer> valid = new ArrayList<>();
        if (ceiling) {
            valid.add(Facing.UP);
        }
        if (floor) {
            valid.add(Facing.DOWN);
        }
        if (wall) {
            for (final int direction : Facing.HORIZONTAL) {
                valid.add(direction);
            }
        }
        final int[] directions = valid.stream().mapToInt(Integer::intValue).toArray();
        return (region, random, x, y, z) -> {
            final Multiface growth = new Multiface(region, block);
            if (!growth.airOrWater(region.id(x, y, z))) {
                return false;
            }
            final int[] searchOrder = Facing.shuffled(directions, random);
            if (growth.placeIfPossible(placedOn, x, y, z, region.id(x, y, z), random, searchOrder, spreadChance)) {
                return true;
            }
            for (final int search : searchOrder) {
                int count = 0;
                for (final int direction : directions) {
                    if (direction != Facing.OPPOSITE[search]) {
                        count++;
                    }
                }
                final int[] others = new int[count];
                int index = 0;
                for (final int direction : directions) {
                    if (direction != Facing.OPPOSITE[search]) {
                        others[index++] = direction;
                    }
                }
                final int[] placement = Facing.shuffled(others, random);
                final int px = x + Facing.DX[search];
                final int py = y + Facing.DY[search];
                final int pz = z + Facing.DZ[search];
                for (int i = 0; i < searchRange; i++) {
                    final char state = region.id(px, py, pz);
                    if (!growth.airOrWater(state) && !sameBlock(region.states(), state, block)) {
                        break;
                    }
                    if (growth.placeIfPossible(placedOn, px, py, pz, state, random, placement, spreadChance)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    /**
     * A block made of faces that cling to the blocks around, such as glow lichen, and how it spreads.
     */
    static final class Multiface {

        private final DecorationRegion region;
        private final StateFlags states;
        private final char block;

        Multiface(final DecorationRegion region, final char block) {
            this.region = region;
            this.states = region.states();
            this.block = block;
        }

        boolean airOrWater(final char state) {
            return states.isAir(state) || states.isWaterBlock(state);
        }

        private boolean isBlock(final char state) {
            return sameBlock(states, state, block);
        }

        private boolean hasFace(final char state, final int direction) {
            return isBlock(state) && "true".equals(states.property(state, Facing.NAMES[direction]));
        }

        private boolean validForPlacement(final char existing, final int x, final int y, final int z, final int direction) {
            if (isBlock(existing) && hasFace(existing, direction)) {
                return false;
            }
            final char neighbour = region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]);
            return states.sturdy(neighbour, face(Facing.OPPOSITE[direction]));
        }

        /**
         * {@return the state with one more face, {@link StateSource#NONE} when the face cannot hold there}
         */
        char withFace(final char existing, final int x, final int y, final int z, final int direction) {
            if (!validForPlacement(existing, x, y, z, direction)) {
                return StateSource.NONE;
            }
            final char state;
            if (isBlock(existing)) {
                state = existing;
            } else if (states.has(existing, StateFlags.WATER_SOURCE)) {
                state = states.with(block, "waterlogged", "true");
            } else {
                state = block;
            }
            return states.with(state, Facing.NAMES[direction], "true");
        }

        boolean placeIfPossible(final StateFlags.Membership placedOn, final int x, final int y, final int z, final char existing,
                                final BitRandom random, final int[] directions, final float spreadChance) {
            for (final int direction : directions) {
                final char neighbour = region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]);
                if (!placedOn.test(neighbour)) {
                    continue;
                }
                final char placed = withFace(existing, x, y, z, direction);
                if (placed == StateSource.NONE) {
                    return false;
                }
                region.set(x, y, z, placed);
                if (random.nextFloat() < spreadChance) {
                    spread(placed, x, y, z, direction, random);
                }
                return true;
            }
            return false;
        }

        /**
         * Spreads a face to a neighbouring spot, toward the first direction that works in a random order: on the
         * same block, onto the next block of the same wall, or around the edge of the wall.
         */
        void spread(final char state, final int x, final int y, final int z, final int fromFace, final BitRandom random) {
            final int[] order = Facing.shuffled(Facing.ALL, random);
            for (final int direction : order) {
                if (Facing.axis(direction).equals(Facing.axis(fromFace))) {
                    continue;
                }
                if (!hasFace(state, fromFace) || hasFace(state, direction)) {
                    continue;
                }
                final int[][] candidates = {
                        {x, y, z, direction},
                        {x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction], fromFace},
                        {x + Facing.DX[direction] + Facing.DX[fromFace], y + Facing.DY[direction] + Facing.DY[fromFace],
                                z + Facing.DZ[direction] + Facing.DZ[fromFace], Facing.OPPOSITE[direction]}
                };
                for (final int[] candidate : candidates) {
                    final char existing = region.id(candidate[0], candidate[1], candidate[2]);
                    final boolean replaceable = states.isAir(existing) || isBlock(existing)
                            || states.isWaterBlock(existing) && states.has(existing, StateFlags.WATER_SOURCE);
                    if (replaceable && validForPlacement(existing, candidate[0], candidate[1], candidate[2], candidate[3])) {
                        final char placed = withFace(existing, candidate[0], candidate[1], candidate[2], candidate[3]);
                        if (placed != StateSource.NONE && region.set(candidate[0], candidate[1], candidate[2], placed)) {
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }
}