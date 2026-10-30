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
        if (BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_DAMAGING.key(), state) || BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_POWDER_SNOW.key(), state)) {
            return BLOCKED;
        }
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        if (ground != null && (BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_DAMAGING.key(), ground) || BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_POWDER_SNOW.key(), ground))) {
            return BLOCKED;
        }

        double cost = 0.0;
        if (state.name().equals(BlockTypeKeys.WATER.key())) {
            cost += 8.0;
        }
        if (BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_RAILS.key(), state)) {
            cost += 16.0;
        }
        if (ground != null && BlockView.isTagged(BlockTypeTagKeys.PATHFINDING_STICKY.key(), ground)) {
            cost += 8.0;
        }
        return cost;
    };

    double cost(ServerWorld world, int x, int y, int z);
}
