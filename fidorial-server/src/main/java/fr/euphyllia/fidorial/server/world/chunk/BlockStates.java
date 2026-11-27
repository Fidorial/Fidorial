package fr.euphyllia.fidorial.server.world.chunk;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.codecs.BlockStateCodecs;
import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockStates {

    private static final Map<TypedKey<BlockType>, BlockState[]> BY_KEY = new ConcurrentHashMap<>(BlockStateIds.BLOCK_COUNT);

    private BlockStates() {
        throw new UnsupportedOperationException("BlockStates cannot be instantiated.");
    }

    /**
     * Creates the state of every registered block.
     *
     * @param registry the block registry, already populated by {@code BlockStateIds.registerAll}
     */
    public static void bootstrap(final BlockRegistry registry) {
        for (final BlockType type : registry.types()) {
            statesOf(type);
        }
    }

    private static BlockState[] statesOf(final BlockType type) {
        return BY_KEY.computeIfAbsent(type.key(), _ -> {
            final BlockState[] states = new BlockState[type.stateCount()];
            for (int ordinal = 0; ordinal < states.length; ordinal++) {
                states[ordinal] = new BlockState(type.stateAt(ordinal));
            }
            return states;
        });
    }

    static BlockState of(final BlockData data) {
        return statesOf(data.type())[data.ordinal()];
    }

    static BlockState defaultOf(final TypedKey<BlockType> key) {
        final BlockType type = Blocks.type(key);
        Preconditions.checkArgument(type != null, "%s is not a registered block type", key.key().asString());
        return of(type.defaultData());
    }

    static @Nullable BlockState parseLeniently(final TypedKey<BlockType> key, final Map<String, String> properties) {
        final BlockType type = Blocks.type(key);
        if (type == null) {
            return null;
        }
        return of(BlockStateCodecs.withNames(type.defaultData(), properties));
    }

    public static BlockState stateAt(final TypedKey<BlockType> key, final int ordinal) {
        final BlockType type = Blocks.type(key);
        Preconditions.checkArgument(type != null, "%s is not a registered block type", key.key().asString());
        return statesOf(type)[ordinal];
    }

    public static @Nullable BlockState defaultStateOf(final TypedKey<BlockType> key) {
        final BlockType type = Blocks.type(key);
        return type == null ? null : of(type.defaultData());
    }
}
