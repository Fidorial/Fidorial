package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.block.BlockProperties;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The features of the End: the obsidian pillars around the main island, the platform players arrive on, the
 * chorus plants of the highlands, the gateways back to the main island, and the small floating islands.
 */
final class EndKinds {

    private EndKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("end_spike", Pillars::parse);
        parsers.put("end_platform", (library, data) -> EndKinds::platform);
        parsers.put("chorus_plant", (library, data) -> Chorus::place);
        parsers.put("end_gateway", EndKinds::gateway);
        parsers.put("end_island", (library, data) -> EndKinds::island);
    }

    private static int[] position(final JsonArray coordinates) {
        return new int[] {coordinates.get(0).getAsInt(), coordinates.get(1).getAsInt(), coordinates.get(2).getAsInt()};
    }

    private static boolean is(final DecorationRegion region, final int x, final int y, final int z, final BlockState block) {
        return region.state(x, y, z).name().equals(block.name());
    }

    /**
     * Lays a floor of obsidian under the given block and clears the three blocks above it, five blocks wide.
     */
    private static boolean platform(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
        final BlockState obsidian = region.blocks().state(BlockTypeKeys.OBSIDIAN);
        final BlockState air = region.blocks().air;
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -1; dy < 3; dy++) {
                    final BlockState wanted = dy == -1 ? obsidian : air;
                    if (!is(region, x + dx, y + dy, z + dz, wanted)) {
                        region.set(x + dx, y + dy, z + dz, wanted);
                    }
                }
            }
        }
        return true;
    }

    /**
     * A floating island of end stone: discs shrinking by a random step as they go down, from a radius of four to
     * six.
     */
    private static boolean island(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
        final char endStone = region.id(region.blocks().state(BlockTypeKeys.END_STONE));
        float size = random.nextInt(3) + 4.0F;
        for (int dy = 0; size > 0.5F; dy--) {
            final float reach = (size + 1.0F) * (size + 1.0F);
            for (int dx = (int) Math.floor(-size); dx <= (int) Math.ceil(size); dx++) {
                for (int dz = (int) Math.floor(-size); dz <= (int) Math.ceil(size); dz++) {
                    if (dx * dx + dz * dz <= reach) {
                        region.set(x + dx, y + dy, z + dz, endStone);
                    }
                }
            }
            size -= random.nextInt(2) + 0.5F;
        }
        return true;
    }

    /**
     * A gateway in a bedrock frame: a cross of bedrock above and below the portal block, its arms one block long,
     * capped at both ends, with air around. The portal may lead to a known exit.
     */
    private static Feature gateway(final FeatureLibrary library, final JsonObject data) {
        final int @Nullable [] exit = data.has("exit") ? position(data.getAsJsonArray("exit")) : null;
        final boolean exact = data.has("exact") && data.get("exact").getAsBoolean();
        return (region, random, x, y, z) -> {
            final BlockLookup blocks = region.blocks();
            final char bedrock = region.id(blocks.state(BlockTypeKeys.BEDROCK));
            final char air = region.id(blocks.air);
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        final boolean end = Math.abs(dy) == 2;
                        final char block;
                        if (dx == 0 && dy == 0 && dz == 0) {
                            block = region.id(blocks.state(BlockTypeKeys.END_GATEWAY));
                        } else if (dy == 0) {
                            block = air;
                        } else if ((dx == 0 || dz == 0) && (!end || (dx == 0 && dz == 0))) {
                            block = bedrock;
                        } else {
                            block = air;
                        }
                        region.set(x + dx, y + dy, z + dz, block);
                    }
                }
            }
            if (exit != null) {
                final CompoundBinaryTag.Builder gateway = CompoundBinaryTag.builder()
                        .put("exit_portal", IntArrayBinaryTag.intArrayBinaryTag(exit[0], exit[1], exit[2]));
                if (exact) {
                    gateway.putBoolean("ExactTeleport", true);
                }
                region.putBlockEntity(x, y, z, gateway.build());
            }
            return true;
        };
    }

    /**
     * The obsidian pillars standing in a ring around the origin, each built by the chunk holding its middle.
     *
     * <p>Without pillars of its own in the data, the ring follows from the world seed: ten pillars 42 blocks away,
     * whose sizes are a shuffle of the seed, the two smallest caged in iron bars. A pillar clears the air above
     * the islands around it, and ends in bedrock with a fire on top. The crystal resting on that fire is an
     * entity, which the decorations do not make; its turn is still drawn so that nothing after it moves.</p>
     */
    private static final class Pillars {

        private static final int COUNT = 10;
        private static final double RING_RADIUS = 42.0;
        private static final int LOWEST_HEIGHT = 76;
        private static final int HEIGHT_STEP = 3;
        private static final int CLEARED_ABOVE = 10;
        private static final int CLEARED_FROM = 65;
        private static final int CAGE_REACH = 2;
        private static final int CAGE_HEIGHT = 3;
        private static final long SIZES_KEY_MASK = 0xFFFFL;

        private record Pillar(int centerX, int centerZ, int radius, int height, boolean guarded) {
        }

        private final @Nullable List<Pillar> given;
        private volatile @Nullable List<Pillar> ofSeed;

        private Pillars(final @Nullable List<Pillar> given) {
            this.given = given;
        }

        static Feature parse(final FeatureLibrary library, final JsonObject data) {
            final List<Pillar> given = new ArrayList<>();
            if (data.has("spikes")) {
                for (final JsonElement element : data.getAsJsonArray("spikes")) {
                    final JsonObject spike = element.getAsJsonObject();
                    given.add(new Pillar(intOr(spike, "centerX"), intOr(spike, "centerZ"), intOr(spike, "radius"),
                            intOr(spike, "height"), spike.has("guarded") && spike.get("guarded").getAsBoolean()));
                }
            }
            final Pillars pillars = new Pillars(given.isEmpty() ? null : List.copyOf(given));
            return pillars::place;
        }

        private static int intOr(final JsonObject object, final String field) {
            return object.has(field) ? object.get(field).getAsInt() : 0;
        }

        /**
         * {@return the ring of a world seed}
         */
        private static List<Pillar> ring(final long seed) {
            final long key = new LegacyRandom(seed).nextLong() & SIZES_KEY_MASK;
            final int[] order = new int[COUNT];
            for (int i = 0; i < COUNT; i++) {
                order[i] = i;
            }
            final int[] sizes = Facing.shuffled(order, new LegacyRandom(key));
            final List<Pillar> ring = new ArrayList<>(COUNT);
            for (int i = 0; i < COUNT; i++) {
                final double angle = 2.0 * (-Math.PI + Math.PI / COUNT * i);
                final int size = sizes[i];
                ring.add(new Pillar((int) Math.floor(RING_RADIUS * Math.cos(angle)), (int) Math.floor(RING_RADIUS * Math.sin(angle)),
                        2 + size / 3, LOWEST_HEIGHT + size * HEIGHT_STEP, size == 1 || size == 2));
            }
            return List.copyOf(ring);
        }

        private List<Pillar> pillars(final long seed) {
            if (given != null) {
                return given;
            }
            List<Pillar> known = ofSeed;
            if (known == null) {
                known = ring(seed);
                ofSeed = known;
            }
            return known;
        }

        private boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            for (final Pillar pillar : pillars(region.seed())) {
                if ((pillar.centerX() >> 4) == (x >> 4) && (pillar.centerZ() >> 4) == (z >> 4)) {
                    build(region, random, pillar);
                }
            }
            return true;
        }

        private static void build(final DecorationRegion region, final BitRandom random, final Pillar pillar) {
            final BlockLookup blocks = region.blocks();
            final char obsidian = region.id(blocks.state(BlockTypeKeys.OBSIDIAN));
            final char air = region.id(blocks.air);
            final int radius = pillar.radius();
            final int reach = radius * radius + 1;
            for (int z = pillar.centerZ() - radius; z <= pillar.centerZ() + radius; z++) {
                for (int y = region.minY(); y <= pillar.height() + CLEARED_ABOVE; y++) {
                    for (int x = pillar.centerX() - radius; x <= pillar.centerX() + radius; x++) {
                        final int dx = x - pillar.centerX();
                        final int dz = z - pillar.centerZ();
                        if (dx * dx + dz * dz <= reach && y < pillar.height()) {
                            region.set(x, y, z, obsidian);
                        } else if (y > CLEARED_FROM) {
                            region.set(x, y, z, air);
                        }
                    }
                }
            }
            if (pillar.guarded()) {
                cage(region, pillar);
            }
            random.nextFloat();
            region.set(pillar.centerX(), pillar.height(), pillar.centerZ(), blocks.state(BlockTypeKeys.BEDROCK));
            // the bedrock under it has a whole top: the fire keeps its plain state
            region.set(pillar.centerX(), pillar.height() + 1, pillar.centerZ(), blocks.state(BlockTypeKeys.FIRE));
        }

        /**
         * Surrounds the top of a pillar with iron bars, four walls and a roof, each bar joined to the bars beside it.
         */
        private static void cage(final DecorationRegion region, final Pillar pillar) {
            final BlockLookup blocks = region.blocks();
            final BlockState bars = blocks.state(BlockTypeKeys.IRON_BARS);
            for (int dx = -CAGE_REACH; dx <= CAGE_REACH; dx++) {
                for (int dz = -CAGE_REACH; dz <= CAGE_REACH; dz++) {
                    for (int dy = 0; dy <= CAGE_HEIGHT; dy++) {
                        final boolean wallX = Math.abs(dx) == CAGE_REACH;
                        final boolean wallZ = Math.abs(dz) == CAGE_REACH;
                        final boolean roof = dy == CAGE_HEIGHT;
                        if (!wallX && !wallZ && !roof) {
                            continue;
                        }
                        final boolean alongZ = wallX || roof;
                        final boolean alongX = wallZ || roof;
                        final BlockState state = bars.withIfAllowed(BlockProperties.NORTH, alongZ && dz != -CAGE_REACH)
                                .withIfAllowed(BlockProperties.SOUTH, alongZ && dz != CAGE_REACH)
                                .withIfAllowed(BlockProperties.WEST, alongX && dx != -CAGE_REACH)
                                .withIfAllowed(BlockProperties.EAST, alongX && dx != CAGE_REACH);
                        region.set(pillar.centerX() + dx, pillar.height() + dy, pillar.centerZ() + dz, state);
                    }
                }
            }
        }
    }

    /**
     * A chorus plant grown at once on the ground: a stem of a few blocks, then branches to the sides, each a
     * smaller plant of its own, four levels deep at most and never more than seven blocks from the root. A branch
     * that grows no further ends in a ripe flower.
     */
    private static final class Chorus {

        private static final int SPREAD = 8;
        private static final int MAX_DEPTH = 4;
        private static final int RIPE = 5;
        private static final int[] SIDES = {Facing.NORTH, Facing.SOUTH, Facing.WEST, Facing.EAST};

        private Chorus() {
        }

        static boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            if (!region.isAir(x, y, z)
                    || !region.blocks().is(region.state(x, y - 1, z), BlockTypeTagKeys.SUPPORTS_CHORUS_PLANT)) {
                return false;
            }
            join(region, x, y, z);
            grow(region, random, x, y, z, x, z, 0);
            return true;
        }

        private static void grow(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                                 final int rootX, final int rootZ, final int depth) {
            int height = random.nextInt(4) + 1;
            if (depth == 0) {
                height++;
            }
            for (int i = 1; i <= height; i++) {
                if (!sidesEmpty(region, x, y + i, z, -1)) {
                    return;
                }
                join(region, x, y + i, z);
                join(region, x, y + i - 1, z);
            }
            boolean branched = false;
            if (depth < MAX_DEPTH) {
                int branches = random.nextInt(4);
                if (depth == 0) {
                    branches++;
                }
                final int top = y + height;
                for (int i = 0; i < branches; i++) {
                    final int side = Facing.randomHorizontal(random);
                    final int bx = x + Facing.DX[side];
                    final int bz = z + Facing.DZ[side];
                    if (Math.abs(bx - rootX) < SPREAD && Math.abs(bz - rootZ) < SPREAD
                            && region.isAir(bx, top, bz) && region.isAir(bx, top - 1, bz)
                            && sidesEmpty(region, bx, top, bz, Facing.OPPOSITE[side])) {
                        branched = true;
                        join(region, bx, top, bz);
                        join(region, x, top, z);
                        grow(region, random, bx, top, bz, rootX, rootZ, depth + 1);
                    }
                }
            }
            if (!branched) {
                region.set(x, y + height, z, region.blocks().state(BlockTypeKeys.CHORUS_FLOWER).withIfAllowed(BlockProperties.AGE_0_5, RIPE));
            }
        }

        /**
         * {@return {@code true} if the four blocks beside a block are empty, but the one on a given side}
         *
         * @param ignored the side not looked at, {@code -1} for none
         */
        private static boolean sidesEmpty(final DecorationRegion region, final int x, final int y, final int z, final int ignored) {
            for (final int side : Facing.HORIZONTAL) {
                if (side != ignored && !region.isAir(x + Facing.DX[side], y, z + Facing.DZ[side])) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isChorus(final DecorationRegion region, final int x, final int y, final int z) {
            final BlockState state = region.state(x, y, z);
            return state.name().equals(BlockTypeKeys.CHORUS_PLANT) || state.name().equals(BlockTypeKeys.CHORUS_FLOWER);
        }

        /**
         * Sets a chorus plant at a block, joined to the chorus around it, and down to the ground it stands on.
         */
        private static void join(final DecorationRegion region, final int x, final int y, final int z) {
            final BlockLookup blocks = region.blocks();
            BlockState plant = blocks.state(BlockTypeKeys.CHORUS_PLANT);
            plant = plant.withIfAllowed(BlockProperties.DOWN, isChorus(region, x, y - 1, z)
                    || blocks.is(region.state(x, y - 1, z), BlockTypeTagKeys.SUPPORTS_CHORUS_PLANT));
            plant = plant.withIfAllowed(BlockProperties.UP, isChorus(region, x, y + 1, z));
            for (final int side : SIDES) {
                plant = plant.withIfAllowed(Facing.FLAGS[side], isChorus(region, x + Facing.DX[side], y, z + Facing.DZ[side]));
            }
            region.set(x, y, z, plant);
        }
    }
}
