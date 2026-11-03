package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.FrogVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:frog_variant} registry.
 */
public final class FrogVariantKeys {
    /**
     * Key for {@code minecraft:cold}.
     */
    public static final TypedKey<FrogVariant> COLD = create("cold");

    /**
     * Key for {@code minecraft:temperate}.
     */
    public static final TypedKey<FrogVariant> TEMPERATE = create("temperate");

    /**
     * Key for {@code minecraft:warm}.
     */
    public static final TypedKey<FrogVariant> WARM = create("warm");

    private static final List<TypedKey<FrogVariant>> VALUES = List.of(
        COLD,
        TEMPERATE,
        WARM
    );

    private FrogVariantKeys() {
        throw new UnsupportedOperationException("FrogVariantKeys cannot be instantiated.");
    }

    private static TypedKey<FrogVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.FROG_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<FrogVariant>> values() {
        return VALUES.stream();
    }
}
