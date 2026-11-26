package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Chooses the state to set at a block: always the same one, a weighted pick, one that follows a noise, or one
 * that depends on the world around.
 *
 * <p>States are given by their number in the palette of the generator.</p>
 */
@FunctionalInterface
public interface StateSource {

    /**
     * The answer of a source that has no state for a block.
     */
    char NONE = Character.MAX_VALUE;

    /**
     * {@return the state for a block, or {@link #NONE}}
     */
    char pick(DecorationRegion region, BitRandom random, int x, int y, int z);

    /**
     * {@return the state for a block, the one already there when the source has none}
     */
    default char pickOrKeep(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
        final char id = pick(region, random, x, y, z);
        return id == NONE ? region.id(x, y, z) : id;
    }

    static StateSource of(final char id) {
        return (region, random, x, y, z) -> id;
    }
}
