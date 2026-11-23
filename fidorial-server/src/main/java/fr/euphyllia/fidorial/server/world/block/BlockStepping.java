package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;

/**
 * What blocks do to the living entities walking on them: brittle ice cracks under them.
 *
 * <p>The block an entity walks on is found under its feet: in the column of the block it rests on,
 * the nearest to its center when it stands across several, the block 0.2 below its feet. A fence, a
 * wall or a fence gate it stands on is that block itself, however high it stands on it.</p>
 */
public final class BlockStepping {

    /**
     * How far below the feet of an entity the block it walks on is looked for.
     */
    private static final double FEET_DEPTH = 0.2;

    private BlockStepping() {
        throw new UnsupportedOperationException("BlockStepping cannot be instantiated.");
    }

    /**
     * Lets the block a living entity on the ground walks on react to it.
     *
     * @param world     the world of the entity
     * @param entity    the entity, on the ground
     * @param shapes    the shapes of the blocks the entity bumps into
     * @param x         where the entity stands along the x axis
     * @param y         the height of the feet of the entity
     * @param z         where the entity stands along the z axis
     * @param halfWidth half the width of the entity
     * @param height    the height of the entity
     */
    public static void walkedOn(final ServerWorld world, final AbstractLivingEntity entity, final BlockCollider.Shapes shapes,
                                final double x, final double y, final double z, final double halfWidth, final double height) {
        final BlockPosition position = blockWalkedOn(world, shapes, x, y, z, halfWidth, height);
        if (!world.scheduler().isOwnedByCurrentThread(world.key(), position.chunk())) {
            return;
        }
        final BlockState state = BlockView.blockAt(world, position.blockX(), position.blockY(), position.blockZ());
        if (state != null && state.name().equals(BlockTypeKeys.BRITTLE_ICE)) {
            BrittleIceBlock.walkedOn(world, position, entity);
        }
    }

    /**
     * {@return the block an entity standing at a point walks on}
     */
    static BlockPosition blockWalkedOn(final ServerWorld world, final BlockCollider.Shapes shapes, final double x,
                                       final double y, final double z, final double halfWidth, final double height) {
        final int feet = (int) Math.floor(y - FEET_DEPTH);
        final BlockPosition supporting = BlockCollider.supportingBlock(world, shapes,
                BlockCollider.Box.at(x, y, z, halfWidth, height), x, y, z);
        if (supporting == null) {
            return Position.block((int) Math.floor(x), feet, (int) Math.floor(z));
        }
        final BlockState state = BlockView.blockAt(world, supporting.blockX(), supporting.blockY(), supporting.blockZ());
        if (state != null && (state.isTagged(BlockTypeTagKeys.FENCES) || state.isTagged(BlockTypeTagKeys.WALLS)
                || state.isTagged(BlockTypeTagKeys.FENCE_GATES))) {
            return supporting;
        }
        return Position.block(supporting.blockX(), feet, supporting.blockZ());
    }
}
