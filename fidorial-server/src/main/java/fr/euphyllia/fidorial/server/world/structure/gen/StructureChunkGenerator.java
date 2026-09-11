package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.ChunkGenerator;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.structure.StructureGenerator;
import fr.euphyllia.fidorial.server.world.structure.StructureRegistry;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.function.BooleanSupplier;

public final class StructureChunkGenerator implements ChunkGenerator {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(StructureChunkGenerator.class);

    private final Key world;
    private final ChunkGenerator base;
    private final TerrainProbe probe;
    private final BooleanSupplier enabled;
    private volatile StructureGenerator structures;

    public StructureChunkGenerator(final Key world, final ChunkGenerator base, final long seed,
                                   final StructureRegistry registry, final BooleanSupplier enabled) {
        this.world = world;
        this.base = base;
        this.probe = new TerrainProbe(base, seed);
        this.enabled = enabled;
        this.structures = new StructureGenerator(registry, probe);
    }

    public Key world() {
        return world;
    }

    public ChunkGenerator base() {
        return base;
    }

    public TerrainProbe probe() {
        return probe;
    }

    public StructureGenerator structures() {
        return structures;
    }

    public void reload(final StructureRegistry registry) {
        this.structures = new StructureGenerator(registry, probe);
    }

    public boolean placesStructures() {
        return enabled.getAsBoolean() && base.generatesStructures();
    }

    @Override
    public ChunkColumn generate(final int chunkX, final int chunkZ) {
        ChunkColumn column = probe.takeProtoChunk(chunkX, chunkZ);
        if (column == null) {
            column = base.generate(chunkX, chunkZ);
        }
        final StructureGenerator current = structures;
        if (!current.hasWork() || !placesStructures()) {
            return column;
        }
        try {
            probe.offer(chunkX, chunkZ, column);
            current.placeInChunk(new ColumnTarget(column), chunkX, chunkZ);
        } catch (final RuntimeException failure) {
            LOGGER.error("Structures could not be placed in chunk ({}, {}) of {}", chunkX, chunkZ, world, failure);
        }
        return column;
    }

    @Override
    public ChunkGeneratorConfig describeForSave() {
        return base.describeForSave();
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return base.dimensionType();
    }

    @Override
    public boolean generatesStructures() {
        return base.generatesStructures();
    }
}
