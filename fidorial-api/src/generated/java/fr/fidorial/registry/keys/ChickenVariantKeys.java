package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChickenVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:chicken_variant} registry.
 */
public final class ChickenVariantKeys {
    /**
     * Key for {@code minecraft:cold}.
     */
    public static final TypedKey<ChickenVariant> COLD = create("cold");

    /**
     * Key for {@code minecraft:temperate}.
     */
    public static final TypedKey<ChickenVariant> TEMPERATE = create("temperate");

    /**
     * Key for {@code minecraft:warm}.
     */
    public static final TypedKey<ChickenVariant> WARM = create("warm");

    private static final List<TypedKey<ChickenVariant>> VALUES = List.of(
        COLD,
        TEMPERATE,
        WARM
    );

    private ChickenVariantKeys() {
        throw new UnsupportedOperationException("ChickenVariantKeys cannot be instantiated.");
    }

    private static TypedKey<ChickenVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.CHICKEN_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<ChickenVariant>> values() {
        return VALUES.stream();
    }
}
