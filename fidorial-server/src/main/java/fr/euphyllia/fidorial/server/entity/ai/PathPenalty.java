package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;

@FunctionalInterface
public interface PathPenalty {

    double BLOCKED = Double.POSITIVE_INFINITY;

    PathPenalty LAND_ANIMAL = (world, x, y, z) -> {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        if (state == null) {
            return 0.0;
        }
        if (BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_DAMAGING, state) || BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_POWDER_SNOW, state)) {
            return BLOCKED;
        }
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        if (ground != null && (BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_DAMAGING, ground) || BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_POWDER_SNOW, ground))) {
            return BLOCKED;
        }

        double cost = 0.0;
        if (state.name().equals(BlockTypeKeys.WATER)) {
            cost += 8.0;
        }
        if (BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_RAILS, state)) {
            cost += 16.0;
        }
        if (ground != null && BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_STICKY, ground)) {
            cost += 8.0;
        }
        return cost;
    };

    double cost(ServerWorld world, int x, int y, int z);
}
