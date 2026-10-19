package fr.euphyllia.fidorial.server.map;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.map.MapColor;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The color terrain maps draw each block with.
 *
 * <p>Vanilla colors come from {@code map/block_colors.txt}: one line per block, its color id, then
 * the states that use another color ({@code axis=x:34}). A {@link BlockBehaviour} can override them.
 * Results are cached on the interned {@link BlockState}.</p>
 */
public final class BlockMapColors {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockMapColors.class);
    private static final String RESOURCE = "/map/block_colors.txt";
    private static final BlockMapColors INSTANCE = load();

    private final Map<String, Entry> vanilla;
    private final Set<Key> warned = ConcurrentHashMap.newKeySet();

    private BlockMapColors(final Map<String, Entry> vanilla) {
        this.vanilla = vanilla;
    }

    public static BlockMapColors get() {
        return INSTANCE;
    }

    private static BlockMapColors load() {
        try (final InputStream in = BlockMapColors.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + RESOURCE);
            }
            return new BlockMapColors(parse(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))));
        } catch (final IOException e) {
            throw new UncheckedIOException("Reading " + RESOURCE + " failed", e);
        }
    }

    static Map<String, Entry> parse(final BufferedReader reader) throws IOException {
        final Map<String, Entry> entries = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            final String[] parts = line.split(" ");
            final List<Rule> rules = new ArrayList<>(parts.length - 2);
            for (int i = 2; i < parts.length; i++) {
                final int equals = parts[i].indexOf('=');
                final int colon = parts[i].lastIndexOf(':');
                rules.add(new Rule(parts[i].substring(0, equals), parts[i].substring(equals + 1, colon),
                        color(Integer.parseInt(parts[i].substring(colon + 1)))));
            }
            entries.put(parts[0], new Entry(color(Integer.parseInt(parts[1])), List.copyOf(rules)));
        }
        return Map.copyOf(entries);
    }

    private static MapColor color(final int id) {
        if (id < 0 || id >= MapColor.values().length) {
            throw new IllegalArgumentException("Unknown map color " + id);
        }
        return MapColor.values()[id];
    }

    /**
     * {@return the color of a state, cached on it}
     *
     * @param state the state
     */
    public MapColor of(final BlockState state) {
        final int cached = state.mapColor();
        if (cached >= 0) {
            return MapColor.values()[cached];
        }
        final MapColor color = state.isAir() ? MapColor.NONE : resolve(state.name(), state.properties());
        state.setMapColor(color.id());
        return color;
    }

    /**
     * {@return the color of a state of the API}
     *
     * @param data the state
     */
    public MapColor of(final BlockData data) {
        if (data.isAir()) {
            return MapColor.NONE;
        }
        final MapColor custom = custom(data);
        return custom != null ? custom : vanilla(data.key(), data.propertyMap());
    }

    private MapColor resolve(final Key name, final Map<String, String> properties) {
        final BlockData data = data(name, properties);
        if (data != null) {
            final MapColor custom = custom(data);
            if (custom != null) {
                return custom;
            }
        }
        return vanilla(name, properties);
    }

    private static @Nullable BlockData data(final Key name, final Map<String, String> properties) {
        try {
            final BlockRegistry registry = Blocks.registry();
            return registry.type(name).map(type -> type.dataOrNull(properties)).orElse(null);
        } catch (final IllegalStateException _) {
            return null; // registry not bootstrapped, tests
        }
    }

    private static @Nullable MapColor custom(final BlockData data) {
        try {
            final BlockBehaviour behaviour = Blocks.registry().behaviour(data.key()).orElse(null);
            return behaviour == null ? null : behaviour.mapColor(data);
        } catch (final IllegalStateException _) {
            return null;
        }
    }

    /**
     * {@return the vanilla color of a state}
     *
     * @param name       the block
     * @param properties the properties of the state
     */
    public MapColor vanilla(final Key name, final Map<String, String> properties) {
        final Entry entry = Key.MINECRAFT_NAMESPACE.equals(name.namespace()) ? vanilla.get(name.value()) : null;
        if (entry == null) {
            if (warned.add(name)) {
                LOGGER.debug("No map color for block {}, drawing it as stone", name.asString());
            }
            return MapColor.STONE;
        }
        for (final Rule rule : entry.rules()) {
            if (rule.value().equals(properties.get(rule.property()))) {
                return rule.color();
            }
        }
        return entry.base();
    }

    record Entry(MapColor base, List<Rule> rules) {
    }

    record Rule(String property, String value, MapColor color) {
    }
}
