package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeKeys;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * The fluid a block holds, and the fluid block holding a fluid.
 */
public final class FluidBlockCodec {

    private static final String LEVEL = "level";

    private FluidBlockCodec() {
        throw new UnsupportedOperationException("FluidBlockCodec cannot be instantiated.");
    }

    /**
     * {@return the fluid a block holds}: the fluid of a fluid block, the water source of a
     * waterlogged block or of a block always under water, none for any other block
     *
     * @param block the block, {@code null} for one that is not loaded
     */
    public static FluidStateImpl fromBlock(final @Nullable BlockState block) {
        if (block == null) {
            return FluidStateImpl.empty();
        }
        final FluidType type = FluidType.byBlockKey(block.name());
        if (type != null) {
            return FluidStateImpl.ofBlockLevel(type, level(block));
        }
        return block.isWater() ? FluidStateImpl.source(FluidType.WATER) : FluidStateImpl.empty();
    }

    /**
     * {@return the level of a fluid block}, {@code 0}, a source, when it cannot be read
     */
    private static int level(final BlockState block) {
        final String raw = block.properties().get(LEVEL);
        if (raw == null) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (final NumberFormatException _) {
            return 0;
        }
    }

    /**
     * {@return the fluid block holding a fluid}, air for no fluid
     *
     * @param state the fluid
     */
    public static BlockState toBlock(final FluidStateImpl state) {
        final FluidType type = state.type();
        if (type == null) {
            return BlockState.withoutProperties(BlockTypeKeys.AIR);
        }
        return BlockState.withProperties(type.blockKey(), Map.of(LEVEL, Integer.toString(state.blockLevel())));
    }
}
