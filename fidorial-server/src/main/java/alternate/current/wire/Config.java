/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

/**
 * The settings of Alternate Current for one world.
 *
 * <p>Alternate Current's {@code Config}. The original reads them from an {@code alternate-current.conf}
 * file and shares the overworld's with every dimension; in Fidorial, they come from the configuration
 * of each world, whose {@code redstone.dust-implementation} also decides whether Alternate Current
 * drives its dust at all.</p>
 *
 * @author Space Walker
 */
@FunctionalInterface
public interface Config {

    /**
     * {@return the order the blocks around a wire are updated in}
     */
    UpdateOrder getUpdateOrder();
}
