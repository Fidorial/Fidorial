package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.SignalGetter;

public final class RedstoneBlock implements RedstoneBehaviour {

    public static final RedstoneBlock INSTANCE = new RedstoneBlock();

    private RedstoneBlock() {

    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return true;
    }

    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return 15;
    }
}
