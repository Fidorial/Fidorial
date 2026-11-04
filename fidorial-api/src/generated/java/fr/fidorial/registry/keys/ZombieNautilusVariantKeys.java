package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ZombieNautilusVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:zombie_nautilus_variant} registry.
 */
public final class ZombieNautilusVariantKeys {
    /**
     * Key for {@code minecraft:temperate}.
     */
    public static final TypedKey<ZombieNautilusVariant> TEMPERATE = create("temperate");

    /**
     * Key for {@code minecraft:warm}.
     */
    public static final TypedKey<ZombieNautilusVariant> WARM = create("warm");

    private static final List<TypedKey<ZombieNautilusVariant>> VALUES = List.of(
        TEMPERATE,
        WARM
    );

    private ZombieNautilusVariantKeys() {
        throw new UnsupportedOperationException("ZombieNautilusVariantKeys cannot be instantiated.");
    }

    private static TypedKey<ZombieNautilusVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<ZombieNautilusVariant>> values() {
        return VALUES.stream();
    }
}
