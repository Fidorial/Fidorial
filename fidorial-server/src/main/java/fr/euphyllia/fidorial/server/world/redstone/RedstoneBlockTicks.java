package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.schedulers.RegionWideTickHandler;
import fr.euphyllia.fidorial.server.util.threading.ThreadContexts;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.key.Key;

/**
 * Runs the ticks scheduled on blocks, kept by each world in its
 * {@linkplain ServerWorld#blockTicks() block ticks}: saved with their chunk, and only run in the
 * chunks that tick.
 */
public final class RedstoneBlockTicks implements RegionWideTickHandler {

    private final FidorialRedstoneEngine engine;
    private final ServerWorldManager worlds;

    RedstoneBlockTicks(final FidorialRedstoneEngine engine, final ServerWorldManager worlds) {
        this.engine = engine;
        this.worlds = worlds;
    }

    @Override
    public void tick(final Key world, final long[] sectionKeys) {
        final ServerWorld serverWorld = worlds.world(world);
        if (serverWorld != null) {
            serverWorld.blockTicks().tickRegion(sectionKeys, (position, block) -> engine.runTick(serverWorld, position, block));
        }
    }

    void schedule(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block,
                  final int delay, final TickPriority priority) {
        // the ticks of a chunk are run by the thread of its region, which alone schedules them
        ThreadContexts.checkOwnedByCurrentThread(world, position,
                "a redstone tick can only be scheduled on the thread of the region owning the block");
        world.blockTicks().schedule(position, block, delay, priority);
    }

    boolean hasScheduledTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        return world.blockTicks().hasScheduledTick(position, block);
    }

    boolean willTickThisTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        return world.blockTicks().willTickThisTick(position, block);
    }
}
