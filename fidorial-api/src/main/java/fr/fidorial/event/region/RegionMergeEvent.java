package fr.fidorial.event.region;

import fr.fidorial.scheduler.Region;

/**
 * Fired after a region was merged into another one: the margins of the two came to touch, so their
 * sections are now ticked together by the thread of {@link #into()}.
 *
 * <p>Per-region data kept for {@link #from()} now belongs to {@link #into()}, which keeps its
 * {@linkplain Region#id() identifier}.</p>
 *
 * @since 0.1.0
 */
public interface RegionMergeEvent extends RegionEvent {

    /**
     * {@return the region that was merged, as it was right before; it no longer exists}
     *
     * @since 0.1.0
     */
    Region from();

    /**
     * {@return the region it was merged into, as it is right after, with the sections of {@link #from()}}
     *
     * @since 0.1.0
     */
    Region into();
}
