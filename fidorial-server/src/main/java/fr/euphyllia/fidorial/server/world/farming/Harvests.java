package fr.euphyllia.fidorial.server.world.farming;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.loot.BlockLootTables;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Lets what breaks blocks without a player, a piston, a fluid or a block losing its support, give
 * what the blocks it breaks drop: the harvest of a plant, the loot of any other block, as if broken
 * by hand.
 */
public final class Harvests {

    private Harvests() {
        throw new UnsupportedOperationException("Harvests cannot be instantiated.");
    }

    /**
     * Drops what the block at a position gives, while it still stands, unless the
     * {@code block_drops} game rule forbids it.
     *
     * @param world    the world of the block
     * @param position where the block is, owned by the calling thread
     */
    public static void drop(final World world, final BlockPosition position) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final FidorialServer server = FidorialServer.getInstance();
        final FarmingWorld farming = server.worlds().farming().world(world);
        final BlockData state = farming.blockAt(position);
        if (state == null || state.isAir() || !world.gameRules().getBoolean(GameRuleKeys.BLOCK_DROPS)) {
            return;
        }
        if (server.registries().farming().behaviourOrNull(state.key()) != null) {
            farming.harvest(position, null, ItemStack.EMPTY);
            return;
        }
        final BlockLootTables tables = BlockLootTables.instance();
        if (tables == null) {
            return;
        }
        final Location center = Location.of(world, position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5);
        for (final ItemStack stack : tables.drops(state, ItemStack.EMPTY, ThreadLocalRandom.current())) {
            if (!stack.isEmpty()) {
                world.dropItem(center, stack);
            }
        }
    }
}
