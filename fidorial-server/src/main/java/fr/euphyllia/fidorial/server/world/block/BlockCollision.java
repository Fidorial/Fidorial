package fr.euphyllia.fidorial.server.world.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * The collision shape of every block state, read from the {@code block_collision.json} dataset
 * written by the {@code generateBlockCollision} task.
 *
 * <p>A block the dataset does not know, such as one a plugin adds, is a full block: what it does not
 * know it treats as a wall rather than as a hole.</p>
 */
public final class BlockCollision {

    public static final String RESOURCE = "/fidorial-data/block_collision.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockCollision.class);

    private static volatile @Nullable CollisionShape[] shapes = new CollisionShape[0];

    private BlockCollision() {
        throw new UnsupportedOperationException("BlockCollision cannot be instantiated.");
    }

    /**
     * Reads the bundled dataset. Without it, every block but air is a full block.
     *
     * @param registry the block registry, with every vanilla block registered
     */
    public static void bootstrap(final BlockRegistry registry) {
        try (final InputStream input = BlockCollision.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: every block will be a full block to entities. "
                        + "Run ':fidorial-server:generateBlockCollision'.", RESOURCE);
                return;
            }
            shapes = load(new InputStreamReader(input, StandardCharsets.UTF_8), registry);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    static @Nullable CollisionShape[] load(final Reader reader, final BlockRegistry registry) {
        final JsonObject dataset = JsonParser.parseReader(reader).getAsJsonObject();
        final JsonArray shapeList = dataset.getAsJsonArray("shapes");
        final CollisionShape[] distinct = new CollisionShape[shapeList.size()];
        for (int index = 0; index < distinct.length; index++) {
            final JsonArray boxes = shapeList.get(index).getAsJsonArray();
            final double[] values = new double[boxes.size() * 6];
            for (int box = 0; box < boxes.size(); box++) {
                final JsonArray bounds = boxes.get(box).getAsJsonArray();
                for (int axis = 0; axis < 6; axis++) {
                    values[box * 6 + axis] = bounds.get(axis).getAsDouble();
                }
            }
            distinct[index] = CollisionShape.of(values);
        }

        final @Nullable CollisionShape[] loaded = new CollisionShape[BlockStateIds.STATE_COUNT];
        int blocks = 0;
        for (final Map.Entry<String, JsonElement> entry : dataset.getAsJsonObject("blocks").entrySet()) {
            final BlockType type = registry.type(TypedKey.withRegistry(RegistryKey.BLOCK, entry.getKey())).orElse(null);
            if (type == null) {
                continue;
            }
            final int[] ids = new int[type.stateCount()];
            int first = Integer.MAX_VALUE;
            for (int ordinal = 0; ordinal < ids.length; ordinal++) {
                ids[ordinal] = type.stateAt(ordinal).networkId();
                first = Math.min(first, ids[ordinal]);
            }
            final JsonElement value = entry.getValue();
            for (final int id : ids) {
                // the dataset lists the states in network order
                final int shape = value.isJsonArray() ? value.getAsJsonArray().get(id - first).getAsInt() : value.getAsInt();
                if (id >= 0 && id < loaded.length && shape >= 0 && shape < distinct.length) {
                    loaded[id] = distinct[shape];
                }
            }
            blocks++;
        }
        LOGGER.debug("Read the collision shapes of {} blocks", blocks);
        return loaded;
    }

    /**
     * {@return the shape entities bump into in a block state, alone in the world}
     */
    public static CollisionShape shape(final BlockState state) {
        if (state.isAir()) {
            return CollisionShape.EMPTY;
        }
        final CollisionShape cached = state.collisionShape();
        if (cached != null) {
            return cached;
        }
        final @Nullable CollisionShape[] current = shapes;
        if (current.length == 0) {
            return CollisionShape.FULL_BLOCK; // not bootstrapped yet: not cached either
        }
        final BlockData data = data(state);
        final int id = data == null ? -1 : data.networkId();
        final CollisionShape found = id >= 0 && id < current.length ? current[id] : null;
        final CollisionShape shape = found != null ? found : CollisionShape.FULL_BLOCK;
        state.setCollisionShape(shape);
        return shape;
    }

    private static @Nullable BlockData data(final BlockState state) {
        final BlockData cached = state.data();
        if (cached != null) {
            return cached;
        }
        try {
            return Blocks.registry().type(state.name()).map(type -> type.dataOrNull(state.properties())).orElse(null);
        } catch (final IllegalStateException _) {
            return null; // the registry is not bootstrapped yet
        }
    }
}
