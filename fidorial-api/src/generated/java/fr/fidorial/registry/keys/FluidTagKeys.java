package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.Fluid;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:fluid} registry.
 */
public final class FluidTagKeys {
    /**
     * Tag {@code #minecraft:axolotl_tries_to_find}.
     */
    public static final TagKey<Fluid> AXOLOTL_TRIES_TO_FIND = create("axolotl_tries_to_find");

    /**
     * Tag {@code #minecraft:bubble_column_can_occupy}.
     */
    public static final TagKey<Fluid> BUBBLE_COLUMN_CAN_OCCUPY = create("bubble_column_can_occupy");

    /**
     * Tag {@code #minecraft:dolphin_tries_to_find}.
     */
    public static final TagKey<Fluid> DOLPHIN_TRIES_TO_FIND = create("dolphin_tries_to_find");

    /**
     * Tag {@code #minecraft:entity_floatable}.
     */
    public static final TagKey<Fluid> ENTITY_FLOATABLE = create("entity_floatable");

    /**
     * Tag {@code #minecraft:frog_tries_to_find_land_near}.
     */
    public static final TagKey<Fluid> FROG_TRIES_TO_FIND_LAND_NEAR = create("frog_tries_to_find_land_near");

    /**
     * Tag {@code #minecraft:lava}.
     */
    public static final TagKey<Fluid> LAVA = create("lava");

    /**
     * Tag {@code #minecraft:supports_frogspawn}.
     */
    public static final TagKey<Fluid> SUPPORTS_FROGSPAWN = create("supports_frogspawn");

    /**
     * Tag {@code #minecraft:supports_lily_pad}.
     */
    public static final TagKey<Fluid> SUPPORTS_LILY_PAD = create("supports_lily_pad");

    /**
     * Tag {@code #minecraft:supports_sugar_cane_adjacently}.
     */
    public static final TagKey<Fluid> SUPPORTS_SUGAR_CANE_ADJACENTLY = create("supports_sugar_cane_adjacently");

    /**
     * Tag {@code #minecraft:swimmable}.
     */
    public static final TagKey<Fluid> SWIMMABLE = create("swimmable");

    /**
     * Tag {@code #minecraft:water}.
     */
    public static final TagKey<Fluid> WATER = create("water");

    private FluidTagKeys() {
        throw new UnsupportedOperationException("FluidTagKeys cannot be instantiated.");
    }

    private static TagKey<Fluid> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.FLUID, value);
    }
}
