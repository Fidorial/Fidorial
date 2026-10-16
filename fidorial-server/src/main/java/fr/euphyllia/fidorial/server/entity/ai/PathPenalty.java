package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import net.kyori.adventure.key.Key;

@FunctionalInterface
public interface PathPenalty {

    Key WATER = Key.key("water");
    double BLOCKED = Double.POSITIVE_INFINITY;

    Key RAILS = Key.key("pathfinding/rails");
    Key DAMAGING = Key.key("pathfinding/damaging");
    Key STICKY = Key.key("pathfinding/sticky");
    Key POWDER_SNOW = Key.key("pathfinding/powder_snow");


    PathPenalty LAND_ANIMAL = (world, x, y, z) -> {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        if (state == null) {
            return 0.0;
        }
        if (BlockView.isTagged(DAMAGING, state) || BlockView.isTagged(POWDER_SNOW, state)) {
            return BLOCKED;
        }
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        if (ground != null && (BlockView.isTagged(DAMAGING, ground) || BlockView.isTagged(POWDER_SNOW, ground))) {
            return BLOCKED;
        }

        double cost = 0.0;
        if (state.name().equals(WATER)) {
            cost += 8.0;
        }
        if (BlockView.isTagged(RAILS, state)) {
            cost += 16.0;
        }
        if (ground != null && BlockView.isTagged(STICKY, ground)) {
            cost += 8.0;
        }
        return cost;
    };

    double cost(ServerWorld world, int x, int y, int z);
}
