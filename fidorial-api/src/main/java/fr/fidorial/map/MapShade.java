package fr.fidorial.map;

/**
 * The four shades a client can draw each {@link MapColor} in.
 *
 * <p>Terrain maps use them to show relief: a pixel is drawn lighter when the ground rises towards
 * the north and darker when it falls. The constants are declared in network order, so
 * {@link #ordinal()} is the value stored in the two low bits of a map pixel, see {@link MapPalette}.</p>
 *
 * @since 0.1.0
 */
public enum MapShade {

    /**
     * Darker than normal, {@code 180/255} of the color.
     */
    SHADOW(180),

    /**
     * The shade flat ground is drawn with, {@code 220/255} of the color.
     */
    NORMAL(220),

    /**
     * The color itself, at full brightness.
     */
    LIGHT(255),

    /**
     * The darkest shade, {@code 135/255} of the color. Vanilla terrain never uses it.
     */
    DEEP_SHADOW(135);

    private final int multiplier;

    MapShade(final int multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * {@return what the client multiplies each channel of the color by, out of {@code 255}}
     *
     * @since 0.1.0
     */
    public int multiplier() {
        return multiplier;
    }
}
