package fr.fidorial.world.block;

import java.util.ArrayList;
import java.util.List;


/**
 * The properties shared by many vanilla blocks, and factories for new ones.
 *
 * @since 0.1.0
 */
public final class BlockProperties {

    /**
     * Facing on all six sides.
     *
     * @since 0.1.0
     */
    public static final BlockProperty FACING =
            of("facing", "north", "east", "south", "west", "up", "down");

    /**
     * Facing on the four horizontal sides.
     *
     * @since 0.1.0
     */
    public static final BlockProperty HORIZONTAL_FACING =
            of("facing", "north", "south", "west", "east");

    /**
     * Facing of a hopper: down or horizontal.
     *
     * @since 0.1.0
     */
    public static final BlockProperty HOPPER_FACING =
            of("facing", "down", "north", "south", "west", "east");

    /**
     * Alignment on any axis.
     *
     * @since 0.1.0
     */
    public static final BlockProperty AXIS = of("axis", "x", "y", "z");

    /**
     * Alignment on a horizontal axis.
     *
     * @since 0.1.0
     */
    public static final BlockProperty HORIZONTAL_AXIS = of("axis", "x", "z");

    /**
     * Whether the block holds water.
     *
     * @since 0.1.0
     */
    public static final BlockProperty WATERLOGGED = bool("waterlogged");
    /**
     * Whether the block receives redstone power.
     *
     * @since 0.1.0
     */
    public static final BlockProperty POWERED = bool("powered");
    /**
     * Whether the block is lit.
     *
     * @since 0.1.0
     */
    public static final BlockProperty LIT = bool("lit");
    /**
     * Whether the block is open.
     *
     * @since 0.1.0
     */
    public static final BlockProperty OPEN = bool("open");
    /**
     * Whether snow covers the block.
     *
     * @since 0.1.0
     */
    public static final BlockProperty SNOWY = bool("snowy");

    /**
     * Top or bottom half, for slabs, stairs and trapdoors.
     *
     * @since 0.1.0
     */
    public static final BlockProperty HALF = of("half", "top", "bottom");

    /**
     * Upper or lower half, for doors and tall plants.
     *
     * @since 0.1.0
     */
    public static final BlockProperty DOUBLE_BLOCK_HALF = of("half", "upper", "lower");

    /**
     * Sixteen-step rotation, for signs, banners and heads.
     *
     * @since 0.1.0
     */
    public static final BlockProperty ROTATION = integer("rotation", 0, 15);

    /**
     * Fluid level, from {@code 0} (source) to {@code 15}.
     *
     * @since 0.1.0
     */
    public static final BlockProperty FLUID_LEVEL = integer("level", 0, 15);

    private BlockProperties() {
    }

    /**
     * {@return a boolean property, accepting {@code true} and {@code false}}
     *
     * @param name the property name
     * @since 0.1.0
     */
    public static BlockProperty bool(final String name) {
        return new BlockProperty(name, List.of("true", "false"));
    }

    /**
     * {@return an integer property accepting every value of a range}
     *
     * @param name the property name
     * @param min  the lowest value, inclusive
     * @param max  the highest value, inclusive
     * @throws IllegalArgumentException if {@code min > max}
     * @since 0.1.0
     */
    public static BlockProperty integer(final String name, final int min, final int max) {
        if (min > max) {
            throw new IllegalArgumentException("The integer property '" + name + "' has a minimum (" + min + ") greater than its maximum (" + max + ")");
        }
        final List<String> values = new ArrayList<>(max - min + 1);
        for (int value = min; value <= max; value++) {
            values.add(Integer.toString(value));
        }
        return new BlockProperty(name, List.copyOf(values));
    }

    /**
     * {@return a property accepting the given values}
     *
     * @param name   the property name
     * @param values the accepted values, the first one being the default
     * @since 0.1.0
     */
    public static BlockProperty of(final String name, final String... values) {
        return new BlockProperty(name, List.of(values));
    }
}
