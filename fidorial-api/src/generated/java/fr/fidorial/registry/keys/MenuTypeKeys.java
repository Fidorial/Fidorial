package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:menu} registry.
 */
public final class MenuTypeKeys {
    /**
     * Key for {@code minecraft:anvil}.
     */
    public static final TypedKey<MenuType> ANVIL = create("anvil");

    /**
     * Key for {@code minecraft:beacon}.
     */
    public static final TypedKey<MenuType> BEACON = create("beacon");

    /**
     * Key for {@code minecraft:blast_furnace}.
     */
    public static final TypedKey<MenuType> BLAST_FURNACE = create("blast_furnace");

    /**
     * Key for {@code minecraft:brewing_stand}.
     */
    public static final TypedKey<MenuType> BREWING_STAND = create("brewing_stand");

    /**
     * Key for {@code minecraft:cartography_table}.
     */
    public static final TypedKey<MenuType> CARTOGRAPHY_TABLE = create("cartography_table");

    /**
     * Key for {@code minecraft:crafter_3x3}.
     */
    public static final TypedKey<MenuType> CRAFTER_3X3 = create("crafter_3x3");

    /**
     * Key for {@code minecraft:crafting}.
     */
    public static final TypedKey<MenuType> CRAFTING = create("crafting");

    /**
     * Key for {@code minecraft:enchantment}.
     */
    public static final TypedKey<MenuType> ENCHANTMENT = create("enchantment");

    /**
     * Key for {@code minecraft:furnace}.
     */
    public static final TypedKey<MenuType> FURNACE = create("furnace");

    /**
     * Key for {@code minecraft:generic_3x3}.
     */
    public static final TypedKey<MenuType> GENERIC_3X3 = create("generic_3x3");

    /**
     * Key for {@code minecraft:generic_9x1}.
     */
    public static final TypedKey<MenuType> GENERIC_9X1 = create("generic_9x1");

    /**
     * Key for {@code minecraft:generic_9x2}.
     */
    public static final TypedKey<MenuType> GENERIC_9X2 = create("generic_9x2");

    /**
     * Key for {@code minecraft:generic_9x3}.
     */
    public static final TypedKey<MenuType> GENERIC_9X3 = create("generic_9x3");

    /**
     * Key for {@code minecraft:generic_9x4}.
     */
    public static final TypedKey<MenuType> GENERIC_9X4 = create("generic_9x4");

    /**
     * Key for {@code minecraft:generic_9x5}.
     */
    public static final TypedKey<MenuType> GENERIC_9X5 = create("generic_9x5");

    /**
     * Key for {@code minecraft:generic_9x6}.
     */
    public static final TypedKey<MenuType> GENERIC_9X6 = create("generic_9x6");

    /**
     * Key for {@code minecraft:grindstone}.
     */
    public static final TypedKey<MenuType> GRINDSTONE = create("grindstone");

    /**
     * Key for {@code minecraft:hopper}.
     */
    public static final TypedKey<MenuType> HOPPER = create("hopper");

    /**
     * Key for {@code minecraft:jukebox}.
     */
    public static final TypedKey<MenuType> JUKEBOX = create("jukebox");

    /**
     * Key for {@code minecraft:lectern}.
     */
    public static final TypedKey<MenuType> LECTERN = create("lectern");

    /**
     * Key for {@code minecraft:loom}.
     */
    public static final TypedKey<MenuType> LOOM = create("loom");

    /**
     * Key for {@code minecraft:merchant}.
     */
    public static final TypedKey<MenuType> MERCHANT = create("merchant");

    /**
     * Key for {@code minecraft:shulker_box}.
     */
    public static final TypedKey<MenuType> SHULKER_BOX = create("shulker_box");

    /**
     * Key for {@code minecraft:smithing}.
     */
    public static final TypedKey<MenuType> SMITHING = create("smithing");

    /**
     * Key for {@code minecraft:smoker}.
     */
    public static final TypedKey<MenuType> SMOKER = create("smoker");

    /**
     * Key for {@code minecraft:stonecutter}.
     */
    public static final TypedKey<MenuType> STONECUTTER = create("stonecutter");

    private static final List<TypedKey<MenuType>> VALUES = List.of(
        ANVIL,
        BEACON,
        BLAST_FURNACE,
        BREWING_STAND,
        CARTOGRAPHY_TABLE,
        CRAFTER_3X3,
        CRAFTING,
        ENCHANTMENT,
        FURNACE,
        GENERIC_3X3,
        GENERIC_9X1,
        GENERIC_9X2,
        GENERIC_9X3,
        GENERIC_9X4,
        GENERIC_9X5,
        GENERIC_9X6,
        GRINDSTONE,
        HOPPER,
        JUKEBOX,
        LECTERN,
        LOOM,
        MERCHANT,
        SHULKER_BOX,
        SMITHING,
        SMOKER,
        STONECUTTER
    );

    private MenuTypeKeys() {
        throw new UnsupportedOperationException("MenuTypeKeys cannot be instantiated.");
    }

    private static TypedKey<MenuType> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.MENU, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<MenuType>> values() {
        return VALUES.stream();
    }
}
