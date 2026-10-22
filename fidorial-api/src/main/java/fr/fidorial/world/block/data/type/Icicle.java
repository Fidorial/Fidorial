package fr.fidorial.world.block.data.type;

/**
 * The block state of a piece of icicle, the speleothem of the ice caves.
 *
 * @since 0.1.0
 */
public interface Icicle extends Speleothem {

    /**
     * {@return {@code true} when this piece is attached to a surface rather than to another piece of icicle}
     *
     * @since 0.1.0
     */
    default boolean isAttached() {
        return Boolean.parseBoolean(get("attached"));
    }

    /**
     * {@return the same piece attached to a surface, or not}
     *
     * @param attached {@code true} for a piece held by a surface
     * @since 0.1.0
     */
    default Icicle withAttached(final boolean attached) {
        return (Icicle) with("attached", String.valueOf(attached));
    }
}
