package fr.euphyllia.fidorial.server.world.redstone;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * How redstone dust works out the power it carries, chosen per world in its configuration.
 */
public enum DustImplementation {

    /**
     * Vanilla's: each dust takes its power from its own neighbours, one dust at a time, and tells
     * the blocks around it each time its power changes. The update order is vanilla's, down to the
     * location-dependent order some circuits rely on.
     */
    VANILLA("vanilla"),

    /**
     * Space Walker's <a href="https://github.com/SpaceWalkerRS/alternate-current">Alternate Current</a>:
     * the power of a whole network of dust is worked out at once, each dust is set once, and the
     * blocks around the network are updated in an order that follows the flow of power rather than
     * the location of the dust. Far less lag, at the cost of the circuits relying on vanilla's
     * location-dependent order.
     */
    ALTERNATE_CURRENT("alternate-current");

    private final String configName;

    DustImplementation(final String configName) {
        this.configName = configName;
    }

    /**
     * {@return the names of every implementation, as written in the configuration}
     */
    public static String names() {
        return Arrays.stream(values()).map(DustImplementation::configName).collect(Collectors.joining(", "));
    }

    /**
     * {@return the implementation with that name, as written in the configuration, {@code null} for
     * an unknown name}: case and surrounding spaces do not matter, nor {@code _} in place of {@code -}
     *
     * @param name the name
     */
    public static @Nullable DustImplementation byName(final String name) {
        final String normalized = name.strip().toLowerCase(Locale.ROOT).replace('_', '-');
        for (final DustImplementation implementation : values()) {
            if (implementation.configName.equals(normalized)) {
                return implementation;
            }
        }
        return null;
    }

    /**
     * {@return the name of the implementation in the configuration}
     */
    public String configName() {
        return configName;
    }
}
