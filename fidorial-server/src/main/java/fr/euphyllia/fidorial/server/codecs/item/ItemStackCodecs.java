package fr.euphyllia.fidorial.server.codecs.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.component.ItemLore;
import fr.fidorial.item.component.MapItemDecorations;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.item.component.WrittenBookContent;
import fr.fidorial.item.data.DataComponentMap;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.data.TypedDataComponentValue;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.KEY_CODEC;

public final class ItemStackCodecs {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ItemStackCodecs.class);
    public static final String REMOVED_PREFIX = "!";
    private static final Map<DataComponentType<?>, Codec<?>> CODECS = new LinkedHashMap<>();

    public static final Codec<ItemLore> LORE_CODEC = ComponentCodecs.COMPONENT_CODEC
            .listOf()
            .comapFlatMap(
                    lines -> lines.size() > ItemLore.MAX_LINES
                            ? DataResult.error(() -> "Lore has " + lines.size()
                            + " lines, the most allowed is " + ItemLore.MAX_LINES)
                            : DataResult.success(new ItemLore(lines)),
                    ItemLore::lines);

    public static final Codec<ItemEnchantments> ENCHANTMENTS_CODEC = Codec.unboundedMap(KEY_CODEC, Codec.intRange(1, ItemEnchantments.MAX_LEVEL))
            .xmap(ItemEnchantments::new, ItemEnchantments::levels);

    public static final Codec<SwingAnimation.SwingAnimationType> SWING_ANIMATION_TYPE_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    return DataResult.success(SwingAnimation.SwingAnimationType.valueOf(s.toUpperCase(Locale.ROOT)));
                } catch (final IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown swing animation type: " + s);
                }
            },
            type -> type.name().toLowerCase(Locale.ROOT)
    );

    public static final Codec<SwingAnimation> SWING_ANIMATION_CODEC = RecordCodec.builder(SwingAnimation.class)
            .defaults(SwingAnimation.DEFAULT)
            .field("type", SwingAnimation::type, SWING_ANIMATION_TYPE_CODEC)
            .field("duration", SwingAnimation::duration, CommonCodecs.NON_NEGATIVE_INT)
            .build();

    public static final Codec<MapItemDecorations.Entry> MAP_DECORATION_ENTRY_CODEC =
            RecordCodec.builder(MapItemDecorations.Entry.class)
                    .required("type", MapItemDecorations.Entry::type,
                            KEY_CODEC.xmap(key -> TypedKey.create(RegistryKey.MAP_DECORATION_TYPE, key), TypedKey::key))
                    .required("x", MapItemDecorations.Entry::x, Codec.DOUBLE)
                    .required("z", MapItemDecorations.Entry::z, Codec.DOUBLE)
                    .required("rotation", MapItemDecorations.Entry::rotation, Codec.FLOAT)
                    .build();

    public static final Codec<MapItemDecorations> MAP_DECORATIONS_CODEC =
            Codec.unboundedMap(Codec.STRING, MAP_DECORATION_ENTRY_CODEC)
                    .xmap(entries -> new MapItemDecorations(Map.copyOf(entries)), MapItemDecorations::entries);

    private static <T> Codec<T> filterableRaw(final Codec<T> codec) {
        final Codec<T> full = RecordCodecBuilder.create(instance -> instance.group(
                codec.fieldOf("raw").forGetter(raw -> raw),
                codec.optionalFieldOf("filtered").forGetter(raw -> Optional.empty())
        ).apply(instance, (raw, filtered) -> raw));
        return Codec.withAlternative(full, codec);
    }

    public static final Codec<WrittenBookContent> WRITTEN_BOOK_CONTENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            filterableRaw(Codec.string(0, WrittenBookContent.MAX_TITLE_LENGTH)).fieldOf("title").forGetter(WrittenBookContent::title),
            Codec.STRING.fieldOf("author").forGetter(WrittenBookContent::author),
            Codec.intRange(0, WrittenBookContent.MAX_GENERATION).optionalFieldOf("generation", 0)
                    .forGetter(WrittenBookContent::generation),
            filterableRaw(ComponentCodecs.COMPONENT_CODEC).listOf(0, WrittenBookContent.MAX_PAGES)
                    .optionalFieldOf("pages", List.of()).forGetter(WrittenBookContent::pages),
            Codec.BOOL.optionalFieldOf("resolved", false).forGetter(WrittenBookContent::resolved)
    ).apply(instance, WrittenBookContent::new));

    static {
        register(DataComponentTypes.MAX_STACK_SIZE, Codec.INT);
        register(DataComponentTypes.MAX_DAMAGE, Codec.INT);
        register(DataComponentTypes.DAMAGE, Codec.INT);
        register(DataComponentTypes.ITEM_MODEL, KEY_CODEC);
        register(DataComponentTypes.CUSTOM_NAME, ComponentCodecs.COMPONENT_CODEC);
        register(DataComponentTypes.ITEM_NAME, ComponentCodecs.COMPONENT_CODEC);
        register(DataComponentTypes.LORE, LORE_CODEC);
        register(DataComponentTypes.ATTACK_ANIMATION, SWING_ANIMATION_CODEC);
        register(DataComponentTypes.INTERACT_ANIMATION, SWING_ANIMATION_CODEC);
        register(DataComponentTypes.MAP_ID, Codec.INT);
        register(DataComponentTypes.MAP_DECORATIONS, MAP_DECORATIONS_CODEC);
        register(DataComponentTypes.ENCHANTMENTS, ENCHANTMENTS_CODEC);
        register(DataComponentTypes.STORED_ENCHANTMENTS, ENCHANTMENTS_CODEC);
        register(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, Codec.BOOL);
        register(DataComponentTypes.WRITTEN_BOOK_CONTENT, WRITTEN_BOOK_CONTENT_CODEC);
    }

    public static final Codec<DataComponentMap> COMPONENT_MAP_CODEC = new Codec<>() {

        @Override
        public <T> DataResult<Pair<DataComponentMap, T>> decode(final DynamicOps<T> ops, final T input) {

            return ops.getMapValues(input).map(entries -> {

                final DataComponentMap.Builder builder = DataComponentMap.builder();

                entries.forEach(entry -> {
                    final Optional<String> name = ops.getStringValue(entry.getFirst()).result();
                    if (name.isEmpty()) {
                        LOGGER.warn("Skipping component with a non-string key");
                        return;
                    }
                    decodeEntry(ops, builder, name.get(), entry.getSecond());
                });

                return Pair.of(builder.build(), ops.empty());
            });
        }

        @Override
        public <T> DataResult<T> encode(final DataComponentMap input, final DynamicOps<T> ops, final T prefix) {

            final RecordBuilder<T> record = ops.mapBuilder();

            for (final Map.Entry<DataComponentType<?>, Object> entry : input.entries()) {
                final DataComponentType<?> type = entry.getKey();

                if (!type.persistent()) {
                    continue;
                }

                final Codec<?> codec = CODECS.get(type);
                if (codec == null) {
                    LOGGER.debug("Not persisting component {}: no NBT codec", type);
                    continue;
                }

                record.add(type.key().asString(), encodeErased(ops, type, codec, entry.getValue()));
            }

            for (final DataComponentType<?> removed : input.removedTypes()) {
                record.add(REMOVED_PREFIX + removed.key().asString(), ops.emptyMap());
            }

            return record.build(prefix);
        }
    };

    /**
     * The {@code components} of an item hover event: a component patch, in the format of
     * {@link #COMPONENT_MAP_CODEC}, holding Adventure data component values.
     *
     * <p>Modelled components are decoded as {@link TypedDataComponentValue}s, the others are kept as
     * binary tag holders so that no component is lost. Values that are neither typed nor binary tags,
     * such as those of the Gson serializer, are converted through the
     * {@link DataComponentValueConverterRegistry}.</p>
     */
    public static final Codec<Map<Key, DataComponentValue>> HOVER_COMPONENTS_CODEC = new Codec<>() {

        @Override
        public <T> DataResult<Pair<Map<Key, DataComponentValue>, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMapValues(input).map(entries -> {
                final Map<Key, DataComponentValue> values = new LinkedHashMap<>();
                entries.forEach(entry -> {
                    final Optional<String> name = ops.getStringValue(entry.getFirst()).result();
                    if (name.isEmpty()) {
                        LOGGER.warn("Skipping hover component with a non-string key");
                        return;
                    }
                    final boolean removed = name.get().startsWith(REMOVED_PREFIX);
                    final String raw = removed ? name.get().substring(REMOVED_PREFIX.length()) : name.get();
                    if (!Key.parseable(raw)) {
                        LOGGER.warn("Skipping unparseable hover component key: {}", name.get());
                        return;
                    }
                    final Key key = Key.key(raw);
                    if (removed) {
                        values.put(key, DataComponentValue.removed());
                        return;
                    }
                    decodeHoverValue(ops, key, entry.getSecond())
                            .resultOrPartial(message -> LOGGER.warn("Skipping hover component {}: {}", raw, message))
                            .ifPresent(value -> values.put(key, value));
                });
                return Pair.of(Collections.unmodifiableMap(values), ops.empty());
            });
        }

        @Override
        public <T> DataResult<T> encode(final Map<Key, DataComponentValue> input, final DynamicOps<T> ops, final T prefix) {
            final RecordBuilder<T> record = ops.mapBuilder();
            for (final Map.Entry<Key, DataComponentValue> entry : input.entrySet()) {
                final Key key = entry.getKey();
                if (entry.getValue() instanceof DataComponentValue.Removed) {
                    record.add(REMOVED_PREFIX + key.asString(), ops.emptyMap());
                    continue;
                }
                encodeHoverValue(ops, key, entry.getValue())
                        .resultOrPartial(message -> LOGGER.warn("Not showing component {} in a hover event: {}",
                                key.asString(), message))
                        .ifPresent(value -> record.add(key.asString(), value));
            }
            return record.build(prefix);
        }
    };

    private ItemStackCodecs() {
        throw new UnsupportedOperationException("ItemStackCodecs cannot be instantiated.");
    }

    private static <T> void register(final DataComponentType<T> type, final Codec<T> codec) {
        CODECS.put(type, codec);
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable Codec<T> codec(final DataComponentType<T> type) {
        return (Codec<T>) CODECS.get(Objects.requireNonNull(type, "type"));
    }

    /**
     * Encodes the value of a data component with the codec of its component.
     *
     * @param ops   the format to encode to
     * @param value the data component value
     * @param <T>   the type of the format
     * @return the encoded value, or an error if the component has no codec
     */
    public static <T> DataResult<T> encodeValue(final DynamicOps<T> ops, final TypedDataComponentValue<?> value) {
        return encodeTyped(ops, value);
    }

    /**
     * Decodes the value of a data component with the codec of its component.
     *
     * @param ops   the format to decode from
     * @param type  the component
     * @param input the encoded value
     * @param <T>   the type of the format
     * @return the data component value, or an error if the component has no codec or the input is invalid
     */
    public static <T> DataResult<TypedDataComponentValue<?>> decodeValue(final DynamicOps<T> ops,
                                                                         final DataComponentType<?> type,
                                                                         final T input) {
        return decodeTyped(ops, type, input);
    }

    private static <T, V> DataResult<T> encodeTyped(final DynamicOps<T> ops, final TypedDataComponentValue<V> value) {
        final Codec<V> codec = codec(value.type());
        if (codec == null) {
            return DataResult.error(() -> "No codec for the data component " + value.type());
        }
        return codec.encodeStart(ops, value.value());
    }

    private static <T, V> DataResult<TypedDataComponentValue<?>> decodeTyped(final DynamicOps<T> ops,
                                                                             final DataComponentType<V> type,
                                                                             final T input) {
        final Codec<V> codec = codec(type);
        if (codec == null) {
            return DataResult.error(() -> "No codec for the data component " + type);
        }
        return codec.parse(ops, input).map(value -> TypedDataComponentValue.of(type, value));
    }

    private static <T> DataResult<DataComponentValue> decodeHoverValue(final DynamicOps<T> ops, final Key key, final T input) {
        final DataComponentType<?> type = DataComponentTypes.byKey(key);
        if (type != null && CODECS.containsKey(type)) {
            final DataResult<TypedDataComponentValue<?>> typed = decodeTyped(ops, type, input);
            if (typed.result().isPresent()) {
                return typed.<DataComponentValue>map(value -> value);
            }
        }
        return NbtCodecs.BINARY_TAG_CODEC.parse(ops, input).map(NbtCodecs::toHolder);
    }

    private static <T> DataResult<T> encodeHoverValue(final DynamicOps<T> ops, final Key key, final DataComponentValue value) {
        if (value instanceof final TypedDataComponentValue<?> typed) {
            return encodeTyped(ops, typed);
        }
        try {
            if (value instanceof final DataComponentValue.TagSerializable tag) {
                return NbtCodecs.BINARY_TAG_CODEC.encodeStart(ops, NbtCodecs.fromHolder(tag.asBinaryTag()));
            }
            final TypedDataComponentValue<?> converted =
                    DataComponentValueConverterRegistry.convert(TypedDataComponentValue.class, key, value);
            return encodeTyped(ops, converted);
        } catch (final RuntimeException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static <T> void decodeEntry(final DynamicOps<T> ops,
                                        final DataComponentMap.Builder builder,
                                        final String rawKey,
                                        final T value) {

        if (rawKey.startsWith(REMOVED_PREFIX)) {
            final String stripped = rawKey.substring(REMOVED_PREFIX.length());
            final DataComponentType<?> removed = lookup(stripped);
            if (removed != null) {
                builder.remove(removed);
            }
            return;
        }

        final DataComponentType<?> type = lookup(rawKey);
        if (type == null) {
            return;
        }

        final Codec<?> codec = CODECS.get(type);
        if (codec == null) {
            LOGGER.warn("Dropping component {} on load: no NBT codec", rawKey);
            return;
        }

        decodeInto(ops, builder, type, codec, value);
    }

    /**
     * Ties the two wildcards together so the decoded value and the component agree.
     */
    @SuppressWarnings("unchecked")
    private static <T, V> void decodeInto(final DynamicOps<T> ops,
                                          final DataComponentMap.Builder builder,
                                          final DataComponentType<V> type,
                                          final Codec<?> codec,
                                          final T value) {

        final DataResult<Pair<V, T>> decoded = ((Codec<V>) codec).decode(ops, value);
        final Optional<Pair<V, T>> result = decoded.result();

        if (result.isEmpty()) {
            LOGGER.warn("Dropping component {} on load: {}", type,
                    decoded.error().map(DataResult.Error::message).orElse("unknown error"));
            return;
        }

        builder.set(type, result.get().getFirst());
    }

    @SuppressWarnings("unchecked")
    private static <T, V> T encodeErased(final DynamicOps<T> ops,
                                         final DataComponentType<V> type,
                                         final Codec<?> codec,
                                         final Object value) {

        return ((Codec<V>) codec)
                .encodeStart(ops, type.valueType().cast(value))
                .resultOrPartial(message -> LOGGER.warn("Failed to encode component {}: {}", type, message))
                .orElseGet(ops::emptyMap);
    }

    private static @Nullable DataComponentType<?> lookup(final String rawKey) {

        if (!Key.parseable(rawKey)) {
            LOGGER.warn("Skipping unparseable component key: {}", rawKey);
            return null;
        }

        final DataComponentType<?> type = DataComponentTypes.byKey(Key.key(rawKey));
        if (type == null) {
            LOGGER.warn("Skipping unmodelled component: {}", rawKey);
        }
        return type;
    }
}
