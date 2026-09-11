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

    public static BlockType of(final Key key, final List<BlockProperty> properties, final int[] stateIds, final int defaultOrdinal) {
        return new BlockType(key, properties, stateIds, defaultOrdinal, BlockTraits.detect(key, properties));
    }

    public static Builder builder(final Key key) {
        return new Builder(key);
    }

    public Key key() {
        return key;
    }

    public List<BlockProperty> properties() {
        return properties;
    }

    public @Nullable BlockProperty property(final String name) {
        for (final BlockProperty property : properties) {
            if (property.name().equals(name)) {
                return property;
            }
        }
        return null;
    }

    public boolean hasProperty(final String name) {
        return property(name) != null;
    }

    public int stateCount() {
        return stateIds.length;
    }

    public List<Class<?>> traits() {
        return List.of(interfaces);
    }

    public @Nullable BlockData defaultData() {
        return defaultData;
    }

    public BlockData stateAt(final int ordinal) {
        if (ordinal == defaultOrdinal && defaultData != null) {
            return defaultData;
        }
        return createData(ordinal);
    }

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

    public int defaultOrdinal() {
        return defaultOrdinal;
    }

    public Map<String, @Nullable String> propertyValuesAt(final int ordinal) {
        return valuesOf(ordinal);
    }

    @Override
    public boolean equals(final Object other) {
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
                    && Proxy.getInvocationHandler(other) instanceof DataHandler(final BlockType type1, final int ordinal1)
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

        public Builder property(final String name, final List<String> values) {
            return property(new BlockProperty(name, values));
        }

        public Builder property(final String name, final String... values) {
            return property(name, Arrays.asList(values));
        }

        public Builder firstStateId(final int firstStateId) {
            this.firstStateId = firstStateId;
            return this;
        }

        public Builder stateIds(final int[] stateIds) {
            this.stateIds = stateIds.clone();
            return this;
        }

        public Builder appearance(final int networkId) {
            this.fixedStateId = networkId;
            return this;
        }

        public Builder defaultValue(final String property, final String value) {
            final Map<String, String> merged = new LinkedHashMap<>(defaultValues);
            merged.put(property, value);
            this.defaultValues = Map.copyOf(merged);
            return this;
        }

        public Builder defaultValues(final Map<String, String> values) {
            this.defaultValues = Map.copyOf(values);
            return this;
        }

        public Builder trait(final Class<? extends BlockData> trait) {
            extraTraits.add(trait);
            return this;
        }

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
