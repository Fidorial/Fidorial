package fr.euphyllia.fidorial.server.world.generation.terrain;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;

/**
 * A body of fluid: what fills it and up to which height.
 *
 * @param level the first height above the fluid
 * @param fluid the fluid below {@code level}
 */
public record FluidLevel(int level, BlockState fluid) {

    /**
     * {@return the fluid at a height, or air above the level}
     */
    public BlockState at(final int y, final BlockState air) {
        return y < level ? fluid : air;
    }
}
