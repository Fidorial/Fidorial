package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.VillagerType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:villager_type} registry.
 */
public final class VillagerTypeKeys {
    /**
     * Key for {@code minecraft:desert}.
     */
    public static final TypedKey<VillagerType> DESERT = create("desert");

    /**
     * Key for {@code minecraft:jungle}.
     */
    public static final TypedKey<VillagerType> JUNGLE = create("jungle");

    /**
     * Key for {@code minecraft:plains}.
     */
    public static final TypedKey<VillagerType> PLAINS = create("plains");

    /**
     * Key for {@code minecraft:savanna}.
     */
    public static final TypedKey<VillagerType> SAVANNA = create("savanna");

    /**
     * Key for {@code minecraft:snow}.
     */
    public static final TypedKey<VillagerType> SNOW = create("snow");

    /**
     * Key for {@code minecraft:swamp}.
     */
    public static final TypedKey<VillagerType> SWAMP = create("swamp");

    /**
     * Key for {@code minecraft:taiga}.
     */
    public static final TypedKey<VillagerType> TAIGA = create("taiga");

    private static final List<TypedKey<VillagerType>> VALUES = List.of(
        DESERT,
        JUNGLE,
        PLAINS,
        SAVANNA,
        SNOW,
        SWAMP,
        TAIGA
    );

    private VillagerTypeKeys() {
        throw new UnsupportedOperationException("VillagerTypeKeys cannot be instantiated.");
    }

    private static TypedKey<VillagerType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.VILLAGER_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<VillagerType>> values() {
        return VALUES.stream();
    }
}
