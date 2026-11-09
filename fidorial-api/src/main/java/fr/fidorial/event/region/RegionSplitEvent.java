package fr.fidorial.event.region;

import fr.fidorial.scheduler.Region;

import java.util.List;

/**
 * Fired after a region was split: its sections drifted apart, so each group of sections that still
 * touch is now a region of its own, ticked independently.
 *
 * <p>Per-region data of {@link #from()} can be shared out with {@link Region#contains}.</p>
 *
 * @since 0.1.0
 */
public interface RegionSplitEvent extends RegionEvent {

    /**
     * {@return the region that was split, as it was right before; it no longer exists}
     *
     * @since 0.1.0
     */
    Region from();

    /**
     * {@return the regions born from the split, at least two, each with a new identifier}
     *
     * @since 0.1.0
     */
    List<Region> into();
}
