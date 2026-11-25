package fr.euphyllia.fidorial.server.world.generation.terrain;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.euphyllia.fidorial.server.world.generation.random.XoroshiroSource;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Decides what fills the empty space under the ground: air, water or lava, and where a wall of stone separates two
 * bodies of fluid.
 *
 * <p>Underground space is split in cells of 16 by 12 by 16 blocks, each with a randomly placed centre and its own
 * fluid level, picked from the surface above it and a few noises: a flooded cell has the sea level, a dry one
 * nothing, a partly flooded one a level of its own. A block looks at the nearest centres; when two of them have
 * different levels, a barrier rises between them, thinner or thicker with a noise.</p>
 *
 * <p>One sampler serves one chunk and keeps what it learnt about the cells around it. Not thread-safe.</p>
 */
public final class AquiferSampler {

    private static final int CELL_WIDTH = 16;
    private static final int CELL_HEIGHT = 12;
    private static final int CENTER_RANGE_XZ = 10;
    private static final int CENTER_RANGE_Y = 9;
    private static final int SHIFT_XZ = -5;
    private static final int SHIFT_Y = 1;
    private static final int SURFACE_MARGIN = 8;
    private static final int FLOODEDNESS_DEPTH = 64;
    private static final int WAY_BELOW = -32512;
    private static final double FLOWING_SIMILARITY = similarity(10 * 10, 12 * 12);
    // where, around a centre, the surface is looked at, in chunks
    private static final int[][] SURFACE_OFFSETS = {
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

    private final Inputs inputs;
    private final DensityContext context;
    private final PositionalRandom random;
    private final FluidPicker global;
    private final BlockState air;
    private final BlockState water;
    private final BlockState lava;
    private final int minGridX;
    private final int minGridY;
    private final int minGridZ;
    private final int gridSizeX;
    private final int gridSizeZ;
    private final @Nullable FluidLevel[] statuses;
    private final long[] centers;
    private final Long2IntOpenHashMap surfaceLevels = new Long2IntOpenHashMap();
    private final int skipAboveY;
    private boolean scheduleFlow;

    /**
     * The functions the aquifer reads.
     *
     * @param barrier      how thick the walls between bodies of fluid are
     * @param floodedness  whether a cell is flooded, partly flooded or dry
     * @param spread       how far the level of a partly flooded cell strays from the middle of its band
     * @param lava         whether a deep cell holds lava rather than water
     * @param exclusion    keeps a cell dry where it is positive
     * @param surfaceLevel the height of the ground, roughly
     */
    public record Inputs(Density barrier, Density floodedness, Density spread, Density lava, Density exclusion, Density surfaceLevel) {
    }

    /**
     * The fluid of a place before any aquifer: the sea above some depth, lava below.
     */
    @FunctionalInterface
    public interface FluidPicker {
        FluidLevel at(int x, int y, int z);
    }

    /**
     * @param inputs the functions of the noise settings
     * @param context the evaluation context of the current thread
     * @param random the positional factory of the aquifers, named {@code minecraft:aquifer}
     * @param global the fluid a dry world would have
     * @param minX   the lowest block of the area served, along x
     * @param minY   the lowest block along y
     * @param minZ   the lowest block along z
     * @param maxX   the highest block along x, included
     * @param maxY   the highest block along y, included
     * @param maxZ   the highest block along z, included
     */
    public AquiferSampler(final Inputs inputs, final DensityContext context, final PositionalRandom random, final FluidPicker global,
                          final BlockState air, final BlockState water, final BlockState lava,
                          final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
        this.inputs = inputs;
        this.context = context;
        this.random = random;
        this.global = global;
        this.air = air;
        this.water = water;
        this.lava = lava;
        this.minGridX = gridXz(minX + SHIFT_XZ);
        final int maxGridX = gridXz(maxX + SHIFT_XZ) + 1;
        this.gridSizeX = maxGridX - minGridX + 1;
        this.minGridY = gridY(minY + SHIFT_Y) - 1;
        final int maxGridY = gridY(maxY + SHIFT_Y) + 1;
        final int gridSizeY = maxGridY - minGridY + 1;
        this.minGridZ = gridXz(minZ + SHIFT_XZ);
        final int maxGridZ = gridXz(maxZ + SHIFT_XZ) + 1;
        this.gridSizeZ = maxGridZ - minGridZ + 1;
        final int total = gridSizeX * gridSizeY * gridSizeZ;
        this.statuses = new FluidLevel[total];
        this.centers = new long[total];
        Arrays.fill(centers, Long.MAX_VALUE);

        // above the highest ground around, plus a cell, the aquifer has nothing to say
        final int highest = maxSurfaceLevel(blockOfGrid(minGridX, 0), blockOfGrid(minGridZ, 0),
                blockOfGrid(maxGridX, 9), blockOfGrid(maxGridZ, 9)) + SURFACE_MARGIN;
        final int skipGridY = gridY(highest + CELL_HEIGHT) + 1;
        this.skipAboveY = skipGridY * CELL_HEIGHT + 11 - 1;
    }

    private static int gridXz(final int block) {
        return block >> 4;
    }

    private static int gridY(final int block) {
        return Math.floorDiv(block, CELL_HEIGHT);
    }

    private static int blockOfGrid(final int grid, final int offset) {
        return (grid << 4) + offset;
    }

    private static double similarity(final int nearest, final int other) {
        return 1.0 - (other - nearest) / 25.0;
    }

    private static long pack(final int x, final int y, final int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | ((long) y & 0xFFFL);
    }

    private static int unpackX(final long packed) {
        return (int) (packed >> 38);
    }

    private static int unpackY(final long packed) {
        return (int) (packed << 52 >> 52);
    }

    private static int unpackZ(final long packed) {
        return (int) (packed << 26 >> 38);
    }

    private static long columnKey(final int x, final int z) {
        return (long) x << 32 | ((long) z & 0xFFFFFFFFL);
    }

    private int surfaceLevel(final int x, final int z) {
        final int quantizedX = x >> 2 << 2;
        final int quantizedZ = z >> 2 << 2;
        final long key = columnKey(quantizedX, quantizedZ);
        if (surfaceLevels.containsKey(key)) {
            return surfaceLevels.get(key);
        }
        final int level = (int) Math.floor(inputs.surfaceLevel().compute(context, quantizedX, 0, quantizedZ));
        surfaceLevels.put(key, level);
        return level;
    }

    private int maxSurfaceLevel(final int minX, final int minZ, final int maxX, final int maxZ) {
        int highest = Integer.MIN_VALUE;
        for (int x = minX >> 2; x <= maxX >> 2; x++) {
            for (int z = minZ >> 2; z <= maxZ >> 2; z++) {
                highest = Math.max(highest, surfaceLevel(x << 2, z << 2));
            }
        }
        return highest;
    }

    private int index(final int gridX, final int gridY, final int gridZ) {
        return ((gridY - minGridY) * gridSizeZ + (gridZ - minGridZ)) * gridSizeX + (gridX - minGridX);
    }

    /**
     * {@return whether the last block handed out is fluid that should be told to flow}
     */
    public boolean scheduleFlow() {
        return scheduleFlow;
    }

    /**
     * Decides a block.
     *
     * @param density the final density at the block; positive means solid ground
     * @return {@code null} for solid ground, air, or the fluid filling the block
     */
    public @Nullable BlockState substance(final int x, final int y, final int z, final double density) {
        if (density > 0.0) {
            scheduleFlow = false;
            return null;
        }
        final FluidLevel globalFluid = global.at(x, y, z);
        if (y > skipAboveY) {
            scheduleFlow = false;
            return globalFluid.at(y, air);
        }
        if (globalFluid.at(y, air) == lava) {
            scheduleFlow = false;
            return lava;
        }

        final int anchorX = gridXz(x + SHIFT_XZ);
        final int anchorY = gridY(y + SHIFT_Y);
        final int anchorZ = gridXz(z + SHIFT_XZ);
        int distance1 = Integer.MAX_VALUE;
        int distance2 = Integer.MAX_VALUE;
        int distance3 = Integer.MAX_VALUE;
        int distance4 = Integer.MAX_VALUE;
        int closest1 = 0;
        int closest2 = 0;
        int closest3 = 0;
        int closest4 = 0;
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    final int gridX = anchorX + dx;
                    final int gridY = anchorY + dy;
                    final int gridZ = anchorZ + dz;
                    final int index = index(gridX, gridY, gridZ);
                    long center = centers[index];
                    if (center == Long.MAX_VALUE) {
                        final XoroshiroSource draw = random.at(gridX, gridY, gridZ);
                        final int cx = blockOfGrid(gridX, draw.nextInt(CENTER_RANGE_XZ));
                        final int cy = gridY * CELL_HEIGHT + draw.nextInt(CENTER_RANGE_Y);
                        final int cz = blockOfGrid(gridZ, draw.nextInt(CENTER_RANGE_XZ));
                        center = pack(cx, cy, cz);
                        centers[index] = center;
                    }
                    final int ox = unpackX(center) - x;
                    final int oy = unpackY(center) - y;
                    final int oz = unpackZ(center) - z;
                    final int distance = ox * ox + oy * oy + oz * oz;
                    if (distance1 >= distance) {
                        closest4 = closest3;
                        closest3 = closest2;
                        closest2 = closest1;
                        closest1 = index;
                        distance4 = distance3;
                        distance3 = distance2;
                        distance2 = distance1;
                        distance1 = distance;
                    } else if (distance2 >= distance) {
                        closest4 = closest3;
                        closest3 = closest2;
                        closest2 = index;
                        distance4 = distance3;
                        distance3 = distance2;
                        distance2 = distance;
                    } else if (distance3 >= distance) {
                        closest4 = closest3;
                        closest3 = index;
                        distance4 = distance3;
                        distance3 = distance;
                    } else if (distance4 >= distance) {
                        closest4 = index;
                        distance4 = distance;
                    }
                }
            }
        }

        final FluidLevel status1 = status(closest1);
        final double similarity12 = similarity(distance1, distance2);
        final BlockState fluid = status1.at(y, air);
        if (similarity12 <= 0.0) {
            if (similarity12 >= FLOWING_SIMILARITY) {
                scheduleFlow = !status1.equals(status(closest2));
            } else {
                scheduleFlow = false;
            }
            return fluid;
        }
        if (fluid == water && global.at(x, y - 1, z).at(y - 1, air) == lava) {
            scheduleFlow = true;
            return fluid;
        }

        final double[] barrierNoise = {Double.NaN};
        final FluidLevel status2 = status(closest2);
        final double barrier12 = similarity12 * pressure(x, y, z, barrierNoise, status1, status2);
        if (density + barrier12 > 0.0) {
            scheduleFlow = false;
            return null;
        }
        final FluidLevel status3 = status(closest3);
        final double similarity13 = similarity(distance1, distance3);
        if (similarity13 > 0.0) {
            final double barrier13 = similarity12 * similarity13 * pressure(x, y, z, barrierNoise, status1, status3);
            if (density + barrier13 > 0.0) {
                scheduleFlow = false;
                return null;
            }
        }
        final double similarity23 = similarity(distance2, distance3);
        if (similarity23 > 0.0) {
            final double barrier23 = similarity12 * similarity23 * pressure(x, y, z, barrierNoise, status2, status3);
            if (density + barrier23 > 0.0) {
                scheduleFlow = false;
                return null;
            }
        }
        final boolean flow12 = !status1.equals(status2);
        final boolean flow23 = similarity23 >= FLOWING_SIMILARITY && !status2.equals(status3);
        final boolean flow13 = similarity13 >= FLOWING_SIMILARITY && !status1.equals(status3);
        if (!flow12 && !flow23 && !flow13) {
            scheduleFlow = similarity13 >= FLOWING_SIMILARITY
                    && similarity(distance1, distance4) >= FLOWING_SIMILARITY
                    && !status1.equals(status(closest4));
        } else {
            scheduleFlow = true;
        }
        return fluid;
    }

    /**
     * {@return how strongly two bodies of fluid push a wall between them; water against lava always does}
     */
    private double pressure(final int x, final int y, final int z, final double[] barrierNoise,
                            final FluidLevel first, final FluidLevel second) {
        final BlockState firstFluid = first.at(y, air);
        final BlockState secondFluid = second.at(y, air);
        if (firstFluid == lava && secondFluid == water || firstFluid == water && secondFluid == lava) {
            return 2.0;
        }
        final int levelGap = Math.abs(first.level() - second.level());
        if (levelGap == 0) {
            return 0.0;
        }
        final double middle = 0.5 * (first.level() + second.level());
        final double aboveMiddle = y + 0.5 - middle;
        final double halfGap = levelGap / 2.0;
        final double towardsMiddle = halfGap - Math.abs(aboveMiddle);
        final double gradient;
        if (aboveMiddle > 0.0) {
            final double center = towardsMiddle;
            gradient = center > 0.0 ? center / 1.5 : center / 2.5;
        } else {
            final double center = 3.0 + towardsMiddle;
            gradient = center > 0.0 ? center / 3.0 : center / 10.0;
        }
        final double noise;
        if (gradient >= -2.0 && gradient <= 2.0) {
            if (Double.isNaN(barrierNoise[0])) {
                barrierNoise[0] = inputs.barrier().compute(context, x, y, z);
            }
            noise = barrierNoise[0];
        } else {
            noise = 0.0;
        }
        return 2.0 * (noise + gradient);
    }

    private FluidLevel status(final int index) {
        final FluidLevel known = statuses[index];
        if (known != null) {
            return known;
        }
        final long center = centers[index];
        final FluidLevel computed = computeFluid(unpackX(center), unpackY(center), unpackZ(center));
        statuses[index] = computed;
        return computed;
    }

    private FluidLevel computeFluid(final int x, final int y, final int z) {
        final FluidLevel globalFluid = global.at(x, y, z);
        int lowestSurface = Integer.MAX_VALUE;
        final int cellTop = y + CELL_HEIGHT;
        final int cellBottom = y - CELL_HEIGHT;
        boolean centerUnderGlobalFluid = false;
        for (final int[] offset : SURFACE_OFFSETS) {
            final int sampleX = x + (offset[0] << 4);
            final int sampleZ = z + (offset[1] << 4);
            final int surface = surfaceLevel(sampleX, sampleZ);
            final int adjusted = surface + SURFACE_MARGIN;
            final boolean start = offset[0] == 0 && offset[1] == 0;
            if (start && cellBottom > adjusted) {
                return globalFluid;
            }
            final boolean pokesAbove = cellTop > adjusted;
            if (pokesAbove || start) {
                final FluidLevel atSurface = global.at(sampleX, adjusted, sampleZ);
                if (atSurface.at(adjusted, air) != air) {
                    if (start) {
                        centerUnderGlobalFluid = true;
                    }
                    if (pokesAbove) {
                        return atSurface;
                    }
                }
            }
            lowestSurface = Math.min(lowestSurface, surface);
        }
        final int level = fluidSurfaceLevel(x, y, z, globalFluid, lowestSurface, centerUnderGlobalFluid);
        return new FluidLevel(level, fluidType(x, y, z, globalFluid, level));
    }

    private int fluidSurfaceLevel(final int x, final int y, final int z, final FluidLevel globalFluid,
                                  final int lowestSurface, final boolean centerUnderGlobalFluid) {
        final double partly;
        final double fully;
        if (inputs.exclusion().compute(context, x, y, z) > 0.0) {
            partly = -1.0;
            fully = -1.0;
        } else {
            final int belowSurface = lowestSurface + SURFACE_MARGIN - y;
            final double factor = centerUnderGlobalFluid ? clampedMap(belowSurface, 0.0, FLOODEDNESS_DEPTH, 1.0, 0.0) : 0.0;
            final double noise = Math.clamp((double) inputs.floodedness().compute(context, x, y, z), -1.0, 1.0);
            final double fullThreshold = map(factor, 1.0, 0.0, -0.3, 0.8);
            final double partThreshold = map(factor, 1.0, 0.0, -0.8, 0.4);
            partly = noise - partThreshold;
            fully = noise - fullThreshold;
        }
        if (fully > 0.0) {
            return globalFluid.level();
        }
        if (partly > 0.0) {
            return randomizedLevel(x, y, z, lowestSurface);
        }
        return WAY_BELOW;
    }

    private static double map(final double value, final double fromMin, final double fromMax, final double toMin, final double toMax) {
        return toMin + (value - fromMin) / (fromMax - fromMin) * (toMax - toMin);
    }

    private static double clampedMap(final double value, final double fromMin, final double fromMax, final double toMin, final double toMax) {
        final double t = (value - fromMin) / (fromMax - fromMin);
        if (t < 0.0) {
            return toMin;
        }
        return t > 1.0 ? toMax : toMin + t * (toMax - toMin);
    }

    private int randomizedLevel(final int x, final int y, final int z, final int lowestSurface) {
        final int cellX = Math.floorDiv(x, 16);
        final int cellY = Math.floorDiv(y, 40);
        final int cellZ = Math.floorDiv(z, 16);
        final int middle = cellY * 40 + 20;
        final double spread = inputs.spread().compute(context, cellX, cellY, cellZ) * 10.0F;
        final int quantized = (int) Math.floor(spread / 3.0) * 3;
        return Math.min(lowestSurface, middle + quantized);
    }

    private BlockState fluidType(final int x, final int y, final int z, final FluidLevel globalFluid, final int level) {
        if (level <= -10 && level != WAY_BELOW && globalFluid.fluid() != lava) {
            final double noise = inputs.lava().compute(context, Math.floorDiv(x, 64), Math.floorDiv(y, 40), Math.floorDiv(z, 64));
            if (Math.abs(noise) > 0.3) {
                return lava;
            }
        }
        return globalFluid.fluid();
    }
}
