package fr.fidorial.map;

/**
 * How many blocks one pixel of a terrain map covers.
 *
 * <p>The constants are declared in network order: {@link #ordinal()} is the zoom level the client
 * shows in the tooltip of a map.</p>
 *
 * @since 0.1.0
 */
public enum MapScale {

    /**
     * One pixel per block: the map covers 128×128 blocks. What an empty map makes.
     */
    ONE_TO_ONE(0),

    /**
     * One pixel per 2×2 blocks: the map covers 256×256 blocks.
     */
    ONE_TO_TWO(1),

    /**
     * One pixel per 4×4 blocks: the map covers 512×512 blocks.
     */
    ONE_TO_FOUR(2),

    /**
     * One pixel per 8×8 blocks: the map covers 1024×1024 blocks.
     */
    ONE_TO_EIGHT(3),

    /**
     * One pixel per 16×16 blocks: the map covers 2048×2048 blocks.
     */
    ONE_TO_SIXTEEN(4);

    private final int level;
    private final int blocksPerPixel;
    private final int blocks;

    MapScale(final int level) {
        this.level = level;
        this.blocksPerPixel = 1 << level;
        this.blocks = MapCanvas.SIZE << level;
    }

    /**
     * {@return the zoom level, {@code 0}-{@code 4}}
     *
     * @since 0.1.0
     */
    public int level() {
        return level;
    }

    /**
     * {@return how many blocks one side of a pixel covers}
     *
     * @since 0.1.0
     */
    public int blocksPerPixel() {
        return blocksPerPixel;
    }

    /**
     * {@return how many blocks one side of the map covers}
     *
     * @since 0.1.0
     */
    public int blocks() {
        return blocks;
    }
}
