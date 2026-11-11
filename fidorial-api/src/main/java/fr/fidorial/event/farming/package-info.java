/**
 * Events fired by farming: soil being tilled, getting wet, drying out or trampled, plants growing,
 * fertilizer being used, and harvests dropping.
 *
 * <p>Every event of this package is a {@link FarmingEvent}, posted on the
 * thread of the region owning the block, before the change it describes, and only built while a
 * listener is subscribed to it.</p>
 *
 * @see fr.fidorial.world.farming
 * @since 0.1.0
 */
package fr.fidorial.event.farming;
