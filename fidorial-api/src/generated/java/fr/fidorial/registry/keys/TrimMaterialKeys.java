package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.TrimMaterial;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:trim_material} registry.
 */
public final class TrimMaterialKeys {
    /**
     * Key for {@code minecraft:amethyst}.
     */
    public static final TypedKey<TrimMaterial> AMETHYST = create("amethyst");

    /**
     * Key for {@code minecraft:copper}.
     */
    public static final TypedKey<TrimMaterial> COPPER = create("copper");

    /**
     * Key for {@code minecraft:diamond}.
     */
    public static final TypedKey<TrimMaterial> DIAMOND = create("diamond");

    /**
     * Key for {@code minecraft:emerald}.
     */
    public static final TypedKey<TrimMaterial> EMERALD = create("emerald");

    /**
     * Key for {@code minecraft:gold}.
     */
    public static final TypedKey<TrimMaterial> GOLD = create("gold");

    /**
     * Key for {@code minecraft:iron}.
     */
    public static final TypedKey<TrimMaterial> IRON = create("iron");

    /**
     * Key for {@code minecraft:lapis}.
     */
    public static final TypedKey<TrimMaterial> LAPIS = create("lapis");

    /**
     * Key for {@code minecraft:netherite}.
     */
    public static final TypedKey<TrimMaterial> NETHERITE = create("netherite");

    /**
     * Key for {@code minecraft:quartz}.
     */
    public static final TypedKey<TrimMaterial> QUARTZ = create("quartz");

    /**
     * Key for {@code minecraft:redstone}.
     */
    public static final TypedKey<TrimMaterial> REDSTONE = create("redstone");

    /**
     * Key for {@code minecraft:resin}.
     */
    public static final TypedKey<TrimMaterial> RESIN = create("resin");

    private static final List<TypedKey<TrimMaterial>> VALUES = List.of(
        AMETHYST,
        COPPER,
        DIAMOND,
        EMERALD,
        GOLD,
        IRON,
        LAPIS,
        NETHERITE,
        QUARTZ,
        REDSTONE,
        RESIN
    );

    private TrimMaterialKeys() {
        throw new UnsupportedOperationException("TrimMaterialKeys cannot be instantiated.");
    }

    private static TypedKey<TrimMaterial> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.TRIM_MATERIAL, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<TrimMaterial>> values() {
        return VALUES.stream();
    }
}
