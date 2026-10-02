package fr.fidorial.world.dimension;

/**
 * Which skybox the client renders for a dimension.
 *
 * @since 0.1.0
 */
public enum Skybox {

    /**
     * No sky, like the nether.
     */
    NONE("none"),
    /**
     * The overworld sky, with sun, moon and stars.
     */
    OVERWORLD("overworld"),
    /**
     * The end sky.
     */
    END("end");

    private final String id;

    Skybox(final String id) {
        this.id = id;
    }

    /**
     * {@return the identifier expected on the wire}
     */
    public String id() {
        return id;
    }
}
