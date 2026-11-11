package fr.euphyllia.fidorial.server.world.farming;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;

/**
 * Lets what breaks blocks outside of farming, a piston or a block losing its support, give the
 * harvest of the plants it breaks.
 */
public final class Harvests {

    private Harvests() {
        throw new UnsupportedOperationException("Harvests cannot be instantiated.");
    }

    /**
     * Drops what the block at a position gives, while it still stands: nothing for a block taking
     * no part in farming.
     *
     * @param world    the world of the block
     * @param position where the block is, owned by the calling thread
     */
    public static void drop(final World world, final BlockPosition position) {
        if (FidorialServer.isInitialized()) {
            FidorialServer.getInstance().worlds().farming().world(world).harvest(position, null, ItemStack.EMPTY);
        }
    }
}
