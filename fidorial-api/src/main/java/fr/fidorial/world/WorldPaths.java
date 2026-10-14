package fr.fidorial.world;

import java.nio.file.Path;

/**
 * Contains paths related to worlds.
 *
 * <p>Paths are those the server writes to; they may not exist yet if nothing has been saved.</p>
 *
 * @see World#paths()
 * @since 0.1.0
 */
public interface WorldPaths {

    /**
     * Returns the directory holding the world, e.g. {@code world/dimensions/minecraft/overworld}.
     *
     * @return the directory holding the world
     * @since 0.1.0
     */
    Path directory();

    /**
     * Returns the directory holding data specific to the world, e.g. {@code world/dimensions/minecraft/overworld/data}.
     *
     * @return the directory holding the data
     * @since 0.1.0
     */
    Path dataDirectory();

    /**
     * {@return the holder for root paths}
     *
     * @since 0.1.0
     */
    RootWorldPaths rootPaths();

    /**
     * Contains paths shared by all worlds.
     *
     * @since 0.1.0
     */
    public interface RootWorldPaths {

        /**
         * Returns the directory holding the world, e.g. {@code world}.
         *
         * @return the directory holding the world
         * @since 0.1.0
         */
        Path directory();

        /**
         * Returns the directory holding global world data, e.g. {@code world/data}.
         *
         * @return the directory holding the data
         * @since 0.1.0
         */
        Path dataDirectory();
    }
}
