package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.euphyllia.fidorial.server.world.block.BlockItems;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockGetter;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public final class BlockStateRegistry {

    private final BlockRegistry registry;
    private final @Nullable BlockState[] stateByNetworkId;

    public BlockStateRegistry(final BlockRegistry registry) {
        this.registry = registry;
        this.stateByNetworkId = new BlockState[BlockStateIds.STATE_COUNT];
        indexGeneratedStates();
    }

    public BlockRegistry registry() {
        return registry;
    }

    private void indexGeneratedStates() {
        for (final BlockType type : registry.types()) {
            for (int ordinal = 0; ordinal < type.stateCount(); ordinal++) {
                final BlockData data = type.stateAt(ordinal);
                if (data.networkId() < stateByNetworkId.length) {
                    stateByNetworkId[data.networkId()] = BlockState.of(data);
                }
            }
        }
    }

    /// Returns the amount of block states with a unique network id.
    public int size() {
        return stateByNetworkId.length;
    }

    public int networkId(final BlockState state) {
        return state.data().networkId();
    }

    public BlockState byId(final int networkId) {
        if (networkId >= 0 && networkId < stateByNetworkId.length) {
            final BlockState cached = stateByNetworkId[networkId];
            if (cached != null) {
                return cached;
            }
        }
        final BlockData data = registry.fromNetworkId(networkId);
        return data == null ? BlockState.defaultOf(BlockTypeKeys.AIR) : BlockState.of(data);
    }

    public BlockState toBlockState(final BlockData data) {
        return BlockState.of(data);
    }

    public @Nullable BlockState placementState(final BlockState state, final BlockPlaceContext context) {
        final BlockBehaviour behaviour = registry.behaviour(state.name()).orElse(null);
        if (behaviour == null) {
            return state;
        }
        final BlockData placed = behaviour.placementState(context);
        return placed == null ? null : toBlockState(placed);
    }

    public BlockGetter view(final ServerWorld world) {
        return pos -> {
            try {
                return world.getBlock(pos.blockX(), pos.blockY(), pos.blockZ()).data();
            } catch (final IOException exception) {
                return null;
            }
        };
    }

    /**
     * {@return the block an item places, in its default state, {@code null} for an item placing
     * none}: a block item places its block, a water or lava bucket its fluid
     *
     * @param itemId the item, {@code null} for none
     */
    public @Nullable BlockState blockForItem(final @Nullable TypedKey<Item> itemId) {
        if (itemId == null) {
            return null;
        }

        if (itemId.equals(ItemKeys.WATER_BUCKET)) {
            return BlockState.defaultOf(BlockTypeKeys.WATER);
        }
        if (itemId.equals(ItemKeys.LAVA_BUCKET)) {
            return BlockState.defaultOf(BlockTypeKeys.LAVA);
        }

        return BlockItems.placesBlock(itemId) ? BlockStates.defaultStateOf(BlockItems.block(itemId)) : null;
    }
}
