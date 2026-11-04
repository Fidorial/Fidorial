package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.fidorial.registry.RegistryKey;
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
import java.util.Map;

public final class BlockStateRegistry {
    private static final int AIR_NETWORK_ID = 0;

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
            final TypedKey<BlockType> key = type.key();
            final BlockState[] chunkStates = BlockStates.statesOf(key);
            if (chunkStates == null) {
                continue;
            }
            final int count = Math.min(chunkStates.length, type.stateCount());
            for (int ordinal = 0; ordinal < count; ordinal++) {
                final BlockData data = type.stateAt(ordinal);
                final BlockState state = chunkStates[ordinal];
                state.setData(data);
                if (data.networkId() < stateByNetworkId.length) {
                    stateByNetworkId[data.networkId()] = state;
                }
            }
        }
    }

    public int networkId(final BlockState state) {
        final BlockData data = resolve(state);
        return data == null ? AIR_NETWORK_ID : data.networkId();
    }

    public BlockState byId(final int networkId) {
        if (networkId >= 0 && networkId < stateByNetworkId.length) {
            final BlockState cached = stateByNetworkId[networkId];
            if (cached != null) {
                return cached;
            }
        }
        final BlockData data = registry.fromNetworkId(networkId);
        if (data == null) {
            return BlockState.withoutProperties(BlockTypeKeys.AIR);
        }
        return BlockState.withProperties(data.key(), data.propertyMap());
    }

    public boolean contains(final BlockState state) {
        return resolve(state) != null;
    }

    public BlockState toBlockState(final BlockData data) {
        return BlockState.withProperties(data.key(), data.propertyMap());
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
                return resolve(world.getBlock(pos.blockX(), pos.blockY(), pos.blockZ()));
            } catch (final IOException exception) {
                return null;
            }
        };
    }

    public @Nullable BlockData resolve(final BlockState state) {
        final BlockData cached = state.data();
        return cached != null ? cached : resolveDynamic(state);
    }

    private @Nullable BlockData resolveDynamic(final BlockState state) {
        final BlockType type = registry.type(state.name()).orElse(null);
        if (type == null) {
            return null;
        }
        return type.dataOrNull(state.properties());
    }

    public @Nullable BlockState blockForItem(final @Nullable TypedKey<Item> itemId) {
        // Todo : Match the item to the block it is actually supposed to relate to. This method is just for testing purposes and will need to be removed!
        if (itemId == null) {
            return null;
        }

        if (itemId.equals(ItemKeys.WATER_BUCKET)) {
            return BlockState.withProperties(BlockTypeKeys.WATER, Map.of("level", "0"));
        }
        if (itemId.equals(ItemKeys.LAVA_BUCKET)) {
            return BlockState.withProperties(BlockTypeKeys.LAVA, Map.of("level", "0"));
        }

        final BlockState defaultState = BlockStates.defaultStateOf(TypedKey.withRegistry(RegistryKey.BLOCK, itemId.key()));
        return defaultState != null ? defaultState : BlockStates.defaultStateOf(BlockTypeKeys.COBBLESTONE);
    }
}
