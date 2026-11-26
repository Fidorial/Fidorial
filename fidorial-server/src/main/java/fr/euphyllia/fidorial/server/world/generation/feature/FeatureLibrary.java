package fr.euphyllia.fidorial.server.world.generation.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.feature.kind.FeatureKinds;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turns the features, placements, block tests and state sources of the data into objects, each named entry
 * once.
 *
 * <p>Thread-safe: the named entries are built under a lock, then shared.</p>
 */
public final class FeatureLibrary {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FeatureLibrary.class);

    /**
     * Builds a feature of one type from its data.
     */
    @FunctionalInterface
    public interface Parser {
        Feature parse(FeatureLibrary library, JsonObject data);
    }

    private final DecorationContext context;
    private final Map<String, Parser> parsers;
    private final Map<String, PlacedFeature> placed = new ConcurrentHashMap<>();
    private final Map<Key, Feature> configured = new ConcurrentHashMap<>();
    private final Map<String, StateSource> namedSources = new ConcurrentHashMap<>();
    private final Set<String> reported = ConcurrentHashMap.newKeySet();

    FeatureLibrary(final DecorationContext context) {
        this.context = context;
        this.parsers = FeatureKinds.parsers();
    }

    public DecorationContext context() {
        return context;
    }

    /**
     * Warns once that a type is not known; what it stands for then does nothing.
     */
    public void unsupported(final String what, final String type) {
        if (reported.add(what + ' ' + type)) {
            LOGGER.warn("Unknown {} type {}: it is left out", what, type);
        }
    }

    /**
     * {@return a placed feature of the data, by identifier}
     */
    public PlacedFeature placed(final String id) {
        final String key = WorldgenData.normalize(id);
        final PlacedFeature known = placed.get(key);
        return known != null ? known : buildPlaced(key);
    }

    private synchronized PlacedFeature buildPlaced(final String key) {
        final PlacedFeature known = placed.get(key);
        if (known != null) {
            return known;
        }
        PlacedFeature built;
        try {
            final JsonObject data = context.data().get(WorldgenData.Section.PLACED_FEATURE, key).getAsJsonObject();
            built = new PlacedFeature(key, feature(data.get("feature")), Placements.parseAll(this, data.get("placement")));
        } catch (final RuntimeException exception) {
            LOGGER.warn("The placed feature {} cannot be read: it is left out", key, exception);
            built = new PlacedFeature(key, Feature.NOTHING, List.of());
        }
        placed.put(key, built);
        return built;
    }

    /**
     * {@return a placed feature written inline, or named}
     */
    public PlacedFeature placedFeature(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return placed(element.getAsString());
        }
        final JsonObject data = element.getAsJsonObject();
        return new PlacedFeature("", feature(data.get("feature")), Placements.parseAll(this, data.get("placement")));
    }

    /**
     * {@return a feature written inline, or named}
     */
    public synchronized Feature feature(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return feature(Key.key(element.getAsString()));
        }
        final JsonObject data = element.getAsJsonObject();
        final String type = Values.type(data);
        final Parser parser = parsers.get(type);
        if (parser == null) {
            unsupported("feature", type);
            return Feature.NOTHING;
        }
        final JsonObject config = data.has("config") && data.get("config").isJsonObject() ? data.getAsJsonObject("config") : data;
        return parser.parse(this, config);
    }

    /**
     * {@return a configured feature named in the data, built once}
     */
    public synchronized Feature feature(final Key id) {
        final Feature known = configured.get(id);
        if (known != null) {
            return known;
        }
        final Feature built = feature(context.data().get(WorldgenData.Section.FEATURE, id.asString()));
        configured.put(id, built);
        return built;
    }

    public List<Placement> placements(final JsonElement element) {
        return Placements.parseAll(this, element);
    }

    public BlockTest test(final JsonElement element) {
        return element == null ? BlockTest.ALWAYS : BlockTests.parse(this, element);
    }

    /**
     * {@return a state source written inline, named in the providers of the data, or a bare state}
     */
    public StateSource source(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            final String key = WorldgenData.normalize(element.getAsString());
            final JsonElement named = context.data().find(WorldgenData.Section.BLOCK_STATE_PROVIDER, key);
            if (named != null) {
                StateSource known = namedSources.get(key);
                if (known == null) {
                    known = StateSources.parse(this, named);
                    namedSources.put(key, known);
                }
                return known;
            }
        }
        return StateSources.parse(this, element);
    }

    /**
     * {@return the number of a state written in the data}
     */
    public char state(final JsonElement element) {
        final BlockState state = context.blocks().parse(element);
        return context.states().palette().id(state);
    }

    /**
     * {@return a test of the blocks of a set written in the data: one block, a list, or a tag}
     */
    public StateFlags.Membership blockSet(final JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsString().startsWith("#")) {
            return context.states().membership(context.blocks().tag(element.getAsString()));
        }
        final Set<TypedKey<BlockType>> ids = new HashSet<>();
        if (element.isJsonArray()) {
            for (final JsonElement entry : element.getAsJsonArray()) {
                ids.add(BlockLookup.key(entry.getAsString()));
            }
        } else {
            ids.add(BlockLookup.key(element.getAsString()));
        }
        return context.states().membership(ids);
    }

    /**
     * {@return the default states of the blocks of a set written in the data, in the order it lists them}
     */
    public char[] blockList(final JsonElement element) {
        final List<String> ids = new ArrayList<>();
        if (element.isJsonArray()) {
            for (final JsonElement entry : element.getAsJsonArray()) {
                ids.addAll(blockIds(entry.getAsString()));
            }
        } else {
            ids.addAll(blockIds(element.getAsString()));
        }
        final char[] states = new char[ids.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = context.states().palette().id(context.blocks().state(ids.get(i)));
        }
        return states;
    }

    private List<String> blockIds(final String entry) {
        if (entry.startsWith("#")) {
            return context.data().tag(WorldgenData.Section.BLOCK_TAG, entry.substring(1));
        }
        return List.of(WorldgenData.normalize(entry));
    }

    /**
     * {@return the biomes of a set written in the data: one biome, a list, or a tag}
     */
    public Set<String> biomeSet(final JsonElement element) {
        final Set<String> ids = new LinkedHashSet<>();
        final List<JsonElement> entries = new ArrayList<>();
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(entries::add);
        } else {
            entries.add(element);
        }
        for (final JsonElement entry : entries) {
            final String id = entry.getAsString();
            if (id.startsWith("#")) {
                ids.addAll(context.data().tag(WorldgenData.Section.BIOME_TAG, id.substring(1)));
            } else {
                ids.add(WorldgenData.normalize(id));
            }
        }
        return ids;
    }
}
