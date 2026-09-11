package fr.euphyllia.fidorial.server.world.structure.worldgen;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record JigsawStructure(
        Key id,
        Set<Key> biomes,
        Key startPool,
        @Nullable Key startJigsawName,
        int maxDepth,
        HeightProvider startHeight,
        @Nullable Heightmap projectStartToHeightmap,
        int maxDistanceHorizontal,
        int maxDistanceVertical,
        boolean useExpansionHack,
        TerrainAdaptation adaptation,
        boolean applyWaterlogging,
        int paddingBottom,
        int paddingTop,
        List<PoolAliasBinding> aliases
) {
}
