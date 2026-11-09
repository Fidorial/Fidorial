package fr.fidorial.scheduler;

import fr.fidorial.event.region.RegionMergeEvent;
import fr.fidorial.event.region.RegionSplitEvent;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;

/**
 * A group of sections of a world, ticked together by one thread at a time.
 *
 * <p>The world is cut into square sections, {@code 2^region-section-shift} chunks wide. A region owns
 * every section holding a loaded chunk or a ticket, plus a margin of empty sections around them
 * ({@code threading.region-buffer-radius}), so the code ticking a block can always reach its
 * neighbours, across a section border included. Two regions whose margins meet are
 * {@linkplain RegionMergeEvent merged}, and a region whose sections drift
 * apart is {@linkplain RegionSplitEvent split} again.</p>
 *
 * <p>A {@code Region} handed out by the API is a snapshot: it describes the region when it was taken
 * and does not follow the merges and splits that come after.</p>
 *
 * @since 0.1.0
 */
public interface Region {

    /**
     * {@return the identifier of the region}
     *
     * <p>Never reused. When two regions merge, the result keeps the identifier of the region the
     * other one was merged into; the regions born from a split all get new identifiers.</p>
     *
     * @since 0.1.0
     */
    long id();

    /**
     * {@return the key of the world the region belongs to}
     *
     * @since 0.1.0
     */
    Key world();

    /**
     * {@return how many sections the region owns, its margin included}
     *
     * @since 0.1.0
     */
    int sectionCount();

    /**
     * Checks whether the region owned a chunk when this snapshot was taken.
     *
     * @param chunk the chunk position, in {@link #world()}
     * @return {@code true} if the section of the chunk belonged to the region
     * @since 0.1.0
     */
    boolean contains(ChunkPos chunk);

    /**
     * {@return a chunk owned by the region, near its middle}
     *
     * @since 0.1.0
     */
    ChunkPos centerChunk();
}
