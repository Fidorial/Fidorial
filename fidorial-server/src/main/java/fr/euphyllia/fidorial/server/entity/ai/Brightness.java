package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.world.time.DayNightCycle;

/**
 * How bright the world is for mobs: the light of a block as vanilla weighs it, with the sky giving
 * less of it at night and under the rain.
 */
public final class Brightness {

    private static final int MAX_LIGHT = 15;
    private static final int NIGHT_DARKENING = 11;

    /**
     * Vanilla calls it day while the sky loses less than this much of its light.
     */
    private static final int DAY_DARKENING_LIMIT = 4;

    private Brightness() {
    }

    /**
     * {@return how much light the sky loses right now, from {@code 0} at noon under a clear sky to
     * 11 at midnight}; rain then thunder dim it further
     */
    public static int skyDarkening(final ServerWorld world) {
        final double dayFraction = world.dayNightCycle().timeOfDay() / (double) DayNightCycle.DAY_LENGTH;
        // the sun is at its highest a quarter of a day after it rises
        final double height = Math.cos((dayFraction - 0.25) * 2.0 * Math.PI);
        double brightness = 0.5 + 2.0 * Math.clamp(height, -0.25, 0.25);
        if (world.weather().isRaining()) {
            brightness *= 1.0 - 5.0 / 16.0;
        }
        if (world.weather().isThundering()) {
            brightness *= 1.0 - 5.0 / 16.0;
        }
        return (int) ((1.0 - brightness) * NIGHT_DARKENING);
    }

    /**
     * {@return {@code true} while the sun lights the world}, which is when the undead burn
     */
    public static boolean isDay(final ServerWorld world) {
        return world.dimensionType().hasSkylight() && skyDarkening(world) < DAY_DARKENING_LIMIT;
    }

    /**
     * {@return {@code true} if nothing stands between a block and the sky}; glass does not count, as
     * in Vanilla. While the light of the chunk is being worked out, nothing is known to see the sky.
     */
    public static boolean canSeeSky(final ServerWorld world, final int x, final int y, final int z) {
        return world.dimensionType().hasSkylight()
                && isLightKnown(world, x, z) && world.skyLightAt(x, y, z) >= MAX_LIGHT;
    }

    /**
     * {@return the light of a block, from 0 to 15}: the brightest of the light of the blocks around
     * and of what the sky gives right now
     */
    public static int light(final ServerWorld world, final int x, final int y, final int z) {
        return light(world, x, y, z, skyDarkening(world));
    }

    /**
     * {@return the light of a block, from 0 to 15, were the sky to lose a given share of its light}
     *
     * @param skyDarkening how much light the sky loses, from 0 to 15
     */
    public static int light(final ServerWorld world, final int x, final int y, final int z, final int skyDarkening) {
        if (!isLightKnown(world, x, z)) {
            return world.dimensionType().hasSkylight() ? MAX_LIGHT - skyDarkening : 0;
        }
        final int block = world.blockLightAt(x, y, z);
        if (block >= MAX_LIGHT || !world.dimensionType().hasSkylight()) {
            return block;
        }
        return Math.max(block, world.skyLightAt(x, y, z) - skyDarkening);
    }

    /**
     * {@return how bright a block is for a mob, from {@code 0} to {@code 1}}, as vanilla weighs the
     * {@linkplain #light light} of a block: above one half from a light of 13
     */
    public static float at(final ServerWorld world, final int x, final int y, final int z) {
        final float light = light(world, x, y, z) / (float) MAX_LIGHT;
        return light / (4.0f - 3.0f * light);
    }

    private static boolean isLightKnown(final ServerWorld world, final int x, final int z) {
        final ChunkColumn column = world.loadedColumn(x >> 4, z >> 4);
        return column != null && column.lightPopulated();
    }
}
