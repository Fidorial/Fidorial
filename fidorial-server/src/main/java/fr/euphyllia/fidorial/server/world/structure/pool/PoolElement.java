package fr.euphyllia.fidorial.server.world.structure.pool;

import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.math.Direction;
import fr.euphyllia.fidorial.server.world.structure.math.Rotations;
import fr.euphyllia.fidorial.server.world.structure.template.JigsawInfo;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import fr.fidorial.world.structure.StructureRotation;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public sealed interface PoolElement {

    Projection projection();

    default int groundLevelDelta() {
        return 1;
    }

    @Nullable Box boundingBox(TemplateSource source, int x, int y, int z, StructureRotation rotation);

    List<JigsawInfo> jigsaws(TemplateSource source, int x, int y, int z, StructureRotation rotation);

    boolean usable(TemplateSource source);

    record Single(Key location, ProcessorRef processors, Projection projection, boolean legacy) implements PoolElement {

        @Override
        public @Nullable Box boundingBox(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            final StructureTemplateImpl template = source.template(location);
            if (template == null) {
                return null;
            }
            return Rotations.box(x, y, z, template.sizeX(), template.sizeY(), template.sizeZ(), rotation);
        }

        @Override
        public List<JigsawInfo> jigsaws(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            final StructureTemplateImpl template = source.template(location);
            if (template == null) {
                return List.of();
            }
            final List<JigsawInfo> result = new ArrayList<>(template.jigsaws().size());
            for (final JigsawInfo jigsaw : template.jigsaws()) {
                result.add(jigsaw.transform(rotation, x, y, z));
            }
            return result;
        }

        @Override
        public boolean usable(final TemplateSource source) {
            return source.template(location) != null;
        }
    }

    record ListOf(List<PoolElement> elements, Projection projection) implements PoolElement {

        public ListOf {
            elements = List.copyOf(elements);
        }

        @Override
        public @Nullable Box boundingBox(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            Box box = null;
            for (final PoolElement element : elements) {
                final Box child = element.boundingBox(source, x, y, z, rotation);
                if (child != null) {
                    box = box == null ? child : box.union(child);
                }
            }
            return box;
        }

        @Override
        public List<JigsawInfo> jigsaws(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            return elements.isEmpty() ? List.of() : elements.getFirst().jigsaws(source, x, y, z, rotation);
        }

        @Override
        public boolean usable(final TemplateSource source) {
            for (final PoolElement element : elements) {
                if (!element.usable(source)) {
                    return false;
                }
            }
            return !elements.isEmpty();
        }
    }

    record Feature(Key feature, Projection projection) implements PoolElement {

        @Override
        public Box boundingBox(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            return new Box(x, y, z, x, y, z);
        }

        @Override
        public List<JigsawInfo> jigsaws(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            return List.of(new JigsawInfo(x, y, z, Direction.DOWN, Direction.SOUTH, Keys.BOTTOM, Keys.EMPTY, Keys.EMPTY,
                    true, 0, 0));
        }

        @Override
        public boolean usable(final TemplateSource source) {
            return true;
        }
    }

    record Empty() implements PoolElement {

        public static final Empty INSTANCE = new Empty();

        @Override
        public Projection projection() {
            return Projection.TERRAIN_MATCHING;
        }

        @Override
        public @Nullable Box boundingBox(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            return null;
        }

        @Override
        public List<JigsawInfo> jigsaws(final TemplateSource source, final int x, final int y, final int z, final StructureRotation rotation) {
            return List.of();
        }

        @Override
        public boolean usable(final TemplateSource source) {
            return true;
        }
    }
}
