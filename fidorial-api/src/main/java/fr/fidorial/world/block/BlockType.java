package fr.fidorial.world.block;

import com.google.common.base.Preconditions;
import fr.fidorial.registry.TypedKey;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A kind of block, such as {@code minecraft:oak_stairs}, with the properties its states combine.
 *
 * <p>Every combination of property values is one {@link BlockData state}, addressed by an ordinal
 * in property order and sent to clients under its network identifier. States are immutable proxies
 * implementing the {@linkplain #traits() traits} detected from the properties.</p>
 *
 * @since 0.1.0
 */
public final class BlockType {

    private final TypedKey<BlockType> key;
    private final List<BlockProperty<?>> properties;
    private final int[] sizes;
    private final int[] radixes;
    private final int[] stateIds;
    private final int defaultOrdinal;
    private final Class<?>[] interfaces;
    private final BlockData[] states;

    private BlockType(final TypedKey<BlockType> key, final List<BlockProperty<?>> properties, final int[] stateIds, final int defaultOrdinal,
                      final List<Class<? extends BlockData>> traits) {
        this.key = key;
        this.properties = List.copyOf(properties);
        final int count = this.properties.size();
        this.sizes = new int[count];
        this.radixes = new int[count];
        int expected = 1;
        for (int index = count - 1; index >= 0; index--) {
            final BlockProperty<?> property = this.properties.get(index);
            Preconditions.checkArgument(this.properties.stream().filter(other -> other.name().equals(property.name())).count() == 1,
                    "Block '%s' declares the property '%s' twice", key.key().asString(), property.name());
            sizes[index] = property.values().size();
            radixes[index] = expected;
            expected *= sizes[index];
        }
        Preconditions.checkArgument(stateIds.length == expected,
                "Block '%s' expects %s states but got %s", key.key().asString(), expected, stateIds.length);
        Preconditions.checkArgument(defaultOrdinal >= 0 && defaultOrdinal < expected,
                "Default ordinal out of range for '%s'", key.key().asString());
        this.stateIds = stateIds.clone();
        this.defaultOrdinal = defaultOrdinal;

        final List<Class<?>> faces = new ArrayList<>(traits.size() + 1);
        faces.add(BlockData.class);
        for (final Class<? extends BlockData> trait : traits) {
            if (!faces.contains(trait)) {
                faces.add(trait);
            }
        }
        this.interfaces = faces.toArray(Class<?>[]::new);
        final MethodHandle constructor = BlockDataFactory.constructor(faces);
        this.states = new BlockData[expected];
        try {
            for (int ordinal = 0; ordinal < expected; ordinal++) {
                states[ordinal] = (BlockData) constructor.invokeExact(this, ordinal);
            }
        } catch (final Throwable exception) {
            throw new IllegalStateException("Cannot create the states of " + key.key().asString(), exception);
        }
    }

    /**
     * Creates a block type from its network identifiers.
     *
     * @param key            the block key
     * @param properties     the properties, in declaration order
     * @param stateIds       the network identifier of each state, indexed by ordinal
     * @param defaultOrdinal the ordinal of the default state
     * @return the block type
     * @throws IllegalArgumentException if the identifiers do not match the property combinations
     * @since 0.1.0
     */
    public static BlockType of(final TypedKey<BlockType> key, final List<BlockProperty<?>> properties, final int[] stateIds, final int defaultOrdinal) {
        return new BlockType(key, properties, stateIds, defaultOrdinal, BlockTraits.detect(properties));
    }

    /// Creates a block type whose states are numbered consecutively from a first network identifier.
    ///
    /// @param key            the block key
    /// @param properties     the properties, in declaration order
    /// @param firstStateId   the network identifier of the state of ordinal `0`
    /// @param defaultOrdinal the ordinal of the default state
    /// @return the block type
    /// @since 0.1.0
    public static BlockType of(final TypedKey<BlockType> key, final List<BlockProperty<?>> properties, final int firstStateId,
                               final int defaultOrdinal) {
        return of(key, properties, consecutive(firstStateId, stateCount(properties)), defaultOrdinal);
    }

    private static int stateCount(final List<BlockProperty<?>> properties) {
        int count = 1;
        for (final BlockProperty<?> property : properties) {
            count *= property.values().size();
        }
        return count;
    }

    private static int[] consecutive(final int first, final int count) {
        final int[] ids = new int[count];
        for (int ordinal = 0; ordinal < count; ordinal++) {
            ids[ordinal] = first + ordinal;
        }
        return ids;
    }

    /**
     * {@return a builder for a block type of the given key}
     *
     * @param key the block key
     * @since 0.1.0
     */
    public static Builder builder(final TypedKey<BlockType> key) {
        return new Builder(key);
    }

    /**
     * {@return the key of this block type}
     *
     * @since 0.1.0
     */
    public TypedKey<BlockType> key() {
        return key;
    }

    /**
     * {@return the properties of this block, in declaration order}
     *
     * @since 0.1.0
     */
    public List<BlockProperty<?>> properties() {
        return properties;
    }

    /**
     * {@return the property with the given name, or {@code null} if this block has none}
     *
     * @param name the property name
     * @since 0.1.0
     */
    public @Nullable BlockProperty<?> property(final String name) {
        for (final BlockProperty<?> property : properties) {
            if (property.name().equals(name)) {
                return property;
            }
        }
        return null;
    }

    /// {@return the flag property with the given name}
    ///
    /// @param name the property name
    /// @throws IllegalStateException if this block has no such flag
    /// @since 0.1.0
    public BlockProperty.Flag flag(final String name) {
        final BlockProperty<?> property = property(name);
        Preconditions.checkState(property instanceof BlockProperty.Flag, "%s has no flag property '%s'", key.key().asString(), name);
        return (BlockProperty.Flag) property;
    }

    /// {@return the range property with the given name}
    ///
    /// @param name the property name
    /// @throws IllegalStateException if this block has no such range
    /// @since 0.1.0
    public BlockProperty.Range range(final String name) {
        final BlockProperty<?> property = property(name);
        Preconditions.checkState(property instanceof BlockProperty.Range, "%s has no range property '%s'", key.key().asString(), name);
        return (BlockProperty.Range) property;
    }

    /// {@return the choice property with the given name, over the given enum}
    ///
    /// @param name the property name
    /// @param type the enum of its values
    /// @param <E>  the enum
    /// @throws IllegalStateException if this block has no such choice
    /// @since 0.1.0
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> BlockProperty.Choice<E> choice(final String name, final Class<E> type) {
        final BlockProperty<?> property = property(name);
        Preconditions.checkState(property instanceof final BlockProperty.Choice<?> choice && choice.type() == type,
                "%s has no property '%s' of %s", key.key().asString(), name, type.getSimpleName());
        return (BlockProperty.Choice<E>) property;
    }

    /**
     * {@return {@code true} if this block declares a property with the given name}
     *
     * @param name the property name
     * @since 0.1.0
     */
    public boolean hasProperty(final String name) {
        return property(name) != null;
    }

    /**
     * {@return the number of states of this block, one per property combination}
     *
     * @since 0.1.0
     */
    public int stateCount() {
        return stateIds.length;
    }

    /**
     * {@return the interfaces every state of this block implements, {@link BlockData} first}
     *
     * @since 0.1.0
     */
    public List<Class<?>> traits() {
        return List.of(interfaces);
    }

    /**
     * {@return the state a block of this type takes when placed without context}
     *
     * @since 0.1.0
     */
    public BlockData defaultData() {
        return states[defaultOrdinal];
    }

    /**
     * {@return the state with the given ordinal}
     *
     * @param ordinal the ordinal, from {@code 0} to {@code stateCount() - 1}
     * @since 0.1.0
     */
    public BlockData stateAt(final int ordinal) {
        return states[ordinal];
    }

    /**
     * {@return the ordinal of the default state}
     *
     * @since 0.1.0
     */
    public int defaultOrdinal() {
        return defaultOrdinal;
    }

    int position(final BlockProperty<?> property) {
        for (int index = 0; index < properties.size(); index++) {
            if (properties.get(index) == property) {
                return index;
            }
        }
        return properties.indexOf(property);
    }

    @SuppressWarnings("unchecked")
    <T> T value(final int ordinal, final BlockProperty<T> property) {
        final int position = position(property);
        Preconditions.checkArgument(position >= 0, "%s has no property %s", key.key().asString(), property);
        final int index = ordinal / radixes[position] % sizes[position];
        // a range builds its value list on each call
        return property instanceof final BlockProperty.Range range ? (T) Integer.valueOf(range.min() + index) : property.values().get(index);
    }

    <T> int withValue(final int ordinal, final BlockProperty<T> property, final T value) {
        final int position = position(property);
        Preconditions.checkArgument(position >= 0, "%s has no property %s", key.key().asString(), property);
        final int index = property.indexOf(value);
        Preconditions.checkArgument(index >= 0, "%s is not a value of the property '%s' of %s", value, property.name(), key.key().asString());
        final int current = ordinal / radixes[position] % sizes[position];
        return ordinal + (index - current) * radixes[position];
    }

    int networkIdAt(final int ordinal) {
        return stateIds[ordinal];
    }

    List<PropertyValue<?>> valuesOf(final int ordinal) {
        final List<PropertyValue<?>> values = new ArrayList<>(properties.size());
        for (final BlockProperty<?> property : properties) {
            values.add(valueOf(ordinal, property));
        }
        return List.copyOf(values);
    }

    private <T> PropertyValue<T> valueOf(final int ordinal, final BlockProperty<T> property) {
        return new PropertyValue<>(property, value(ordinal, property));
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof final BlockType type && type.key.equals(key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "BlockType[" + key.key().asString() + ", states=" + stateIds.length + "]";
    }

    /**
     * Builds a {@link BlockType}, typically for a block a plugin adds.
     *
     * <p>Network identifiers come from exactly one of {@link #firstStateId(int)},
     * {@link #stateIds(int[])} or {@link #appearance(int)}.</p>
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private final TypedKey<BlockType> key;
        private final List<BlockProperty<?>> properties = new ArrayList<>();
        private final List<Class<? extends BlockData>> extraTraits = new ArrayList<>();
        private final Map<BlockProperty<?>, Object> defaultValues = new LinkedHashMap<>();
        private int @Nullable [] stateIds;
        private int firstStateId = -1;
        private int fixedStateId = -1;

        private Builder(final TypedKey<BlockType> key) {
            this.key = key;
        }

        /**
         * Declares a property.
         *
         * @param property the property
         * @return this builder
         * @throws IllegalArgumentException if a property with the same name is already declared
         * @since 0.1.0
         */
        public Builder property(final BlockProperty<?> property) {
            Preconditions.checkArgument(property != null, "The property of a block type builder must not be null");
            for (final BlockProperty<?> declared : properties) {
                Preconditions.checkArgument(!declared.name().equals(property.name()),
                        "Property '%s' declared twice on block '%s'", property.name(), key.key().asString());
            }
            properties.add(property);
            return this;
        }

        /**
         * Numbers the states consecutively from a first network identifier.
         *
         * @param firstStateId the network identifier of the state of ordinal {@code 0}
         * @return this builder
         * @since 0.1.0
         */
        public Builder firstStateId(final int firstStateId) {
            this.firstStateId = firstStateId;
            return this;
        }

        /**
         * Gives the network identifier of every state explicitly.
         *
         * @param stateIds the identifiers, indexed by ordinal
         * @return this builder
         * @since 0.1.0
         */
        public Builder stateIds(final int[] stateIds) {
            this.stateIds = stateIds.clone();
            return this;
        }

        /**
         * Makes every state render client-side as one existing block state.
         *
         * @param networkId the network identifier of the vanilla state to render as
         * @return this builder
         * @since 0.1.0
         */
        public Builder appearance(final int networkId) {
            this.fixedStateId = networkId;
            return this;
        }

        /**
         * Sets the value a property takes in the default state.
         *
         * @param property the property, declared on this builder
         * @param value    one of its values
         * @param <T>      the type of the values
         * @return this builder
         * @since 0.1.0
         */
        public <T> Builder defaultValue(final BlockProperty<T> property, final T value) {
            Preconditions.checkArgument(properties.contains(property),
                    "Property %s is not declared on block '%s'", property, key.key().asString());
            Preconditions.checkArgument(property.indexOf(value) >= 0,
                    "%s is not a value of the property '%s'", value, property.name());
            defaultValues.put(property, value);
            return this;
        }

        /**
         * Adds a trait interface the states implement, on top of the detected ones.
         *
         * @param trait the trait interface
         * @return this builder
         * @since 0.1.0
         */
        public Builder trait(final Class<? extends BlockData> trait) {
            extraTraits.add(trait);
            return this;
        }

        /**
         * {@return the block type}
         *
         * @throws IllegalStateException if no network identifier source was given
         * @since 0.1.0
         */
        public BlockType build() {
            final int count = stateCount(properties);
            final int[] ids;
            if (stateIds != null) {
                ids = stateIds;
            } else if (firstStateId >= 0) {
                ids = consecutive(firstStateId, count);
            } else {
                Preconditions.checkState(fixedStateId >= 0,
                        "firstStateId(...), stateIds(...) or appearance(...) must be set for '%s'", key.key().asString());
                ids = new int[count];
                Arrays.fill(ids, fixedStateId);
            }
            final List<Class<? extends BlockData>> traits = new ArrayList<>(BlockTraits.detect(properties));
            traits.addAll(extraTraits);
            return new BlockType(key, properties, ids, defaultOrdinal(), traits);
        }

        @SuppressWarnings("unchecked")
        private int defaultOrdinal() {
            int ordinal = 0;
            for (final BlockProperty<?> property : properties) {
                final Object value = defaultValues.get(property);
                final int index = value == null ? 0 : ((BlockProperty<Object>) property).indexOf(value);
                ordinal = ordinal * property.values().size() + index;
            }
            return ordinal;
        }
    }
}
