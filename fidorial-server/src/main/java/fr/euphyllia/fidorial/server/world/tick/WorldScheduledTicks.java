package fr.euphyllia.fidorial.server.world.tick;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.TickPriority;
import fr.fidorial.world.tick.ScheduledTicks;

/**
 * The scheduled ticks of one world.
 *
 * @param world the world
 * @param ticks the ticks of every world
 */
public record WorldScheduledTicks(ServerWorld world, ScheduledTickEngine ticks) implements ScheduledTicks {

    @Override
    public void scheduleBlockTick(final BlockPosition position, final TypedKey<BlockType> block, final int delay,
                                  final TickPriority priority) {
        Preconditions.checkArgument(position != null, "The position of a scheduled block tick must not be null");
        Preconditions.checkArgument(block != null, "The block of a scheduled block tick must not be null");
        Preconditions.checkArgument(priority != null, "The priority of a scheduled block tick must not be null");
        ticks.scheduleBlockTick(world, position, block, delay, priority);
    }

    @Override
    public void scheduleBlockTick(final BlockPosition position, final TypedKey<BlockType> block, final int delay) {
        scheduleBlockTick(position, block, delay, TickPriority.NORMAL);
    }

    @Override
    public boolean hasBlockTick(final BlockPosition position, final TypedKey<BlockType> block) {
        return ticks.hasBlockTick(world, position, block);
    }

    @Override
    public boolean willBlockTickThisTick(final BlockPosition position, final TypedKey<BlockType> block) {
        return ticks.willBlockTickThisTick(world, position, block);
    }

    @Override
    public void scheduleFluidTick(final BlockPosition position, final TypedKey<Fluid> fluid, final int delay) {
        Preconditions.checkArgument(position != null, "The position of a scheduled fluid tick must not be null");
        ticks.scheduleFluidTick(world, position, simulated(fluid), delay);
    }

    @Override
    public boolean hasFluidTick(final BlockPosition position, final TypedKey<Fluid> fluid) {
        return ticks.hasFluidTick(world, position, simulated(fluid));
    }

    private static FluidType simulated(final TypedKey<Fluid> fluid) {
        Preconditions.checkArgument(fluid != null, "The fluid of a scheduled fluid tick must not be null");
        final FluidType type = FluidType.byFluidKey(fluid);
        Preconditions.checkArgument(type != null, "Only water and lava tick, not %s", fluid);
        return type;
    }
}
