package fr.euphyllia.fidorial.server.world.block.chest;

/**
 * The chests that join one another into a large chest: a chest only joins a chest of its own
 * family.
 */
public enum ChestFamily {

    /**
     * The wooden chest.
     */
    WOODEN,

    /**
     * The trapped chest, which never joins a plain chest.
     */
    TRAPPED,

    /**
     * The eight copper chests, which join each other whatever their oxidation and wax.
     */
    COPPER
}
