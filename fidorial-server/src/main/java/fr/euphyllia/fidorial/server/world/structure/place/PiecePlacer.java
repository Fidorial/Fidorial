package fr.euphyllia.fidorial.server.world.structure.place;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.StateTransforms;
import fr.euphyllia.fidorial.server.world.structure.StructureRegistry;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.PlacedPiece;
import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.euphyllia.fidorial.server.world.structure.math.Rotations;
import fr.euphyllia.fidorial.server.world.structure.pool.PoolElement;
import fr.euphyllia.fidorial.server.world.structure.pool.Projection;
import fr.euphyllia.fidorial.server.world.structure.processor.PlacedBlock;
import fr.euphyllia.fidorial.server.world.structure.processor.ProcessContext;
import fr.euphyllia.fidorial.server.world.structure.processor.ProcessorList;
import fr.euphyllia.fidorial.server.world.structure.processor.Processors;
import fr.euphyllia.fidorial.server.world.structure.processor.StructureProcessor;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;
import fr.euphyllia.fidorial.server.world.structure.worldgen.JigsawStructure;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainAdaptation;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainView;
import fr.fidorial.world.structure.StructureRotation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PiecePlacer {

    private static final int MAX_FOUNDATION_DEPTH = 12;

    private final StructureRegistry registry;
    private final TerrainView terrain;
    private final BlockState air;
    private final StructureProcessor jigsawReplacement;

    public PiecePlacer(final StructureRegistry registry, final TerrainView terrain) {
        this.registry = registry;
        this.terrain = terrain;
        final BlockState resolvedAir = registry.validator().resolve(Keys.AIR, Map.of());
        this.air = resolvedAir == null ? BlockState.of(Keys.AIR) : resolvedAir;
        this.jigsawReplacement = Processors.jigsawReplacement(registry.validator());
    }

    public void place(final PlacedPiece piece, final @Nullable JigsawStructure structure, final BlockTarget target) {
        final Box box = piece.box();
        if (!box.intersectsXZ(target.minX(), target.minZ(), target.maxX(), target.maxZ())) {
            return;
        }
        final TerrainAdaptation adaptation = structure == null ? TerrainAdaptation.NONE : structure.adaptation();
        final boolean waterlog = structure == null || structure.applyWaterlogging();
        final boolean beard = adaptation.beard() && piece.projection() == Projection.RIGID;
        if (beard) {
            clearInside(box, target);
        }
        placeElement(piece.element(), piece.x(), piece.y(), piece.z(), piece.rotation(), target, waterlog);
        if (beard) {
            buildFoundation(box, target);
        }
    }

    private void placeElement(final PoolElement element, final int x, final int y, final int z,
                              final StructureRotation rotation, final BlockTarget target, final boolean waterlog) {
        switch (element) {
            case final PoolElement.Single single -> {
                final StructureTemplateImpl template = registry.template(single.location());
                if (template == null) {
                    return;
                }
                final List<StructureProcessor> processors = new ArrayList<>();
                processors.add(single.legacy() ? Processors.IGNORE_STRUCTURE_AND_AIR : Processors.IGNORE_STRUCTURE_BLOCK);
                processors.add(jigsawReplacement);
                processors.addAll(registry.processorList(single.processors()).processors());
                if (single.projection() == Projection.TERRAIN_MATCHING) {
                    processors.add(Processors.TERRAIN_MATCHING);
                }
                placeTemplate(template, x, y, z, rotation, new ProcessorList(processors), target, waterlog);
            }
            case final PoolElement.ListOf list -> {
                for (final PoolElement child : list.elements()) {
                    placeElement(child, x, y, z, rotation, target, waterlog);
                }
            }
            case final PoolElement.Feature feature -> {
                // features are not supported: nothing to place
            }
            case final PoolElement.Empty empty -> {
                // nothing to place
            }
        }
    }

    public int placeTemplate(final StructureTemplateImpl template, final int originX, final int originY, final int originZ,
                             final StructureRotation rotation, final ProcessorList processorList,
                             final BlockTarget target, final boolean waterlog) {
        final List<StructureProcessor> processors = processorList.processors();
        final boolean wholePiece = processorList.needsWholePiece();
        final @Nullable BlockState[] palette = template.paletteCount() > 1
                ? template.palette(LegacyRandom.atPosition(originX, originY, originZ).nextInt(template.paletteCount()))
                : template.palette(0);
        final Context context = new Context(target, originX, originY, originZ);

        final List<int[]> originals = new ArrayList<>();
        final List<PlacedBlock> processed = new ArrayList<>();
        for (int i = 0; i < template.blockCount(); i++) {
            final int lx = template.x(i);
            final int ly = template.y(i);
            final int lz = template.z(i);
            final int wx = originX + Rotations.x(rotation, lx, lz);
            final int wz = originZ + Rotations.z(rotation, lx, lz);
            if (!wholePiece && (wx < target.minX() || wx > target.maxX() || wz < target.minZ() || wz > target.maxZ())) {
                continue;
            }
            final int index = template.stateIndex(i);
            final BlockState state = index < palette.length ? palette[index] : null;
            if (state == null) {
                continue;
            }
            PlacedBlock current = new PlacedBlock(wx, originY + ly, wz, state, template.nbt(i));
            for (final StructureProcessor processor : processors) {
                current = processor.process(context, lx, ly, lz, current);
                if (current == null) {
                    break;
                }
            }
            if (current != null) {
                originals.add(new int[]{lx, ly, lz});
                processed.add(current);
            }
        }
        for (final StructureProcessor processor : processors) {
            processor.finish(context, originals, processed);
        }

        int written = 0;
        for (final PlacedBlock block : processed) {
            if (!target.contains(block.x(), block.y(), block.z())) {
                continue;
            }
            BlockState state = StateTransforms.rotate(block.state(), rotation);
            if (waterlog && state.properties().containsKey("waterlogged") && !"true".equals(state.properties().get("waterlogged"))) {
                final BlockState existing = target.get(block.x(), block.y(), block.z());
                if (existing.name().equals(Keys.WATER) || "true".equals(existing.properties().get("waterlogged"))) {
                    state = StateTransforms.with(state, "waterlogged", "true");
                }
            }
            target.set(block.x(), block.y(), block.z(), state, block.nbt());
            written++;
        }
        return written;
    }

    private void clearInside(final Box box, final BlockTarget target) {
        final int minX = Math.max(box.minX(), target.minX());
        final int maxX = Math.min(box.maxX(), target.maxX());
        final int minZ = Math.max(box.minZ(), target.minZ());
        final int maxZ = Math.min(box.maxZ(), target.maxZ());
        final int minY = Math.max(box.minY() + 1, target.minY());
        final int maxY = Math.min(box.maxY(), target.maxY());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    final BlockState existing = target.get(x, y, z);
                    if (existing.isAir() || existing.isFluid() || target.placedByStructure(x, y, z)) {
                        continue;
                    }
                    target.set(x, y, z, air, null);
                }
            }
        }
    }

    private void buildFoundation(final Box box, final BlockTarget target) {
        final int minX = Math.max(box.minX(), target.minX());
        final int maxX = Math.min(box.maxX(), target.maxX());
        final int minZ = Math.max(box.minZ(), target.minZ());
        final int maxZ = Math.min(box.maxZ(), target.maxZ());
        final int floor = box.minY();
        if (floor - 1 < target.minY()) {
            return;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final BlockState floorBlock = target.get(x, floor, z);
                if (floorBlock.isAir() || !target.placedByStructure(x, floor, z)) {
                    continue;
                }
                int y = floor - 1;
                final int limit = Math.max(target.minY(), floor - MAX_FOUNDATION_DEPTH);
                while (y >= limit && !target.isGround(target.get(x, y, z))) {
                    y--;
                }
                if (y < limit) {
                    continue;
                }
                final BlockState filler = foundationBlock(target.get(x, y, z));
                for (int fill = y + 1; fill < floor; fill++) {
                    target.set(x, fill, z, filler, null);
                }
            }
        }
    }

    private BlockState foundationBlock(final BlockState ground) {
        final String name = ground.name().value();
        if (name.equals("grass_block") || name.equals("mycelium") || name.equals("podzol") || name.contains("nylium")) {
            final BlockState dirt = registry.validator().resolve(net.kyori.adventure.key.Key.key(
                    name.contains("nylium") ? "netherrack" : "dirt"), Map.of());
            return dirt == null ? ground : dirt;
        }
        return ground;
    }

    private final class Context implements ProcessContext {

        private final BlockTarget target;
        private final int pivotX;
        private final int pivotY;
        private final int pivotZ;

        Context(final BlockTarget target, final int pivotX, final int pivotY, final int pivotZ) {
            this.target = target;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
        }

        @Override
        public BlockState existing(final int x, final int y, final int z) {
            return target.contains(x, y, z) ? target.get(x, y, z) : air;
        }

        @Override
        public int height(final Heightmap heightmap, final int x, final int z) {
            return terrain.height(heightmap, x, z);
        }

        @Override
        public int pivotX() {
            return pivotX;
        }

        @Override
        public int pivotY() {
            return pivotY;
        }

        @Override
        public int pivotZ() {
            return pivotZ;
        }

        @Override
        public long worldSeed() {
            return terrain.seed();
        }
    }
}
