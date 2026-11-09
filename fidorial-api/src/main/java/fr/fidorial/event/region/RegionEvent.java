package fr.fidorial.event.region;

import fr.fidorial.event.Event;
import net.kyori.adventure.key.Key;

/**
 * An event about the regions of a world changing shape.
 *
 * @since 0.1.0
 */
public interface RegionEvent extends Event {

    /**
     * {@return the key of the world whose regions changed}
     *
     * @since 0.1.0
     */
    Key world();
}
