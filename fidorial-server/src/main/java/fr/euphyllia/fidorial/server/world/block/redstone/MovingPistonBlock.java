package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.redstone.MovingPistons;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;

/**
 * The block standing in for whatever a piston is moving, vanilla's {@code minecraft:moving_piston}:
 * it is there for the two game ticks the move takes, then turns into what was moved.
 *
 * <p>What it stands for is kept by
 * {@link MovingPistons}; the block itself only has it
 * forgotten when something else replaces it.</p>
 */
public final class MovingPistonBlock implements RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.MOVING_PISTON;
    public static final MovingPistonBlock INSTANCE = new MovingPistonBlock();

    private MovingPistonBlock() {
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (FidorialServer.isInitialized()) {
            Redstone.movingPistons().forget(world.world(), position);
        }
    }
}
