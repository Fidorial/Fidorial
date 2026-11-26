package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.world.block.BlockType;

import java.util.function.Predicate;

/**
 * Which blocks are solid, a rough measure of how much room a block fills that two fluid sources
 * need below the block between them to turn it into a third one.
 *
 * <p>A block is solid when its collision box, the box around all its collision shapes, is on
 * average at least 0.73 of a block across its three sides, or a whole block high. The blocks of the
 * {@link #ALWAYS_SOLID} tag are solid however thin, such as signs, walls or pressure plates; those
 * of the {@link #NEVER_SOLID} tag are not however large, such as ladders, layers of snow or
 * scaffolding, whose shape depends on who stands on it. Data packs can change both tags.</p>
 */
public final class BlockSolidity {

    /**
     * The blocks solid whatever their shape.
     */
    public static final TagKey<BlockType> ALWAYS_SOLID = TagKey.withRegistry(RegistryKey.BLOCK, "fidorial:always_solid");

    /**
     * The blocks never solid, whatever their shape.
     */
    public static final TagKey<BlockType> NEVER_SOLID = TagKey.withRegistry(RegistryKey.BLOCK, "fidorial:never_solid");

    /**
     * How large on average across its three sides the collision box of a solid block is, at least.
     */
    private static final double MIN_AVERAGE_SIZE = 0.7291666666666666;

    private BlockSolidity() {
        throw new UnsupportedOperationException("BlockSolidity cannot be instantiated.");
    }

    /**
     * {@return {@code true} if a block is solid}
     *
     * @param state the block
     */
    public static boolean isSolid(final BlockState state) {
        return isSolid(state, state::isTagged);
    }

    /**
     * {@return {@code true} if a block is solid}, its tags read by the caller, such as world generation which
     * reads them through its own lookup
     *
     * @param state  the block
     * @param tagged whether the block is in a tag
     */
    public static boolean isSolid(final BlockState state, final Predicate<TagKey<BlockType>> tagged) {
        if (tagged.test(ALWAYS_SOLID)) {
            return true;
        }
        if (tagged.test(NEVER_SOLID)) {
            return false;
        }
        final CollisionShape shape = BlockCollision.shape(state);
        if (shape.isEmpty()) {
            return false;
        }
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int box = 0; box < shape.boxCount(); box++) {
            minX = Math.min(minX, shape.minX(box));
            minY = Math.min(minY, shape.minY(box));
            minZ = Math.min(minZ, shape.minZ(box));
            maxX = Math.max(maxX, shape.maxX(box));
            maxY = Math.max(maxY, shape.maxY(box));
            maxZ = Math.max(maxZ, shape.maxZ(box));
        }
        final double averageSize = ((maxX - minX) + (maxY - minY) + (maxZ - minZ)) / 3.0;
        return averageSize >= MIN_AVERAGE_SIZE || maxY - minY >= 1.0;
    }
}
