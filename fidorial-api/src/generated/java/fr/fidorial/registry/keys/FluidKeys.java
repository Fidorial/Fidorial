package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:fluid} registry.
 */
public final class FluidKeys {
    /**
     * Key for {@code minecraft:empty}.
     */
    public static final TypedKey<Fluid> EMPTY = create("empty");

    /**
     * Key for {@code minecraft:flowing_lava}.
     */
    public static final TypedKey<Fluid> FLOWING_LAVA = create("flowing_lava");

    /**
     * Key for {@code minecraft:flowing_water}.
     */
    public static final TypedKey<Fluid> FLOWING_WATER = create("flowing_water");

    /**
     * Key for {@code minecraft:lava}.
     */
    public static final TypedKey<Fluid> LAVA = create("lava");

    /**
     * Key for {@code minecraft:water}.
     */
    public static final TypedKey<Fluid> WATER = create("water");

    private static final List<TypedKey<Fluid>> VALUES = List.of(
        EMPTY,
        FLOWING_LAVA,
        FLOWING_WATER,
        LAVA,
        WATER
    );

    private FluidKeys() {
        throw new UnsupportedOperationException("FluidKeys cannot be instantiated.");
    }

    private static TypedKey<Fluid> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.FLUID, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<Fluid>> values() {
        return VALUES.stream();
    }
}
