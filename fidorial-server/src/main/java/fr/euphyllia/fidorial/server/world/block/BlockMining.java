package fr.euphyllia.fidorial.server.world.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * How hard each block is to mine, read from the {@code block_mining.json} dataset written by the
 * {@code generateBlockMining} task.
 */
public final class BlockMining {

    public static final String RESOURCE = "/fidorial-data/block_mining.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockMining.class);

    /**
     * The hardness of a block the dataset does not know: it takes time to mine.
     */
    private static final float UNKNOWN_HARDNESS = 1.0f;

    private static volatile Map<TypedKey<BlockType>, Float> hardness = Map.of();

    private BlockMining() {
        throw new UnsupportedOperationException("BlockMining cannot be instantiated.");
    }

    /**
     * Reads the bundled dataset. Without it, no block is mined at once.
     */
    public static void bootstrap() {
        try (final InputStream input = BlockMining.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: no block will be mined at once. "
                        + "Run ':fidorial-server:generateBlockMining'.", RESOURCE);
                return;
            }
            hardness = load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    static Map<TypedKey<BlockType>, Float> load(final Reader reader) {
        final Map<TypedKey<BlockType>, Float> loaded = new HashMap<>();
        final JsonObject blocks = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("blocks");
        if (blocks != null) {
            for (final Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                final JsonElement value = entry.getValue().getAsJsonObject().get("hardness");
                if (value != null) {
                    loaded.put(TypedKey.withRegistry(RegistryKey.BLOCK, entry.getKey()), value.getAsFloat());
                }
            }
        }
        LOGGER.debug("Read the hardness of {} blocks", loaded.size());
        return Map.copyOf(loaded);
    }

    /**
     * {@return the hardness of a block: {@code 0} for one breaking at a touch, negative for one no
     * tool breaks}
     *
     * @param block the block type
     */
    public static float hardness(final TypedKey<BlockType> block) {
        return hardness.getOrDefault(block, UNKNOWN_HARDNESS);
    }

    /**
     * {@return {@code true} if a block breaks as soon as a player starts mining it, whatever they
     * hold}: a crop, a flower, a torch
     *
     * @param block the block type
     */
    public static boolean breaksInstantly(final TypedKey<BlockType> block) {
        return hardness(block) == 0.0f;
    }
}
