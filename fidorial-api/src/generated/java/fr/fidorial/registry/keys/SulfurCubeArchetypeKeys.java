package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SulfurCubeArchetype;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:sulfur_cube_archetype} registry.
 */
public final class SulfurCubeArchetypeKeys {
    /**
     * Key for {@code minecraft:bouncy}.
     */
    public static final TypedKey<SulfurCubeArchetype> BOUNCY = create("bouncy");

    /**
     * Key for {@code minecraft:cold}.
     */
    public static final TypedKey<SulfurCubeArchetype> COLD = create("cold");

    /**
     * Key for {@code minecraft:explosive}.
     */
    public static final TypedKey<SulfurCubeArchetype> EXPLOSIVE = create("explosive");

    /**
     * Key for {@code minecraft:fast_flat}.
     */
    public static final TypedKey<SulfurCubeArchetype> FAST_FLAT = create("fast_flat");

    /**
     * Key for {@code minecraft:fast_sliding}.
     */
    public static final TypedKey<SulfurCubeArchetype> FAST_SLIDING = create("fast_sliding");

    /**
     * Key for {@code minecraft:high_resistance}.
     */
    public static final TypedKey<SulfurCubeArchetype> HIGH_RESISTANCE = create("high_resistance");

    /**
     * Key for {@code minecraft:hot}.
     */
    public static final TypedKey<SulfurCubeArchetype> HOT = create("hot");

    /**
     * Key for {@code minecraft:light}.
     */
    public static final TypedKey<SulfurCubeArchetype> LIGHT = create("light");

    /**
     * Key for {@code minecraft:regular}.
     */
    public static final TypedKey<SulfurCubeArchetype> REGULAR = create("regular");

    /**
     * Key for {@code minecraft:slow_bouncy}.
     */
    public static final TypedKey<SulfurCubeArchetype> SLOW_BOUNCY = create("slow_bouncy");

    /**
     * Key for {@code minecraft:slow_flat}.
     */
    public static final TypedKey<SulfurCubeArchetype> SLOW_FLAT = create("slow_flat");

    /**
     * Key for {@code minecraft:slow_sliding}.
     */
    public static final TypedKey<SulfurCubeArchetype> SLOW_SLIDING = create("slow_sliding");

    /**
     * Key for {@code minecraft:sticky}.
     */
    public static final TypedKey<SulfurCubeArchetype> STICKY = create("sticky");

    private static final List<TypedKey<SulfurCubeArchetype>> VALUES = List.of(
        BOUNCY,
        COLD,
        EXPLOSIVE,
        FAST_FLAT,
        FAST_SLIDING,
        HIGH_RESISTANCE,
        HOT,
        LIGHT,
        REGULAR,
        SLOW_BOUNCY,
        SLOW_FLAT,
        SLOW_SLIDING,
        STICKY
    );

    private SulfurCubeArchetypeKeys() {
        throw new UnsupportedOperationException("SulfurCubeArchetypeKeys cannot be instantiated.");
    }

    private static TypedKey<SulfurCubeArchetype> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.SULFUR_CUBE_ARCHETYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<SulfurCubeArchetype>> values() {
        return VALUES.stream();
    }
}
