package fr.fidorial.world.block;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    private final Key key;
    private final List<BlockProperty> properties;
    private final int[] stateIds;
    private final int defaultOrdinal;
    private final Class<?>[] interfaces;
    private final @Nullable BlockData defaultData;

    private BlockType(final Key key, final List<BlockProperty> properties, final int[] stateIds, final int defaultOrdinal,
                      final List<Class<? extends BlockData>> traits) {
        this.key = key;
        this.properties = List.copyOf(properties);
        int expected = 1;
        for (final BlockProperty property : this.properties) {
            expected *= property.values().size();
        }
        if (stateIds.length != expected) {
            throw new IllegalArgumentException("Block '" + key.asString() + "' expects " + expected
                    + " states but got " + stateIds.length);
        }
        if (defaultOrdinal < 0 || defaultOrdinal >= expected) {
            throw new IllegalArgumentException("Default ordinal out of range for '" + key.asString() + "'");
        }
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
        this.defaultData = createData(defaultOrdinal);
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
    public static BlockType of(final Key key, final List<BlockProperty> properties, final int[] stateIds, final int defaultOrdinal) {
        return new BlockType(key, properties, stateIds, defaultOrdinal, BlockTraits.detect(key, properties));
    }

    /**
     * {@return a builder for a block type of the given key}
     *
     * @param key the block key
     * @since 0.1.0
     */
    public static Builder builder(final Key key) {
        return new Builder(key);
    }

    /**
     * {@return the block key}
     *
     * @since 0.1.0
     */
    public Key key() {
        return key;
    }

    /**
     * {@return the properties of this block, in declaration order}
     *
     * @since 0.1.0
     */
    public List<BlockProperty> properties() {
        return properties;
    }

    /**
     * {@return the property with the given name, or {@code null} if this block has none}
     *
     * @param name the property name
     * @since 0.1.0
     */
    public @Nullable BlockProperty property(final String name) {
        for (final BlockProperty property : properties) {
            if (property.name().equals(name)) {
                return property;
            }
        }
        return null;
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
    public @Nullable BlockData defaultData() {
        return defaultData;
    }

    /**
     * {@return the state with the given ordinal}
     *
     * @param ordinal the ordinal, from {@code 0} to {@code stateCount() - 1}
     * @since 0.1.0
     */
    public BlockData stateAt(final int ordinal) {
        if (ordinal == defaultOrdinal && defaultData != null) {
            return defaultData;
        }
        return createData(ordinal);
    }

    /**
     * Gets the state matching the given property values, starting from the default state.
     *
     * @param values property name to value; {@code null} or empty for the default state
     * @return the matching state
     * @throws IllegalArgumentException if a property or value is unknown to this block
     * @since 0.1.0
     */
    public @Nullable BlockData data(@Nullable final Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return defaultData;
        }
        int ordinal = defaultOrdinal;
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            ordinal = withValue(ordinal, entry.getKey(), entry.getValue());
        }
        return stateAt(ordinal);
    }

    /**
     * Gets the state matching the given property values, starting from the default state.
     *
     * @param values property name to value
     * @return the matching state, or {@code null} if a property or value is unknown to this block
     * @since 0.1.0
     */
    public @Nullable BlockData dataOrNull(final Map<String, String> values) {
        try {
            return data(values);
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    private BlockData createData(final int ordinal) {
        return (BlockData) Proxy.newProxyInstance(
                BlockType.class.getClassLoader(), interfaces, new DataHandler(this, ordinal));
    }

    private @Nullable String value(final int ordinal, final String propertyName) {
        int radix = 1;
        for (int i = properties.size() - 1; i >= 0; i--) {
            final BlockProperty property = properties.get(i);
            final int size = property.values().size();
            if (property.name().equals(propertyName)) {
                return property.values().get((ordinal / radix) % size);
            }
            radix *= size;
        }
        return null;
    }

    private int withValue(final int ordinal, final String propertyName, final String value) {
        int radix = 1;
        for (int i = properties.size() - 1; i >= 0; i--) {
            final BlockProperty property = properties.get(i);
            final int size = property.values().size();
            if (property.name().equals(propertyName)) {
                final int index = property.indexOf(value);
                if (index < 0) {
                    throw new IllegalArgumentException("Invalid value '" + value + "' for property '"
                            + propertyName + "' of block '" + key.asString() + "'");
                }
                final int current = (ordinal / radix) % size;
                return ordinal + (index - current) * radix;
            }
            radix *= size;
        }
        throw new IllegalArgumentException("Unknown property '" + propertyName + "' for block '" + key.asString() + "'");
    }

    private Map<String, @Nullable String> valuesOf(final int ordinal) {
        final Map<String, @Nullable String> map = new LinkedHashMap<>();
        for (final BlockProperty property : properties) {
            map.put(property.name(), value(ordinal, property.name()));
        }
        return map;
    }

    /**
     * {@return the ordinal of the default state}
     *
     * @since 0.1.0
     */
    public int defaultOrdinal() {
        return defaultOrdinal;
    }

    /**
     * {@return the property values of the state with the given ordinal, in declaration order}
     *
     * @param ordinal the ordinal of the state
     * @since 0.1.0
     */
    public Map<String, @Nullable String> propertyValuesAt(final int ordinal) {
        return valuesOf(ordinal);
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
        return "BlockType[" + key.asString() + ", states=" + stateIds.length + "]";
    }

    private record DataHandler(BlockType type, int ordinal) implements InvocationHandler {
        @Override
        public @Nullable Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            return switch (method.getName()) {
                case "type" -> type;
                case "networkId" -> type.stateIds[ordinal];
                case "get" -> type.value(ordinal, (String) args[0]);
                case "with" -> type.stateAt(type.withValue(ordinal, (String) args[0], (String) args[1]));
                case "propertyMap" -> type.valuesOf(ordinal);
                case "equals" -> equalsData(args[0]);
                case "hashCode" -> type.hashCode() * 31 + ordinal;
                case "toString" -> ((BlockData) proxy).asString();
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private boolean equalsData(@Nullable final Object other) {
            if (other == null) return false;
            return Proxy.isProxyClass(other.getClass())
                    && Proxy.getInvocationHandler(other) instanceof DataHandler(
                    final BlockType type1, final int ordinal1
            )
                    && type1.equals(type)
                    && ordinal1 == ordinal;
        }
    }

    private static int ordinalOf(final Key key, final List<BlockProperty> properties, final Map<String, String> values) {
        int ordinal = 0;
        for (final BlockProperty property : properties) {
            final String value = values.get(property.name());
            final int index = value == null ? 0 : property.indexOf(value);
            if (index < 0) {
                throw new IllegalArgumentException("Invalid value '" + value + "' for property '"
                        + property.name() + "' of block '" + key.asString() + "'");
            }
            ordinal = ordinal * property.values().size() + index;
        }
        return ordinal;
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

        private final Key key;
        private final List<BlockProperty> properties = new ArrayList<>();
        private final List<Class<? extends BlockData>> extraTraits = new ArrayList<>();
        private int @Nullable [] stateIds;
        private int firstStateId = -1;
        private int fixedStateId = -1;
        private Map<String, String> defaultValues = Map.of();

        private Builder(final Key key) {
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
        public Builder property(final BlockProperty property) {
            for (final BlockProperty declared : properties) {
                if (declared.name().equals(property.name())) {
                    throw new IllegalArgumentException("Property '" + property.name()
                            + "' declared twice on block '" + key.asString() + "'");
                }
            }
            properties.add(property);
            return this;
        }

        /**
         * Declares a property.
         *
         * @param name   the property name
         * @param values the accepted values, the first one being the default
         * @return this builder
         * @since 0.1.0
         */
        public Builder property(final String name, final List<String> values) {
            return property(new BlockProperty(name, values));
        }

        /**
         * Declares a property.
         *
         * @param name   the property name
         * @param values the accepted values, the first one being the default
         * @return this builder
         * @since 0.1.0
         */
        public Builder property(final String name, final String... values) {
            return property(name, Arrays.asList(values));
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
         * @param property the property name
         * @param value    the value
         * @return this builder
         * @since 0.1.0
         */
        public Builder defaultValue(final String property, final String value) {
            final Map<String, String> merged = new LinkedHashMap<>(defaultValues);
            merged.put(property, value);
            this.defaultValues = Map.copyOf(merged);
            return this;
        }

        /**
         * Sets the values the properties take in the default state, replacing earlier ones.
         *
         * @param values property name to value
         * @return this builder
         * @since 0.1.0
         */
        public Builder defaultValues(final Map<String, String> values) {
            this.defaultValues = Map.copyOf(values);
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
            int count = 1;
            for (final BlockProperty property : properties) {
                count *= property.values().size();
            }
            final int[] ids;
            if (stateIds == null) {
                ids = new int[count];
                if (firstStateId >= 0) {
                    for (int ordinal = 0; ordinal < count; ordinal++) {
                        ids[ordinal] = firstStateId + ordinal;
                    }
                } else if (fixedStateId >= 0) {
                    Arrays.fill(ids, fixedStateId);
                } else {
                    throw new IllegalStateException(
                            "firstStateId(...), stateIds(...) or appearance(...) must be set for '"
                                    + key.asString() + "'");
                }
            } else {
                ids = stateIds;
            }
            final List<Class<? extends BlockData>> traits = new ArrayList<>(BlockTraits.detect(key, properties));
            traits.addAll(extraTraits);

            return new BlockType(key, properties, ids, ordinalOf(key, properties, defaultValues), traits);
        }
    }

}
