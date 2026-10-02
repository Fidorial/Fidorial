package fr.fidorial.math;

import fr.fidorial.world.BlockFace;
import org.jetbrains.annotations.Contract;

public sealed interface FinePosition extends Position permits FinePositionImpl, Location {
    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition relative(BlockFace face);

    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetX(int x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetY(int y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetZ(int z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    FinePosition offset(int x, int y, int z);

    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition withX(int x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition withY(int y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    FinePosition withZ(int z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    FinePosition with(int x, int y, int z);
}
