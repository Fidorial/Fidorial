package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.WolfVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:wolf_variant} registry.
 */
public final class WolfVariantKeys {
    /**
     * Key for {@code minecraft:ashen}.
     */
    public static final TypedKey<WolfVariant> ASHEN = create("ashen");

    /**
     * Key for {@code minecraft:black}.
     */
    public static final TypedKey<WolfVariant> BLACK = create("black");

    /**
     * Key for {@code minecraft:chestnut}.
     */
    public static final TypedKey<WolfVariant> CHESTNUT = create("chestnut");

    /**
     * Key for {@code minecraft:pale}.
     */
    public static final TypedKey<WolfVariant> PALE = create("pale");

    /**
     * Key for {@code minecraft:rusty}.
     */
    public static final TypedKey<WolfVariant> RUSTY = create("rusty");

    /**
     * Key for {@code minecraft:snowy}.
     */
    public static final TypedKey<WolfVariant> SNOWY = create("snowy");

    /**
     * Key for {@code minecraft:spotted}.
     */
    public static final TypedKey<WolfVariant> SPOTTED = create("spotted");

    /**
     * Key for {@code minecraft:striped}.
     */
    public static final TypedKey<WolfVariant> STRIPED = create("striped");

    /**
     * Key for {@code minecraft:woods}.
     */
    public static final TypedKey<WolfVariant> WOODS = create("woods");

    private static final List<TypedKey<WolfVariant>> VALUES = List.of(
        ASHEN,
        BLACK,
        CHESTNUT,
        PALE,
        RUSTY,
        SNOWY,
        SPOTTED,
        STRIPED,
        WOODS
    );

    private WolfVariantKeys() {
        throw new UnsupportedOperationException("WolfVariantKeys cannot be instantiated.");
    }

    private static TypedKey<WolfVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.WOLF_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<WolfVariant>> values() {
        return VALUES.stream();
    }
}
