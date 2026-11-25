package fr.euphyllia.fidorial.server.world.generation.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.euphyllia.fidorial.server.world.fluid.FluidBlockCodec;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

/**
 * Turns the block states written in world generation data into server states, and answers the questions
 * generation asks about blocks: tags, fluids, air.
 *
 * <p>States are always complete: properties a definition leaves out keep the default value of the block. Safe to
 * share between threads.</p>
 */
public final class BlockLookup {

    private final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> tagged;
    private final Map<String, TagKey<BlockType>> tags = new ConcurrentHashMap<>();
    private final Map<TypedKey<BlockType>, BlockState> defaults = new ConcurrentHashMap<>();

    public final BlockState air;
    public final BlockState caveAir;
    public final BlockState water;
    public final BlockState lava;
    public final BlockState stone;

    /**
     * @param tagged tells whether a block is in a tag, data packs included
     */
    public BlockLookup(final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> tagged) {
        this.tagged = tagged;
        this.air = state(BlockTypeKeys.AIR);
        this.caveAir = state(BlockTypeKeys.CAVE_AIR);
        this.water = state(BlockTypeKeys.WATER);
        this.lava = state(BlockTypeKeys.LAVA);
        this.stone = state(BlockTypeKeys.STONE);
    }

    /**
     * {@return the typed key of a block identifier}
     */
    public static TypedKey<BlockType> key(final String id) {
        return TypedKey.withRegistry(RegistryKey.BLOCK, Key.key(id));
    }

    /**
     * {@return the default state of a block}
     *
     * @throws IllegalArgumentException if the block does not exist
     */
    public BlockState state(final TypedKey<BlockType> block) {
        final BlockState known = defaults.get(block);
        if (known != null) {
            return known;
        }
        final BlockState found = BlockStates.defaultStateOf(block);
        if (found == null) {
            throw new IllegalArgumentException("Unknown block " + block.key().asString());
        }
        defaults.put(block, found);
        return found;
    }

    /**
     * {@return the default state of a block named in the data}
     *
     * @throws IllegalArgumentException if the block does not exist
     */
    public BlockState state(final String id) {
        return state(key(id));
    }

    /**
     * {@return a state read from data: an identifier, or an object with an {@code id} (or {@code Name}) and
     * optional {@code properties} (or {@code Properties})}
     */
    public BlockState parse(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return state(element.getAsString());
        }
        final JsonObject object = element.getAsJsonObject();
        final String id = object.has("id") ? object.get("id").getAsString() : object.get("Name").getAsString();
        final JsonObject properties = object.has("properties") ? object.getAsJsonObject("properties")
                : object.has("Properties") ? object.getAsJsonObject("Properties") : null;
        BlockState state = state(id);
        if (properties != null) {
            final Map<String, String> values = new HashMap<>(state.properties());
            for (final Map.Entry<String, JsonElement> property : properties.entrySet()) {
                if (values.containsKey(property.getKey())) {
                    values.put(property.getKey(), property.getValue().getAsString());
                }
            }
            state = BlockState.withProperties(state.name(), values);
        }
        return state;
    }

    /**
     * {@return a state with one property changed, or the state itself when it has no such property}
     */
    public BlockState with(final BlockState state, final String property, final String value) {
        final String current = state.properties().get(property);
        if (current == null || current.equals(value)) {
            return state;
        }
        final Map<String, String> values = new HashMap<>(state.properties());
        values.put(property, value);
        return BlockState.withProperties(state.name(), values);
    }

    /**
     * {@return the value of a property, or {@code null} when the state has none}
     */
    public static @Nullable String property(final BlockState state, final String property) {
        return state.properties().get(property);
    }

    /**
     * {@return the tag key of a tag identifier, with or without the leading {@code #}}
     */
    public TagKey<BlockType> tag(final String id) {
        return tags.computeIfAbsent(WorldgenData.normalize(id), name -> TagKey.withRegistry(RegistryKey.BLOCK, name));
    }

    public boolean is(final BlockState state, final TagKey<BlockType> tag) {
        return tagged.test(state.name(), tag);
    }

    public boolean is(final BlockState state, final String tag) {
        return is(state, tag(tag));
    }

    public boolean is(final BlockState state, final BlockState block) {
        return state.name().equals(block.name());
    }

    public boolean isAir(final BlockState state) {
        return state.isAir();
    }

    /**
     * {@return {@code true} for water itself, still or flowing}
     */
    public boolean isWaterBlock(final BlockState state) {
        return FluidType.byBlockKey(state.name()) == FluidType.WATER;
    }

    public boolean isLavaBlock(final BlockState state) {
        return FluidType.byBlockKey(state.name()) == FluidType.LAVA;
    }

    /**
     * {@return {@code true} for a source of water or lava, or a block holding a source of water}
     */
    public boolean isSource(final BlockState state) {
        return FluidBlockCodec.fromBlock(state).isSource();
    }
}
