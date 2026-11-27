package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.FloatSource;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Trig;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.data.type.Speleothem;

import java.util.Map;
import java.util.function.IntPredicate;

/**
 * Stalactites and stalagmites of any pointed block, such as dripstone: single ones, clusters filling a cave
 * between floor and ceiling, and large columns of the base block.
 */
final class SpeleothemKinds {

    private static final int NONE = Integer.MIN_VALUE;

    private SpeleothemKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("speleothem", SpeleothemKinds::single);
        parsers.put("speleothem_cluster", (library, data) -> new Cluster(library, data));
        parsers.put("large_speleothem", (library, data) -> new Large(library, data));
    }

    /**
     * The blocks a speleothem is made of.
     *
     * @param base        the block it grows from
     * @param pointed     the block of the speleothem itself
     * @param replaceable the blocks the base may replace
     */
    private record Blocks(char base, char pointed, StateFlags.Membership replaceable) {

        static Blocks read(final FeatureLibrary library, final JsonObject data) {
            return new Blocks(library.state(data.get("base_block")),
                    data.has("pointed_block") ? library.state(data.get("pointed_block")) : '\0',
                    library.blockSet(data.get("replaceable_blocks")));
        }

        boolean isBase(final StateFlags states, final char state) {
            return PlantKinds.sameBlock(states, state, base) || replaceable.test(state);
        }

        boolean placeBase(final DecorationRegion region, final int x, final int y, final int z) {
            if (replaceable.test(region.id(x, y, z))) {
                region.set(x, y, z, base);
                return true;
            }
            return false;
        }

        /**
         * Grows a speleothem from the block next to its base, as long as the base is there.
         *
         * @param down        {@code true} for a stalactite
         * @param attachBase  {@code true} if the block against the base is marked as attached
         */
        void grow(final DecorationRegion region, final int x, final int y, final int z, final boolean down, final int length,
                  final boolean mergedTip, final boolean attachBase) {
            final StateFlags states = region.states();
            final int step = down ? -1 : 1;
            if (!isBase(states, region.id(x, y - step, z))) {
                return;
            }
            int py = y;
            for (int remaining = length; remaining > 0; remaining--) {
                final boolean atBase = remaining == length;
                final Speleothem.Thickness thickness;
                if (remaining == 2) {
                    thickness = Speleothem.Thickness.FRUSTUM;
                } else if (remaining == 1) {
                    thickness = mergedTip ? Speleothem.Thickness.TIP_MERGE : Speleothem.Thickness.TIP;
                } else if (atBase) {
                    thickness = Speleothem.Thickness.BASE;
                } else {
                    thickness = Speleothem.Thickness.MIDDLE;
                }
                char state = states.with(states.with(pointed, BlockProperties.VERTICAL_DIRECTION, down ? BlockFace.DOWN : BlockFace.UP), BlockProperties.THICKNESS, thickness);
                if (atBase && attachBase) {
                    state = states.with(state, BlockProperties.ATTACHED, true);
                }
                state = BasicKinds.keepWater(region, state, x, py, z);
                region.set(x, py, z, state);
                py += step;
            }
        }
    }

    private static boolean emptyOrWater(final StateFlags states, final char state) {
        return states.isAir(state) || states.isWaterBlock(state);
    }

    private static boolean emptyOrWaterOrLava(final StateFlags states, final char state) {
        return emptyOrWater(states, state) || states.isLavaBlock(state);
    }

    /**
     * The open part of a column between a floor and a ceiling, either may be missing.
     *
     * @param floor   the height of the block under the opening, {@link #NONE} when none was found
     * @param ceiling the height of the block over the opening, {@link #NONE} when none was found
     */
    private record Column(int floor, int ceiling) {

        boolean hasFloor() {
            return floor != NONE;
        }

        boolean hasCeiling() {
            return ceiling != NONE;
        }

        /**
         * {@return the room between floor and ceiling, {@link #NONE} unless both are known}
         */
        int height() {
            return hasFloor() && hasCeiling() ? ceiling - floor - 1 : NONE;
        }

        /**
         * Scans up and down from a block that belongs to the opening, a limited number of steps each way.
         *
         * @return {@code null} when the starting block is not part of an opening
         */
        static Column scan(final DecorationRegion region, final int x, final int y, final int z, final int range,
                           final IntPredicate inside, final IntPredicate edge) {
            if (!inside.test(region.id(x, y, z))) {
                return null;
            }
            return new Column(edge(region, x, y, z, range, inside, edge, -1), edge(region, x, y, z, range, inside, edge, 1));
        }

        private static int edge(final DecorationRegion region, final int x, final int y, final int z, final int range,
                                final IntPredicate inside, final IntPredicate edge, final int step) {
            int py = y;
            for (int i = 1; i < range && inside.test(region.id(x, py, z)); i++) {
                py += step;
            }
            return edge.test(region.id(x, py, z)) ? py : NONE;
        }
    }

    private static float chance(final JsonObject data, final String key, final float fallback) {
        return data.has(key) ? data.get(key).getAsFloat() : fallback;
    }

    private static Feature single(final FeatureLibrary library, final JsonObject data) {
        final Blocks blocks = Blocks.read(library, data);
        final float taller = chance(data, "chance_of_taller_generation", 0.2F);
        final float directional = chance(data, "chance_of_directional_spread", 0.7F);
        final float radius2 = chance(data, "chance_of_spread_radius2", 0.5F);
        final float radius3 = chance(data, "chance_of_spread_radius3", 0.5F);
        return (region, random, x, y, z) -> {
            final StateFlags states = region.states();
            final boolean above = blocks.isBase(states, region.id(x, y + 1, z));
            final boolean below = blocks.isBase(states, region.id(x, y - 1, z));
            final boolean down;
            if (above && below) {
                down = random.nextBoolean();
            } else if (above || below) {
                down = above;
            } else {
                return false;
            }
            final int rootY = down ? y + 1 : y - 1;
            blocks.placeBase(region, x, rootY, z);
            for (final int side : Facing.HORIZONTAL) {
                if (random.nextFloat() > directional) {
                    continue;
                }
                final int x1 = x + Facing.DX[side];
                final int z1 = z + Facing.DZ[side];
                blocks.placeBase(region, x1, rootY, z1);
                if (random.nextFloat() > radius2) {
                    continue;
                }
                final int d2 = Facing.random(random);
                final int x2 = x1 + Facing.DX[d2];
                final int y2 = rootY + Facing.DY[d2];
                final int z2 = z1 + Facing.DZ[d2];
                blocks.placeBase(region, x2, y2, z2);
                if (random.nextFloat() > radius3) {
                    continue;
                }
                final int d3 = Facing.random(random);
                blocks.placeBase(region, x2 + Facing.DX[d3], y2 + Facing.DY[d3], z2 + Facing.DZ[d3]);
            }
            final int tipY = down ? y - 1 : y + 1;
            final int height = random.nextFloat() < taller && emptyOrWater(states, region.id(x, tipY, z)) ? 2 : 1;
            blocks.grow(region, x, y, z, down, height, false, false);
            return true;
        };
    }

    /**
     * A cluster: in each column of an area, a stalactite and a stalagmite whose height falls off from the
     * middle, sometimes meeting, sometimes over a small pool.
     */
    private static final class Cluster implements Feature {

        private final Blocks blocks;
        private final int searchRange;
        private final IntSource height;
        private final IntSource radius;
        private final int maxHeightDifference;
        private final int heightDeviation;
        private final IntSource layerThickness;
        private final FloatSource density;
        private final FloatSource wetness;
        private final float edgeChance;
        private final int edgeDistance;
        private final int centerDistance;
        private final String mode;
        private final boolean attachBase;
        private final boolean allowWater;

        Cluster(final FeatureLibrary library, final JsonObject data) {
            this.blocks = Blocks.read(library, data);
            this.searchRange = data.get("floor_to_ceiling_search_range").getAsInt();
            this.height = IntSource.parse(data.get("height"));
            this.radius = IntSource.parse(data.get("radius"));
            this.maxHeightDifference = data.get("max_stalagmite_stalactite_height_diff").getAsInt();
            this.heightDeviation = data.get("height_deviation").getAsInt();
            this.layerThickness = IntSource.parse(data.get("speleothem_block_layer_thickness"));
            this.density = FloatSource.parse(data.get("density"));
            this.wetness = FloatSource.parse(data.get("wetness"));
            this.edgeChance = data.get("chance_of_speleothem_at_max_distance_from_center").getAsFloat();
            this.edgeDistance = data.get("max_distance_from_edge_affecting_chance_of_speleothem").getAsInt();
            this.centerDistance = data.get("max_distance_from_center_affecting_height_bias").getAsInt();
            final JsonObject options = data.has("placement_options") ? data.getAsJsonObject("placement_options") : null;
            this.mode = options == null ? "floor_and_ceiling" : options.get("placement_mode").getAsString();
            this.attachBase = options != null && "set_attached".equals(options.get("base_block_transformer").getAsString());
            this.allowWater = options == null || options.get("allow_water_placement").getAsBoolean();
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            if (!emptyOrWater(region.states(), region.id(x, y, z))) {
                return false;
            }
            final int clusterHeight = height.sample(random);
            final float wet = wetness.sample(random);
            final float dense = density.sample(random);
            final int xRadius = radius.sample(random);
            final int zRadius = radius.sample(random);
            for (int dx = -xRadius; dx <= xRadius; dx++) {
                for (int dz = -zRadius; dz <= zRadius; dz++) {
                    final double chance = chanceAt(xRadius, zRadius, dx, dz);
                    column(region, random, x + dx, y, z + dz, dx, dz, wet, chance, clusterHeight, dense);
                }
            }
            return true;
        }

        private boolean canPlace(final StateFlags states, final char state) {
            return allowWater ? emptyOrWater(states, state) : states.isAir(state);
        }

        private void column(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                            final int dx, final int dz, final float wet, final double chance, final int clusterHeight,
                            final float dense) {
            final StateFlags states = region.states();
            final IntPredicate canPlace = state -> canPlace(states, (char) state);
            final Column scanned = Column.scan(region, x, y, z, searchRange, canPlace,
                    state -> !states.isAir((char) state) && !states.isWaterBlock((char) state));
            if (scanned == null) {
                return;
            }
            final Column filtered = new Column(mode.equals("ceiling_only") ? NONE : scanned.floor(),
                    mode.equals("floor_only") ? NONE : scanned.ceiling());
            final int ceiling = filtered.ceiling();
            if (ceiling == NONE && filtered.floor() == NONE) {
                return;
            }
            final boolean wantPool = random.nextFloat() < wet;
            Column column = filtered;
            if (wantPool && filtered.hasFloor() && canPool(region, x, filtered.floor(), z)) {
                column = new Column(filtered.floor() - 1, filtered.ceiling());
                region.set(x, filtered.floor(), z, region.blocks().water);
            }
            final int floor = column.floor();
            final boolean wantStalactite = random.nextDouble() < chance;
            int stalactite = 0;
            if (ceiling != NONE && wantStalactite && !states.isLavaBlock(region.id(x, ceiling, z))) {
                final int thickness = layerThickness.sample(random);
                replaceWithBase(region, x, ceiling, z, thickness, 1);
                int maxHeight = maxHeight(scanned, clusterHeight);
                if (mode.equals("floor_and_ceiling") && floor != NONE) {
                    maxHeight = Math.min(clusterHeight, ceiling - floor);
                }
                stalactite = speleothemHeight(random, dx, dz, dense, maxHeight);
            }
            final boolean wantStalagmite = random.nextDouble() < chance;
            int stalagmite = 0;
            if (floor != NONE && wantStalagmite && !states.isLavaBlock(region.id(x, floor, z))) {
                final int thickness = layerThickness.sample(random);
                replaceWithBase(region, x, floor, z, thickness, -1);
                if (ceiling != NONE) {
                    stalagmite = Math.max(0, stalactite + random.nextIntBetweenInclusive(-maxHeightDifference, maxHeightDifference));
                } else {
                    stalagmite = speleothemHeight(random, dx, dz, dense, maxHeight(scanned, clusterHeight));
                }
            }
            int actualStalactite = stalactite;
            int actualStalagmite = stalagmite;
            if (ceiling != NONE && floor != NONE && ceiling - stalactite <= floor + stalagmite) {
                final int lowestBottom = Math.max(ceiling - stalactite, floor + 1);
                final int highestTop = Math.min(floor + stalagmite, ceiling - 1);
                final int bottom = random.nextIntBetweenInclusive(lowestBottom, highestTop + 1);
                actualStalactite = ceiling - bottom;
                actualStalagmite = bottom - 1 - floor;
            }
            if (ceiling != NONE) {
                actualStalactite = room(region, x, ceiling - 1, z, -1, actualStalactite, canPlace);
            }
            if (floor != NONE) {
                actualStalagmite = room(region, x, floor + 1, z, 1, actualStalagmite, canPlace);
            }
            final boolean merge = random.nextBoolean() && actualStalactite > 0 && actualStalagmite > 0
                    && column.height() != NONE && actualStalactite + actualStalagmite == column.height();
            if (ceiling != NONE) {
                blocks.grow(region, x, ceiling - 1, z, true, actualStalactite, merge, attachBase);
            }
            if (floor != NONE) {
                blocks.grow(region, x, floor + 1, z, false, actualStalagmite, merge, attachBase);
            }
        }

        private int maxHeight(final Column scanned, final int clusterHeight) {
            return !mode.equals("floor_and_ceiling") && scanned.height() != NONE ? Math.min(clusterHeight, scanned.height()) : clusterHeight;
        }

        private static int room(final DecorationRegion region, final int x, final int y, final int z, final int step,
                                final int desired, final IntPredicate canPlace) {
            int available = 0;
            int py = y;
            while (available < desired && canPlace.test(region.id(x, py, z))) {
                available++;
                py += step;
            }
            return available;
        }

        private int speleothemHeight(final BitRandom random, final int dx, final int dz, final float dense, final int maxHeight) {
            if (random.nextFloat() > dense) {
                return 0;
            }
            final int distance = Math.abs(dx) + Math.abs(dz);
            final float mean = (float) clampedMap(distance, 0.0, centerDistance, maxHeight / 2.0, 0.0);
            final float drawn = mean + (float) random.nextGaussian() * heightDeviation;
            return (int) Math.clamp(drawn, 0.0F, (float) maxHeight);
        }

        private boolean canPool(final DecorationRegion region, final int x, final int y, final int z) {
            final StateFlags states = region.states();
            final char state = region.id(x, y, z);
            if (states.isWaterBlock(state) || PlantKinds.sameBlock(states, state, blocks.base())
                    || PlantKinds.sameBlock(states, state, blocks.pointed())) {
                return false;
            }
            if (states.has(region.id(x, y + 1, z), StateFlags.WATER)) {
                return false;
            }
            for (final int side : Facing.HORIZONTAL) {
                if (!nextToWaterAllowed(region, x + Facing.DX[side], y, z + Facing.DZ[side])) {
                    return false;
                }
            }
            return nextToWaterAllowed(region, x, y - 1, z);
        }

        private static boolean nextToWaterAllowed(final DecorationRegion region, final int x, final int y, final int z) {
            final char state = region.id(x, y, z);
            return region.states().isTagged(state, BlockTypeTagKeys.BASE_STONE_OVERWORLD) || region.states().has(state, StateFlags.WATER);
        }

        private void replaceWithBase(final DecorationRegion region, final int x, final int y, final int z, final int count, final int step) {
            int py = y;
            for (int i = 0; i < count; i++) {
                if (!blocks.placeBase(region, x, py, z)) {
                    return;
                }
                py += step;
            }
        }

        private double chanceAt(final int xRadius, final int zRadius, final int dx, final int dz) {
            final int fromEdge = Math.min(xRadius - Math.abs(dx), zRadius - Math.abs(dz));
            return clampedMap((float) fromEdge, 0.0F, (float) edgeDistance, edgeChance, 1.0F);
        }
    }

    private static double clampedMap(final double value, final double fromLow, final double fromHigh, final double toLow, final double toHigh) {
        final double t = (value - fromLow) / (fromHigh - fromLow);
        if (t < 0.0) {
            return toLow;
        }
        if (t > 1.0) {
            return toHigh;
        }
        return toLow + t * (toHigh - toLow);
    }

    private static float clampedMap(final float value, final float fromLow, final float fromHigh, final float toLow, final float toHigh) {
        final float t = (value - fromLow) / (fromHigh - fromLow);
        if (t < 0.0F) {
            return toLow;
        }
        if (t > 1.0F) {
            return toHigh;
        }
        return toLow + t * (toHigh - toLow);
    }

    /**
     * A column of the base block, a stalactite and a stalagmite of the same width shaped by a smooth curve,
     * sometimes bent by a sideways drift.
     */
    private static final class Large implements Feature {

        private final Blocks blocks;
        private final int searchRange;
        private final IntSource radius;
        private final FloatSource heightScale;
        private final float radiusRatio;
        private final FloatSource stalactiteBluntness;
        private final FloatSource stalagmiteBluntness;
        private final FloatSource windSpeed;
        private final int minRadiusForWind;
        private final float minBluntnessForWind;

        Large(final FeatureLibrary library, final JsonObject data) {
            this.blocks = Blocks.read(library, data);
            this.searchRange = data.has("floor_to_ceiling_search_range") ? data.get("floor_to_ceiling_search_range").getAsInt() : 30;
            this.radius = IntSource.parse(data.get("column_radius"));
            this.heightScale = FloatSource.parse(data.get("height_scale"));
            this.radiusRatio = data.get("max_column_radius_to_cave_height_ratio").getAsFloat();
            this.stalactiteBluntness = FloatSource.parse(data.get("stalactite_bluntness"));
            this.stalagmiteBluntness = FloatSource.parse(data.get("stalagmite_bluntness"));
            this.windSpeed = FloatSource.parse(data.get("wind_speed"));
            this.minRadiusForWind = data.get("min_radius_for_wind").getAsInt();
            this.minBluntnessForWind = data.get("min_bluntness_for_wind").getAsFloat();
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            final StateFlags states = region.states();
            if (!emptyOrWater(states, region.id(x, y, z))) {
                return false;
            }
            final Column column = Column.scan(region, x, y, z, searchRange, state -> emptyOrWater(states, (char) state),
                    state -> blocks.isBase(states, (char) state) || states.isLavaBlock((char) state));
            if (column == null || column.height() == NONE || column.height() < 4) {
                return false;
            }
            final int byHeight = (int) (column.height() * radiusRatio);
            final int maxRadius = (int) Math.clamp((long) byHeight, radius.min(), radius.max());
            final int width = random.nextIntBetweenInclusive(radius.min(), maxRadius);
            final Spike stalactite = new Spike(x, column.ceiling() - 1, z, false, width,
                    stalactiteBluntness.sample(random), heightScale.sample(random));
            final Spike stalagmite = new Spike(x, column.floor() + 1, z, true, width,
                    stalagmiteBluntness.sample(random), heightScale.sample(random));
            final Wind wind;
            if (stalactite.windy(minRadiusForWind, minBluntnessForWind) && stalagmite.windy(minRadiusForWind, minBluntnessForWind)) {
                wind = new Wind(y, random, windSpeed, 16 - width);
            } else {
                wind = Wind.CALM;
            }
            final boolean topRooted = stalactite.root(region, wind);
            final boolean bottomRooted = stalagmite.root(region, wind);
            if (topRooted) {
                stalactite.build(region, random, wind, blocks.base());
            }
            if (bottomRooted) {
                stalagmite.build(region, random, wind, blocks.base());
            }
            return true;
        }
    }

    /**
     * A sideways drift that grows with the distance from a height.
     */
    private static final class Wind {

        static final Wind CALM = new Wind();

        private final int originY;
        private final double speedX;
        private final double speedZ;
        private final int maxOffset;
        private final boolean calm;

        private Wind() {
            this.originY = 0;
            this.speedX = 0.0;
            this.speedZ = 0.0;
            this.maxOffset = 0;
            this.calm = true;
        }

        Wind(final int originY, final BitRandom random, final FloatSource speedRange, final int maxOffset) {
            this.originY = originY;
            this.maxOffset = maxOffset;
            final float speed = speedRange.sample(random);
            final float direction = random.nextFloat() * (float) Math.PI;
            this.speedX = Trig.cos(direction) * speed;
            this.speedZ = Trig.sin(direction) * speed;
            this.calm = false;
        }

        int dx(final int y) {
            return calm ? 0 : (int) Math.clamp((long) Math.floor(speedX * (originY - y)), -maxOffset, maxOffset);
        }

        int dz(final int y) {
            return calm ? 0 : (int) Math.clamp((long) Math.floor(speedZ * (originY - y)), -maxOffset, maxOffset);
        }
    }

    /**
     * One half of a large speleothem.
     */
    private static final class Spike {

        private final int x;
        private int rootY;
        private final int z;
        private final boolean up;
        private int width;
        private final double bluntness;
        private final double scale;

        Spike(final int x, final int rootY, final int z, final boolean up, final int width, final double bluntness, final double scale) {
            this.x = x;
            this.rootY = rootY;
            this.z = z;
            this.up = up;
            this.width = width;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        boolean windy(final int minRadius, final float minBluntness) {
            return width >= minRadius && bluntness >= minBluntness;
        }

        private int heightAt(final float distance) {
            return (int) curve(distance, width, scale, bluntness);
        }

        /**
         * Moves the root back into the rock until a circle around it is mostly rock, halving the width when it
         * cannot.
         *
         * @return {@code false} if no root was found, or lava was met
         */
        boolean root(final DecorationRegion region, final Wind wind) {
            final StateFlags states = region.states();
            while (width > 1) {
                int y = rootY;
                final int tries = Math.min(10, heightAt(0.0F));
                for (int i = 0; i < tries; i++) {
                    if (states.isLavaBlock(region.id(x, y, z))) {
                        return false;
                    }
                    if (embedded(region, x + wind.dx(y), y, z + wind.dz(y), width)) {
                        rootY = y;
                        return true;
                    }
                    y += up ? -1 : 1;
                }
                width /= 2;
            }
            return false;
        }

        void build(final DecorationRegion region, final BitRandom random, final Wind wind, final char base) {
            final StateFlags states = region.states();
            for (int dx = -width; dx <= width; dx++) {
                for (int dz = -width; dz <= width; dz++) {
                    final float distance = (float) Math.sqrt(dx * dx + dz * dz);
                    if (distance > width) {
                        continue;
                    }
                    int height = heightAt(distance);
                    if (height <= 0) {
                        continue;
                    }
                    if (random.nextFloat() < 0.2) {
                        height = (int) (height * (random.nextFloat() * (1.0F - 0.8F) + 0.8F));
                    }
                    int y = rootY;
                    boolean leftRock = false;
                    final int maxY = up ? region.height(Heightmap.WORLD_SURFACE_WG, x + dx, z + dz) : Integer.MAX_VALUE;
                    for (int i = 0; i < height && y < maxY; i++) {
                        final int px = x + dx + wind.dx(y);
                        final int pz = z + dz + wind.dz(y);
                        final char state = region.id(px, y, pz);
                        if (emptyOrWaterOrLava(states, state)) {
                            leftRock = true;
                            region.set(px, y, pz, base);
                        } else if (leftRock && states.isTagged(state, BlockTypeTagKeys.BASE_STONE_OVERWORLD)) {
                            break;
                        }
                        y += up ? 1 : -1;
                    }
                }
            }
        }

        private static boolean embedded(final DecorationRegion region, final int x, final int y, final int z, final int radius) {
            final StateFlags states = region.states();
            if (emptyOrWaterOrLava(states, region.id(x, y, z))) {
                return false;
            }
            final float increment = 6.0F / radius;
            for (float angle = 0.0F; angle < (float) (Math.PI * 2); angle += increment) {
                final int dx = (int) (Trig.cos(angle) * radius);
                final int dz = (int) (Trig.sin(angle) * radius);
                if (emptyOrWaterOrLava(states, region.id(x + dx, y, z + dz))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * {@return the height of a speleothem at a distance from its axis}: a smooth bell that ends at its radius,
     * flattened near the axis by the bluntness
     */
    static double curve(double distance, final double radius, final double scale, final double bluntness) {
        if (distance < bluntness) {
            distance = bluntness;
        }
        final double r = distance / radius * 0.384;
        final double value = scale * (0.75 * Math.pow(r, 1.3333333333333333) - Math.pow(r, 0.6666666666666666)
                - 0.3333333333333333 * Math.log(r));
        return Math.max(value, 0.0) / 0.384 * radius;
    }
}
