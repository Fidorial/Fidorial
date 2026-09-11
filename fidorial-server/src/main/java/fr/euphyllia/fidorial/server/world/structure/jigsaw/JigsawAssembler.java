package fr.euphyllia.fidorial.server.world.structure.jigsaw;

import fr.euphyllia.fidorial.server.world.structure.DataProblems;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.StructureRegistry;
import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.euphyllia.fidorial.server.world.structure.math.Rotations;
import fr.euphyllia.fidorial.server.world.structure.pool.PoolElement;
import fr.euphyllia.fidorial.server.world.structure.pool.Projection;
import fr.euphyllia.fidorial.server.world.structure.pool.TemplatePool;
import fr.euphyllia.fidorial.server.world.structure.template.JigsawInfo;
import fr.euphyllia.fidorial.server.world.structure.worldgen.JigsawStructure;
import fr.euphyllia.fidorial.server.world.structure.worldgen.PoolAliasBinding;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainView;
import fr.fidorial.world.structure.StructureRotation;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class JigsawAssembler {

    private static final int MAX_PIECES = 1024;

    private final StructureRegistry registry;
    private final TerrainView terrain;
    private final DataProblems problems;

    public JigsawAssembler(final StructureRegistry registry, final TerrainView terrain) {
        this.registry = registry;
        this.terrain = terrain;
        this.problems = registry.problems();
    }

    public record Stub(
            JigsawStructure structure,
            PlacedPiece start,
            int centerX,
            int centerY,
            int centerZ,
            Map<Key, Key> aliases,
            LegacyRandom random
    ) {
    }

    public StructureStart generate(final JigsawStructure structure, final int chunkX, final int chunkZ, final boolean checkBiome) {
        final Stub stub = stub(structure, chunkX, chunkZ, checkBiome);
        return stub == null ? StructureStart.EMPTY : assemble(stub);
    }

    public @Nullable Stub stub(final JigsawStructure structure, final int chunkX, final int chunkZ, final boolean checkBiome) {
        final LegacyRandom random = new LegacyRandom(0L);
        random.setLargeFeatureSeed(terrain.seed(), chunkX, chunkZ);
        final int startY = structure.startHeight().sample(random, terrain.minY(), terrain.height());
        return stub(structure, structure.startPool(), structure.startJigsawName(),
                chunkX << 4, startY, chunkZ << 4, random, checkBiome);
    }

    public @Nullable Stub stub(final JigsawStructure structure, final Key startPoolKey, final @Nullable Key startJigsawName,
                               final int startX, final int startY, final int startZ, final LegacyRandom random,
                               final boolean checkBiome) {
        final Map<Key, Key> aliases = new HashMap<>();
        if (!structure.aliases().isEmpty()) {
            final LegacyRandom aliasRandom = new LegacyRandom(terrain.seed() ^ LegacyRandom.positionSeed(startX, startY, startZ));
            for (final PoolAliasBinding binding : structure.aliases()) {
                binding.resolve(aliasRandom, aliases);
            }
        }

        final StructureRotation rotation = StructureRotation.values()[random.nextInt(4)];
        final Key poolKey = aliases.getOrDefault(startPoolKey, startPoolKey);
        final TemplatePool pool = registry.pool(poolKey);
        if (pool == null) {
            problems.warn("Structure " + structure.id().asString() + " starts from pool " + poolKey.asString()
                    + " which no datapack provides");
            return null;
        }
        final PoolElement element = pool.randomTemplate(random);
        if (element instanceof PoolElement.Empty || !element.usable(registry)) {
            return null;
        }

        int originX = startX;
        int originY = startY;
        int originZ = startZ;
        if (startJigsawName != null) {
            final List<JigsawInfo> jigsaws = shuffledJigsaws(element, startX, startY, startZ, rotation, random);
            JigsawInfo named = null;
            for (final JigsawInfo jigsaw : jigsaws) {
                if (jigsaw.name().equals(startJigsawName)) {
                    named = jigsaw;
                    break;
                }
            }
            if (named == null) {
                problems.warn("No jigsaw named " + startJigsawName.asString() + " in the start pool of "
                        + structure.id().asString());
                return null;
            }
            originX = startX - (named.x() - startX);
            originY = startY - (named.y() - startY);
            originZ = startZ - (named.z() - startZ);
        }

        final Box box = element.boundingBox(registry, originX, originY, originZ, rotation);
        if (box == null) {
            return null;
        }
        final int centerX = (box.maxX() + box.minX()) / 2;
        final int centerZ = (box.maxZ() + box.minZ()) / 2;
        final int centerY = structure.projectStartToHeightmap() != null
                ? startY + terrain.height(structure.projectStartToHeightmap(), centerX, centerZ)
                : originY;
        final int bottom = box.minY() + element.groundLevelDelta();
        final PlacedPiece start = new PlacedPiece(element, originX, originY, originZ, rotation, box, element.groundLevelDelta())
                .moved(centerY - bottom);

        if (structure.paddingBottom() != 0 || structure.paddingTop() != 0) {
            final int min = terrain.minY() + structure.paddingBottom();
            final int max = terrain.maxY() - structure.paddingTop();
            if (start.box().minY() < min || start.box().maxY() > max) {
                return null;
            }
        }
        if (checkBiome && !structure.biomes().contains(terrain.biome(centerX, centerY, centerZ))) {
            return null;
        }
        return new Stub(structure, start, centerX, centerY, centerZ, aliases, random);
    }

    public StructureStart assemble(final Stub stub) {
        final JigsawStructure structure = stub.structure();
        final List<PlacedPiece> pieces = new ArrayList<>();
        pieces.add(stub.start());
        if (structure.maxDepth() > 0) {
            final int h = structure.maxDistanceHorizontal();
            final int v = structure.maxDistanceVertical();
            final Box container = new Box(
                    stub.centerX() - h,
                    Math.max(stub.centerY() - v, terrain.minY() + structure.paddingBottom()),
                    stub.centerZ() - h,
                    stub.centerX() + h,
                    Math.min(stub.centerY() + v, terrain.maxY() - structure.paddingTop()),
                    stub.centerZ() + h);
            final FreeSpace outer = new FreeSpace(container);
            outer.occupy(stub.start().box());
            new Placer(structure, stub.aliases(), stub.random(), pieces).run(stub.start(), outer);
        }
        return StructureStart.of(structure, pieces);
    }

    private List<JigsawInfo> shuffledJigsaws(final PoolElement element, final int x, final int y, final int z,
                                             final StructureRotation rotation, final LegacyRandom random) {
        final List<JigsawInfo> jigsaws = new ArrayList<>(element.jigsaws(registry, x, y, z, rotation));
        random.shuffle(jigsaws);
        jigsaws.sort(Comparator.comparingInt(JigsawInfo::selectionPriority).reversed());
        return jigsaws;
    }

    private static final class FreeSpace {

        private final Box container;
        private final List<Box> occupied = new ArrayList<>();

        FreeSpace(final Box container) {
            this.container = container;
        }

        boolean fits(final Box box) {
            if (!container.contains(box)) {
                return false;
            }
            for (final Box taken : occupied) {
                if (taken.intersects(box)) {
                    return false;
                }
            }
            return true;
        }

        void occupy(final Box box) {
            occupied.add(box);
        }
    }

    private record PieceState(PlacedPiece piece, FreeSpace free, int depth) {
    }

    private final class Placer {

        private final JigsawStructure structure;
        private final Map<Key, Key> aliases;
        private final LegacyRandom random;
        private final List<PlacedPiece> pieces;
        private final TreeMap<Integer, ArrayDeque<PieceState>> queue = new TreeMap<>(Comparator.reverseOrder());

        Placer(final JigsawStructure structure, final Map<Key, Key> aliases, final LegacyRandom random, final List<PlacedPiece> pieces) {
            this.structure = structure;
            this.aliases = aliases;
            this.random = random;
            this.pieces = pieces;
        }

        void run(final PlacedPiece start, final FreeSpace free) {
            tryPlacingChildren(start, free, 0);
            while (!queue.isEmpty() && pieces.size() < MAX_PIECES) {
                final Map.Entry<Integer, ArrayDeque<PieceState>> first = queue.firstEntry();
                final PieceState state = first.getValue().pollFirst();
                if (first.getValue().isEmpty()) {
                    queue.remove(first.getKey());
                }
                if (state != null) {
                    tryPlacingChildren(state.piece(), state.free(), state.depth());
                }
            }
        }

        private @Nullable TemplatePool resolvePool(final Key raw) {
            final Key key = aliases.getOrDefault(raw, raw);
            final TemplatePool pool = registry.pool(key);
            if (pool == null) {
                problems.warn("Jigsaw pool " + key.asString() + " (used by " + structure.id().asString()
                        + ") is not provided by any datapack");
                return null;
            }
            if (pool.size() == 0 && !key.equals(Keys.EMPTY)) {
                problems.warn("Jigsaw pool " + key.asString() + " is empty");
                return null;
            }
            return pool;
        }

        private void tryPlacingChildren(final PlacedPiece piece, final FreeSpace free, final int depth) {
            final PoolElement element = piece.element();
            final boolean parentRigid = element.projection() == Projection.RIGID;
            final Box parentBox = piece.box();
            final int parentMinY = parentBox.minY();
            FreeSpace inner = null;

            outer:
            for (final JigsawInfo jigsaw : shuffledJigsaws(element, piece.x(), piece.y(), piece.z(), piece.rotation(), random)) {
                final int targetX = jigsaw.x() + jigsaw.front().stepX();
                final int targetY = jigsaw.y() + jigsaw.front().stepY();
                final int targetZ = jigsaw.z() + jigsaw.front().stepZ();
                final int jigsawYOffset = jigsaw.y() - parentMinY;
                int surface = Integer.MIN_VALUE;

                final TemplatePool pool = resolvePool(jigsaw.pool());
                if (pool == null) {
                    continue;
                }
                final TemplatePool fallback = resolvePool(pool.fallback());
                if (fallback == null) {
                    continue;
                }

                final FreeSpace space;
                if (parentBox.isInside(targetX, targetY, targetZ)) {
                    if (inner == null) {
                        inner = new FreeSpace(parentBox);
                    }
                    space = inner;
                } else {
                    space = free;
                }

                final List<PoolElement> candidates = new ArrayList<>();
                if (depth != structure.maxDepth()) {
                    candidates.addAll(pool.shuffledTemplates(random));
                }
                candidates.addAll(fallback.shuffledTemplates(random));
                final int placementPriority = jigsaw.placementPriority();

                for (final PoolElement candidate : candidates) {
                    if (candidate instanceof PoolElement.Empty) {
                        break;
                    }
                    if (!candidate.usable(registry)) {
                        continue;
                    }
                    for (final StructureRotation rotation : Rotations.shuffled(random)) {
                        final List<JigsawInfo> childJigsaws = shuffledJigsaws(candidate, 0, 0, 0, rotation, random);
                        final Box atZero = candidate.boundingBox(registry, 0, 0, 0, rotation);
                        if (atZero == null) {
                            continue;
                        }
                        final int expansion = structure.useExpansionHack() && atZero.ySpan() <= 16
                                ? expansionHeight(childJigsaws, atZero)
                                : 0;

                        for (final JigsawInfo childJigsaw : childJigsaws) {
                            if (!jigsaw.canAttach(childJigsaw)) {
                                continue;
                            }
                            final int childX = targetX - childJigsaw.x();
                            final int childY = targetY - childJigsaw.y();
                            final int childZ = targetZ - childJigsaw.z();
                            final Box childBox = candidate.boundingBox(registry, childX, childY, childZ, rotation);
                            if (childBox == null) {
                                continue;
                            }
                            final boolean childRigid = candidate.projection() == Projection.RIGID;
                            final int childJigsawYOffset = childJigsaw.y();
                            final int deltaY = jigsawYOffset - childJigsawYOffset + jigsaw.front().stepY();
                            final int adjustedY;
                            if (parentRigid && childRigid) {
                                adjustedY = parentMinY + deltaY;
                            } else {
                                if (surface == Integer.MIN_VALUE) {
                                    surface = terrain.worldSurface(jigsaw.x(), jigsaw.z());
                                }
                                adjustedY = surface - childJigsawYOffset;
                            }
                            final int shift = adjustedY - childBox.minY();
                            final Box placedBox = childBox.moved(0, shift, 0);
                            final Box claimed;
                            if (expansion > 0) {
                                final int expandBy = Math.max(expansion + 1, placedBox.maxY() - placedBox.minY());
                                claimed = placedBox.encapsulate(placedBox.minX(), placedBox.minY() + expandBy, placedBox.minZ());
                            } else {
                                claimed = placedBox;
                            }
                            if (!space.fits(claimed)) {
                                continue;
                            }
                            space.occupy(claimed);
                            final int groundDelta = childRigid
                                    ? piece.groundLevelDelta() - deltaY
                                    : candidate.groundLevelDelta();
                            final PlacedPiece child = new PlacedPiece(candidate, childX, childY + shift, childZ,
                                    rotation, claimed, groundDelta);
                            pieces.add(child);
                            if (depth + 1 <= structure.maxDepth()) {
                                queue.computeIfAbsent(placementPriority, _ -> new ArrayDeque<>())
                                        .addLast(new PieceState(child, space, depth + 1));
                            }
                            continue outer;
                        }
                    }
                }
            }
        }

        private int expansionHeight(final List<JigsawInfo> childJigsaws, final Box atZero) {
            int max = 0;
            for (final JigsawInfo child : childJigsaws) {
                if (!atZero.isInside(child.x() + child.front().stepX(), child.y() + child.front().stepY(),
                        child.z() + child.front().stepZ())) {
                    continue;
                }
                final Key key = aliases.getOrDefault(child.pool(), child.pool());
                final TemplatePool pool = registry.pool(key);
                if (pool == null) {
                    continue;
                }
                final TemplatePool fallback = registry.pool(pool.fallback());
                max = Math.max(max, Math.max(pool.maxSize(registry), fallback == null ? 0 : fallback.maxSize(registry)));
            }
            return max;
        }
    }
}
