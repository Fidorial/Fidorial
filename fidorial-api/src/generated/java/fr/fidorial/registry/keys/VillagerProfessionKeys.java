package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.VillagerProfession;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:villager_profession} registry.
 */
public final class VillagerProfessionKeys {
    /**
     * Key for {@code minecraft:armorer}.
     */
    public static final TypedKey<VillagerProfession> ARMORER = create("armorer");

    /**
     * Key for {@code minecraft:butcher}.
     */
    public static final TypedKey<VillagerProfession> BUTCHER = create("butcher");

    /**
     * Key for {@code minecraft:cartographer}.
     */
    public static final TypedKey<VillagerProfession> CARTOGRAPHER = create("cartographer");

    /**
     * Key for {@code minecraft:cleric}.
     */
    public static final TypedKey<VillagerProfession> CLERIC = create("cleric");

    /**
     * Key for {@code minecraft:farmer}.
     */
    public static final TypedKey<VillagerProfession> FARMER = create("farmer");

    /**
     * Key for {@code minecraft:fisherman}.
     */
    public static final TypedKey<VillagerProfession> FISHERMAN = create("fisherman");

    /**
     * Key for {@code minecraft:fletcher}.
     */
    public static final TypedKey<VillagerProfession> FLETCHER = create("fletcher");

    /**
     * Key for {@code minecraft:leatherworker}.
     */
    public static final TypedKey<VillagerProfession> LEATHERWORKER = create("leatherworker");

    /**
     * Key for {@code minecraft:librarian}.
     */
    public static final TypedKey<VillagerProfession> LIBRARIAN = create("librarian");

    /**
     * Key for {@code minecraft:mason}.
     */
    public static final TypedKey<VillagerProfession> MASON = create("mason");

    /**
     * Key for {@code minecraft:nitwit}.
     */
    public static final TypedKey<VillagerProfession> NITWIT = create("nitwit");

    /**
     * Key for {@code minecraft:none}.
     */
    public static final TypedKey<VillagerProfession> NONE = create("none");

    /**
     * Key for {@code minecraft:shepherd}.
     */
    public static final TypedKey<VillagerProfession> SHEPHERD = create("shepherd");

    /**
     * Key for {@code minecraft:toolsmith}.
     */
    public static final TypedKey<VillagerProfession> TOOLSMITH = create("toolsmith");

    /**
     * Key for {@code minecraft:weaponsmith}.
     */
    public static final TypedKey<VillagerProfession> WEAPONSMITH = create("weaponsmith");

    private static final List<TypedKey<VillagerProfession>> VALUES = List.of(
        ARMORER,
        BUTCHER,
        CARTOGRAPHER,
        CLERIC,
        FARMER,
        FISHERMAN,
        FLETCHER,
        LEATHERWORKER,
        LIBRARIAN,
        MASON,
        NITWIT,
        NONE,
        SHEPHERD,
        TOOLSMITH,
        WEAPONSMITH
    );

    private VillagerProfessionKeys() {
        throw new UnsupportedOperationException("VillagerProfessionKeys cannot be instantiated.");
    }

    private static TypedKey<VillagerProfession> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.VILLAGER_PROFESSION, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<VillagerProfession>> values() {
        return VALUES.stream();
    }
}
