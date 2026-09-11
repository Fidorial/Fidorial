package fr.euphyllia.fidorial.server.world.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.datapack.DatapackContents;
import fr.euphyllia.fidorial.server.datapack.TagFile;
import fr.euphyllia.fidorial.server.world.structure.pool.PoolElement;
import fr.euphyllia.fidorial.server.world.structure.pool.ProcessorRef;
import fr.euphyllia.fidorial.server.world.structure.pool.Projection;
import fr.euphyllia.fidorial.server.world.structure.pool.TemplatePool;
import fr.euphyllia.fidorial.server.world.structure.pool.TemplateSource;
import fr.euphyllia.fidorial.server.world.structure.processor.ProcessorList;
import fr.euphyllia.fidorial.server.world.structure.processor.ProcessorParser;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import fr.euphyllia.fidorial.server.world.structure.template.TemplateParser;
import fr.euphyllia.fidorial.server.world.structure.worldgen.HeightProvider;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;
import fr.euphyllia.fidorial.server.world.structure.worldgen.JigsawStructure;
import fr.euphyllia.fidorial.server.world.structure.worldgen.PoolAliasBinding;
import fr.euphyllia.fidorial.server.world.structure.worldgen.StructurePlacement;
import fr.euphyllia.fidorial.server.world.structure.worldgen.StructureSet;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainAdaptation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public final class StructureRegistry implements TemplateSource {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(StructureRegistry.class);

    private final DatapackContents contents;
    private final BlockValidator validator;
    private final DataProblems problems;
    private final Map<Key, Set<Key>> biomeTags = new ConcurrentHashMap<>();
    private final Map<Key, Set<Key>> blockTags = new ConcurrentHashMap<>();
    private final Map<Key, Optional<StructureTemplateImpl>> templates = new ConcurrentHashMap<>();
    private final Map<Key, ProcessorList> processorLists = new LinkedHashMap<>();
    private final Map<Key, TemplatePool> pools = new LinkedHashMap<>();
    private final Map<Key, JigsawStructure> structures = new LinkedHashMap<>();
    private final List<StructureSet> sets = new ArrayList<>();
    private final Map<Key, StructureSet> setsById = new LinkedHashMap<>();
    private final ProcessorParser processorParser;

    private StructureRegistry(final DatapackContents contents, final BlockValidator validator, final DataProblems problems) {
        this.contents = contents;
        this.validator = validator;
        this.problems = problems;
        this.processorParser = new ProcessorParser(validator, this::blockTag, problems);
    }

    public static StructureRegistry empty(final BlockValidator validator) {
        return build(DatapackContents.empty(), validator);
    }

    public static StructureRegistry build(final DatapackContents contents, final BlockValidator validator) {
        final StructureRegistry registry = new StructureRegistry(contents, validator, new DataProblems());
        registry.parseAll();
        return registry;
    }

    private void parseAll() {
        processorLists.put(Keys.EMPTY, ProcessorList.EMPTY);
        for (final Map.Entry<Key, JsonElement> entry : contents.processorLists().entrySet()) {
            processorLists.put(entry.getKey(), processorParser.parseList(entry.getValue(), entry.getKey().asString()));
        }

        pools.put(Keys.EMPTY, new TemplatePool(Keys.EMPTY, Keys.EMPTY, List.of()));
        for (final Map.Entry<Key, JsonElement> entry : contents.templatePools().entrySet()) {
            try {
                pools.put(entry.getKey(), parsePool(entry.getKey(), entry.getValue().getAsJsonObject()));
            } catch (final RuntimeException invalid) {
                problems.warn("Template pool " + entry.getKey().asString() + " is invalid: " + invalid.getMessage());
            }
        }

        final Set<String> unsupported = new TreeSet<>();
        for (final Map.Entry<Key, JsonElement> entry : contents.structures().entrySet()) {
            try {
                final JsonObject object = entry.getValue().getAsJsonObject();
                final String type = string(object, "type", "").replace("minecraft:", "");
                if (!type.equals("jigsaw")) {
                    unsupported.add(entry.getKey().asString() + " (minecraft:" + type + ")");
                    continue;
                }
                structures.put(entry.getKey(), parseStructure(entry.getKey(), object));
            } catch (final RuntimeException invalid) {
                problems.warn("Structure " + entry.getKey().asString() + " is invalid: " + invalid.getMessage());
            }
        }
        if (!unsupported.isEmpty()) {
            LOGGER.warn("[datapacks] Only jigsaw structures are generated; skipped: {}", String.join(", ", unsupported));
        }

        final List<Map.Entry<Key, JsonElement>> setEntries = new ArrayList<>(contents.structureSets().entrySet());
        setEntries.sort(Comparator.comparing(entry -> entry.getKey().asString()));
        for (final Map.Entry<Key, JsonElement> entry : setEntries) {
            try {
                final StructureSet set = parseSet(entry.getKey(), entry.getValue().getAsJsonObject());
                setsById.put(set.id(), set);
                if (!(set.placement() instanceof StructurePlacement.RandomSpread)) {
                    problems.warn("Structure set " + set.id().asString() + " uses an unsupported placement ("
                            + ((StructurePlacement.Unsupported) set.placement()).type() + "), it is not generated");
                    continue;
                }
                final List<StructureSet.Entry> usable = new ArrayList<>();
                for (final StructureSet.Entry structure : set.structures()) {
                    if (structures.containsKey(structure.structure())) {
                        usable.add(structure);
                    }
                }
                if (!usable.isEmpty()) {
                    sets.add(new StructureSet(set.id(), usable, set.placement()));
                }
            } catch (final RuntimeException invalid) {
                problems.warn("Structure set " + entry.getKey().asString() + " is invalid: " + invalid.getMessage());
            }
        }
    }

    private TemplatePool parsePool(final Key id, final JsonObject object) {
        final Key fallback = Keys.parseOr(string(object, "fallback", "minecraft:empty"), Keys.EMPTY);
        final List<PoolElement> expanded = new ArrayList<>();
        final JsonArray elements = object.has("elements") ? object.getAsJsonArray("elements") : new JsonArray();
        for (final JsonElement element : elements) {
            final JsonObject weighted = element.getAsJsonObject();
            final int weight = Math.clamp(weighted.has("weight") ? weighted.get("weight").getAsInt() : 1, 1, 150);
            final PoolElement parsed = parseElement(weighted.getAsJsonObject("element"), id, null);
            if (parsed == null) {
                continue;
            }
            for (int i = 0; i < weight; i++) {
                expanded.add(parsed);
            }
        }
        return new TemplatePool(id, fallback, expanded);
    }

    private @Nullable PoolElement parseElement(final JsonObject object, final Key pool, final @Nullable Projection forced) {
        final String type = string(object, "element_type", "minecraft:single_pool_element").replace("minecraft:", "");
        final Projection projection = forced != null ? forced : Projection.byName(string(object, "projection", "rigid"));
        return switch (type) {
            case "single_pool_element", "legacy_single_pool_element" -> {
                final Key location = Keys.parse(string(object, "location", ""));
                if (location == null) {
                    problems.warn("An element of pool " + pool.asString() + " has no valid location");
                    yield null;
                }
                yield new PoolElement.Single(location, processorRef(object.get("processors"), pool), projection,
                        type.startsWith("legacy"));
            }
            case "list_pool_element" -> {
                final List<PoolElement> children = new ArrayList<>();
                for (final JsonElement child : object.getAsJsonArray("elements")) {
                    final PoolElement parsed = parseElement(child.getAsJsonObject(), pool, projection);
                    if (parsed != null) {
                        children.add(parsed);
                    }
                }
                yield new PoolElement.ListOf(children, projection);
            }
            case "feature_pool_element" -> new PoolElement.Feature(
                    Keys.parseOr(string(object, "feature", "minecraft:empty"), Keys.EMPTY), projection);
            case "empty_pool_element" -> PoolElement.Empty.INSTANCE;
            default -> {
                problems.warn("Pool element type minecraft:" + type + " (pool " + pool.asString() + ") is not supported");
                yield null;
            }
        };
    }

    private ProcessorRef processorRef(final @Nullable JsonElement element, final Key pool) {
        if (element == null || element.isJsonNull()) {
            return ProcessorRef.EMPTY;
        }
        if (element.isJsonPrimitive()) {
            final Key id = Keys.parse(element.getAsString());
            return id == null ? ProcessorRef.EMPTY : new ProcessorRef(id, null);
        }
        return new ProcessorRef(null, processorParser.parseList(element, "pool " + pool.asString()));
    }

    private JigsawStructure parseStructure(final Key id, final JsonObject object) {
        final Set<Key> biomes = biomeSet(object.get("biomes"));
        final Key startPool = Keys.parse(string(object, "start_pool", ""));
        if (startPool == null) {
            throw new IllegalArgumentException("missing start_pool");
        }
        final JsonElement maxDistance = object.get("max_distance_from_center");
        int horizontal = 80;
        int vertical = 80;
        if (maxDistance != null && maxDistance.isJsonPrimitive()) {
            horizontal = maxDistance.getAsInt();
            vertical = horizontal;
        } else if (maxDistance != null && maxDistance.isJsonObject()) {
            horizontal = maxDistance.getAsJsonObject().get("horizontal").getAsInt();
            vertical = maxDistance.getAsJsonObject().has("vertical")
                    ? maxDistance.getAsJsonObject().get("vertical").getAsInt() : horizontal;
        }
        int paddingBottom = 0;
        int paddingTop = 0;
        final JsonElement padding = object.get("dimension_padding");
        if (padding != null && padding.isJsonPrimitive()) {
            paddingBottom = paddingTop = padding.getAsInt();
        } else if (padding != null && padding.isJsonObject()) {
            paddingBottom = padding.getAsJsonObject().has("bottom") ? padding.getAsJsonObject().get("bottom").getAsInt() : 0;
            paddingTop = padding.getAsJsonObject().has("top") ? padding.getAsJsonObject().get("top").getAsInt() : 0;
        }
        final List<PoolAliasBinding> aliases = new ArrayList<>();
        if (object.has("pool_aliases")) {
            for (final JsonElement binding : object.getAsJsonArray("pool_aliases")) {
                final PoolAliasBinding parsed = aliasBinding(binding.getAsJsonObject());
                if (parsed != null) {
                    aliases.add(parsed);
                }
            }
        }
        return new JigsawStructure(
                id,
                biomes,
                startPool,
                Keys.parse(string(object, "start_jigsaw_name", "")),
                Math.clamp(object.has("size") ? object.get("size").getAsInt() : 1, 0, 20),
                object.has("start_height") ? HeightProvider.parse(object.get("start_height")) : (random, minY, height) -> 0,
                object.has("project_start_to_heightmap")
                        ? Heightmap.byName(object.get("project_start_to_heightmap").getAsString(), Heightmap.WORLD_SURFACE_WG)
                        : null,
                horizontal,
                vertical,
                object.has("use_expansion_hack") && object.get("use_expansion_hack").getAsBoolean(),
                TerrainAdaptation.byName(string(object, "terrain_adaptation", "none")),
                !"ignore_waterlogging".equals(string(object, "liquid_settings", "apply_waterlogging")),
                paddingBottom,
                paddingTop,
                aliases);
    }

    private @Nullable PoolAliasBinding aliasBinding(final JsonObject object) {
        final String type = string(object, "type", "minecraft:direct").replace("minecraft:", "");
        return switch (type) {
            case "direct" -> new PoolAliasBinding.Direct(
                    Keys.parseOr(string(object, "alias", ""), Keys.EMPTY),
                    Keys.parseOr(string(object, "target", ""), Keys.EMPTY));
            case "random" -> {
                final List<Key> targets = new ArrayList<>();
                final List<Integer> weights = new ArrayList<>();
                for (final JsonElement target : object.getAsJsonArray("targets")) {
                    targets.add(Keys.parseOr(string(target.getAsJsonObject(), "data", ""), Keys.EMPTY));
                    weights.add(target.getAsJsonObject().get("weight").getAsInt());
                }
                yield new PoolAliasBinding.RandomTarget(Keys.parseOr(string(object, "alias", ""), Keys.EMPTY), targets, weights);
            }
            case "random_group" -> {
                final List<List<PoolAliasBinding>> groups = new ArrayList<>();
                final List<Integer> weights = new ArrayList<>();
                for (final JsonElement group : object.getAsJsonArray("groups")) {
                    final List<PoolAliasBinding> bindings = new ArrayList<>();
                    for (final JsonElement binding : group.getAsJsonObject().getAsJsonArray("data")) {
                        final PoolAliasBinding parsed = aliasBinding(binding.getAsJsonObject());
                        if (parsed != null) {
                            bindings.add(parsed);
                        }
                    }
                    groups.add(bindings);
                    weights.add(group.getAsJsonObject().get("weight").getAsInt());
                }
                yield new PoolAliasBinding.RandomGroup(groups, weights);
            }
            default -> null;
        };
    }

    private StructureSet parseSet(final Key id, final JsonObject object) {
        final List<StructureSet.Entry> entries = new ArrayList<>();
        if (object.has("structures")) {
            for (final JsonElement element : object.getAsJsonArray("structures")) {
                final JsonObject entry = element.getAsJsonObject();
                final Key structure = Keys.parse(string(entry, "structure", ""));
                if (structure != null) {
                    entries.add(new StructureSet.Entry(structure, Math.max(1, entry.has("weight") ? entry.get("weight").getAsInt() : 1)));
                }
            }
        } else if (object.has("structure")) {
            final Key structure = Keys.parse(string(object, "structure", ""));
            if (structure != null) {
                entries.add(new StructureSet.Entry(structure, 1));
            }
        }
        final JsonObject placement = object.getAsJsonObject("placement");
        final String type = string(placement, "type", "").replace("minecraft:", "");
        final StructurePlacement parsed;
        if (type.equals("random_spread")) {
            final int spacing = placement.get("spacing").getAsInt();
            final int separation = placement.get("separation").getAsInt();
            if (spacing <= separation) {
                throw new IllegalArgumentException("spacing must be greater than separation");
            }
            Key exclusionSet = null;
            int exclusionChunks = 0;
            if (placement.has("exclusion_zone")) {
                final JsonObject zone = placement.getAsJsonObject("exclusion_zone");
                exclusionSet = Keys.parse(string(zone, "other_set", ""));
                exclusionChunks = zone.get("chunk_count").getAsInt();
            }
            parsed = new StructurePlacement.RandomSpread(
                    spacing,
                    separation,
                    "triangular".equals(string(placement, "spread_type", "linear")),
                    placement.has("salt") ? placement.get("salt").getAsInt() : 0,
                    placement.has("frequency") ? placement.get("frequency").getAsFloat() : 1.0F,
                    exclusionSet,
                    exclusionChunks);
        } else {
            parsed = new StructurePlacement.Unsupported("minecraft:" + type);
        }
        return new StructureSet(id, entries, parsed);
    }

    private Set<Key> biomeSet(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Set.of();
        }
        if (element.isJsonArray()) {
            final Set<Key> result = new HashSet<>();
            for (final JsonElement child : element.getAsJsonArray()) {
                result.addAll(biomeSet(child));
            }
            return result;
        }
        final String raw = element.getAsString();
        if (raw.startsWith("#")) {
            final Key tag = Keys.parse(raw.substring(1));
            return tag == null ? Set.of() : biomeTag(tag);
        }
        final Key key = Keys.parse(raw);
        return key == null ? Set.of() : Set.of(key);
    }

    public Set<Key> biomeTag(final Key tag) {
        return resolveTag(contents.biomeTags(), biomeTags, tag, new HashSet<>(), "biome");
    }

    public Set<Key> blockTag(final Key tag) {
        return resolveTag(contents.blockTags(), blockTags, tag, new HashSet<>(), "block");
    }

    private Set<Key> resolveTag(final Map<Key, List<TagFile>> files, final Map<Key, Set<Key>> cache, final Key tag,
                                final Set<Key> visiting, final String kind) {
        final Set<Key> cached = cache.get(tag);
        if (cached != null) {
            return cached;
        }
        final List<TagFile> definitions = files.get(tag);
        if (definitions == null) {
            problems.warn("The " + kind + " tag #" + tag.asString() + " is not defined by any datapack; it is treated as empty");
            cache.put(tag, Set.of());
            return Set.of();
        }
        if (!visiting.add(tag)) {
            problems.warn("The " + kind + " tag #" + tag.asString() + " references itself");
            return Set.of();
        }
        final Set<Key> result = new LinkedHashSet<>();
        for (final TagFile file : definitions) {
            if (file.replace()) {
                result.clear();
            }
            for (final TagFile.Entry entry : file.values()) {
                if (entry.isTag()) {
                    final Key nested = Keys.parse(entry.reference().substring(1));
                    if (nested != null && (entry.required() || files.containsKey(nested))) {
                        result.addAll(resolveTag(files, cache, nested, visiting, kind));
                    }
                } else {
                    final Key key = Keys.parse(entry.reference());
                    if (key != null) {
                        result.add(key);
                    }
                }
            }
        }
        visiting.remove(tag);
        final Set<Key> frozen = Collections.unmodifiableSet(result);
        cache.put(tag, frozen);
        return frozen;
    }

    @Override
    public @Nullable StructureTemplateImpl template(final Key key) {
        return templates.computeIfAbsent(key, this::loadTemplate).orElse(null);
    }

    private Optional<StructureTemplateImpl> loadTemplate(final Key key) {
        final byte[] data = contents.templates().get(key);
        if (data == null) {
            problems.warn("Structure template " + key.asString() + " is not provided by any datapack");
            return Optional.empty();
        }
        try {
            final Set<String> unknown = new TreeSet<>();
            final StructureTemplateImpl template = TemplateParser.parse(key, data, validator, unknown);
            if (!unknown.isEmpty()) {
                problems.warn("Template " + key.asString() + " uses blocks this server does not know, they are left out: "
                        + String.join(", ", unknown));
            }
            return Optional.of(template);
        } catch (final IOException | RuntimeException failure) {
            problems.warn("Template " + key.asString() + " could not be read: " + failure);
            return Optional.empty();
        }
    }

    @Override
    public ProcessorList processorList(final ProcessorRef ref) {
        if (ref.inline() != null) {
            return ref.inline();
        }
        final Key id = ref.id();
        if (id == null) {
            return ProcessorList.EMPTY;
        }
        final ProcessorList list = processorLists.get(id);
        if (list == null) {
            problems.warn("Processor list " + id.asString() + " is not provided by any datapack; no processor is applied");
            return ProcessorList.EMPTY;
        }
        return list;
    }

    @Override
    public @Nullable TemplatePool pool(final Key key) {
        return pools.get(key);
    }

    public @Nullable JigsawStructure structure(final Key key) {
        return structures.get(key);
    }

    public Map<Key, JigsawStructure> structures() {
        return Collections.unmodifiableMap(structures);
    }

    public List<StructureSet> structureSets() {
        return Collections.unmodifiableList(sets);
    }

    public @Nullable StructureSet structureSet(final Key id) {
        return setsById.get(id);
    }

    public Set<Key> templateKeys() {
        return contents.templates().keySet();
    }

    public DatapackContents contents() {
        return contents;
    }

    public BlockValidator validator() {
        return validator;
    }

    public DataProblems problems() {
        return problems;
    }

    private static String string(final JsonObject object, final String member, final String fallback) {
        final JsonElement element = object.get(member);
        return element == null || element.isJsonNull() ? fallback : element.getAsString();
    }

    public boolean isEmpty() {
        return contents.packs().isEmpty();
    }
}
