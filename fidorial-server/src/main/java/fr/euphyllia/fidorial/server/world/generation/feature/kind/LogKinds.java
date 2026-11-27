package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.BlockTest;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fallen trees, a stump with a log lying next to it, and huge mushrooms.
 */
final class LogKinds {

    private LogKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("fallen_tree", LogKinds::fallenTree);
        parsers.put("huge_brown_mushroom", (library, data) -> new Mushroom(library, data, true));
        parsers.put("huge_red_mushroom", (library, data) -> new Mushroom(library, data, false));
    }

    private static List<TreeDecorations.Decoration> decorations(final FeatureLibrary library, final JsonElement element) {
        final List<TreeDecorations.Decoration> list = new ArrayList<>();
        for (final JsonElement decorator : element.getAsJsonArray()) {
            list.add(TreeDecorations.parse(library, decorator.getAsJsonObject()));
        }
        return list;
    }

    private static Feature fallenTree(final FeatureLibrary library, final JsonObject data) {
        final StateSource trunk = library.source(data.get("trunk_provider"));
        final IntSource length = IntSource.parse(data.get("log_length"));
        final List<TreeDecorations.Decoration> stumpDecorations = decorations(library, data.get("stump_decorators"));
        final List<TreeDecorations.Decoration> logDecorations = decorations(library, data.get("log_decorators"));
        return (region, random, x, y, z) -> {
            final StateFlags states = region.states();
            region.set(x, y, z, trunk.pickOrKeep(region, random, x, y, z));
            region.markAbove(x, y, z);
            decorate(region, random, Set.of(new PlantKinds.Spot(x, y, z)), stumpDecorations);
            final int direction = Facing.randomHorizontal(random);
            final int logLength = length.sample(random) - 2;
            final int distance = 2 + random.nextInt(2);
            final int sx = x + Facing.DX[direction] * distance;
            int sy = y + 1;
            final int sz = z + Facing.DZ[direction] * distance;
            for (int i = 0; i < 6; i++) {
                if (validTreePos(region, sx, sy, sz) && overSolidGround(region, sx, sy, sz)) {
                    break;
                }
                sy--;
            }
            int gap = 0;
            for (int i = 0; i < logLength; i++) {
                final int px = sx + Facing.DX[direction] * i;
                final int pz = sz + Facing.DZ[direction] * i;
                if (!validTreePos(region, px, sy, pz)) {
                    return true;
                }
                if (!overSolidGround(region, px, sy, pz)) {
                    if (++gap > 2) {
                        return true;
                    }
                } else {
                    gap = 0;
                }
            }
            final Set<PlantKinds.Spot> logs = new HashSet<>();
            final String axis = Facing.axis(direction);
            for (int i = 0; i < logLength; i++) {
                final int px = sx + Facing.DX[direction] * i;
                final int pz = sz + Facing.DZ[direction] * i;
                region.set(px, sy, pz, states.withIfAllowed(trunk.pickOrKeep(region, random, px, sy, pz), "axis", axis));
                region.markAbove(px, sy, pz);
                logs.add(new PlantKinds.Spot(px, sy, pz));
            }
            decorate(region, random, logs, logDecorations);
            return true;
        };
    }

    private static void decorate(final DecorationRegion region, final BitRandom random, final Set<PlantKinds.Spot> logs,
                                 final List<TreeDecorations.Decoration> decorations) {
        if (decorations.isEmpty()) {
            return;
        }
        final TreeDecorations.Context context = new TreeDecorations.Context(region, random, logs, Set.of(), Set.of(), region::set);
        for (final TreeDecorations.Decoration decoration : decorations) {
            decoration.place(context);
        }
    }

    private static boolean validTreePos(final DecorationRegion region, final int x, final int y, final int z) {
        final char state = region.id(x, y, z);
        return region.states().isAir(state) || region.states().isTagged(state, BlockTypeTagKeys.REPLACEABLE_BY_TREES);
    }

    private static boolean overSolidGround(final DecorationRegion region, final int x, final int y, final int z) {
        return region.states().sturdy(region.id(x, y - 1, z), BlockFace.UP);
    }

    /**
     * A huge mushroom: a stem and a cap, flat and wide for the brown one, a dome for the red one.
     */
    private static final class Mushroom implements Feature {

        private final StateSource cap;
        private final StateSource stem;
        private final int radius;
        private final BlockTest canPlaceOn;
        private final boolean flat;

        Mushroom(final FeatureLibrary library, final JsonObject data, final boolean flat) {
            this.cap = library.source(data.get("cap_provider"));
            this.stem = library.source(data.get("stem_provider"));
            this.radius = data.has("foliage_radius") ? data.get("foliage_radius").getAsInt() : 2;
            this.canPlaceOn = library.test(data.get("can_place_on"));
            this.flat = flat;
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            int height = random.nextInt(3) + 4;
            if (random.nextInt(12) == 0) {
                height *= 2;
            }
            if (!validPosition(region, x, y, z, height)) {
                return false;
            }
            if (flat) {
                flatCap(region, random, x, y, z, height);
            } else {
                domeCap(region, random, x, y, z, height);
            }
            for (int dy = 0; dy < height; dy++) {
                setIfFree(region, x, y + dy, z, stem.pickOrKeep(region, random, x, y, z));
            }
            return true;
        }

        private int checkRadius(final int dy) {
            if (flat) {
                return dy <= 3 ? 0 : radius;
            }
            // the dome is checked as if the mushroom had no height: only the stem column counts
            return 0;
        }

        private boolean validPosition(final DecorationRegion region, final int x, final int y, final int z, final int height) {
            if (y < region.minY() + 1 || y + height + 1 > region.maxY()) {
                return false;
            }
            if (!canPlaceOn.test(region, x, y - 1, z)) {
                return false;
            }
            final StateFlags states = region.states();
            for (int dy = 0; dy <= height; dy++) {
                final int r = checkRadius(dy);
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        final char state = region.id(x + dx, y + dy, z + dz);
                        if (!states.isAir(state) && !states.has(state, StateFlags.LEAVES)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private void setIfFree(final DecorationRegion region, final int x, final int y, final int z, final char state) {
            final char current = region.id(x, y, z);
            if (region.states().isAir(current) || region.states().isTagged(current, BlockTypeTagKeys.REPLACEABLE_BY_MUSHROOMS)) {
                region.set(x, y, z, state);
            }
        }

        private char faces(final StateFlags states, char state, final boolean up, final boolean west, final boolean east,
                           final boolean north, final boolean south, final boolean withUp) {
            if (withUp) {
                state = states.withIfAllowed(state, "up", Boolean.toString(up));
            }
            state = states.withIfAllowed(state, "west", Boolean.toString(west));
            state = states.withIfAllowed(state, "east", Boolean.toString(east));
            state = states.withIfAllowed(state, "north", Boolean.toString(north));
            return states.withIfAllowed(state, "south", Boolean.toString(south));
        }

        private void flatCap(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z, final int height) {
            final StateFlags states = region.states();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    final boolean minX = dx == -radius;
                    final boolean maxX = dx == radius;
                    final boolean minZ = dz == -radius;
                    final boolean maxZ = dz == radius;
                    final boolean xEdge = minX || maxX;
                    final boolean zEdge = minZ || maxZ;
                    if (xEdge && zEdge) {
                        continue;
                    }
                    final boolean west = minX || zEdge && dx == 1 - radius;
                    final boolean east = maxX || zEdge && dx == radius - 1;
                    final boolean north = minZ || xEdge && dz == 1 - radius;
                    final boolean south = maxZ || xEdge && dz == radius - 1;
                    final char state = faces(states, cap.pickOrKeep(region, random, x, y, z), false, west, east, north, south, false);
                    setIfFree(region, x + dx, y + height, z + dz, state);
                }
            }
        }

        private void domeCap(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z, final int height) {
            final StateFlags states = region.states();
            for (int dy = height - 3; dy <= height; dy++) {
                final int r = dy < height ? radius : radius - 1;
                final int centre = radius - 2;
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        final boolean xEdge = dx == -r || dx == r;
                        final boolean zEdge = dz == -r || dz == r;
                        if (dy >= height || xEdge != zEdge) {
                            final char state = faces(states, cap.pickOrKeep(region, random, x, y, z), dy >= height - 1,
                                    dx < -centre, dx > centre, dz < -centre, dz > centre, true);
                            setIfFree(region, x + dx, y + dy, z + dz, state);
                        }
                    }
                }
            }
        }
    }
}
