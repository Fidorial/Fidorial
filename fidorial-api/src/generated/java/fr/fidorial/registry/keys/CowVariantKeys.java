package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CowVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:cow_variant} registry.
 */
public final class CowVariantKeys {
    /**
     * Key for {@code minecraft:cold}.
     */
    public static final TypedKey<CowVariant> COLD = create("cold");

    /**
     * Key for {@code minecraft:temperate}.
     */
    public static final TypedKey<CowVariant> TEMPERATE = create("temperate");

    /**
     * Key for {@code minecraft:warm}.
     */
    public static final TypedKey<CowVariant> WARM = create("warm");

    private static final List<TypedKey<CowVariant>> VALUES = List.of(
        COLD,
        TEMPERATE,
        WARM
    );

    private CowVariantKeys() {
        throw new UnsupportedOperationException("CowVariantKeys cannot be instantiated.");
    }

    private static TypedKey<CowVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.COW_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<CowVariant>> values() {
        return VALUES.stream();
    }
}
