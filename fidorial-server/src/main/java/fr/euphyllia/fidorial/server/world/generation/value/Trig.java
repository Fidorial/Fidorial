package fr.euphyllia.fidorial.server.world.generation.value;

/**
 * Sine and cosine read from a table of 65536 steps per turn, rounded to the nearest step: fast, and the same on
 * every machine, which the shapes built by the generation rely on.
 */
public final class Trig {

    private static final float[] SINE = new float[65536];
    private static final double STEPS_PER_RADIAN = 10430.378350470453;
    private static final double QUARTER_TURN = 16384.0;

    static {
        for (int i = 0; i < SINE.length; i++) {
            SINE[i] = (float) Math.sin(i / STEPS_PER_RADIAN);
        }
    }

    private Trig() {
    }

    public static float sin(final double angle) {
        return angle >= 0.0
                ? SINE[(int) ((long) (angle * STEPS_PER_RADIAN + 0.5) & 65535L)]
                : -SINE[(int) ((long) (-angle * STEPS_PER_RADIAN + 0.5) & 65535L)];
    }

    public static float cos(final double angle) {
        return angle >= 0.0
                ? SINE[(int) ((long) (angle * STEPS_PER_RADIAN + QUARTER_TURN + 0.5) & 65535L)]
                : SINE[(int) ((long) (-angle * STEPS_PER_RADIAN + QUARTER_TURN + 0.5) & 65535L)];
    }
}
