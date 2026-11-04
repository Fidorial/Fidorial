package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CatVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:cat_variant} registry.
 */
public final class CatVariantKeys {
    /**
     * Key for {@code minecraft:all_black}.
     */
    public static final TypedKey<CatVariant> ALL_BLACK = create("all_black");

    /**
     * Key for {@code minecraft:black}.
     */
    public static final TypedKey<CatVariant> BLACK = create("black");

    /**
     * Key for {@code minecraft:british_shorthair}.
     */
    public static final TypedKey<CatVariant> BRITISH_SHORTHAIR = create("british_shorthair");

    /**
     * Key for {@code minecraft:calico}.
     */
    public static final TypedKey<CatVariant> CALICO = create("calico");

    /**
     * Key for {@code minecraft:jellie}.
     */
    public static final TypedKey<CatVariant> JELLIE = create("jellie");

    /**
     * Key for {@code minecraft:persian}.
     */
    public static final TypedKey<CatVariant> PERSIAN = create("persian");

    /**
     * Key for {@code minecraft:ragdoll}.
     */
    public static final TypedKey<CatVariant> RAGDOLL = create("ragdoll");

    /**
     * Key for {@code minecraft:red}.
     */
    public static final TypedKey<CatVariant> RED = create("red");

    /**
     * Key for {@code minecraft:siamese}.
     */
    public static final TypedKey<CatVariant> SIAMESE = create("siamese");

    /**
     * Key for {@code minecraft:tabby}.
     */
    public static final TypedKey<CatVariant> TABBY = create("tabby");

    /**
     * Key for {@code minecraft:white}.
     */
    public static final TypedKey<CatVariant> WHITE = create("white");

    private static final List<TypedKey<CatVariant>> VALUES = List.of(
        ALL_BLACK,
        BLACK,
        BRITISH_SHORTHAIR,
        CALICO,
        JELLIE,
        PERSIAN,
        RAGDOLL,
        RED,
        SIAMESE,
        TABBY,
        WHITE
    );

    private CatVariantKeys() {
        throw new UnsupportedOperationException("CatVariantKeys cannot be instantiated.");
    }

    private static TypedKey<CatVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.CAT_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<CatVariant>> values() {
        return VALUES.stream();
    }
}
