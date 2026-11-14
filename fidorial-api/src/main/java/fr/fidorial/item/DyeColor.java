package fr.fidorial.item;

import java.util.Locale;

/**
 * The sixteen colours of dyes, wool and everything dyed, in the order of vanilla's ids.
 *
 * @since 0.1.0
 */
public enum DyeColor {
    /**
     * White, id {@code 0}.
     *
     * @since 0.1.0
     */
    WHITE,
    /**
     * Orange, id {@code 1}.
     *
     * @since 0.1.0
     */
    ORANGE,
    /**
     * Magenta, id {@code 2}.
     *
     * @since 0.1.0
     */
    MAGENTA,
    /**
     * Light blue, id {@code 3}.
     *
     * @since 0.1.0
     */
    LIGHT_BLUE,
    /**
     * Yellow, id {@code 4}.
     *
     * @since 0.1.0
     */
    YELLOW,
    /**
     * Lime, id {@code 5}.
     *
     * @since 0.1.0
     */
    LIME,
    /**
     * Pink, id {@code 6}.
     *
     * @since 0.1.0
     */
    PINK,
    /**
     * Gray, id {@code 7}.
     *
     * @since 0.1.0
     */
    GRAY,
    /**
     * Light gray, id {@code 8}.
     *
     * @since 0.1.0
     */
    LIGHT_GRAY,
    /**
     * Cyan, id {@code 9}.
     *
     * @since 0.1.0
     */
    CYAN,
    /**
     * Purple, id {@code 10}.
     *
     * @since 0.1.0
     */
    PURPLE,
    /**
     * Blue, id {@code 11}.
     *
     * @since 0.1.0
     */
    BLUE,
    /**
     * Brown, id {@code 12}.
     *
     * @since 0.1.0
     */
    BROWN,
    /**
     * Green, id {@code 13}.
     *
     * @since 0.1.0
     */
    GREEN,
    /**
     * Red, id {@code 14}.
     *
     * @since 0.1.0
     */
    RED,
    /**
     * Black, id {@code 15}.
     *
     * @since 0.1.0
     */
    BLACK;

    private static final DyeColor[] BY_ID = values();

    /**
     * {@return the id vanilla gives this colour, from {@code 0} for white to {@code 15} for black}
     *
     * @since 0.1.0
     */
    public int id() {
        return ordinal();
    }

    /**
     * {@return the name vanilla gives this colour, as in {@code light_blue_wool}}
     *
     * @since 0.1.0
     */
    public String colorName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * {@return the colour of an id, white if the id is out of range}
     *
     * @param id the id vanilla gives the colour
     * @since 0.1.0
     */
    public static DyeColor byId(final int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : WHITE;
    }
}
