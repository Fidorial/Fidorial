package fr.fidorial.math;

import fr.fidorial.world.BlockFace;
import org.jetbrains.annotations.Contract;

public sealed interface BlockPosition extends Position permits BlockPositionImpl {
    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition relative(BlockFace face);

    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition offsetX(int x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition offsetY(int y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition offsetZ(int z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    BlockPosition offset(int x, int y, int z);

    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition withX(int x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition withY(int y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    BlockPosition withZ(int z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    BlockPosition with(int x, int y, int z);
}
