package fr.euphyllia.fidorial.server.world.generation.feature;

/**
 * A question about the world around a block, such as "is it air" or "can a sapling grow here".
 */
@FunctionalInterface
public interface BlockTest {

    BlockTest ALWAYS = (region, x, y, z) -> true;

    boolean test(DecorationRegion region, int x, int y, int z);

    default BlockTest negate() {
        return (region, x, y, z) -> !test(region, x, y, z);
    }
}
