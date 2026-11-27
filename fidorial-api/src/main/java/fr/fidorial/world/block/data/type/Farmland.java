package fr.fidorial.world.block.data.type;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;

/**
 * The block state of farmland: how moist the soil is.
 *
 * @since 0.1.0
 */
@BlockTrait(properties = Names.MOISTURE)
public interface Farmland extends BlockData {

    /**
     * {@return the current moisture, from {@code 0} (dry) to {@link #getMaximumMoisture()} (wet)}
     *
     * @since 0.1.0
     */
    default int getMoisture() {
        return get(type().range(Names.MOISTURE));
    }

    /**
     * {@return the same farmland with another moisture}
     *
     * @param moisture the moisture, from {@code 0} to {@link #getMaximumMoisture()}
     * @throws IllegalArgumentException if the moisture is out of range
     * @since 0.1.0
     */
    default Farmland withMoisture(final int moisture) {
        return (Farmland) with(type().range(Names.MOISTURE), moisture);
    }

    /**
     * {@return the moisture of fully wet farmland}
     *
     * @throws IllegalStateException if the block type declares no {@code moisture} property
     * @since 0.1.0
     */
    default int getMaximumMoisture() {
        return type().range(Names.MOISTURE).max();
    }

    /**
     * {@return {@code true} when the farmland is fully wet, which is what the client renders as wet soil}
     *
     * @since 0.1.0
     */
    default boolean isMoist() {
        return getMoisture() >= getMaximumMoisture();
    }
}
