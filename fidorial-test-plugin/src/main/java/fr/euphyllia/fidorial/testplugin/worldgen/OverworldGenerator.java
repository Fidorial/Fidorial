package fr.euphyllia.fidorial.testplugin.worldgen;

import fr.euphyllia.fidorial.testplugin.worldgen.climate.BiomeTable;
import fr.euphyllia.fidorial.testplugin.worldgen.climate.ClimatePoint;
import fr.euphyllia.fidorial.testplugin.worldgen.climate.ClimateSampler;
import fr.euphyllia.fidorial.testplugin.worldgen.shape.TerrainShaper;
import fr.euphyllia.fidorial.testplugin.worldgen.stage.BiomeStage;
import fr.euphyllia.fidorial.testplugin.worldgen.stage.DecorationStage;
import fr.euphyllia.fidorial.testplugin.worldgen.stage.OreStage;
import fr.euphyllia.fidorial.testplugin.worldgen.stage.RavineCarver;
import fr.euphyllia.fidorial.testplugin.worldgen.stage.SurfaceStage;
import fr.euphyllia.fidorial.testplugin.worldgen.stage.TerrainStage;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.GenerationDescriptor;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public final class OverworldGenerator implements WorldGenerator {

    private final GeneratorSettings settings;
    private final DimensionTypeDefinition dimensionType;
    private final ClimateSampler climate;
    private final TerrainStage terrain;
    private final BiomeStage biomes;
    private final SurfaceStage surface;
    private final RavineCarver ravines;
    private final OreStage ores;
    private final DecorationStage decoration;

    private final ThreadLocal<ChunkScratch> scratches = ThreadLocal.withInitial(ChunkScratch::new);

    public OverworldGenerator(final GeneratorSettings settings) {
        this(settings, VanillaDimensionTypes.OVERWORLD);
    }

    public OverworldGenerator(final GeneratorSettings settings, final DimensionTypeDefinition dimensionType) {
        this.settings = settings;
        this.dimensionType = dimensionType;
        this.climate = new ClimateSampler(settings.seed());

        final TerrainShaper shaper = new TerrainShaper(settings.seaLevel());
        this.terrain = new TerrainStage(settings, climate, shaper);
        this.biomes = new BiomeStage();
        this.surface = new SurfaceStage(settings);
        this.ravines = new RavineCarver(settings);
        this.ores = new OreStage(settings, climate);
        this.decoration = new DecorationStage(settings, terrain);
    }

    public OverworldGenerator(final long seed) {
        this(GeneratorSettings.defaults(seed));
    }

    public GeneratorSettings settings() {
        return settings;
    }

    @Override
    public void generate(final GeneratedChunk chunk) {
        final ChunkScratch scratch = scratches.get();
        scratch.reset(chunk.chunkX(), chunk.chunkZ(), chunk.minY(), chunk.height());

        terrain.fill(scratch);
        biomes.assign(scratch);
        surface.apply(scratch);
        if (settings.caves()) {
            ravines.carve(scratch);
        }
        if (settings.ores()) {
            ores.apply(scratch);
        }
        if (settings.decoration()) {
            decoration.decorate(scratch);
        }

        scratch.flush(chunk);
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }

    @Override
    public GenerationDescriptor describeForSave() {
        return GenerationDescriptor.noise(dimensionType.key().key(), Key.key("fidorial", "overworld"));
    }

    public ClimatePoint climateAt(final int worldX, final int worldZ) {
        return climate.sample(worldX, worldZ);
    }

    public TypedKey<BiomeDefinition> biomeAt(final int worldX, final int worldZ) {
        return BiomeTable.surface(climate.sample(worldX, worldZ));
    }

    public int surfaceAt(final int worldX, final int worldZ, final int minY, final int maxY) {
        return terrain.surfaceAt(worldX, worldZ, minY, maxY);
    }

    public int @Nullable [] findSpawn(final int centreX, final int centreZ, final int radius, final int minY, final int maxY) {
        for (int distance = 0; distance <= radius; distance += 8) {
            for (int angle = 0; angle < 16; angle++) {
                final double theta = angle * Math.PI / 8.0;
                final int x = centreX + (int) (Math.cos(theta) * distance);
                final int z = centreZ + (int) (Math.sin(theta) * distance);
                final int ground = terrain.surfaceAt(x, z, minY, maxY);
                if (ground <= settings.seaLevel() + 1 || ground > maxY - 40) {
                    continue;
                }
                return new int[]{x, ground + 1, z};
            }
        }
        return null;
    }
}
