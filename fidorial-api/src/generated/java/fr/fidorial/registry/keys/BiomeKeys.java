package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:worldgen/biome} registry.
 */
public final class BiomeKeys {
    /**
     * Key for {@code minecraft:badlands}.
     */
    public static final TypedKey<BiomeDefinition> BADLANDS = create("badlands");

    /**
     * Key for {@code minecraft:bamboo_jungle}.
     */
    public static final TypedKey<BiomeDefinition> BAMBOO_JUNGLE = create("bamboo_jungle");

    /**
     * Key for {@code minecraft:basalt_deltas}.
     */
    public static final TypedKey<BiomeDefinition> BASALT_DELTAS = create("basalt_deltas");

    /**
     * Key for {@code minecraft:beach}.
     */
    public static final TypedKey<BiomeDefinition> BEACH = create("beach");

    /**
     * Key for {@code minecraft:birch_forest}.
     */
    public static final TypedKey<BiomeDefinition> BIRCH_FOREST = create("birch_forest");

    /**
     * Key for {@code minecraft:cherry_grove}.
     */
    public static final TypedKey<BiomeDefinition> CHERRY_GROVE = create("cherry_grove");

    /**
     * Key for {@code minecraft:cold_ocean}.
     */
    public static final TypedKey<BiomeDefinition> COLD_OCEAN = create("cold_ocean");

    /**
     * Key for {@code minecraft:crimson_forest}.
     */
    public static final TypedKey<BiomeDefinition> CRIMSON_FOREST = create("crimson_forest");

    /**
     * Key for {@code minecraft:dappled_forest}.
     */
    public static final TypedKey<BiomeDefinition> DAPPLED_FOREST = create("dappled_forest");

    /**
     * Key for {@code minecraft:dark_forest}.
     */
    public static final TypedKey<BiomeDefinition> DARK_FOREST = create("dark_forest");

    /**
     * Key for {@code minecraft:deep_cold_ocean}.
     */
    public static final TypedKey<BiomeDefinition> DEEP_COLD_OCEAN = create("deep_cold_ocean");

    /**
     * Key for {@code minecraft:deep_dark}.
     */
    public static final TypedKey<BiomeDefinition> DEEP_DARK = create("deep_dark");

    /**
     * Key for {@code minecraft:deep_frozen_ocean}.
     */
    public static final TypedKey<BiomeDefinition> DEEP_FROZEN_OCEAN = create("deep_frozen_ocean");

    /**
     * Key for {@code minecraft:deep_lukewarm_ocean}.
     */
    public static final TypedKey<BiomeDefinition> DEEP_LUKEWARM_OCEAN = create("deep_lukewarm_ocean");

    /**
     * Key for {@code minecraft:deep_ocean}.
     */
    public static final TypedKey<BiomeDefinition> DEEP_OCEAN = create("deep_ocean");

    /**
     * Key for {@code minecraft:desert}.
     */
    public static final TypedKey<BiomeDefinition> DESERT = create("desert");

    /**
     * Key for {@code minecraft:dripstone_caves}.
     */
    public static final TypedKey<BiomeDefinition> DRIPSTONE_CAVES = create("dripstone_caves");

    /**
     * Key for {@code minecraft:end_barrens}.
     */
    public static final TypedKey<BiomeDefinition> END_BARRENS = create("end_barrens");

    /**
     * Key for {@code minecraft:end_highlands}.
     */
    public static final TypedKey<BiomeDefinition> END_HIGHLANDS = create("end_highlands");

    /**
     * Key for {@code minecraft:end_midlands}.
     */
    public static final TypedKey<BiomeDefinition> END_MIDLANDS = create("end_midlands");

    /**
     * Key for {@code minecraft:eroded_badlands}.
     */
    public static final TypedKey<BiomeDefinition> ERODED_BADLANDS = create("eroded_badlands");

    /**
     * Key for {@code minecraft:flower_forest}.
     */
    public static final TypedKey<BiomeDefinition> FLOWER_FOREST = create("flower_forest");

    /**
     * Key for {@code minecraft:forest}.
     */
    public static final TypedKey<BiomeDefinition> FOREST = create("forest");

    /**
     * Key for {@code minecraft:frozen_ocean}.
     */
    public static final TypedKey<BiomeDefinition> FROZEN_OCEAN = create("frozen_ocean");

    /**
     * Key for {@code minecraft:frozen_peaks}.
     */
    public static final TypedKey<BiomeDefinition> FROZEN_PEAKS = create("frozen_peaks");

    /**
     * Key for {@code minecraft:frozen_river}.
     */
    public static final TypedKey<BiomeDefinition> FROZEN_RIVER = create("frozen_river");

    /**
     * Key for {@code minecraft:grove}.
     */
    public static final TypedKey<BiomeDefinition> GROVE = create("grove");

    /**
     * Key for {@code minecraft:ice_caves}.
     */
    public static final TypedKey<BiomeDefinition> ICE_CAVES = create("ice_caves");

    /**
     * Key for {@code minecraft:ice_spikes}.
     */
    public static final TypedKey<BiomeDefinition> ICE_SPIKES = create("ice_spikes");

    /**
     * Key for {@code minecraft:jagged_peaks}.
     */
    public static final TypedKey<BiomeDefinition> JAGGED_PEAKS = create("jagged_peaks");

    /**
     * Key for {@code minecraft:jungle}.
     */
    public static final TypedKey<BiomeDefinition> JUNGLE = create("jungle");

    /**
     * Key for {@code minecraft:lukewarm_ocean}.
     */
    public static final TypedKey<BiomeDefinition> LUKEWARM_OCEAN = create("lukewarm_ocean");

    /**
     * Key for {@code minecraft:lush_caves}.
     */
    public static final TypedKey<BiomeDefinition> LUSH_CAVES = create("lush_caves");

    /**
     * Key for {@code minecraft:mangrove_swamp}.
     */
    public static final TypedKey<BiomeDefinition> MANGROVE_SWAMP = create("mangrove_swamp");

    /**
     * Key for {@code minecraft:meadow}.
     */
    public static final TypedKey<BiomeDefinition> MEADOW = create("meadow");

    /**
     * Key for {@code minecraft:mushroom_fields}.
     */
    public static final TypedKey<BiomeDefinition> MUSHROOM_FIELDS = create("mushroom_fields");

    /**
     * Key for {@code minecraft:nether_wastes}.
     */
    public static final TypedKey<BiomeDefinition> NETHER_WASTES = create("nether_wastes");

    /**
     * Key for {@code minecraft:ocean}.
     */
    public static final TypedKey<BiomeDefinition> OCEAN = create("ocean");

    /**
     * Key for {@code minecraft:old_growth_birch_forest}.
     */
    public static final TypedKey<BiomeDefinition> OLD_GROWTH_BIRCH_FOREST = create("old_growth_birch_forest");

    /**
     * Key for {@code minecraft:old_growth_pine_taiga}.
     */
    public static final TypedKey<BiomeDefinition> OLD_GROWTH_PINE_TAIGA = create("old_growth_pine_taiga");

    /**
     * Key for {@code minecraft:old_growth_spruce_taiga}.
     */
    public static final TypedKey<BiomeDefinition> OLD_GROWTH_SPRUCE_TAIGA = create("old_growth_spruce_taiga");

    /**
     * Key for {@code minecraft:pale_garden}.
     */
    public static final TypedKey<BiomeDefinition> PALE_GARDEN = create("pale_garden");

    /**
     * Key for {@code minecraft:plains}.
     */
    public static final TypedKey<BiomeDefinition> PLAINS = create("plains");

    /**
     * Key for {@code minecraft:river}.
     */
    public static final TypedKey<BiomeDefinition> RIVER = create("river");

    /**
     * Key for {@code minecraft:savanna}.
     */
    public static final TypedKey<BiomeDefinition> SAVANNA = create("savanna");

    /**
     * Key for {@code minecraft:savanna_plateau}.
     */
    public static final TypedKey<BiomeDefinition> SAVANNA_PLATEAU = create("savanna_plateau");

    /**
     * Key for {@code minecraft:small_end_islands}.
     */
    public static final TypedKey<BiomeDefinition> SMALL_END_ISLANDS = create("small_end_islands");

    /**
     * Key for {@code minecraft:snowy_beach}.
     */
    public static final TypedKey<BiomeDefinition> SNOWY_BEACH = create("snowy_beach");

    /**
     * Key for {@code minecraft:snowy_plains}.
     */
    public static final TypedKey<BiomeDefinition> SNOWY_PLAINS = create("snowy_plains");

    /**
     * Key for {@code minecraft:snowy_slopes}.
     */
    public static final TypedKey<BiomeDefinition> SNOWY_SLOPES = create("snowy_slopes");

    /**
     * Key for {@code minecraft:snowy_taiga}.
     */
    public static final TypedKey<BiomeDefinition> SNOWY_TAIGA = create("snowy_taiga");

    /**
     * Key for {@code minecraft:soul_sand_valley}.
     */
    public static final TypedKey<BiomeDefinition> SOUL_SAND_VALLEY = create("soul_sand_valley");

    /**
     * Key for {@code minecraft:sparse_jungle}.
     */
    public static final TypedKey<BiomeDefinition> SPARSE_JUNGLE = create("sparse_jungle");

    /**
     * Key for {@code minecraft:stony_peaks}.
     */
    public static final TypedKey<BiomeDefinition> STONY_PEAKS = create("stony_peaks");

    /**
     * Key for {@code minecraft:stony_shore}.
     */
    public static final TypedKey<BiomeDefinition> STONY_SHORE = create("stony_shore");

    /**
     * Key for {@code minecraft:sulfur_caves}.
     */
    public static final TypedKey<BiomeDefinition> SULFUR_CAVES = create("sulfur_caves");

    /**
     * Key for {@code minecraft:sunflower_plains}.
     */
    public static final TypedKey<BiomeDefinition> SUNFLOWER_PLAINS = create("sunflower_plains");

    /**
     * Key for {@code minecraft:swamp}.
     */
    public static final TypedKey<BiomeDefinition> SWAMP = create("swamp");

    /**
     * Key for {@code minecraft:taiga}.
     */
    public static final TypedKey<BiomeDefinition> TAIGA = create("taiga");

    /**
     * Key for {@code minecraft:the_end}.
     */
    public static final TypedKey<BiomeDefinition> THE_END = create("the_end");

    /**
     * Key for {@code minecraft:the_void}.
     */
    public static final TypedKey<BiomeDefinition> THE_VOID = create("the_void");

    /**
     * Key for {@code minecraft:warm_ocean}.
     */
    public static final TypedKey<BiomeDefinition> WARM_OCEAN = create("warm_ocean");

    /**
     * Key for {@code minecraft:warped_forest}.
     */
    public static final TypedKey<BiomeDefinition> WARPED_FOREST = create("warped_forest");

    /**
     * Key for {@code minecraft:windswept_forest}.
     */
    public static final TypedKey<BiomeDefinition> WINDSWEPT_FOREST = create("windswept_forest");

    /**
     * Key for {@code minecraft:windswept_gravelly_hills}.
     */
    public static final TypedKey<BiomeDefinition> WINDSWEPT_GRAVELLY_HILLS = create("windswept_gravelly_hills");

    /**
     * Key for {@code minecraft:windswept_hills}.
     */
    public static final TypedKey<BiomeDefinition> WINDSWEPT_HILLS = create("windswept_hills");

    /**
     * Key for {@code minecraft:windswept_savanna}.
     */
    public static final TypedKey<BiomeDefinition> WINDSWEPT_SAVANNA = create("windswept_savanna");

    /**
     * Key for {@code minecraft:wooded_badlands}.
     */
    public static final TypedKey<BiomeDefinition> WOODED_BADLANDS = create("wooded_badlands");

    private static final List<TypedKey<BiomeDefinition>> VALUES = List.of(
        BADLANDS,
        BAMBOO_JUNGLE,
        BASALT_DELTAS,
        BEACH,
        BIRCH_FOREST,
        CHERRY_GROVE,
        COLD_OCEAN,
        CRIMSON_FOREST,
        DAPPLED_FOREST,
        DARK_FOREST,
        DEEP_COLD_OCEAN,
        DEEP_DARK,
        DEEP_FROZEN_OCEAN,
        DEEP_LUKEWARM_OCEAN,
        DEEP_OCEAN,
        DESERT,
        DRIPSTONE_CAVES,
        END_BARRENS,
        END_HIGHLANDS,
        END_MIDLANDS,
        ERODED_BADLANDS,
        FLOWER_FOREST,
        FOREST,
        FROZEN_OCEAN,
        FROZEN_PEAKS,
        FROZEN_RIVER,
        GROVE,
        ICE_CAVES,
        ICE_SPIKES,
        JAGGED_PEAKS,
        JUNGLE,
        LUKEWARM_OCEAN,
        LUSH_CAVES,
        MANGROVE_SWAMP,
        MEADOW,
        MUSHROOM_FIELDS,
        NETHER_WASTES,
        OCEAN,
        OLD_GROWTH_BIRCH_FOREST,
        OLD_GROWTH_PINE_TAIGA,
        OLD_GROWTH_SPRUCE_TAIGA,
        PALE_GARDEN,
        PLAINS,
        RIVER,
        SAVANNA,
        SAVANNA_PLATEAU,
        SMALL_END_ISLANDS,
        SNOWY_BEACH,
        SNOWY_PLAINS,
        SNOWY_SLOPES,
        SNOWY_TAIGA,
        SOUL_SAND_VALLEY,
        SPARSE_JUNGLE,
        STONY_PEAKS,
        STONY_SHORE,
        SULFUR_CAVES,
        SUNFLOWER_PLAINS,
        SWAMP,
        TAIGA,
        THE_END,
        THE_VOID,
        WARM_OCEAN,
        WARPED_FOREST,
        WINDSWEPT_FOREST,
        WINDSWEPT_GRAVELLY_HILLS,
        WINDSWEPT_HILLS,
        WINDSWEPT_SAVANNA,
        WOODED_BADLANDS
    );

    private BiomeKeys() {
        throw new UnsupportedOperationException("BiomeKeys cannot be instantiated.");
    }

    private static TypedKey<BiomeDefinition> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.BIOME, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<BiomeDefinition>> values() {
        return VALUES.stream();
    }
}
