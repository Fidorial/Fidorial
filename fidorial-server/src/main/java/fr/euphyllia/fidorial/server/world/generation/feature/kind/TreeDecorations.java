package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.EntityTypeKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What decorates a tree once it stands: vines, cocoa, a bee nest, mushrooms on its bark, propagules hanging from
 * its leaves, a ring of podzol, moss.
 */
final class TreeDecorations {

    /**
     * The configured feature the pale moss decoration spreads on the ground, named in the data.
     */
    private static final Key PALE_MOSS_PATCH = Key.key("pale_moss_patch");

    private TreeDecorations() {
    }

    /**
     * A decoration of a tree.
     */
    @FunctionalInterface
    interface Decoration {
        void place(Context context);
    }

    /**
     * What a decoration sees of the tree: its blocks sorted by height, and a way to add blocks to it.
     */
    static final class Context {

        /**
         * Where the blocks of the decorations go.
         */
        @FunctionalInterface
        interface Setter {
            void set(int x, int y, int z, char state);
        }

        final DecorationRegion region;
        final StateFlags states;
        final BitRandom random;
        final List<PlantKinds.Spot> logs;
        final List<PlantKinds.Spot> leaves;
        final List<PlantKinds.Spot> roots;
        private final Setter setter;

        Context(final Trees.Build build) {
            this(build.region, build.random, build.logs, build.leaves, build.roots, build::setDecoration);
        }

        Context(final DecorationRegion region, final BitRandom random, final Set<PlantKinds.Spot> logs,
                final Set<PlantKinds.Spot> leaves, final Set<PlantKinds.Spot> roots, final Setter setter) {
            this.region = region;
            this.states = region.states();
            this.random = random;
            this.roots = Trees.byHeight(roots);
            this.logs = Trees.byHeight(logs);
            this.leaves = Trees.byHeight(leaves);
            this.setter = setter;
        }

        boolean isAir(final int x, final int y, final int z) {
            return region.isAir(x, y, z);
        }

        void set(final int x, final int y, final int z, final char state) {
            setter.set(x, y, z, state);
        }

        void vine(final int x, final int y, final int z, final String face) {
            set(x, y, z, states.with(region.id(region.blocks().state(BlockTypeKeys.VINE)), face, "true"));
        }

        /**
         * {@return the lowest blocks of the tree: its logs, its roots, or both when they start at the same height}
         */
        List<PlantKinds.Spot> lowest() {
            final List<PlantKinds.Spot> result = new ArrayList<>();
            if (roots.isEmpty()) {
                result.addAll(logs);
            } else if (!logs.isEmpty() && roots.getFirst().y() == logs.getFirst().y()) {
                result.addAll(logs);
                result.addAll(roots);
            } else {
                result.addAll(roots);
            }
            return result;
        }
    }

    static Decoration parse(final FeatureLibrary library, final JsonObject data) {
        return switch (Values.type(data)) {
            case "trunk_vine" -> TreeDecorations::trunkVines;
            case "leave_vine" -> leaveVines(data.get("probability").getAsFloat());
            case "cocoa" -> cocoa(data.get("probability").getAsFloat());
            case "beehive" -> beehive(data.get("probability").getAsFloat());
            case "alter_ground" -> alterGround(library.source(data.get("provider")));
            case "attached_to_leaves" -> attachedToLeaves(library, data);
            case "attached_to_logs" -> attachedToLogs(library, data);
            case "place_on_ground" -> placeOnGround(library, data);
            case "creaking_heart" -> creakingHeart(data.get("probability").getAsFloat());
            case "pale_moss" -> paleMoss(library, data);
            case "shelf_mushroom" -> shelfMushroom(data.get("probability").getAsFloat());
            default -> {
                library.unsupported("tree decorator", Values.type(data));
                yield context -> {
                };
            }
        };
    }

    private static int[] directions(final JsonElement element) {
        final List<Integer> list = new ArrayList<>();
        for (final JsonElement direction : element.getAsJsonArray()) {
            list.add(Facing.byName(direction.getAsString()));
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private static void trunkVines(final Context context) {
        for (final PlantKinds.Spot log : context.logs) {
            if (context.random.nextInt(3) > 0 && context.isAir(log.x() - 1, log.y(), log.z())) {
                context.vine(log.x() - 1, log.y(), log.z(), "east");
            }
            if (context.random.nextInt(3) > 0 && context.isAir(log.x() + 1, log.y(), log.z())) {
                context.vine(log.x() + 1, log.y(), log.z(), "west");
            }
            if (context.random.nextInt(3) > 0 && context.isAir(log.x(), log.y(), log.z() - 1)) {
                context.vine(log.x(), log.y(), log.z() - 1, "south");
            }
            if (context.random.nextInt(3) > 0 && context.isAir(log.x(), log.y(), log.z() + 1)) {
                context.vine(log.x(), log.y(), log.z() + 1, "north");
            }
        }
    }

    private static Decoration leaveVines(final float probability) {
        return context -> {
            for (final PlantKinds.Spot leaf : context.leaves) {
                if (context.random.nextFloat() < probability && context.isAir(leaf.x() - 1, leaf.y(), leaf.z())) {
                    hangingVine(context, leaf.x() - 1, leaf.y(), leaf.z(), "east");
                }
                if (context.random.nextFloat() < probability && context.isAir(leaf.x() + 1, leaf.y(), leaf.z())) {
                    hangingVine(context, leaf.x() + 1, leaf.y(), leaf.z(), "west");
                }
                if (context.random.nextFloat() < probability && context.isAir(leaf.x(), leaf.y(), leaf.z() - 1)) {
                    hangingVine(context, leaf.x(), leaf.y(), leaf.z() - 1, "south");
                }
                if (context.random.nextFloat() < probability && context.isAir(leaf.x(), leaf.y(), leaf.z() + 1)) {
                    hangingVine(context, leaf.x(), leaf.y(), leaf.z() + 1, "north");
                }
            }
        };
    }

    private static void hangingVine(final Context context, final int x, final int y, final int z, final String face) {
        context.vine(x, y, z, face);
        int py = y - 1;
        for (int left = 4; context.isAir(x, py, z) && left > 0; left--) {
            context.vine(x, py, z, face);
            py--;
        }
    }

    private static Decoration cocoa(final float probability) {
        return context -> {
            final BitRandom random = context.random;
            if (random.nextFloat() >= probability || context.logs.isEmpty()) {
                return;
            }
            final int baseY = context.logs.getFirst().y();
            final char plain = context.region.id(context.region.blocks().state(BlockTypeKeys.COCOA));
            for (final PlantKinds.Spot log : context.logs) {
                if (log.y() - baseY > 2) {
                    continue;
                }
                for (final int direction : Facing.HORIZONTAL) {
                    if (random.nextFloat() <= 0.25F) {
                        final int opposite = Facing.OPPOSITE[direction];
                        final int px = log.x() + Facing.DX[opposite];
                        final int pz = log.z() + Facing.DZ[opposite];
                        if (context.isAir(px, log.y(), pz)) {
                            final char state = context.states.with(
                                    context.states.with(plain, "age", Integer.toString(random.nextInt(3))), "facing", Facing.NAMES[direction]);
                            context.set(px, log.y(), pz, state);
                        }
                    }
                }
            }
        };
    }

    private static Decoration beehive(final float probability) {
        // the nest faces south; bees may leave by any side but the back
        final int[] sides = {Facing.EAST, Facing.SOUTH, Facing.WEST};
        return context -> {
            final List<PlantKinds.Spot> logs = context.logs;
            final List<PlantKinds.Spot> leaves = context.leaves;
            if (logs.isEmpty()) {
                return;
            }
            final BitRandom random = context.random;
            if (random.nextFloat() >= probability) {
                return;
            }
            final int hiveY = !leaves.isEmpty()
                    ? Math.max(leaves.getFirst().y() - 1, logs.getFirst().y() + 1)
                    : Math.min(logs.getFirst().y() + 1 + random.nextInt(3), logs.getLast().y());
            final List<int[]> spots = new ArrayList<>();
            for (final PlantKinds.Spot log : logs) {
                if (log.y() == hiveY) {
                    for (final int side : sides) {
                        spots.add(new int[] {log.x() + Facing.DX[side], log.y(), log.z() + Facing.DZ[side]});
                    }
                }
            }
            if (spots.isEmpty()) {
                return;
            }
            for (int i = spots.size(); i > 1; i--) {
                final int swapTo = random.nextInt(i);
                final int[] kept = spots.get(i - 1);
                spots.set(i - 1, spots.get(swapTo));
                spots.set(swapTo, kept);
            }
            for (final int[] spot : spots) {
                if (context.isAir(spot[0], spot[1], spot[2]) && context.isAir(spot[0], spot[1], spot[2] + 1)) {
                    final char nest = context.states.with(context.region.id(context.region.blocks().state(BlockTypeKeys.BEE_NEST)),
                            "facing", "south");
                    context.set(spot[0], spot[1], spot[2], nest);
                    final int bees = 2 + random.nextInt(2);
                    final ListBinaryTag.Builder<CompoundBinaryTag> list = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
                    for (int b = 0; b < bees; b++) {
                        list.add(CompoundBinaryTag.builder()
                                .put("entity_data", CompoundBinaryTag.builder().putString("id", EntityTypeKeys.BEE.key().asString()).build())
                                .putInt("ticks_in_hive", random.nextInt(599))
                                .putInt("min_ticks_in_hive", 600)
                                .build());
                    }
                    context.region.putBlockEntity(spot[0], spot[1], spot[2], CompoundBinaryTag.builder().put("bees", list.build()).build());
                    return;
                }
            }
        };
    }

    private static Decoration alterGround(final StateSource provider) {
        return context -> {
            final List<PlantKinds.Spot> lowest = context.lowest();
            if (lowest.isEmpty()) {
                return;
            }
            final int minY = lowest.getFirst().y();
            for (final PlantKinds.Spot spot : lowest) {
                if (spot.y() != minY) {
                    continue;
                }
                circle(context, provider, spot.x() - 1, spot.y(), spot.z() - 1);
                circle(context, provider, spot.x() + 2, spot.y(), spot.z() - 1);
                circle(context, provider, spot.x() - 1, spot.y(), spot.z() + 2);
                circle(context, provider, spot.x() + 2, spot.y(), spot.z() + 2);
                for (int i = 0; i < 5; i++) {
                    final int placement = context.random.nextInt(64);
                    final int xx = placement % 8;
                    final int zz = placement / 8;
                    if (xx == 0 || xx == 7 || zz == 0 || zz == 7) {
                        circle(context, provider, spot.x() - 3 + xx, spot.y(), spot.z() - 3 + zz);
                    }
                }
            }
        };
    }

    private static void circle(final Context context, final StateSource provider, final int x, final int y, final int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                    continue;
                }
                for (int dy = 2; dy >= -3; dy--) {
                    final int py = y + dy;
                    final char state = provider.pick(context.region, context.random, x + dx, py, z + dz);
                    if (state != StateSource.NONE) {
                        context.set(x + dx, py, z + dz, state);
                        break;
                    }
                    if (!context.isAir(x + dx, py, z + dz) && dy < 0) {
                        break;
                    }
                }
            }
        }
    }

    private static Decoration attachedToLeaves(final FeatureLibrary library, final JsonObject data) {
        final float probability = data.get("probability").getAsFloat();
        final int radiusXZ = data.get("exclusion_radius_xz").getAsInt();
        final int radiusY = data.get("exclusion_radius_y").getAsInt();
        final StateSource provider = library.source(data.get("block_provider"));
        final int emptyNeeded = data.get("required_empty_blocks").getAsInt();
        final int[] directions = directions(data.get("directions"));
        return context -> {
            final BitRandom random = context.random;
            final Set<PlantKinds.Spot> excluded = new HashSet<>();
            for (final PlantKinds.Spot leaf : shuffled(context.leaves, random)) {
                final int direction = directions[random.nextInt(directions.length)];
                final int px = leaf.x() + Facing.DX[direction];
                final int py = leaf.y() + Facing.DY[direction];
                final int pz = leaf.z() + Facing.DZ[direction];
                if (excluded.contains(new PlantKinds.Spot(px, py, pz)) || !(random.nextFloat() < probability)) {
                    continue;
                }
                boolean empty = true;
                for (int i = 1; i <= emptyNeeded; i++) {
                    if (!context.isAir(leaf.x() + Facing.DX[direction] * i, leaf.y() + Facing.DY[direction] * i,
                            leaf.z() + Facing.DZ[direction] * i)) {
                        empty = false;
                        break;
                    }
                }
                if (!empty) {
                    continue;
                }
                for (int ex = -radiusXZ; ex <= radiusXZ; ex++) {
                    for (int ey = -radiusY; ey <= radiusY; ey++) {
                        for (int ez = -radiusXZ; ez <= radiusXZ; ez++) {
                            excluded.add(new PlantKinds.Spot(px + ex, py + ey, pz + ez));
                        }
                    }
                }
                context.set(px, py, pz, provider.pickOrKeep(context.region, random, px, py, pz));
            }
        };
    }

    private static Decoration attachedToLogs(final FeatureLibrary library, final JsonObject data) {
        final float probability = data.get("probability").getAsFloat();
        final StateSource provider = library.source(data.get("block_provider"));
        final int[] directions = directions(data.get("directions"));
        return context -> {
            final BitRandom random = context.random;
            for (final PlantKinds.Spot log : shuffled(context.logs, random)) {
                final int direction = directions[random.nextInt(directions.length)];
                final int px = log.x() + Facing.DX[direction];
                final int py = log.y() + Facing.DY[direction];
                final int pz = log.z() + Facing.DZ[direction];
                if (random.nextFloat() <= probability && context.isAir(px, py, pz)) {
                    context.set(px, py, pz, provider.pickOrKeep(context.region, random, px, py, pz));
                }
            }
        };
    }

    private static List<PlantKinds.Spot> shuffled(final List<PlantKinds.Spot> list, final BitRandom random) {
        final List<PlantKinds.Spot> copy = new ArrayList<>(list);
        for (int i = copy.size(); i > 1; i--) {
            final int swapTo = random.nextInt(i);
            final PlantKinds.Spot kept = copy.get(i - 1);
            copy.set(i - 1, copy.get(swapTo));
            copy.set(swapTo, kept);
        }
        return copy;
    }

    private static Decoration placeOnGround(final FeatureLibrary library, final JsonObject data) {
        final int tries = Trees.integer(data, "tries", 128);
        final int radius = Trees.integer(data, "radius", 2);
        final int height = Trees.integer(data, "height", 1);
        final StateSource provider = library.source(data.get("block_state_provider"));
        return context -> {
            final List<PlantKinds.Spot> lowest = context.lowest();
            if (lowest.isEmpty()) {
                return;
            }
            final PlantKinds.Spot origin = lowest.getFirst();
            final int minY = origin.y();
            int minX = origin.x();
            int maxX = origin.x();
            int minZ = origin.z();
            int maxZ = origin.z();
            for (final PlantKinds.Spot spot : lowest) {
                if (spot.y() == minY) {
                    minX = Math.min(minX, spot.x());
                    maxX = Math.max(maxX, spot.x());
                    minZ = Math.min(minZ, spot.z());
                    maxZ = Math.max(maxZ, spot.z());
                }
            }
            final BitRandom random = context.random;
            final StateFlags states = context.states;
            for (int i = 0; i < tries; i++) {
                final int x = random.nextIntBetweenInclusive(minX - radius, maxX + radius);
                final int y = random.nextIntBetweenInclusive(minY - height, minY + height);
                final int z = random.nextIntBetweenInclusive(minZ - radius, maxZ + radius);
                final char above = context.region.id(x, y + 1, z);
                final char ground = context.region.id(x, y, z);
                if ((states.isAir(above) || states.is(above, BlockTypeKeys.VINE))
                        && states.has(ground, StateFlags.FULL_COLLISION) && !states.has(ground, StateFlags.LEAVES)
                        && context.region.height(Heightmap.MOTION_BLOCKING_NO_LEAVES, x, z) <= y + 1) {
                    context.set(x, y + 1, z, provider.pickOrKeep(context.region, random, x, y + 1, z));
                }
            }
        };
    }

    private static Decoration creakingHeart(final float probability) {
        return context -> {
            final BitRandom random = context.random;
            if (context.logs.isEmpty() || random.nextFloat() >= probability) {
                return;
            }
            final List<PlantKinds.Spot> candidates = shuffled(context.logs, random);
            final StateFlags.Membership logTag = context.states.membership(BlockTypeTagKeys.LOGS);
            for (final PlantKinds.Spot spot : candidates) {
                boolean enclosed = true;
                for (final int direction : Facing.ALL) {
                    if (!logTag.test(context.region.id(spot.x() + Facing.DX[direction], spot.y() + Facing.DY[direction],
                            spot.z() + Facing.DZ[direction]))) {
                        enclosed = false;
                        break;
                    }
                }
                if (enclosed) {
                    final char heart = context.region.id(context.region.blocks().state(BlockTypeKeys.CREAKING_HEART));
                    context.set(spot.x(), spot.y(), spot.z(),
                            context.states.with(context.states.with(heart, "creaking_heart_state", "dormant"), "natural", "true"));
                    return;
                }
            }
        };
    }

    private static Decoration paleMoss(final FeatureLibrary library, final JsonObject data) {
        final float leavesProbability = data.get("leaves_probability").getAsFloat();
        final float trunkProbability = data.get("trunk_probability").getAsFloat();
        final float groundProbability = data.get("ground_probability").getAsFloat();
        final Feature[] patch = new Feature[1];
        return context -> {
            final BitRandom random = context.random;
            final List<PlantKinds.Spot> logs = shuffled(context.logs, random);
            if (logs.isEmpty()) {
                return;
            }
            PlantKinds.Spot lowest = logs.getFirst();
            for (final PlantKinds.Spot log : logs) {
                if (log.y() < lowest.y()) {
                    lowest = log;
                }
            }
            if (random.nextFloat() < groundProbability) {
                if (patch[0] == null) {
                    patch[0] = library.feature(PALE_MOSS_PATCH);
                }
                patch[0].place(context.region, random, lowest.x(), lowest.y() + 1, lowest.z());
            }
            for (final PlantKinds.Spot log : context.logs) {
                if (random.nextFloat() < trunkProbability && context.isAir(log.x(), log.y() - 1, log.z())) {
                    mossHanger(context, log.x(), log.y() - 1, log.z());
                }
            }
            for (final PlantKinds.Spot leaf : context.leaves) {
                if (random.nextFloat() < leavesProbability && context.isAir(leaf.x(), leaf.y() - 1, leaf.z())) {
                    mossHanger(context, leaf.x(), leaf.y() - 1, leaf.z());
                }
            }
        };
    }

    private static void mossHanger(final Context context, final int x, final int y, final int z) {
        final char moss = context.region.id(context.region.blocks().state(BlockTypeKeys.PALE_HANGING_MOSS));
        int py = y;
        while (context.isAir(x, py - 1, z) && !(context.random.nextFloat() < 0.5)) {
            context.set(x, py, z, context.states.with(moss, "tip", "false"));
            py--;
        }
        context.set(x, py, z, context.states.with(moss, "tip", "true"));
    }

    private static Decoration shelfMushroom(final float probability) {
        return context -> {
            final BitRandom random = context.random;
            if (random.nextFloat() >= probability || context.logs.isEmpty()) {
                return;
            }
            final List<PlantKinds.Spot> logs = context.logs;
            if (logs.getFirst().y() == logs.getLast().y()) {
                final boolean alongX = logs.getFirst().x() != logs.getLast().x();
                final int[] sides = alongX ? new int[] {Facing.NORTH, Facing.SOUTH} : new int[] {Facing.EAST, Facing.WEST};
                for (final PlantKinds.Spot log : logs) {
                    for (final int side : sides) {
                        if (!(random.nextFloat() > 0.25F)) {
                            final int px = log.x() + Facing.DX[side];
                            final int pz = log.z() + Facing.DZ[side];
                            if (fitsMushroom(context, px, log.y(), pz) && !mushroomBeside(context, px, log.y(), pz)
                                    && !mushroomBeside(context, log.x(), log.y(), log.z())) {
                                placeMushroom(context, px, log.y(), pz, side);
                            }
                        }
                    }
                }
                return;
            }
            final int first = Facing.randomHorizontal(random);
            final int[] sides = {first, Facing.clockwise(first)};
            final int baseY = logs.getFirst().y();
            for (final PlantKinds.Spot log : logs) {
                final int dy = log.y() - baseY;
                if (dy < 1 || dy > 4) {
                    continue;
                }
                for (final int side : sides) {
                    if (random.nextFloat() > 0.25F) {
                        continue;
                    }
                    final int px = log.x() + Facing.DX[side];
                    final int pz = log.z() + Facing.DZ[side];
                    if (fitsMushroom(context, px, log.y(), pz) && !isMushroom(context, px, log.y() - 1, pz)) {
                        placeMushroom(context, px, log.y(), pz, side);
                        break;
                    }
                }
            }
        };
    }

    private static boolean isMushroom(final Context context, final int x, final int y, final int z) {
        return context.states.is(context.region.id(x, y, z), BlockTypeKeys.SHELF_MUSHROOM);
    }

    private static boolean mushroomBeside(final Context context, final int x, final int y, final int z) {
        for (final int side : Facing.HORIZONTAL) {
            if (isMushroom(context, x + Facing.DX[side], y, z + Facing.DZ[side])) {
                return true;
            }
        }
        return false;
    }

    private static boolean fitsMushroom(final Context context, final int x, final int y, final int z) {
        if (!context.region.has(x, y, z, StateFlags.REPLACEABLE)) {
            return false;
        }
        final StateFlags states = context.states;
        final int[][] around = {{0, 0}, {1, 0}, {-1, 0}, {0, -1}, {0, 1}};
        for (final int[] offset : around) {
            if (states.isWaterBlock(context.region.id(x + offset[0], y, z + offset[1]))) {
                return false;
            }
        }
        return true;
    }

    private static void placeMushroom(final Context context, final int x, final int y, final int z, final int facing) {
        final char plain = context.region.id(context.region.blocks().state(BlockTypeKeys.SHELF_MUSHROOM));
        final char state = context.states.with(context.states.with(plain, "age", Integer.toString(context.random.nextInt(2))),
                "facing", Facing.NAMES[facing]);
        context.set(x, y, z, state);
    }
}
