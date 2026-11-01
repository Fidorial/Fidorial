package fr.fidorial.registrygen.generate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes, for each block state, whether it conducts redstone power and which of its faces hold
 * other blocks: the dataset redstone runs on.
 *
 * <p>Built from PrismarineJS's {@code minecraft-data} (collision shapes, and whether the block lets
 * light through) and from Mojang's block tags, out of the data generator. The general rules are
 * the documented ones: a full, opaque block conducts; a face holds a lever when the shape covers it,
 * a standing torch when it covers its center, a repeater when it covers its rim. The blocks vanilla
 * treats otherwise, documented on the Minecraft Wiki, are listed in {@link Rules}.</p>
 *
 * <p>The output maps each block to one integer, or to one integer per state when they differ. Faces
 * are numbered {@code down, up, north, south, west, east}:</p>
 * <ul>
 *   <li>bit {@code 0}: conducts redstone power;</li>
 *   <li>bits {@code 1..6}: the face holds something needing the whole face (a lever, a wall torch);</li>
 *   <li>bits {@code 7..12}: the face holds something needing its center (a standing torch);</li>
 *   <li>bits {@code 13..18}: the face holds something needing its rim (a repeater, a comparator);</li>
 *   <li>bit {@code 19}: the collision shape is a full block.</li>
 * </ul>
 *
 * @since 0.1.0
 */
public final class BlockSupportDatasetGenerator {

    private static final String NAMESPACE = "minecraft:";
    private static final double EPSILON = 1.0E-7;
    private static final double[][] FULL_CUBE = {{0, 0, 0, 1, 1, 1}};

    /**
     * The blocks vanilla treats differently from what their shape and opacity suggest.
     */
    static final class Rules {

        /**
         * Conduct although they let light through or are not full blocks.
         */
        static final Set<String> CONDUCTORS = Set.of(
                "#shulker_boxes", "barrier", "spawner", "trial_spawner", "vault", "test_instance_block",
                "slime_block", "mangrove_roots", "mud", "soul_sand");

        /**
         * Never conduct although they are full opaque blocks: light sources, and blocks emitting or
         * moving with power themselves.
         */
        static final Set<String> NON_CONDUCTORS = Set.of(
                "glowstone", "sea_lantern", "redstone_block", "observer", "piston", "sticky_piston", "tnt",
                "copper_bulb", "exposed_copper_bulb", "weathered_copper_bulb", "oxidized_copper_bulb",
                "waxed_copper_bulb", "waxed_exposed_copper_bulb", "waxed_weathered_copper_bulb",
                "waxed_oxidized_copper_bulb");

        /**
         * Hold nothing on any face, even though one can stand on them.
         */
        static final Set<String> HOLD_NOTHING = Set.of("#leaves");

        /**
         * Hold anything like a full block, although entities sink in them a little.
         */
        static final Set<String> HOLD_AS_FULL_BLOCK = Set.of("soul_sand", "mud", "honey_block");

        private Rules() {
        }
    }

    /**
     * @param prismarineShapes Prismarine's {@code blockCollisionShapes.json}
     * @param prismarineBlocks Prismarine's {@code blocks.json}
     * @param blockTags        Mojang's block tags directory, {@code data/minecraft/tags/block}
     * @param outputFile       the dataset to write, replaced when it exists
     * @return how many blocks were written
     * @throws IOException if an input cannot be read or the dataset cannot be written
     */
    public int generate(final Path prismarineShapes, final Path prismarineBlocks, final Path blockTags, final Path outputFile)
            throws IOException {
        Objects.requireNonNull(prismarineShapes, "prismarineShapes");
        Objects.requireNonNull(prismarineBlocks, "prismarineBlocks");
        Objects.requireNonNull(blockTags, "blockTags");
        Objects.requireNonNull(outputFile, "outputFile");

        final JsonObject shapesReport = parse(prismarineShapes).getAsJsonObject();
        final JsonObject blockShapes = shapesReport.getAsJsonObject("blocks");
        final JsonObject shapes = shapesReport.getAsJsonObject("shapes");
        final BlockTags tags = new BlockTags(blockTags);
        final Set<String> conductors = tags.expand(Rules.CONDUCTORS);
        final Set<String> nonConductors = tags.expand(Rules.NON_CONDUCTORS);
        final Set<String> holdNothing = tags.expand(Rules.HOLD_NOTHING);
        final Set<String> holdAsFullBlock = tags.expand(Rules.HOLD_AS_FULL_BLOCK);

        final Map<String, List<Integer>> flagsByBlock = new TreeMap<>();
        for (final JsonElement element : parse(prismarineBlocks).getAsJsonArray()) {
            final JsonObject block = element.getAsJsonObject();
            final String name = block.get("name").getAsString();
            final JsonElement shapeIds = blockShapes.get(name);
            if (shapeIds == null) {
                throw new IOException("No collision shape for " + name + " in " + prismarineShapes);
            }
            final boolean transparent = block.get("transparent").getAsBoolean();
            final int stateCount = block.get("maxStateId").getAsInt() - block.get("minStateId").getAsInt() + 1;
            final List<Integer> flags = new ArrayList<>(stateCount);
            for (int ordinal = 0; ordinal < stateCount; ordinal++) {
                final int shapeId = shapeIds.isJsonArray() ? shapeIds.getAsJsonArray().get(ordinal).getAsInt() : shapeIds.getAsInt();
                final double[][] collision = boxes(shapes.getAsJsonArray(Integer.toString(shapeId)));
                final boolean fullBlock = isFullCube(collision);

                boolean conductor = fullBlock && !transparent;
                if (conductors.contains(name)) {
                    conductor = true;
                } else if (nonConductors.contains(name)) {
                    conductor = false;
                }

                final double[][] support;
                if (holdNothing.contains(name)) {
                    support = new double[0][];
                } else if (holdAsFullBlock.contains(name)) {
                    support = FULL_CUBE;
                } else if ("snow".equals(name)) {
                    // a layer of snow holds blocks by its visible height, a layer taller than it lets entities stand
                    final int layers = Integer.parseInt(stateValues(block, ordinal).get("layers"));
                    support = new double[][] {{0, 0, 0, 1, layers / 8.0, 1}};
                } else {
                    support = collision;
                }

                int value = conductor ? 1 : 0;
                for (int face = 0; face < 6; face++) {
                    final List<double[]> faceShape = faceShape(support, face);
                    if (covers(faceShape, 0, 0, 1, 1)) {
                        value |= 1 << (1 + face);
                    }
                    if (face < 2 ? covers(faceShape, 7 / 16.0, 7 / 16.0, 9 / 16.0, 9 / 16.0)
                            : covers(faceShape, 7 / 16.0, 0, 9 / 16.0, 10 / 16.0)) {
                        value |= 1 << (7 + face);
                    }
                    if (covers(faceShape, 0, 0, 1, 2 / 16.0) && covers(faceShape, 0, 14 / 16.0, 1, 1)
                            && covers(faceShape, 0, 0, 2 / 16.0, 1) && covers(faceShape, 14 / 16.0, 0, 1, 1)) {
                        value |= 1 << (13 + face);
                    }
                }
                if (fullBlock) {
                    value |= 1 << 19;
                }
                flags.add(value);
            }
            flagsByBlock.put(NAMESPACE + name, flags);
        }
        if (flagsByBlock.isEmpty()) {
            throw new IOException("No block in " + prismarineBlocks);
        }

        final StringBuilder json = new StringBuilder("{\n");
        boolean first = true;
        for (final Map.Entry<String, List<Integer>> entry : flagsByBlock.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            final List<Integer> flags = entry.getValue();
            json.append("  \"").append(entry.getKey()).append("\": ");
            json.append(flags.stream().distinct().count() == 1 ? flags.getFirst().toString() : flags.toString());
        }
        json.append("\n}\n");
        write(outputFile, json.toString());
        return flagsByBlock.size();
    }

    private static boolean isFullCube(final double[][] boxes) {
        return boxes.length == 1 && boxes[0][0] == 0 && boxes[0][1] == 0 && boxes[0][2] == 0
                && boxes[0][3] == 1 && boxes[0][4] == 1 && boxes[0][5] == 1;
    }

    private static double[][] boxes(final JsonArray array) {
        final double[][] boxes = new double[array.size()][];
        for (int i = 0; i < array.size(); i++) {
            final JsonArray box = array.get(i).getAsJsonArray();
            boxes[i] = new double[6];
            for (int j = 0; j < 6; j++) {
                boxes[i][j] = box.get(j).getAsDouble();
            }
        }
        return boxes;
    }

    /**
     * {@return the rectangles where the shape touches one face of the block, in that face's two
     * coordinates (horizontal then vertical for the sides)}
     */
    private static List<double[]> faceShape(final double[][] boxes, final int face) {
        final List<double[]> rectangles = new ArrayList<>();
        for (final double[] box : boxes) {
            final double minX = box[0];
            final double minY = box[1];
            final double minZ = box[2];
            final double maxX = box[3];
            final double maxY = box[4];
            final double maxZ = box[5];
            final double[] rectangle = switch (face) {
                case 0 -> minY <= EPSILON && maxY > EPSILON ? new double[] {minX, minZ, maxX, maxZ} : null;
                case 1 -> maxY >= 1 - EPSILON && minY < 1 - EPSILON ? new double[] {minX, minZ, maxX, maxZ} : null;
                case 2 -> minZ <= EPSILON && maxZ > EPSILON ? new double[] {minX, minY, maxX, maxY} : null;
                case 3 -> maxZ >= 1 - EPSILON && minZ < 1 - EPSILON ? new double[] {minX, minY, maxX, maxY} : null;
                case 4 -> minX <= EPSILON && maxX > EPSILON ? new double[] {minZ, minY, maxZ, maxY} : null;
                default -> maxX >= 1 - EPSILON && minX < 1 - EPSILON ? new double[] {minZ, minY, maxZ, maxY} : null;
            };
            if (rectangle != null) {
                rectangles.add(rectangle);
            }
        }
        return rectangles;
    }

    /**
     * {@return {@code true} when the rectangles together cover the given region}
     */
    private static boolean covers(final List<double[]> rectangles, final double minA, final double minB,
                                  final double maxA, final double maxB) {
        final List<Double> as = edges(rectangles, minA, maxA, 0);
        final List<Double> bs = edges(rectangles, minB, maxB, 1);
        for (int i = 0; i + 1 < as.size(); i++) {
            for (int j = 0; j + 1 < bs.size(); j++) {
                if (as.get(i + 1) - as.get(i) < EPSILON || bs.get(j + 1) - bs.get(j) < EPSILON) {
                    continue;
                }
                final double a = (as.get(i) + as.get(i + 1)) / 2;
                final double b = (bs.get(j) + bs.get(j + 1)) / 2;
                boolean inside = false;
                for (final double[] rectangle : rectangles) {
                    if (rectangle[0] <= a && a <= rectangle[2] && rectangle[1] <= b && b <= rectangle[3]) {
                        inside = true;
                        break;
                    }
                }
                if (!inside) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Double> edges(final List<double[]> rectangles, final double min, final double max, final int axis) {
        final Set<Double> edges = new HashSet<>();
        edges.add(min);
        edges.add(max);
        for (final double[] rectangle : rectangles) {
            edges.add(Math.min(Math.max(rectangle[axis], min), max));
            edges.add(Math.min(Math.max(rectangle[axis + 2], min), max));
        }
        final List<Double> sorted = new ArrayList<>(edges);
        sorted.sort(null);
        return sorted;
    }

    /**
     * {@return the property values of a state of a Prismarine block, the last property varying
     * fastest as in the state identifiers}
     */
    private static Map<String, String> stateValues(final JsonObject block, final int ordinal) {
        final JsonArray properties = block.getAsJsonArray("states");
        final Map<String, String> values = new LinkedHashMap<>();
        int rest = ordinal;
        for (int i = properties.size() - 1; i >= 0; i--) {
            final JsonObject property = properties.get(i).getAsJsonObject();
            final int count = property.get("num_values").getAsInt();
            final JsonArray names = property.getAsJsonArray("values");
            final int index = rest % count;
            rest /= count;
            values.put(property.get("name").getAsString(), names == null
                    ? Boolean.toString(index == 0)
                    : names.get(index).getAsString());
        }
        return values;
    }

    private static JsonElement parse(final Path file) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }

    private static void write(final Path output, final String content) throws IOException {
        final Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, content, StandardCharsets.UTF_8);
    }
}