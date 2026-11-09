/**
 * Events about the {@linkplain Region regions} of a world being merged and
 * split.
 *
 * <p>They are informative and cannot be cancelled: two regions whose margins touch must be merged
 * for their threads not to touch the same blocks. They are posted once the change is done, on the
 * thread that caused it, which may be a region thread finishing its tick or the thread that loaded a
 * chunk: a listener must not touch the world, and must be quick.</p>
 *
 * @since 0.1.0
 */
package fr.fidorial.event.region;

import fr.fidorial.scheduler.Region;
