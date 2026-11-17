package fr.euphyllia.fidorial.server.world.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
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
 * Which block each item places, read from the {@code block_items.json} dataset written by the
 * {@code generateBlockItems} task.
 */
public final class BlockItems {

    public static final String RESOURCE = "/fidorial-data/block_items.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockItems.class);

    private static volatile Map<Key, TypedKey<BlockType>> blocks = Map.of();
    private static volatile Map<Key, TypedKey<Item>> items = Map.of();

    private BlockItems() {
        throw new UnsupportedOperationException("BlockItems cannot be instantiated.");
    }

    /**
     * Reads the bundled dataset. Without it, every item places the block of the same name.
     */
    public static void bootstrap() {
        try (final InputStream input = BlockItems.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: items will place the block of the same name. "
                        + "Run ':fidorial-server:generateBlockItems'.", RESOURCE);
                return;
            }
            blocks = load(new InputStreamReader(input, StandardCharsets.UTF_8));
            items = itemsOf(blocks);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    static Map<Key, TypedKey<BlockType>> load(final Reader reader) {
        final Map<Key, TypedKey<BlockType>> loaded = new HashMap<>();
        for (final Map.Entry<String, JsonElement> entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
            final JsonElement block = entry.getValue().getAsJsonObject().get("block");
            if (block != null) {
                loaded.put(Key.key(entry.getKey()), TypedKey.withRegistry(RegistryKey.BLOCK, block.getAsString()));
            }
        }
        LOGGER.debug("Read the blocks of {} items", loaded.size());
        return Map.copyOf(loaded);
    }

    /**
     * {@return the item placing each block}: the item of the same name as the block when several
     * place it
     */
    static Map<Key, TypedKey<Item>> itemsOf(final Map<Key, TypedKey<BlockType>> blocks) {
        final Map<Key, TypedKey<Item>> loaded = new HashMap<>();
        for (final Map.Entry<Key, TypedKey<BlockType>> entry : blocks.entrySet()) {
            final Key block = entry.getValue().key();
            if (!loaded.containsKey(block) || entry.getKey().equals(block)) {
                loaded.put(block, TypedKey.withRegistry(RegistryKey.ITEM, entry.getKey()));
            }
        }
        return Map.copyOf(loaded);
    }

    /**
     * {@return the item that places a block, the item of the same name when the dataset does not
     * say}
     *
     * @param block the block
     */
    public static TypedKey<Item> item(final TypedKey<BlockType> block) {
        final TypedKey<Item> item = items.get(block.key());
        return item != null ? item : TypedKey.withRegistry(RegistryKey.ITEM, block.key());
    }

    /**
     * {@return the block an item places, the block of the same name when the dataset does not say}
     *
     * @param item the item
     */
    public static TypedKey<BlockType> block(final TypedKey<Item> item) {
        final TypedKey<BlockType> block = blocks.get(item.key());
        return block != null ? block : TypedKey.withRegistry(RegistryKey.BLOCK, item.key());
    }
}
