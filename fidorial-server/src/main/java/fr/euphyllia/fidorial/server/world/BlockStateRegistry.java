package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockGetter;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public final class BlockStateRegistry {
    private static final int AIR_NETWORK_ID = 0;

    private static final Key WATER_BUCKET = Key.key("water_bucket");
    private static final Key LAVA_BUCKET = Key.key("lava_bucket");

    private final BlockRegistry registry;
    private final Object2ObjectOpenHashMap<BlockState, BlockData> dataByState;
    private final Int2ObjectOpenHashMap<BlockState> stateByNetworkId;

    public BlockStateRegistry(final BlockRegistry registry) {
        this.registry = registry;
        this.dataByState = new Object2ObjectOpenHashMap<>();
        this.stateByNetworkId = new Int2ObjectOpenHashMap<>();
        indexGeneratedStates();
    }

    public BlockRegistry registry() {
        return registry;
    }

    private void indexGeneratedStates() {
        for (final BlockType type : registry.types()) {
            final Key key = type.key();
            final BlockState[] chunkStates = BlockStates.statesOf(key);
            if (chunkStates == null) {
                continue;
            }
            final int count = Math.min(chunkStates.length, type.stateCount());
            for (int ordinal = 0; ordinal < count; ordinal++) {
                final BlockData data = type.stateAt(ordinal);
                final BlockState state = chunkStates[ordinal];
                dataByState.put(state, data);
                stateByNetworkId.put(data.networkId(), state);
            }
        }
    }

    public int networkId(final BlockState state) {
        final BlockData cached = dataByState.get(state);
        if (cached != null) {
            return cached.networkId();
        }
        final BlockData resolved = resolveDynamic(state);
        return resolved == null ? AIR_NETWORK_ID : resolved.networkId();
    }

    public BlockState byId(final int networkId) {
        final BlockState cached = stateByNetworkId.get(networkId);
        if (cached != null) {
            return cached;
        }
        final BlockData data = registry.fromNetworkId(networkId);
        if (data == null) {
            return BlockState.of(BlockTypeKeys.AIR.key());
        }
        return BlockState.of(data.key(), data.propertyMap());
    }

    public boolean contains(final BlockState state) {
        return dataByState.containsKey(state) || resolveDynamic(state) != null;
    }

    public BlockState toBlockState(final BlockData data) {
        return BlockState.of(data.key(), data.propertyMap());
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
                return resolve(world.getBlock(pos.x(), pos.y(), pos.z()));
            } catch (final IOException exception) {
                return null;
            }
        };
    }

    public @Nullable BlockData resolve(final BlockState state) {
        final BlockData cached = dataByState.get(state);
        return cached != null ? cached : resolveDynamic(state);
    }

    private @Nullable BlockData resolveDynamic(final BlockState state) {
        final BlockType type = registry.type(state.name()).orElse(null);
        if (type == null) {
            return null;
        }
        return type.dataOrNull(state.properties());
    }

    public @Nullable BlockState blockForItem(final @Nullable Key itemId) {
        // Todo : Match the item to the block it is actually supposed to relate to. This method is just for testing purposes and will need to be removed!
        if (itemId == null) {
            return null;
        }

        if (itemId.equals(WATER_BUCKET)) {
            return BlockState.of(BlockTypeKeys.WATER.key(), Map.of("level", "0"));
        }
        if (itemId.equals(LAVA_BUCKET)) {
            return BlockState.of(BlockTypeKeys.LAVA.key(), Map.of("level", "0"));
        }

        final BlockState defaultState = BlockStates.defaultStateOf(itemId);
        return defaultState != null ? defaultState : BlockStates.defaultStateOf(BlockTypeKeys.COBBLESTONE.key());
    }
}
