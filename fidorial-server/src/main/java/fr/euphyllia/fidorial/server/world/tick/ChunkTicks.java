package fr.euphyllia.fidorial.server.world.tick;

import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;

import java.util.ArrayList;
import java.util.List;

/**
 * The ticks pending for the blocks and the fluids of a chunk, in the order they were scheduled.
 *
 * @param blocks the block ticks
 * @param fluids the fluid ticks
 */
public record ChunkTicks(List<PendingTick<TypedKey<BlockType>>> blocks, List<PendingTick<FluidType>> fluids) {

    /**
     * No tick at all.
     */
    public static final ChunkTicks NONE = new ChunkTicks(List.of(), List.of());

    public ChunkTicks {
        blocks = List.copyOf(blocks);
        fluids = List.copyOf(fluids);
    }

    /**
     * {@return {@code true} if no tick is pending}
     */
    public boolean isEmpty() {
        return blocks.isEmpty() && fluids.isEmpty();
    }

    /**
     * {@return these ticks, then those of another list}
     *
     * @param more the ticks scheduled after these
     */
    public ChunkTicks and(final ChunkTicks more) {
        if (more.isEmpty()) {
            return this;
        }
        if (isEmpty()) {
            return more;
        }
        final List<PendingTick<TypedKey<BlockType>>> allBlocks = new ArrayList<>(blocks);
        allBlocks.addAll(more.blocks);
        final List<PendingTick<FluidType>> allFluids = new ArrayList<>(fluids);
        allFluids.addAll(more.fluids);
        return new ChunkTicks(allBlocks, allFluids);
    }
}
