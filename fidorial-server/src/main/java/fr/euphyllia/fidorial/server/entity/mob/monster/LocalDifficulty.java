package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;

/**
 * How hard the game is at a place, as vanilla's regional difficulty: the difficulty of the world,
 * raised by how long the world has lasted, how long players have stayed in the chunk and how full
 * the moon is.
 *
 * @param base      the difficulty of the world, from {@code 0} for peaceful to {@code 3} for hard
 * @param effective the regional difficulty: {@code 0} in peaceful, up to {@code 1.5} on easy,
 *                  {@code 4} on normal and {@code 6.75} on hard
 */
public record LocalDifficulty(int base, float effective) {

    public static final int PEACEFUL = 0;
    public static final int EASY = 1;
    public static final int NORMAL = 2;
    public static final int HARD = 3;

    private static final float GLOBAL_TIME_OFFSET = -72_000.0f;
    private static final float MAX_GLOBAL_TIME = 1_440_000.0f;
    private static final float MAX_LOCAL_TIME = 3_600_000.0f;
    private static final float[] MOON_BRIGHTNESS = {1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};

    /**
     * {@return the difficulty at a block of a world}
     */
    public static LocalDifficulty at(final ServerWorld world, final int x, final int z) {
        final int base = FidorialServer.getInstance().worlds().manager().levelData().difficulty;
        final ChunkColumn column = world.loadedColumn(x >> 4, z >> 4);
        final long localTime = column == null ? 0L : column.inhabitedTime();
        final float moon = column == null ? 0.0f : MOON_BRIGHTNESS[world.dayNightCycle().moonPhase()];
        return new LocalDifficulty(base, effective(base, world.dayNightCycle().time(), localTime, moon));
    }

    private static float effective(final int base, final long worldTime, final long localTime, final float moon) {
        if (base <= PEACEFUL) {
            return 0.0f;
        }
        final float global = Math.clamp((worldTime + GLOBAL_TIME_OFFSET) / MAX_GLOBAL_TIME, 0.0f, 1.0f) * 0.25f;
        float local = Math.clamp(localTime / MAX_LOCAL_TIME, 0.0f, 1.0f) * (base == HARD ? 1.0f : 0.75f);
        local += Math.clamp(moon * 0.25f, 0.0f, global);
        if (base == EASY) {
            local *= 0.5f;
        }
        return base * (0.75f + global + local);
    }

    /**
     * {@return the share of the special traits monsters get at this difficulty, between {@code 0}
     * and {@code 1}}: none below a regional difficulty of 2, all of them above 4
     */
    public float specialMultiplier() {
        if (effective < 2.0f) {
            return 0.0f;
        }
        return effective > 4.0f ? 1.0f : (effective - 2.0f) / 2.0f;
    }
}
