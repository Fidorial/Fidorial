package fr.fidorial.registrygen.generate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Writes the collision shape of every block state: the boxes entities bump into, stand on and
 * walk through.
 *
 * <p>Built from PrismarineJS's {@code minecraft-data} {@code blockCollisionShapes.json}. A shape is
 * a list of boxes {@code [minX, minY, minZ, maxX, maxY, maxZ]}, in blocks, relative to the corner of
 * the block; most stay within it, a fence or a wall reaches up to 1.5. An empty shape is a block
 * nothing collides with, such as air, water, grass or a flower.</p>
 *
 * <p>The output holds the distinct shapes once, under {@code "shapes"}, the empty one first and the
 * full block second, and maps each block to the index of its shape, or to one index per state, in
 * network order, when its states differ, under {@code "blocks"}.</p>
 *
 * <p>The shapes are those of an entity alone in the world. The few blocks whose collision depends
 * on who touches them, such as powder snow or scaffolding, are left for the server to handle.</p>
 *
 * @since 0.1.0
 */
public final class BlockCollisionDatasetGenerator {

    private static final String NAMESPACE = "minecraft:";

    /**
     * @param prismarineShapes Prismarine's {@code blockCollisionShapes.json}
     * @param prismarineBlocks Prismarine's {@code blocks.json}
     * @param outputFile       the dataset to write, replaced when it exists
     * @return how many blocks were written
     * @throws IOException if an input cannot be read or the dataset cannot be written
     */
    public int generate(final Path prismarineShapes, final Path prismarineBlocks, final Path outputFile) throws IOException {
        Objects.requireNonNull(prismarineShapes, "prismarineShapes");
        Objects.requireNonNull(prismarineBlocks, "prismarineBlocks");
        Objects.requireNonNull(outputFile, "outputFile");

        final JsonObject shapesReport = parse(prismarineShapes).getAsJsonObject();
        final JsonObject blockShapes = shapesReport.getAsJsonObject("blocks");
        final JsonObject shapes = shapesReport.getAsJsonObject("shapes");

        final List<String> written = new ArrayList<>();
        final Map<String, Integer> indexOf = new HashMap<>();
        index(written, indexOf, "[]");
        index(written, indexOf, "[[0,0,0,1,1,1]]");

        final Map<String, List<Integer>> indicesByBlock = new TreeMap<>();
        for (final JsonElement element : parse(prismarineBlocks).getAsJsonArray()) {
            final JsonObject block = element.getAsJsonObject();
            final String name = block.get("name").getAsString();
            final JsonElement shapeIds = blockShapes.get(name);
            if (shapeIds == null) {
                throw new IOException("No collision shape for " + name + " in " + prismarineShapes);
            }
            final int stateCount = block.get("maxStateId").getAsInt() - block.get("minStateId").getAsInt() + 1;
            final List<Integer> indices = new ArrayList<>(stateCount);
            for (int ordinal = 0; ordinal < stateCount; ordinal++) {
                final int shapeId = shapeIds.isJsonArray() ? shapeIds.getAsJsonArray().get(ordinal).getAsInt() : shapeIds.getAsInt();
                final JsonArray boxes = shapes.getAsJsonArray(Integer.toString(shapeId));
                if (boxes == null) {
                    throw new IOException("No shape " + shapeId + " for " + name + " in " + prismarineShapes);
                }
                indices.add(index(written, indexOf, encode(boxes)));
            }
            indicesByBlock.put(NAMESPACE + name, indices);
        }
        if (indicesByBlock.isEmpty()) {
            throw new IOException("No block in " + prismarineBlocks);
        }

        final StringBuilder json = new StringBuilder("{\n  \"shapes\": [\n");
        for (int i = 0; i < written.size(); i++) {
            json.append("    ").append(written.get(i)).append(i + 1 < written.size() ? ",\n" : "\n");
        }
        json.append("  ],\n  \"blocks\": {\n");
        boolean first = true;
        for (final Map.Entry<String, List<Integer>> entry : indicesByBlock.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            final List<Integer> indices = entry.getValue();
            json.append("    \"").append(entry.getKey()).append("\": ");
            json.append(indices.stream().distinct().count() == 1 ? indices.getFirst().toString() : indices.toString().replace(" ", ""));
        }
        json.append("\n  }\n}\n");
        write(outputFile, json.toString());
        return indicesByBlock.size();
    }

    private static int index(final List<String> written, final Map<String, Integer> indexOf, final String shape) {
        return indexOf.computeIfAbsent(shape, key -> {
            written.add(key);
            return written.size() - 1;
        });
    }

    /**
     * {@return a shape as a compact JSON array, its numbers written as briefly as they can be}
     */
    private static String encode(final JsonArray boxes) {
        final StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < boxes.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            final JsonArray box = boxes.get(i).getAsJsonArray();
            out.append('[');
            for (int j = 0; j < 6; j++) {
                if (j > 0) {
                    out.append(',');
                }
                out.append(new BigDecimal(box.get(j).getAsString()).stripTrailingZeros().toPlainString());
            }
            out.append(']');
        }
        return out.append(']').toString();
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