package fr.euphyllia.fidorial.server.world.farming;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.block.BlockMining;
import fr.euphyllia.fidorial.server.world.loot.BlockLootTables;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lets what breaks blocks give what the blocks it breaks drop: the harvest of a plant, the loot of
 * any other block. A player breaks them with what they hold; a piston, a fluid or a block losing
 * its support breaks them as if by hand.
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
        if (tables != null) {
            dropAll(world, position, tables.drops(state, ItemStack.EMPTY, ThreadLocalRandom.current()));
        }
    }

    /**
     * Rolls what a player breaking a block gets from its loot table, without dropping it: nothing
     * with the {@code block_drops} game rule off or when the block needs a tool the player does not
     * hold ({@link BlockMining#canHarvest}); otherwise the loot rolled with the tool, so that Silk
     * Touch and Fortune count. A block taking part in farming gives nothing here:
     * {@link #harvest} works out its harvest.
     *
     * @param world the world of the block
     * @param state the state being broken
     * @param tool  what the player breaks it with, {@link ItemStack#EMPTY} for an empty hand
     * @return the stacks to drop, empty for none
     */
    public static List<ItemStack> loot(final World world, final BlockData state, final ItemStack tool) {
        if (!FidorialServer.isInitialized() || state.isAir() || !world.gameRules().getBoolean(GameRuleKeys.BLOCK_DROPS)) {
            return List.of();
        }
        final FidorialServer server = FidorialServer.getInstance();
        if (server.registries().farming().behaviourOrNull(state.key()) != null || !canHarvest(server, state, tool)) {
            return List.of();
        }
        final BlockLootTables tables = BlockLootTables.instance();
        return tables == null ? List.of() : tables.drops(state, tool, ThreadLocalRandom.current());
    }

    /**
     * Lets the farming world give the harvest of a block a player breaks, while it still stands,
     * through {@link FarmingWorld#harvest}, which fires a
     * {@link fr.fidorial.event.farming.HarvestEvent} for a block taking part in farming and gives
     * nothing for any other. Nothing happens when the block needs a tool the player does not hold.
     *
     * @param world    the world of the block
     * @param position where the block is, owned by the calling thread
     * @param state    the state being broken
     * @param player   the player breaking the block
     * @param tool     what the player breaks it with, {@link ItemStack#EMPTY} for an empty hand
     */
    public static void harvest(final World world, final BlockPosition position, final BlockData state,
                               final Player player, final ItemStack tool) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final FidorialServer server = FidorialServer.getInstance();
        if (canHarvest(server, state, tool)) {
            server.worlds().farming().world(world).harvest(position, player, tool);
        }
    }

    /**
     * Drops stacks from the middle of a block.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param stacks   the stacks, the empty ones are left out
     */
    public static void dropAll(final World world, final BlockPosition position, final List<ItemStack> stacks) {
        final Location center = Location.of(world, position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5);
        for (final ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                world.dropItem(center, stack);
            }
        }
    }

    private static boolean canHarvest(final FidorialServer server, final BlockData state, final ItemStack tool) {
        return BlockMining.canHarvest(state.key(), tool, server.registries().registry(RegistryKey.BLOCK)::isTagged);
    }
}
