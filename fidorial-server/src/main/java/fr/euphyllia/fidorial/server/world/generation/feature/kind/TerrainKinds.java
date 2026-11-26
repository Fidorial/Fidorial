package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.feature.BlockTest;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.HeightmapKind;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * The features that reshape the ground a little: disks of sand or clay, lakes, springs, the frozen top layer,
 * mossy boulders, ice spikes, blue ice, magma under the sea.
 */
final class TerrainKinds {

    private static final int[][] HORIZONTAL = {{0, 0, -1}, {1, 0, 0}, {0, 0, 1}, {-1, 0, 0}};
    private static final int[][] ALL_SIDES = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};

    private TerrainKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("disk", TerrainKinds::disk);
        parsers.put("lake", TerrainKinds::lake);
        parsers.put("spring_feature", TerrainKinds::spring);
        parsers.put("freeze_top_layer", (library, data) -> TerrainKinds::freezeTopLayer);
        parsers.put("block_blob", TerrainKinds::blockBlob);
        parsers.put("blue_ice", (library, data) -> TerrainKinds::blueIce);
        parsers.put("spike", TerrainKinds::spike);
        parsers.put("underwater_magma", TerrainKinds::underwaterMagma);
    }

    private static Feature disk(final FeatureLibrary library, final JsonObject data) {
        final StateSource source = library.source(data.get("state_provider"));
        final BlockTest target = library.test(data.get("target"));
        final IntSource radius = IntSource.parse(data.get("radius"));
        final int halfHeight = data.get("half_height").getAsInt();
        return (region, random, x, y, z) -> {
            boolean placedAny = false;
            final int top = y + halfHeight;
            final int bottom = y - halfHeight - 1;
            final int r = radius.sample(random);
            for (int pz = z - r; pz <= z + r; pz++) {
                for (int px = x - r; px <= x + r; px++) {
                    final int dx = px - x;
                    final int dz = pz - z;
                    if (dx * dx + dz * dz > r * r) {
                        continue;
                    }
                    for (int py = top; py > bottom; py--) {
                        if (target.test(region, px, py, pz)) {
                            final char state = source.pick(region, random, px, py, pz);
                            if (state != StateSource.NONE) {
                                region.set(px, py, pz, state);
                                placedAny = true;
                            }
                        }
                    }
                }
            }
            return placedAny;
        };
    }

    private static Feature lake(final FeatureLibrary library, final JsonObject data) {
        final StateSource fluidSource = library.source(data.get("fluid"));
        final StateSource barrierSource = library.source(data.get("barrier"));
        final BlockTest canPlace = library.test(data.get("can_place_feature"));
        final BlockTest canEmpty = library.test(data.get("can_replace_with_air_or_fluid"));
        final BlockTest canWall = library.test(data.get("can_replace_with_barrier"));
        return (region, random, originX, originY, originZ) -> {
            if (originY <= region.minY() + 4) {
                return false;
            }
            final int x0 = originX - 8;
            final int y0 = originY - 4;
            final int z0 = originZ - 8;
            final boolean[] grid = new boolean[2048];
            final int spots = random.nextInt(4) + 4;
            for (int i = 0; i < spots; i++) {
                final double xr = random.nextDouble() * 6.0 + 3.0;
                final double yr = random.nextDouble() * 4.0 + 2.0;
                final double zr = random.nextDouble() * 6.0 + 3.0;
                final double xp = random.nextDouble() * (16.0 - xr - 2.0) + 1.0 + xr / 2.0;
                final double yp = random.nextDouble() * (8.0 - yr - 4.0) + 2.0 + yr / 2.0;
                final double zp = random.nextDouble() * (16.0 - zr - 2.0) + 1.0 + zr / 2.0;
                for (int xx = 1; xx < 15; xx++) {
                    for (int zz = 1; zz < 15; zz++) {
                        for (int yy = 1; yy < 7; yy++) {
                            final double xd = (xx - xp) / (xr / 2.0);
                            final double yd = (yy - yp) / (yr / 2.0);
                            final double zd = (zz - zp) / (zr / 2.0);
                            if (xd * xd + yd * yd + zd * zd < 1.0) {
                                grid[(xx * 16 + zz) * 8 + yy] = true;
                            }
                        }
                    }
                }
            }
            final StateFlags states = region.states();
            final char fluid = fluidSource.pickOrKeep(region, random, originX, originY, originZ);
            for (int xx = 0; xx < 16; xx++) {
                for (int zz = 0; zz < 16; zz++) {
                    for (int yy = 0; yy < 8; yy++) {
                        if (!onLakeEdge(grid, xx, yy, zz)) {
                            continue;
                        }
                        final char state = region.id(x0 + xx, y0 + yy, z0 + zz);
                        if (yy >= 4 && states.has(state, StateFlags.LIQUID_BLOCK)) {
                            return false;
                        }
                        if (yy < 4 && !states.has(state, StateFlags.SOLID) && state != fluid) {
                            return false;
                        }
                        if (!canPlace.test(region, x0 + xx, y0 + yy, z0 + zz)) {
                            return false;
                        }
                    }
                }
            }
            final char caveAir = region.id(region.blocks().caveAir);
            for (int xx = 0; xx < 16; xx++) {
                for (int zz = 0; zz < 16; zz++) {
                    for (int yy = 0; yy < 8; yy++) {
                        if (grid[(xx * 16 + zz) * 8 + yy] && canEmpty.test(region, x0 + xx, y0 + yy, z0 + zz)) {
                            region.set(x0 + xx, y0 + yy, z0 + zz, yy >= 4 ? caveAir : fluid);
                        }
                    }
                }
            }
            final char barrier = barrierSource.pickOrKeep(region, random, originX, originY, originZ);
            if (!states.isAir(barrier)) {
                for (int xx = 0; xx < 16; xx++) {
                    for (int zz = 0; zz < 16; zz++) {
                        for (int yy = 0; yy < 8; yy++) {
                            if (onLakeEdge(grid, xx, yy, zz) && (yy < 4 || random.nextInt(2) != 0)) {
                                final int px = x0 + xx;
                                final int py = y0 + yy;
                                final int pz = z0 + zz;
                                if (region.has(px, py, pz, StateFlags.SOLID) && canWall.test(region, px, py, pz)) {
                                    region.set(px, py, pz, barrier);
                                }
                            }
                        }
                    }
                }
            }
            if (states.has(fluid, StateFlags.WATER)) {
                for (int xx = 0; xx < 16; xx++) {
                    for (int zz = 0; zz < 16; zz++) {
                        final int px = x0 + xx;
                        final int py = y0 + 4;
                        final int pz = z0 + zz;
                        if (shouldFreeze(region, px, py, pz) && canEmpty.test(region, px, py, pz)) {
                            region.set(px, py, pz, region.blocks().state(BlockTypeKeys.ICE));
                        }
                    }
                }
            }
            return true;
        };
    }

    private static boolean onLakeEdge(final boolean[] grid, final int xx, final int yy, final int zz) {
        return !grid[(xx * 16 + zz) * 8 + yy]
                && (xx < 15 && grid[((xx + 1) * 16 + zz) * 8 + yy]
                || xx > 0 && grid[((xx - 1) * 16 + zz) * 8 + yy]
                || zz < 15 && grid[(xx * 16 + zz + 1) * 8 + yy]
                || zz > 0 && grid[(xx * 16 + (zz - 1)) * 8 + yy]
                || yy < 7 && grid[(xx * 16 + zz) * 8 + yy + 1]
                || yy > 0 && grid[(xx * 16 + zz) * 8 + (yy - 1)]);
    }

    private static boolean coldAt(final DecorationRegion region, final BiomeInfo biome, final int x, final int y, final int z) {
        return region.generator().biomeClimate().coldEnoughToSnow(biome, x, y, z, region.heights().seaLevel());
    }

    /**
     * {@return {@code true} if the water at a block turns to ice}: a still water block in a cold place
     */
    static boolean shouldFreeze(final DecorationRegion region, final int x, final int y, final int z) {
        if (region.isOutsideHeight(y)) {
            return false;
        }
        final char state = region.id(x, y, z);
        final StateFlags states = region.states();
        if (!states.has(state, StateFlags.WATER_SOURCE) || !states.has(state, StateFlags.LIQUID_BLOCK)) {
            return false;
        }
        return coldAt(region, region.biome(x, y, z), x, y, z);
    }

    /**
     * {@return {@code true} if snow settles at a block}: an empty block or a snow layer, cold, where it snows,
     * on ground that holds snow
     */
    static boolean shouldSnow(final DecorationRegion region, final BiomeInfo biome, final int x, final int y, final int z) {
        if (!biome.hasPrecipitation() || !coldAt(region, biome, x, y, z) || region.isOutsideHeight(y)) {
            return false;
        }
        final char state = region.id(x, y, z);
        final StateFlags states = region.states();
        if (!states.isAir(state) && !states.is(state, BlockTypeKeys.SNOW)) {
            return false;
        }
        return region.canSurvive(region.id(region.blocks().state(BlockTypeKeys.SNOW)), x, y, z);
    }

    private static boolean freezeTopLayer(final DecorationRegion region, final BitRandom random, final int originX,
                                          final int originY, final int originZ) {
        final StateFlags states = region.states();
        final char ice = region.id(region.blocks().state(BlockTypeKeys.ICE));
        final char snow = region.id(region.blocks().state(BlockTypeKeys.SNOW));
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                final int x = originX + dx;
                final int z = originZ + dz;
                final int y = region.height(HeightmapKind.MOTION_BLOCKING, x, z);
                final BiomeInfo biome = region.biome(x, y, z);
                if (!coldAt(region, biome, x, y - 1, z) ? false : shouldFreezeIn(region, biome, x, y - 1, z)) {
                    region.set(x, y - 1, z, ice);
                }
                if (shouldSnow(region, biome, x, y, z)) {
                    region.set(x, y, z, snow);
                    final char below = region.id(x, y - 1, z);
                    if (states.property(below, "snowy") != null) {
                        region.set(x, y - 1, z, states.with(below, "snowy", "true"));
                    }
                }
            }
        }
        return true;
    }

    /**
     * Like {@link #shouldFreeze}, with the biome of the block above.
     */
    private static boolean shouldFreezeIn(final DecorationRegion region, final BiomeInfo biome, final int x, final int y, final int z) {
        if (region.isOutsideHeight(y)) {
            return false;
        }
        final char state = region.id(x, y, z);
        final StateFlags states = region.states();
        return states.has(state, StateFlags.WATER_SOURCE) && states.has(state, StateFlags.LIQUID_BLOCK);
    }

    private static Feature spring(final FeatureLibrary library, final JsonObject data) {
        final JsonObject fluidData = data.getAsJsonObject("state");
        final Key fluidId = Key.key((fluidData.has("id") ? fluidData.get("id") : fluidData.get("Name")).getAsString());
        final FluidType fluidType = FluidType.byFluidKey(TypedKey.withRegistry(RegistryKey.FLUID, fluidId));
        if (fluidType == null) {
            throw new IllegalArgumentException("A spring cannot be made of " + fluidId.asString());
        }
        final char fluid = library.context().states().palette().id(library.context().blocks().state(fluidType.blockKey()));
        final boolean needsBelow = !data.has("requires_block_below") || data.get("requires_block_below").getAsBoolean();
        final int rocks = data.has("rock_count") ? data.get("rock_count").getAsInt() : 4;
        final int holes = data.has("hole_count") ? data.get("hole_count").getAsInt() : 1;
        final StateFlags.Membership valid = library.blockSet(data.get("valid_blocks"));
        return (region, random, x, y, z) -> {
            if (!valid.test(region.id(x, y + 1, z))) {
                return false;
            }
            if (needsBelow && !valid.test(region.id(x, y - 1, z))) {
                return false;
            }
            final char current = region.id(x, y, z);
            if (!region.states().isAir(current) && !valid.test(current)) {
                return false;
            }
            int rockCount = 0;
            int holeCount = 0;
            final int[][] around = {{-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 0, 1}, {0, -1, 0}};
            for (final int[] offset : around) {
                if (valid.test(region.id(x + offset[0], y + offset[1], z + offset[2]))) {
                    rockCount++;
                }
            }
            for (final int[] offset : around) {
                if (region.isAir(x + offset[0], y + offset[1], z + offset[2])) {
                    holeCount++;
                }
            }
            if (rockCount == rocks && holeCount == holes) {
                region.set(x, y, z, fluid);
                region.scheduleFluid(x, y, z);
                return true;
            }
            return false;
        };
    }

    private static Feature blockBlob(final FeatureLibrary library, final JsonObject data) {
        final char state = library.state(data.get("state"));
        final BlockTest canPlaceOn = library.test(data.get("can_place_on"));
        return (region, random, x, y, z) -> {
            int cy = y;
            while (cy > region.minY() + 3 && !canPlaceOn.test(region, x, cy - 1, z)) {
                cy--;
            }
            if (cy <= region.minY() + 3) {
                return false;
            }
            int cx = x;
            int cz = z;
            for (int c = 0; c < 3; c++) {
                final int xr = random.nextInt(2);
                final int yr = random.nextInt(2);
                final int zr = random.nextInt(2);
                final float reach = (xr + yr + zr) * 0.333F + 0.5F;
                for (int px = cx - xr; px <= cx + xr; px++) {
                    for (int py = cy - yr; py <= cy + yr; py++) {
                        for (int pz = cz - zr; pz <= cz + zr; pz++) {
                            final double dx = px - cx;
                            final double dy = py - cy;
                            final double dz = pz - cz;
                            if (dx * dx + dy * dy + dz * dz <= reach * reach) {
                                region.set(px, py, pz, state);
                            }
                        }
                    }
                }
                cx += -1 + random.nextInt(2);
                cy += -random.nextInt(2);
                cz += -1 + random.nextInt(2);
            }
            return true;
        };
    }

    private static boolean blueIce(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
        final StateFlags states = region.states();
        if (y > region.heights().seaLevel() - 1) {
            return false;
        }
        if (!states.isWaterBlock(region.id(x, y, z)) && !states.isWaterBlock(region.id(x, y - 1, z))) {
            return false;
        }
        boolean packedIceNear = false;
        for (final int[] side : ALL_SIDES) {
            if (side[1] != -1 && states.is(region.id(x + side[0], y + side[1], z + side[2]), BlockTypeKeys.PACKED_ICE)) {
                packedIceNear = true;
                break;
            }
        }
        if (!packedIceNear) {
            return false;
        }
        final char blueIce = region.id(region.blocks().state(BlockTypeKeys.BLUE_ICE));
        region.set(x, y, z, blueIce);
        for (int i = 0; i < 200; i++) {
            final int dy = random.nextInt(5) - random.nextInt(6);
            int spread = 3;
            if (dy < 2) {
                spread += dy / 2;
            }
            if (spread < 1) {
                continue;
            }
            final int px = x + random.nextInt(spread) - random.nextInt(spread);
            final int py = y + dy;
            final int pz = z + random.nextInt(spread) - random.nextInt(spread);
            final char state = region.id(px, py, pz);
            if (states.isAir(state) || states.isWaterBlock(state) || states.is(state, BlockTypeKeys.PACKED_ICE)
                    || states.is(state, BlockTypeKeys.ICE)) {
                for (final int[] side : ALL_SIDES) {
                    if (region.id(px + side[0], py + side[1], pz + side[2]) == blueIce) {
                        region.set(px, py, pz, blueIce);
                        break;
                    }
                }
            }
        }
        return true;
    }

    private static Feature spike(final FeatureLibrary library, final JsonObject data) {
        final char state = library.state(data.get("state"));
        final BlockTest canPlaceOn = library.test(data.get("can_place_on"));
        final BlockTest canReplace = library.test(data.get("can_replace"));
        return (region, random, x, y, z) -> {
            int oy = y;
            while (region.isAir(x, oy, z) && oy > region.minY() + 2) {
                oy--;
            }
            if (!canPlaceOn.test(region, x, oy, z)) {
                return false;
            }
            oy += random.nextInt(4);
            final int height = random.nextInt(4) + 7;
            final int width = height / 4 + random.nextInt(2);
            if (width > 1 && random.nextInt(60) == 0) {
                oy += 10 + random.nextInt(30);
            }
            for (int dy = 0; dy < height; dy++) {
                final float scale = (1.0F - (float) dy / height) * width;
                final int reach = (int) Math.ceil(scale);
                for (int dx = -reach; dx <= reach; dx++) {
                    final float fx = Math.abs(dx) - 0.25F;
                    for (int dz = -reach; dz <= reach; dz++) {
                        final float fz = Math.abs(dz) - 0.25F;
                        final boolean inside = dx == 0 && dz == 0 || !(fx * fx + fz * fz > scale * scale);
                        final boolean rim = dx == -reach || dx == reach || dz == -reach || dz == reach;
                        if (inside && (!rim || !(random.nextFloat() > 0.75F))) {
                            if (region.isAir(x + dx, oy + dy, z + dz) || canReplace.test(region, x + dx, oy + dy, z + dz)) {
                                region.set(x + dx, oy + dy, z + dz, state);
                            }
                            if (dy != 0 && reach > 1
                                    && (region.isAir(x + dx, oy - dy, z + dz) || canReplace.test(region, x + dx, oy - dy, z + dz))) {
                                region.set(x + dx, oy - dy, z + dz, state);
                            }
                        }
                    }
                }
            }
            final int pillar = Math.clamp(width - 1, 0, 1);
            for (int dx = -pillar; dx <= pillar; dx++) {
                for (int dz = -pillar; dz <= pillar; dz++) {
                    int cy = oy - 1;
                    int run = 50;
                    if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
                        run = random.nextInt(5);
                    }
                    while (cy > 50) {
                        final char current = region.id(x + dx, cy, z + dz);
                        if (!region.states().isAir(current) && !canReplace.test(region, x + dx, cy, z + dz) && current != state) {
                            break;
                        }
                        region.set(x + dx, cy, z + dz, state);
                        cy--;
                        if (--run <= 0) {
                            cy -= random.nextInt(5) + 1;
                            run = random.nextInt(5);
                        }
                    }
                }
            }
            return true;
        };
    }

    private static Feature underwaterMagma(final FeatureLibrary library, final JsonObject data) {
        final int searchRange = data.get("floor_search_range").getAsInt();
        final int radius = data.get("placement_radius_around_floor").getAsInt();
        final float chance = data.get("placement_probability_per_valid_position").getAsFloat();
        return (region, random, x, y, z) -> {
            final StateFlags states = region.states();
            final int floor = waterFloor(region, x, y, z, searchRange);
            if (floor == Integer.MIN_VALUE) {
                return false;
            }
            final char magma = region.id(region.blocks().state(BlockTypeKeys.MAGMA_BLOCK));
            int placed = 0;
            for (int pz = z - radius; pz <= z + radius; pz++) {
                for (int py = floor - radius; py <= floor + radius; py++) {
                    for (int px = x - radius; px <= x + radius; px++) {
                        if (!(random.nextFloat() < chance)) {
                            continue;
                        }
                        if (magmaFits(region, states, px, py, pz)) {
                            region.set(px, py, pz, magma);
                            placed++;
                        }
                    }
                }
            }
            return placed > 0;
        };
    }

    /**
     * {@return the height of the floor under the water column through a block, {@code Integer.MIN_VALUE} when the
     * column does not end on a floor in range}
     */
    private static int waterFloor(final DecorationRegion region, final int x, final int y, final int z, final int range) {
        final StateFlags states = region.states();
        if (!states.isWaterBlock(region.id(x, y, z))) {
            return Integer.MIN_VALUE;
        }
        int py = y;
        for (int i = 1; i < range && states.isWaterBlock(region.id(x, py, z)); i++) {
            py--;
        }
        return states.isWaterBlock(region.id(x, py, z)) ? Integer.MIN_VALUE : py;
    }

    private static boolean magmaFits(final DecorationRegion region, final StateFlags states, final int x, final int y, final int z) {
        final char state = region.id(x, y, z);
        if (states.isWaterBlock(state) || states.isAir(state)) {
            return false;
        }
        if (open(region, states, x, y - 1, z)) {
            return false;
        }
        for (final int[] side : HORIZONTAL) {
            if (open(region, states, x + side[0], y, z + side[2])) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@return {@code true} if a block lets the one next to it be seen: it is not a full solid cube}
     */
    private static boolean open(final DecorationRegion region, final StateFlags states, final int x, final int y, final int z) {
        return !states.has(region.id(x, y, z), StateFlags.FULL_COLLISION);
    }
}
