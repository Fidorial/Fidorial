package fr.euphyllia.fidorial.server.world.structure.processor;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;

public interface ProcessContext {

    BlockState existing(int x, int y, int z);

    int height(Heightmap heightmap, int x, int z);

    int pivotX();

    int pivotY();

    int pivotZ();

    long worldSeed();
}
