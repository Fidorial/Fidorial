package fr.euphyllia.fidorial.server.world.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.SupportType;
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
 * Which block states conduct redstone power and which of their faces hold other blocks, read from
 * the {@code block_support.json} dataset written by the {@code generateBlockSupport} task.
 */
public final class BlockSupport {

    public static final String RESOURCE = "/fidorial-data/block_support.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockSupport.class);

    private static final int CONDUCTOR = 1;
    private static final int FULL_SHIFT = 1;
    private static final int CENTER_SHIFT = 7;
    private static final int RIGID_SHIFT = 13;

    private static volatile int[] flags = new int[0];

    private BlockSupport() {
        throw new UnsupportedOperationException("BlockSupport cannot be instantiated.");
    }

    /**
     * Reads the bundled dataset. Without it, no block conducts nor holds anything.
     *
     * @param registry the block registry, with every vanilla block registered
     */
    public static void bootstrap(final BlockRegistry registry) {
        try (final InputStream input = BlockSupport.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: no block will conduct redstone power. "
                        + "Run ':fidorial-server:generateBlockSupport'.", RESOURCE);
                return;
            }
            flags = load(new InputStreamReader(input, StandardCharsets.UTF_8), registry);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    static int[] load(final Reader reader, final BlockRegistry registry) {
        final int[] loaded = new int[BlockStateIds.STATE_COUNT];
        final JsonObject dataset = JsonParser.parseReader(reader).getAsJsonObject();
        int blocks = 0;
        for (final Map.Entry<String, JsonElement> entry : dataset.entrySet()) {
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
                final int flag = value.isJsonArray() ? value.getAsJsonArray().get(id - first).getAsInt() : value.getAsInt();
                if (id >= 0 && id < loaded.length) {
                    loaded[id] = flag;
                }
            }
            blocks++;
        }
        LOGGER.debug("Read the support of {} blocks", blocks);
        return loaded;
    }

    /**
     * {@return {@code true} if the state conducts redstone power}
     *
     * @param data the state, {@code null} for a block that is not loaded
     */
    public static boolean isConductor(final @Nullable BlockData data) {
        return (flagsOf(data) & CONDUCTOR) != 0;
    }

    /**
     * {@return {@code true} if a face of the state can hold a block attached to it}
     *
     * @param data    the state, {@code null} for a block that is not loaded
     * @param face    the face, as seen from the block itself
     * @param support how much of the face must be covered
     */
    public static boolean isFaceSturdy(final @Nullable BlockData data, final BlockFace face, final SupportType support) {
        final int shift = switch (support) {
            case FULL -> FULL_SHIFT;
            case CENTER -> CENTER_SHIFT;
            case RIGID -> RIGID_SHIFT;
        };
        return (flagsOf(data) & (1 << (shift + faceIndex(face)))) != 0;
    }

    /**
     * {@return the index of a face in the flags of the dataset: down, up, north, south, west, east}
     */
    private static int faceIndex(final BlockFace face) {
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    private static int flagsOf(final @Nullable BlockData data) {
        if (data == null) {
            return 0;
        }
        final int[] current = flags;
        final int id = data.networkId();
        return id >= 0 && id < current.length ? current[id] : 0;
    }
}
