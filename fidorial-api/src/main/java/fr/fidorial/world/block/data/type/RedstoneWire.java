package fr.fidorial.world.block.data.type;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.AnaloguePowerable;

import java.util.Locale;

/**
 * The block state of redstone dust: how it connects on each of its four sides, and the power it
 * carries.
 *
 * @since 0.1.0
 */
@BlockTrait(values = {
        @BlockTrait.Values(property = Names.NORTH, allowed = {"up", "side", "none"}, exactly = true),
        @BlockTrait.Values(property = Names.EAST, allowed = {"up", "side", "none"}, exactly = true),
        @BlockTrait.Values(property = Names.SOUTH, allowed = {"up", "side", "none"}, exactly = true),
        @BlockTrait.Values(property = Names.WEST, allowed = {"up", "side", "none"}, exactly = true)
})
public interface RedstoneWire extends AnaloguePowerable {

    @Override
    default RedstoneWire withPower(final int power) {
        return (RedstoneWire) AnaloguePowerable.super.withPower(power);
    }

    /**
     * {@return how the dust connects on one side}
     *
     * @param side a horizontal side
     * @throws IllegalArgumentException if {@code side} is {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * @since 0.1.0
     */
    default Connection getConnection(final BlockFace side) {
        return get(type().choice(sideName(side), Connection.class));
    }

    /**
     * {@return the same dust connecting another way on one side}
     *
     * @param side       a horizontal side
     * @param connection how to connect
     * @throws IllegalArgumentException if {@code side} is {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * @since 0.1.0
     */
    default RedstoneWire withConnection(final BlockFace side, final Connection connection) {
        return (RedstoneWire) with(type().choice(sideName(side), Connection.class), connection);
    }

    private static String sideName(final BlockFace side) {
        Preconditions.checkArgument(side != BlockFace.UP && side != BlockFace.DOWN, "Redstone dust only connects horizontally, not %s", side);
        return side.name().toLowerCase(Locale.ROOT);
    }

    /**
     * How redstone dust connects on one side.
     *
     * @since 0.1.0
     */
    enum Connection {
        /**
         * Stored as {@code up}: climbs the side of the block next to it, towards dust on top of it.
         */
        UP("up"),
        /**
         * Stored as {@code side}: runs along the ground towards that side.
         */
        SIDE("side"),
        /**
         * Stored as {@code none}: not connected on that side.
         */
        NONE("none");

        private final String value;

        Connection(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Connection fromValue(final String value) {
            for (final Connection connection : values()) {
                if (connection.value.equals(value)) {
                    return connection;
                }
            }
            throw new IllegalArgumentException("Unknown redstone dust connection: " + value);
        }

        /**
         * {@return {@code true} for {@link #UP} and {@link #SIDE}}
         *
         * @since 0.1.0
         */
        public boolean isConnected() {
            return this != NONE;
        }

        /**
         * {@return the value as stored in the block state}
         *
         * @since 0.1.0
         */
        public String value() {
            return value;
        }
    }
}
