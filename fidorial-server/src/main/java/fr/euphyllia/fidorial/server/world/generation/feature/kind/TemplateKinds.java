package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.HeightmapKind;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.TargetTest;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.euphyllia.fidorial.server.world.structure.JsonNbt;
import fr.euphyllia.fidorial.server.world.structure.StateTransforms;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.euphyllia.fidorial.server.world.structure.math.Rotations;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BlockEntityTypeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.structure.StructureRotation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The features that copy a structure template into the world: springs, wells, and fossils buried under the
 * surface. The templates come from the data packs the server loaded; a feature whose template is not there places
 * nothing.
 */
final class TemplateKinds {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TemplateKinds.class);
    private static final StructureRotation[] ROTATIONS = StructureRotation.values();
    /**
     * The block entities filled from a loot table, which get their own seed when a template places them.
     */
    private static final Set<TypedKey<BlockEntityType>> LOOT_CONTAINERS = Set.of(BlockEntityTypeKeys.CHEST,
            BlockEntityTypeKeys.TRAPPED_CHEST, BlockEntityTypeKeys.BARREL, BlockEntityTypeKeys.HOPPER,
            BlockEntityTypeKeys.DISPENSER, BlockEntityTypeKeys.DROPPER, BlockEntityTypeKeys.SHULKER_BOX);
    /**
     * The shapes made from each template, forgotten with the template when the data packs are reloaded.
     */
    private static final Map<StructureTemplateImpl, Shape> SHAPES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Shape NO_SHAPE = new Shape(0, 0, 0, List.of());

    private TemplateKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("template", TemplateKinds::template);
        parsers.put("fossil", TemplateKinds::fossil);
    }

    /**
     * A block of a template, relative to its corner.
     */
    private record Block(int x, int y, int z, char state, @Nullable CompoundBinaryTag nbt) {
    }

    /**
     * A template ready to be placed: its size, and its blocks in the order they are processed, full blocks first,
     * then the others, then those holding data, each group from the bottom up.
     */
    private record Shape(int sizeX, int sizeY, int sizeZ, List<List<Block>> palettes) {

        boolean isEmpty() {
            return palettes.isEmpty();
        }
    }

    /**
     * A template named by a feature. It is looked up each time it is placed, so that reloading the data packs
     * takes effect at once.
     */
    private record TemplateRef(FeatureLibrary library, Key key) {

        static TemplateRef of(final FeatureLibrary library, final String id) {
            return new TemplateRef(library, Key.key(id));
        }

        Shape shape() {
            final StructureTemplateImpl template = library.context().generator().environment().templates().apply(key);
            if (template == null) {
                return NO_SHAPE;
            }
            return SHAPES.computeIfAbsent(template, found -> shapeOf(library.context().states(), found));
        }
    }

    private static Shape shapeOf(final StateFlags states, final StructureTemplateImpl template) {
        final List<List<Block>> palettes = new ArrayList<>();
        for (int p = 0; p < template.paletteCount(); p++) {
            final @Nullable BlockState[] palette = template.palette(p);
            final List<Block> blocks = new ArrayList<>();
            for (int i = 0; i < template.blockCount(); i++) {
                final int index = template.stateIndex(i);
                final BlockState state = index < palette.length ? palette[index] : null;
                if (state == null || state.name().equals(BlockTypeKeys.STRUCTURE_VOID)) {
                    continue;
                }
                blocks.add(new Block(template.x(i), template.y(i), template.z(i), states.palette().id(state), template.nbt(i)));
            }
            blocks.sort(Comparator.<Block>comparingInt(block -> group(states, block))
                    .thenComparingInt(Block::y).thenComparingInt(Block::x).thenComparingInt(Block::z));
            palettes.add(List.copyOf(blocks));
        }
        return new Shape(template.sizeX(), template.sizeY(), template.sizeZ(), List.copyOf(palettes));
    }

    private static int group(final StateFlags states, final Block block) {
        if (block.nbt() != null) {
            return 2;
        }
        return states.has(block.state(), StateFlags.FULL_COLLISION) ? 0 : 1;
    }

    /**
     * A block about to be placed, in world coordinates.
     */
    private record Placed(int x, int y, int z, char state, @Nullable CompoundBinaryTag nbt) {
    }

    /**
     * A step that changes or drops the blocks of a template before they are placed.
     */
    @FunctionalInterface
    private interface Processor {
        @Nullable Placed process(DecorationRegion region, BitRandom random, Placed block);
    }

    private static List<Processor> processors(final FeatureLibrary library, final @Nullable JsonElement element) {
        final List<Processor> processors = new ArrayList<>();
        if (element == null) {
            return processors;
        }
        JsonElement list = element;
        if (list.isJsonPrimitive()) {
            list = library.context().data().get(WorldgenData.Section.PROCESSOR_LIST, list.getAsString());
        }
        if (list.isJsonObject()) {
            list = list.getAsJsonObject().get("processors");
        }
        for (final JsonElement entry : list.getAsJsonArray()) {
            processors.add(processor(library, entry.getAsJsonObject()));
        }
        return processors;
    }

    private static Processor processor(final FeatureLibrary library, final JsonObject data) {
        final String type = Values.typeName(data.get("processor_type").getAsString());
        final StateFlags states = library.context().states();
        return switch (type) {
            case "block_rot" -> {
                final float integrity = data.get("integrity").getAsFloat();
                final StateFlags.Membership rottable = data.has("rottable_blocks") ? library.blockSet(data.get("rottable_blocks")) : null;
                yield (region, random, block) -> (rottable == null || rottable.test(block.state())) && !(random.nextFloat() <= integrity)
                        ? null : block;
            }
            case "protected_blocks" -> {
                final StateFlags.Membership protectedBlocks = library.blockSet(data.get("value"));
                yield (region, random, block) -> protectedBlocks.test(region.id(block.x(), block.y(), block.z())) ? null : block;
            }
            case "rule" -> {
                final List<Rule> rules = new ArrayList<>();
                for (final JsonElement rule : data.getAsJsonArray("rules")) {
                    rules.add(Rule.read(library, rule.getAsJsonObject()));
                }
                yield (region, random, block) -> {
                    final LegacyRandom positional = LegacyRandom.atPosition(block.x(), block.y(), block.z());
                    for (final Rule rule : rules) {
                        if (rule.input().test(block.state(), block.y(), positional)
                                && rule.location().test(region.id(block.x(), block.y(), block.z()), block.y(), positional)) {
                            return new Placed(block.x(), block.y(), block.z(), rule.output(), rule.modify(positional, block.nbt()));
                        }
                    }
                    return block;
                };
            }
            case "nop" -> (region, random, block) -> block;
            default -> {
                library.unsupported("structure processor", type);
                yield (region, random, block) -> block;
            }
        };
    }

    /**
     * A rule of a rule processor: what it matches, and what it puts instead.
     */
    private record Rule(TargetTest input, TargetTest location, char output, @Nullable JsonObject modifier) {

        static Rule read(final FeatureLibrary library, final JsonObject data) {
            return new Rule(TargetTest.parse(library, data.get("input_predicate")),
                    TargetTest.parse(library, data.get("location_predicate")), library.state(data.get("output_state")),
                    data.has("block_entity_modifier") ? data.getAsJsonObject("block_entity_modifier") : null);
        }

        @Nullable CompoundBinaryTag modify(final LegacyRandom random, final @Nullable CompoundBinaryTag nbt) {
            if (modifier == null) {
                return nbt;
            }
            final String type = Values.typeName(modifier.get("type").getAsString());
            return switch (type) {
                case "append_loot" -> (nbt == null ? CompoundBinaryTag.empty() : nbt)
                        .putString("LootTable", Key.key(modifier.get("loot_table").getAsString()).asString())
                        .putLong("LootTableSeed", random.nextLong());
                case "append_static" -> {
                    CompoundBinaryTag merged = nbt == null ? CompoundBinaryTag.empty() : nbt;
                    final CompoundBinaryTag extra = JsonNbt.compound(modifier.get("data"));
                    for (final String key : extra.keySet()) {
                        merged = merged.put(key, extra.get(key));
                    }
                    yield merged;
                }
                case "clear" -> CompoundBinaryTag.empty();
                default -> nbt;
            };
        }
    }

    /**
     * Copies a template into the world, turned and moved, through its processors.
     *
     * @param bounds the area outside which nothing is processed nor set: minX, minY, minZ, maxX, maxY, maxZ;
     *               {@code null} for no limit
     * @return {@code true} if the template has blocks
     */
    private static boolean placeTemplate(final DecorationRegion region, final BitRandom random, final Shape shape,
                                         final int x, final int y, final int z, final StructureRotation rotation,
                                         final List<Processor> processors, final int @Nullable [] bounds) {
        if (shape.isEmpty()) {
            return false;
        }
        final List<Block> blocks = shape.palettes().get(random.nextInt(shape.palettes().size()));
        final List<Placed> processed = new ArrayList<>(blocks.size());
        for (final Block block : blocks) {
            final int px = x + Rotations.x(rotation, block.x(), block.z());
            final int py = y + block.y();
            final int pz = z + Rotations.z(rotation, block.x(), block.z());
            if (!inside(bounds, px, py, pz)) {
                continue;
            }
            Placed current = new Placed(px, py, pz, block.state(), block.nbt());
            for (final Processor processor : processors) {
                current = processor.process(region, random, current);
                if (current == null) {
                    break;
                }
            }
            if (current != null) {
                processed.add(current);
            }
        }
        final StateFlags states = region.states();
        for (final Placed block : processed) {
            if (!inside(bounds, block.x(), block.y(), block.z())) {
                continue;
            }
            final char previous = region.id(block.x(), block.y(), block.z());
            char state = region.id(StateTransforms.rotate(region.state(block.state()), rotation));
            if (states.has(previous, StateFlags.WATER_SOURCE) && "false".equals(states.property(state, "waterlogged"))) {
                state = states.with(state, "waterlogged", "true");
            }
            if (!region.set(block.x(), block.y(), block.z(), state)) {
                continue;
            }
            if (states.has(state, StateFlags.LIQUID_BLOCK)) {
                region.scheduleFluid(block.x(), block.y(), block.z());
            }
            if (block.nbt() != null) {
                CompoundBinaryTag nbt = block.nbt();
                if (isContainer(nbt)) {
                    nbt = nbt.putLong("LootTableSeed", random.nextLong());
                }
                region.putBlockEntity(block.x(), block.y(), block.z(), nbt);
            }
        }
        return true;
    }

    /**
     * {@return {@code true} for the data of a container whose content comes from a loot table}
     */
    private static boolean isContainer(final CompoundBinaryTag nbt) {
        final String id = nbt.getString("id");
        return Key.parseable(id) && LOOT_CONTAINERS.contains(TypedKey.withRegistry(RegistryKey.BLOCK_ENTITY_TYPE, id));
    }

    private static boolean inside(final int @Nullable [] bounds, final int x, final int y, final int z) {
        return bounds == null || x >= bounds[0] && y >= bounds[1] && z >= bounds[2] && x <= bounds[3] && y <= bounds[4] && z <= bounds[5];
    }

    /**
     * {@return the step one block along a horizontal direction, after a rotation}
     */
    private static int[] rotatedStep(final StructureRotation rotation, final int dx, final int dz) {
        return new int[] {Rotations.x(rotation, dx, dz), Rotations.z(rotation, dx, dz)};
    }

    private static Feature template(final FeatureLibrary library, final JsonObject data) {
        final List<TemplateRef> shapes = new ArrayList<>();
        final List<StructureRotation[]> rotations = new ArrayList<>();
        final List<Integer> weights = new ArrayList<>();
        for (final JsonElement entry : data.getAsJsonArray("templates")) {
            final JsonObject object = entry.getAsJsonObject();
            final JsonObject template = object.getAsJsonObject("data");
            shapes.add(TemplateRef.of(library, template.get("id").getAsString()));
            if (template.has("rotations")) {
                final List<StructureRotation> allowed = new ArrayList<>();
                for (final JsonElement rotation : template.getAsJsonArray("rotations")) {
                    allowed.add(StructureRotation.valueOf(rotation.getAsString().toUpperCase(Locale.ROOT)));
                }
                rotations.add(allowed.toArray(StructureRotation[]::new));
            } else {
                rotations.add(ROTATIONS);
            }
            weights.add(object.has("weight") ? object.get("weight").getAsInt() : 1);
        }
        final int[] weightArray = weights.stream().mapToInt(Integer::intValue).toArray();
        final int total = weights.stream().mapToInt(Integer::intValue).sum();
        final List<Processor> processors = processors(library, data.get("processors"));
        return (region, random, x, y, z) -> {
            final int chosen = Values.pickWeighted(random, weightArray, total);
            final Shape shape = shapes.get(chosen).shape();
            final StructureRotation[] allowed = rotations.get(chosen);
            final StructureRotation rotation = allowed[random.nextInt(allowed.length)];
            // centre the template on the origin: half its size back along west and north, turned with it
            final int[] west = rotatedStep(rotation, -1, 0);
            final int[] north = rotatedStep(rotation, 0, -1);
            final int px = x + west[0] * (shape.sizeX() / 2) + north[0] * (shape.sizeZ() / 2);
            final int pz = z + west[1] * (shape.sizeX() / 2) + north[1] * (shape.sizeZ() / 2);
            return placeTemplate(region, random, shape, px, y, pz, rotation, processors, null);
        };
    }

    private static Feature fossil(final FeatureLibrary library, final JsonObject data) {
        final List<TemplateRef> bases = new ArrayList<>();
        final List<TemplateRef> overlays = new ArrayList<>();
        for (final JsonElement id : data.getAsJsonArray("fossil_structures")) {
            bases.add(TemplateRef.of(library, id.getAsString()));
        }
        for (final JsonElement id : data.getAsJsonArray("overlay_structures")) {
            overlays.add(TemplateRef.of(library, id.getAsString()));
        }
        final List<Processor> baseProcessors = processors(library, data.get("fossil_processors"));
        final List<Processor> overlayProcessors = processors(library, data.get("overlay_processors"));
        final int maxEmptyCorners = data.get("max_empty_corners_allowed").getAsInt();
        return (region, random, x, y, z) -> {
            final StructureRotation rotation = ROTATIONS[random.nextInt(ROTATIONS.length)];
            final int index = random.nextInt(bases.size());
            final Shape base = bases.get(index).shape();
            final Shape overlay = overlays.get(index).shape();
            if (base.isEmpty()) {
                return false;
            }
            final int chunkMinX = (x >> 4) << 4;
            final int chunkMinZ = (z >> 4) << 4;
            final int[] bounds = {chunkMinX - 16, region.minY(), chunkMinZ - 16, chunkMinX + 15 + 16, region.maxY(), chunkMinZ + 15 + 16};
            final boolean turned = rotation == StructureRotation.CLOCKWISE_90 || rotation == StructureRotation.COUNTERCLOCKWISE_90;
            final int sizeX = turned ? base.sizeZ() : base.sizeX();
            final int sizeZ = turned ? base.sizeX() : base.sizeZ();
            final int lowX = x - sizeX / 2;
            final int lowZ = z - sizeZ / 2;
            int lowest = y;
            for (int dx = 0; dx < sizeX; dx++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    lowest = Math.min(lowest, region.height(HeightmapKind.OCEAN_FLOOR_WG, lowX + dx, lowZ + dz));
                }
            }
            final int targetY = Math.max(lowest - 15 - random.nextInt(10), region.minY() + 10);
            final int lastX = base.sizeX() - 1;
            final int lastZ = base.sizeZ() - 1;
            final int originX;
            final int originZ;
            switch (rotation) {
                case COUNTERCLOCKWISE_90 -> {
                    originX = lowX;
                    originZ = lowZ + lastX;
                }
                case CLOCKWISE_90 -> {
                    originX = lowX + lastZ;
                    originZ = lowZ;
                }
                case CLOCKWISE_180 -> {
                    originX = lowX + lastX;
                    originZ = lowZ + lastZ;
                }
                default -> {
                    originX = lowX;
                    originZ = lowZ;
                }
            }
            if (emptyCorners(region, base, originX, targetY, originZ, rotation) > maxEmptyCorners) {
                return false;
            }
            placeTemplate(region, random, base, originX, targetY, originZ, rotation, baseProcessors, bounds);
            placeTemplate(region, random, overlay, originX, targetY, originZ, rotation, overlayProcessors, bounds);
            return true;
        };
    }

    private static int emptyCorners(final DecorationRegion region, final Shape shape, final int x, final int y, final int z,
                                    final StructureRotation rotation) {
        final int ax = Rotations.x(rotation, shape.sizeX() - 1, shape.sizeZ() - 1);
        final int az = Rotations.z(rotation, shape.sizeX() - 1, shape.sizeZ() - 1);
        final int minX = Math.min(x, x + ax);
        final int maxX = Math.max(x, x + ax);
        final int minZ = Math.min(z, z + az);
        final int maxZ = Math.max(z, z + az);
        final int maxY = y + shape.sizeY() - 1;
        int empty = 0;
        final StateFlags states = region.states();
        for (final int cx : new int[] {minX, maxX}) {
            for (final int cy : new int[] {y, maxY}) {
                for (final int cz : new int[] {minZ, maxZ}) {
                    final char state = region.id(cx, cy, cz);
                    if (states.isAir(state) || states.has(state, StateFlags.LIQUID_BLOCK)) {
                        empty++;
                    }
                }
            }
        }
        return empty;
    }
}
