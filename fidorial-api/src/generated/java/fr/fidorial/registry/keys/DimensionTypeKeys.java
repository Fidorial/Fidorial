package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:dimension_type} registry.
 */
public final class DimensionTypeKeys {
    /**
     * Key for {@code minecraft:overworld}.
     */
    public static final TypedKey<DimensionTypeDefinition> OVERWORLD = create("overworld");

    /**
     * Key for {@code minecraft:overworld_caves}.
     */
    public static final TypedKey<DimensionTypeDefinition> OVERWORLD_CAVES = create("overworld_caves");

    /**
     * Key for {@code minecraft:the_end}.
     */
    public static final TypedKey<DimensionTypeDefinition> THE_END = create("the_end");

    /**
     * Key for {@code minecraft:the_nether}.
     */
    public static final TypedKey<DimensionTypeDefinition> THE_NETHER = create("the_nether");

    private static final List<TypedKey<DimensionTypeDefinition>> VALUES = List.of(
        OVERWORLD,
        OVERWORLD_CAVES,
        THE_END,
        THE_NETHER
    );

    private DimensionTypeKeys() {
        throw new UnsupportedOperationException("DimensionTypeKeys cannot be instantiated.");
    }

    private static TypedKey<DimensionTypeDefinition> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.DIMENSION_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<DimensionTypeDefinition>> values() {
        return VALUES.stream();
    }
}
