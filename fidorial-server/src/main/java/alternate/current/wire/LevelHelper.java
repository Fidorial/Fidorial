/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import fr.euphyllia.fidorial.server.world.redstone.ServerRedstoneWorld;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;

/**
 * Alternate Current's {@code LevelHelper}.
 *
 * @author Space Walker
 */
final class LevelHelper {

    private LevelHelper() {
        throw new UnsupportedOperationException("LevelHelper cannot be instantiated.");
    }

    /**
     * An optimized version of setting a block, only used to update the power of redstone wire: light,
     * fluids and neighbor updates are left out, players still see the change.
     *
     * @param world                the redstone of the world
     * @param pos                  where the wire is
     * @param state                the wire with its new power
     * @param updateNeighborShapes {@code true} for a wire just placed, whose neighbors follow its shape
     * @return {@code false} if nothing changed
     * @see ServerRedstoneWorld#setWireState(BlockPosition, BlockData, boolean)
     */
    static boolean setWireState(final ServerRedstoneWorld world, final BlockPosition pos, final BlockData state,
                                final boolean updateNeighborShapes) {
        return world.setWireState(pos, state, updateNeighborShapes);
    }
}
