package fr.fidorial.world.biome;

/**
 * Post-processing applied by the client on top of the computed grass color.
 *
 * @since 0.1.0
 */
public enum GrassColorModifier {

    /**
     * No post-processing.
     */
    NONE("none"),

    /**
     * Dark forest tinting.
     */
    DARK_FOREST("dark_forest"),

    /**
     * Swamp tinting, which alternates between two hardcoded greens using a noise field.
     */
    SWAMP("swamp");

    private final String id;

    GrassColorModifier(final String id) {
        this.id = id;
    }

    /**
     * {@return the identifier used in the biome definition}
     *
     * @since 0.1.0
     */
    public String id() {
        return id;
    }
}
