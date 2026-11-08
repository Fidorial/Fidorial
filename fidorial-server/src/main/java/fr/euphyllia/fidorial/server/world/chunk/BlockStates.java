package fr.euphyllia.fidorial.server.world.chunk;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

public final class BlockStates {

    private static final Object2ObjectOpenHashMap<TypedKey<BlockType>, BlockState[]> BY_KEY = new Object2ObjectOpenHashMap<>(BlockStateIds.BLOCK_COUNT);
    private static final Object2ObjectOpenHashMap<TypedKey<BlockType>, BlockState> DEFAULT = new Object2ObjectOpenHashMap<>(BlockStateIds.BLOCK_COUNT);

    private BlockStates() {
        throw new UnsupportedOperationException("BlockStates cannot be instantiated.");
    }

    /**
     * Computes and registers every block's state permutations from its registered
     * property definitions.
     *
     * @param registry the block registry, already populated by {@code BlockStateIds.registerAll}
     */
    public static void bootstrap(final BlockRegistry registry) {
        for (final BlockType type : registry.types()) {
            registerBlock(type);
        }
    }

    private static void registerBlock(final BlockType type) {

        final TypedKey<BlockType> key = type.key();
        final int stateCount = type.stateCount();

        final BlockState[] states = new BlockState[stateCount];
        for (int ordinal = 0; ordinal < stateCount; ordinal++) {
            states[ordinal] = BlockState.withProperties(key, type.propertyValuesAt(ordinal), false); // safe here as this is during bootstrap so there isn't anything interned for us
        }

        BY_KEY.put(key, states);
        DEFAULT.put(key, states[type.defaultOrdinal()]);
    }

    public static BlockState @Nullable [] statesOf(final TypedKey<BlockType> key) {
        return BY_KEY.get(key);
    }

    public static BlockState stateAt(final TypedKey<BlockType> key, final int ordinal) {
        final BlockState[] states = BY_KEY.get(key);
        Preconditions.checkArgument(states != null, "%s is not a registered block type", key);
        return states[ordinal];
    }

    public static @Nullable BlockState defaultStateOf(final TypedKey<BlockType> key) {
        return DEFAULT.get(key);
    }
}
