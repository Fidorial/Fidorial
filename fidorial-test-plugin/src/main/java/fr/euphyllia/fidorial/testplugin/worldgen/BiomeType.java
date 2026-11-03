package fr.euphyllia.fidorial.testplugin.worldgen;

import fr.fidorial.registry.keys.BiomeKeys;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;


public enum BiomeType {

    OCEAN(SurfaceKind.OCEAN_FLOOR, false),
    DEEP_OCEAN(SurfaceKind.OCEAN_FLOOR, false),
    COLD_OCEAN(SurfaceKind.OCEAN_FLOOR, false),
    DEEP_COLD_OCEAN(SurfaceKind.OCEAN_FLOOR, false),
    FROZEN_OCEAN(SurfaceKind.OCEAN_FLOOR, true),
    DEEP_FROZEN_OCEAN(SurfaceKind.OCEAN_FLOOR, true),
    LUKEWARM_OCEAN(SurfaceKind.OCEAN_FLOOR, false),
    DEEP_LUKEWARM_OCEAN(SurfaceKind.OCEAN_FLOOR, false),
    WARM_OCEAN(SurfaceKind.SAND, false),

    RIVER(SurfaceKind.OCEAN_FLOOR, false),
    FROZEN_RIVER(SurfaceKind.OCEAN_FLOOR, true),
    BEACH(SurfaceKind.SAND, false),
    SNOWY_BEACH(SurfaceKind.SAND, true),
    STONY_SHORE(SurfaceKind.GRAVEL, false),

    PLAINS(SurfaceKind.GRASS, false),
    SUNFLOWER_PLAINS(SurfaceKind.GRASS, false),
    SNOWY_PLAINS(SurfaceKind.GRASS, true),
    ICE_SPIKES(SurfaceKind.SNOW_BLOCK, true),
    MEADOW(SurfaceKind.GRASS, false),
    CHERRY_GROVE(SurfaceKind.GRASS, false),
    FOREST(SurfaceKind.GRASS, false),
    FLOWER_FOREST(SurfaceKind.GRASS, false),
    BIRCH_FOREST(SurfaceKind.GRASS, false),
    OLD_GROWTH_BIRCH_FOREST(SurfaceKind.GRASS, false),
    DARK_FOREST(SurfaceKind.GRASS, false),
    PALE_GARDEN(SurfaceKind.GRASS, false),
    TAIGA(SurfaceKind.GRASS, false),
    SNOWY_TAIGA(SurfaceKind.GRASS, true),
    OLD_GROWTH_SPRUCE_TAIGA(SurfaceKind.PODZOL, false),
    OLD_GROWTH_PINE_TAIGA(SurfaceKind.PODZOL, false),
    GROVE(SurfaceKind.SNOW_BLOCK, true),

    DESERT(SurfaceKind.SAND, false),
    SAVANNA(SurfaceKind.GRASS, false),
    SAVANNA_PLATEAU(SurfaceKind.GRASS, false),
    WINDSWEPT_SAVANNA(SurfaceKind.GRASS, false),
    JUNGLE(SurfaceKind.GRASS, false),
    SPARSE_JUNGLE(SurfaceKind.GRASS, false),
    BAMBOO_JUNGLE(SurfaceKind.GRASS, false),
    BADLANDS(SurfaceKind.TERRACOTTA, false),
    ERODED_BADLANDS(SurfaceKind.TERRACOTTA, false),
    WOODED_BADLANDS(SurfaceKind.TERRACOTTA, false),

    SWAMP(SurfaceKind.SWAMP, false),
    MANGROVE_SWAMP(SurfaceKind.MUD, false),

    WINDSWEPT_HILLS(SurfaceKind.GRASS, false),
    WINDSWEPT_GRAVELLY_HILLS(SurfaceKind.GRAVEL, false),
    WINDSWEPT_FOREST(SurfaceKind.GRASS, false),
    SNOWY_SLOPES(SurfaceKind.POWDER_SNOW, true),
    JAGGED_PEAKS(SurfaceKind.STONE_SNOW, true),
    FROZEN_PEAKS(SurfaceKind.PACKED_ICE, true),
    STONY_PEAKS(SurfaceKind.STONY_PEAKS, false),

    MUSHROOM_FIELDS(SurfaceKind.MYCELIUM, false),

    LUSH_CAVES(SurfaceKind.GRASS, false),
    DRIPSTONE_CAVES(SurfaceKind.STONE, false),
    SULFUR_CAVES(SurfaceKind.STONE, false),
    DEEP_DARK(SurfaceKind.STONE, false);

    private static final Map<Key, BiomeType> BY_KEY = new HashMap<>();

    static {
        register(BiomeKeys.OCEAN, OCEAN);
        register(BiomeKeys.DEEP_OCEAN, DEEP_OCEAN);
        register(BiomeKeys.COLD_OCEAN, COLD_OCEAN);
        register(BiomeKeys.DEEP_COLD_OCEAN, DEEP_COLD_OCEAN);
        register(BiomeKeys.FROZEN_OCEAN, FROZEN_OCEAN);
        register(BiomeKeys.DEEP_FROZEN_OCEAN, DEEP_FROZEN_OCEAN);
        register(BiomeKeys.LUKEWARM_OCEAN, LUKEWARM_OCEAN);
        register(BiomeKeys.DEEP_LUKEWARM_OCEAN, DEEP_LUKEWARM_OCEAN);
        register(BiomeKeys.WARM_OCEAN, WARM_OCEAN);
        register(BiomeKeys.RIVER, RIVER);
        register(BiomeKeys.FROZEN_RIVER, FROZEN_RIVER);
        register(BiomeKeys.BEACH, BEACH);
        register(BiomeKeys.SNOWY_BEACH, SNOWY_BEACH);
        register(BiomeKeys.STONY_SHORE, STONY_SHORE);
        register(BiomeKeys.PLAINS, PLAINS);
        register(BiomeKeys.SUNFLOWER_PLAINS, SUNFLOWER_PLAINS);
        register(BiomeKeys.SNOWY_PLAINS, SNOWY_PLAINS);
        register(BiomeKeys.ICE_SPIKES, ICE_SPIKES);
        register(BiomeKeys.MEADOW, MEADOW);
        register(BiomeKeys.CHERRY_GROVE, CHERRY_GROVE);
        register(BiomeKeys.FOREST, FOREST);
        register(BiomeKeys.FLOWER_FOREST, FLOWER_FOREST);
        register(BiomeKeys.BIRCH_FOREST, BIRCH_FOREST);
        register(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, OLD_GROWTH_BIRCH_FOREST);
        register(BiomeKeys.DARK_FOREST, DARK_FOREST);
        register(BiomeKeys.PALE_GARDEN, PALE_GARDEN);
        register(BiomeKeys.TAIGA, TAIGA);
        register(BiomeKeys.SNOWY_TAIGA, SNOWY_TAIGA);
        register(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, OLD_GROWTH_SPRUCE_TAIGA);
        register(BiomeKeys.OLD_GROWTH_PINE_TAIGA, OLD_GROWTH_PINE_TAIGA);
        register(BiomeKeys.GROVE, GROVE);
        register(BiomeKeys.DESERT, DESERT);
        register(BiomeKeys.SAVANNA, SAVANNA);
        register(BiomeKeys.SAVANNA_PLATEAU, SAVANNA_PLATEAU);
        register(BiomeKeys.WINDSWEPT_SAVANNA, WINDSWEPT_SAVANNA);
        register(BiomeKeys.JUNGLE, JUNGLE);
        register(BiomeKeys.SPARSE_JUNGLE, SPARSE_JUNGLE);
        register(BiomeKeys.BAMBOO_JUNGLE, BAMBOO_JUNGLE);
        register(BiomeKeys.BADLANDS, BADLANDS);
        register(BiomeKeys.ERODED_BADLANDS, ERODED_BADLANDS);
        register(BiomeKeys.WOODED_BADLANDS, WOODED_BADLANDS);
        register(BiomeKeys.SWAMP, SWAMP);
        register(BiomeKeys.MANGROVE_SWAMP, MANGROVE_SWAMP);
        register(BiomeKeys.WINDSWEPT_HILLS, WINDSWEPT_HILLS);
        register(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, WINDSWEPT_GRAVELLY_HILLS);
        register(BiomeKeys.WINDSWEPT_FOREST, WINDSWEPT_FOREST);
        register(BiomeKeys.SNOWY_SLOPES, SNOWY_SLOPES);
        register(BiomeKeys.JAGGED_PEAKS, JAGGED_PEAKS);
        register(BiomeKeys.FROZEN_PEAKS, FROZEN_PEAKS);
        register(BiomeKeys.STONY_PEAKS, STONY_PEAKS);
        register(BiomeKeys.MUSHROOM_FIELDS, MUSHROOM_FIELDS);
        register(BiomeKeys.LUSH_CAVES, LUSH_CAVES);
        register(BiomeKeys.DRIPSTONE_CAVES, DRIPSTONE_CAVES);
        register(BiomeKeys.SULFUR_CAVES, SULFUR_CAVES);
        register(BiomeKeys.DEEP_DARK, DEEP_DARK);
    }

    private final SurfaceKind surface;
    private final boolean cold;

    BiomeType(final SurfaceKind surface, final boolean cold) {
        this.surface = surface;
        this.cold = cold;
    }

    private static void register(final Key key, final BiomeType type) {
        BY_KEY.put(key, type);
    }

    public static BiomeType of(final Key key) {
        return BY_KEY.getOrDefault(key, PLAINS);
    }

    public SurfaceKind surface() {
        return surface;
    }

    public boolean cold() {
        return cold;
    }

    public boolean cave() {
        return this == LUSH_CAVES || this == DRIPSTONE_CAVES || this == SULFUR_CAVES || this == DEEP_DARK;
    }
}
