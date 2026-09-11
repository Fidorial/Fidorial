package fr.euphyllia.fidorial.server.world.chunk;

import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public final class BlockStates {

    private static final Object2ObjectOpenHashMap<Key, BlockState[]> BY_KEY = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Key, BlockState> DEFAULT = new Object2ObjectOpenHashMap<>();

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

        final Key key = type.key();
        final int stateCount = type.stateCount();

        final BlockState[] states = new BlockState[stateCount];
        for (int ordinal = 0; ordinal < stateCount; ordinal++) {
            states[ordinal] = BlockState.of(key, type.propertyValuesAt(ordinal));
        }

        BY_KEY.put(key, states);
        DEFAULT.put(key, states[type.defaultOrdinal()]);
    }

    public static BlockState @Nullable [] statesOf(final Key key) {
        return BY_KEY.get(key);
    }

    public static BlockState stateAt(final Key key, final int ordinal) {
        return BY_KEY.get(key)[ordinal];
    }

    public static @Nullable BlockState defaultStateOf(final Key key) {
        return DEFAULT.get(key);
    }
}
