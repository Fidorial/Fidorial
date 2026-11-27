package fr.euphyllia.fidorial.server.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.PropertyValue;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Codecs for block states, and the property and value names they are written with.
 */
public final class BlockStateCodecs {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockStateCodecs.class);
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static final Codec<Map<String, String>> PROPERTIES = Codec.unboundedMap(Codec.STRING, Codec.STRING)
            .orElse((Consumer<String>) error -> warnOnce("properties:" + error, "Ignored malformed block state properties: {}", error), Map.of());

    private static final Codec<Definition> DEFINITION = RecordCodec.builder(Definition.class)
            .required("id", Definition::id, ReferenceCodecs.key(RegistryKey.BLOCK))
            .field("properties", Definition::properties, PROPERTIES, Map.of())
            .build();

    private static final Codec<Definition> NBT_DEFINITION = RecordCodec.builder(Definition.class)
            .keys(RecordCodec.KeyStyle.UNCHECKED)
            .required("Name", Definition::id, ReferenceCodecs.key(RegistryKey.BLOCK))
            .field("Properties", Definition::properties, PROPERTIES, Map.of())
            .build();

    /**
     * A state as data files write it: a block id for the default state, or {@code {id, properties}}.
     */
    public static final Codec<BlockData> STATE = Codec.either(ReferenceCodecs.key(RegistryKey.BLOCK), DEFINITION).comapFlatMap(
            either -> either.map(id -> type(id).map(BlockType::defaultData), BlockStateCodecs::decode),
            state -> state == state.type().defaultData()
                    ? Either.left(state.key())
                    : Either.right(Definition.of(state)));

    /**
     * A state as chunks and block entities store it: {@code {Name, Properties}}.
     */
    public static final Codec<BlockData> NBT = NBT_DEFINITION.comapFlatMap(BlockStateCodecs::decode, Definition::of);

    private BlockStateCodecs() {
        throw new UnsupportedOperationException("BlockStateCodecs cannot be instantiated.");
    }

    public static CompoundBinaryTag toNbt(final BlockData state) {
        return (CompoundBinaryTag) NBT.encodeStart(BinaryTagOps.binaryTagOps(), state).getOrThrow(IllegalStateException::new);
    }

    /**
     * {@return the state stored in a tag, {@code null} if its block is unknown}
     */
    public static @Nullable BlockData fromNbt(final BinaryTag tag) {
        return NBT.parse(BinaryTagOps.binaryTagOps(), tag).result().orElse(null);
    }

    public static Map<String, String> names(final BlockData data) {
        final Map<String, String> names = new LinkedHashMap<>();
        for (final PropertyValue<?> value : data.values()) {
            names.put(value.property().name(), value.valueName());
        }
        return names;
    }

    /**
     * {@return the value name of a property of a state, {@code null} if its block has no such property}
     */
    public static @Nullable String valueName(final BlockData data, final String property) {
        final BlockProperty<?> declared = data.type().property(property);
        return declared == null ? null : valueName(data, declared);
    }

    /**
     * Like vanilla, a property unknown to the block or given a value it lacks keeps its current value.
     */
    public static BlockData withNames(final BlockData data, final Map<String, String> names) {
        BlockData result = data;
        for (final Map.Entry<String, String> entry : names.entrySet()) {
            final BlockProperty<?> property = result.type().property(entry.getKey());
            final BlockData changed = property == null ? null : with(result, property, entry.getValue());
            if (changed == null) {
                final String block = data.key().key().asString();
                warnOnce(block + '[' + entry.getKey() + '=' + entry.getValue() + ']',
                        "Ignored the property {}={} of {}: unknown to the block", entry.getKey(), entry.getValue(), block);
            } else {
                result = changed;
            }
        }
        return result;
    }

    private static <T> String valueName(final BlockData data, final BlockProperty<T> property) {
        return property.nameOf(data.get(property));
    }

    private static <T> @Nullable BlockData with(final BlockData data, final BlockProperty<T> property, final String name) {
        final T value = property.parse(name);
        return value == null ? null : data.with(property, value);
    }

    private static DataResult<BlockType> type(final TypedKey<BlockType> id) {
        final BlockType type = Blocks.type(id);
        return type == null ? DataResult.error(() -> "Unknown block: " + id.key().asString()) : DataResult.success(type);
    }

    private static DataResult<BlockData> decode(final Definition definition) {
        return type(definition.id()).map(type -> withNames(type.defaultData(), definition.properties()));
    }

    private static void warnOnce(final String key, final String message, final Object... arguments) {
        if (WARNED.add(key)) {
            LOGGER.warn(message, arguments);
        }
    }

    private record Definition(TypedKey<BlockType> id, Map<String, String> properties) {

        static Definition of(final BlockData state) {
            return new Definition(state.key(), names(state));
        }
    }
}
