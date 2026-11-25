package fr.euphyllia.fidorial.server.world.generation.density;

/**
 * A function of the block position, compiled from a density function of a data pack. Terrain is solid where the
 * final density is positive; the climate, the caves, the ore veins and the aquifers read other functions.
 *
 * <p>Every function knows the range of its values and the axes it depends on. The range lets a minimum or a
 * maximum skip its second side when the first one already decides; the axes let the compiler cache what does not
 * change along a column.</p>
 *
 * <p>Functions are immutable and evaluated with a {@link DensityContext}, which holds the caches of one thread.</p>
 */
public abstract class Density {

    /**
     * The function depends on x.
     */
    public static final int X = 1;
    /**
     * The function depends on y.
     */
    public static final int Y = 2;
    /**
     * The function depends on z.
     */
    public static final int Z = 4;
    /**
     * The function depends on the three axes.
     */
    public static final int XYZ = X | Y | Z;

    private final float min;
    private final float max;

    /**
     * @param min the lowest value the function can take
     * @param max the highest value the function can take
     */
    protected Density(final float min, final float max) {
        this.min = min;
        this.max = max;
    }

    /**
     * {@return the value at a block}
     */
    public abstract float compute(DensityContext context, int x, int y, int z);

    /**
     * {@return the axes the value depends on, a combination of {@link #X}, {@link #Y} and {@link #Z}}
     */
    public int axes() {
        return XYZ;
    }

    public final float min() {
        return min;
    }

    public final float max() {
        return max;
    }

    /**
     * {@return {@code true} if the function is the same everywhere}
     */
    public boolean isConstant() {
        return false;
    }
}
