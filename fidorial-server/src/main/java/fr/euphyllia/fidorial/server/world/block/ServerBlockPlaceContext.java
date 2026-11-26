package fr.euphyllia.fidorial.server.world.block;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockGetter;
import fr.fidorial.world.block.BlockPlaceContext;

/**
 * Where and how a player is placing a block, as the server hands it to the behaviour of the block.
 *
 * @param pos         the position the block is placed at
 * @param clickedFace the face of the neighbouring block the player clicked
 * @param placer      the location and orientation of the player
 * @param world       read access to the surrounding blocks
 * @param cursorY     the height of the click on the clicked face, from {@code 0} to {@code 1}
 * @param sneaking    whether the player sneaks
 */
public record ServerBlockPlaceContext(BlockPosition pos, BlockFace clickedFace, Location placer, BlockGetter world,
                                      float cursorY, boolean sneaking) implements BlockPlaceContext {
}
