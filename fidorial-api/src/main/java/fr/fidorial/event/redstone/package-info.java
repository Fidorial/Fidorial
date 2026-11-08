/**
 * Events fired by redstone: a signal spreading through dust, components switching, blocks reacting
 * to being powered, and the block updates carrying it all from one block to the next.
 *
 * <p>Every event of this package is a {@link RedstoneEvent}, posted on
 * the thread of the region owning the block, and only built while a listener is subscribed to it.</p>
 *
 * @since 0.1.0
 */
package fr.fidorial.event.redstone;