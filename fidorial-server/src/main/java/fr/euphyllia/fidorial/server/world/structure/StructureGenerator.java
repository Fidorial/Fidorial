package fr.euphyllia.fidorial.server.world.structure;

import fr.euphyllia.fidorial.server.world.structure.jigsaw.JigsawAssembler;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.PlacedPiece;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.StructureStart;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.euphyllia.fidorial.server.world.structure.place.BlockTarget;
import fr.euphyllia.fidorial.server.world.structure.place.PiecePlacer;
import fr.euphyllia.fidorial.server.world.structure.worldgen.JigsawStructure;
import fr.euphyllia.fidorial.server.world.structure.worldgen.StructurePlacement;
import fr.euphyllia.fidorial.server.world.structure.worldgen.StructureSet;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainView;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class StructureGenerator {

    public static final int REACH = 8;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(StructureGenerator.class);

    private final StructureRegistry registry;
    private final TerrainView terrain;
    private final JigsawAssembler assembler;
    private final PiecePlacer placer;
    private final FutureCache<StartKey, StructureStart> starts = new FutureCache<>(4096);

    private record StartKey(int set, int chunkX, int chunkZ) {
    }

    public StructureGenerator(final StructureRegistry registry, final TerrainView terrain) {
        this.registry = registry;
        this.terrain = terrain;
        this.assembler = new JigsawAssembler(registry, terrain);
        this.placer = new PiecePlacer(registry, terrain);
    }

    public StructureRegistry registry() {
        return registry;
    }

    public TerrainView terrain() {
        return terrain;
    }

    public JigsawAssembler assembler() {
        return assembler;
    }

    public PiecePlacer placer() {
        return placer;
    }

    public boolean hasWork() {
        return !registry.structureSets().isEmpty();
    }

    public int placeInChunk(final BlockTarget target, final int chunkX, final int chunkZ) {
        final int minX = chunkX << 4;
        final int minZ = chunkZ << 4;
        int placed = 0;
        final List<StructureSet> sets = registry.structureSets();
        for (int setIndex = 0; setIndex < sets.size(); setIndex++) {
            final StructureSet set = sets.get(setIndex);
            if (!(set.placement() instanceof final StructurePlacement.RandomSpread spread)) {
                continue;
            }
            final int cellMinX = Math.floorDiv(chunkX - REACH, spread.spacing());
            final int cellMaxX = Math.floorDiv(chunkX + REACH, spread.spacing());
            final int cellMinZ = Math.floorDiv(chunkZ - REACH, spread.spacing());
            final int cellMaxZ = Math.floorDiv(chunkZ + REACH, spread.spacing());
            for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
                for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
                    final int[] candidate = spread.cellCandidate(terrain.seed(), cellX, cellZ);
                    if (Math.abs(candidate[0] - chunkX) > REACH || Math.abs(candidate[1] - chunkZ) > REACH) {
                        continue;
                    }
                    if (!isStartChunk(spread, candidate[0], candidate[1], 0)) {
                        continue;
                    }
                    final StructureStart start = startAt(setIndex, set, candidate[0], candidate[1]);
                    if (start.isEmpty() || start.bounds() == null
                            || !start.bounds().intersectsXZ(minX, minZ, minX + 15, minZ + 15)) {
                        continue;
                    }
                    for (final PlacedPiece piece : start.pieces()) {
                        if (piece.box().intersectsXZ(minX, minZ, minX + 15, minZ + 15)) {
                            try {
                                placer.place(piece, start.structure(), target);
                                placed++;
                            } catch (final RuntimeException failure) {
                                registry.problems().warn("A piece of " + start.structure().id().asString()
                                        + " could not be placed: " + failure);
                            }
                        }
                    }
                }
            }
        }
        return placed;
    }

    public boolean isStartChunk(final StructurePlacement.RandomSpread spread, final int chunkX, final int chunkZ, final int depth) {
        final int[] candidate = spread.potentialChunk(terrain.seed(), chunkX, chunkZ);
        if (candidate[0] != chunkX || candidate[1] != chunkZ) {
            return false;
        }
        if (!spread.passesFrequency(terrain.seed(), chunkX, chunkZ)) {
            return false;
        }
        if (spread.exclusionSet() != null && depth < 2) {
            final StructureSet other = registry.structureSet(spread.exclusionSet());
            if (other != null && other.placement() instanceof final StructurePlacement.RandomSpread otherSpread) {
                final int range = spread.exclusionChunks();
                for (int x = chunkX - range; x <= chunkX + range; x++) {
                    for (int z = chunkZ - range; z <= chunkZ + range; z++) {
                        if (isStartChunk(otherSpread, x, z, depth + 1)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public StructureStart startAt(final int setIndex, final StructureSet set, final int chunkX, final int chunkZ) {
        return starts.get(new StartKey(setIndex, chunkX, chunkZ), key -> {
            final JigsawAssembler.Stub stub = select(set, chunkX, chunkZ);
            if (stub == null) {
                return StructureStart.EMPTY;
            }
            final long begin = System.nanoTime();
            final StructureStart start = assembler.assemble(stub);
            final long millis = (System.nanoTime() - begin) / 1_000_000L;
            if (millis > 500) {
                LOGGER.info("[datapacks] {} at chunk {}, {} assembled in {} ms ({} pieces)",
                        stub.structure().id().asString(), chunkX, chunkZ, millis, start.pieces().size());
            }
            return start;
        });
    }

    public JigsawAssembler.@Nullable Stub select(final StructureSet set, final int chunkX, final int chunkZ) {
        final List<StructureSet.Entry> entries = set.structures();
        if (entries.size() == 1) {
            final JigsawStructure structure = registry.structure(entries.getFirst().structure());
            return structure == null ? null : assembler.stub(structure, chunkX, chunkZ, true);
        }
        final List<StructureSet.Entry> remaining = new ArrayList<>(entries);
        final LegacyRandom random = new LegacyRandom(0L);
        random.setLargeFeatureSeed(terrain.seed(), chunkX, chunkZ);
        int total = 0;
        for (final StructureSet.Entry entry : remaining) {
            total += entry.weight();
        }
        while (!remaining.isEmpty() && total > 0) {
            int roll = random.nextInt(total);
            int index = 0;
            for (final StructureSet.Entry entry : remaining) {
                roll -= entry.weight();
                if (roll < 0) {
                    break;
                }
                index++;
            }
            index = Math.min(index, remaining.size() - 1);
            final StructureSet.Entry chosen = remaining.get(index);
            final JigsawStructure structure = registry.structure(chosen.structure());
            if (structure != null) {
                final JigsawAssembler.Stub stub = assembler.stub(structure, chunkX, chunkZ, true);
                if (stub != null) {
                    return stub;
                }
            }
            remaining.remove(index);
            total -= chosen.weight();
        }
        return null;
    }

    public record Located(Key structure, int x, int y, int z, int chunkX, int chunkZ) {
    }

    public @Nullable Located locate(final Key structureId, final int originX, final int originZ, final int radiusCells) {
        Located best = null;
        long bestDistance = Long.MAX_VALUE;
        final int originChunkX = originX >> 4;
        final int originChunkZ = originZ >> 4;
        for (final StructureSet set : registry.structureSets()) {
            if (!(set.placement() instanceof final StructurePlacement.RandomSpread spread)) {
                continue;
            }
            boolean listed = false;
            for (final StructureSet.Entry entry : set.structures()) {
                listed |= entry.structure().equals(structureId);
            }
            if (!listed) {
                continue;
            }
            final int centerCellX = Math.floorDiv(originChunkX, spread.spacing());
            final int centerCellZ = Math.floorDiv(originChunkZ, spread.spacing());
            for (int ring = 0; ring <= radiusCells; ring++) {
                boolean foundInRing = false;
                for (int dx = -ring; dx <= ring; dx++) {
                    for (int dz = -ring; dz <= ring; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                            continue;
                        }
                        final int[] candidate = spread.cellCandidate(terrain.seed(), centerCellX + dx, centerCellZ + dz);
                        if (!isStartChunk(spread, candidate[0], candidate[1], 0)) {
                            continue;
                        }
                        final JigsawAssembler.Stub stub = select(set, candidate[0], candidate[1]);
                        if (stub == null || !stub.structure().id().equals(structureId)) {
                            continue;
                        }
                        final long distX = stub.centerX() - originX;
                        final long distZ = stub.centerZ() - originZ;
                        final long distance = distX * distX + distZ * distZ;
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = new Located(structureId, stub.centerX(), stub.centerY(), stub.centerZ(),
                                    candidate[0], candidate[1]);
                        }
                        foundInRing = true;
                    }
                }
                if (foundInRing) {
                    break;
                }
            }
        }
        return best;
    }
}
